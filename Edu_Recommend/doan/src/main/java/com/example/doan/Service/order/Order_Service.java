package com.example.doan.Service.order;

import com.example.doan.Model.course.*;
import com.example.doan.Model.user.*;
import com.example.doan.Model.order.*;
import com.example.doan.Model.enrollment.*;
import com.example.doan.Model.review.*;
import com.example.doan.Model.refund.*;
import com.example.doan.Model.notification.*;
import com.example.doan.Model.recommend.*;
import com.example.doan.Repository.course.*;
import com.example.doan.Repository.user.*;
import com.example.doan.Repository.order.*;
import com.example.doan.Repository.enrollment.*;
import com.example.doan.Repository.review.*;
import com.example.doan.Repository.refund.*;
import com.example.doan.Repository.notification.*;
import com.example.doan.Service.notification.Notification_Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
public class Order_Service {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final PaymentRepository paymentRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final CouponRepository couponRepo;
    private final Invoice_Service invoiceService;
    private final InstructorEarningRepository earningRepo;
    private final Notification_Service notificationService;

    public Order_Service(OrderRepository orderRepo,
                         OrderItemRepository orderItemRepo,
                         PaymentRepository paymentRepo,
                         EnrollmentRepository enrollmentRepo,
                         CouponRepository couponRepo,
                         Invoice_Service invoiceService,
                         InstructorEarningRepository earningRepo,
                         Notification_Service notificationService) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.paymentRepo = paymentRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.couponRepo = couponRepo;
        this.invoiceService = invoiceService;
        this.earningRepo = earningRepo;
        this.notificationService = notificationService;
    }

    public Order Get_Order_ById(Long id) {
        return orderRepo.findById(id).orElse(null);
    }

    /**
     * Tạo đơn hàng mới ở trạng thái 'pending' kèm thông tin mã giảm giá áp dụng.
     * Tự động phân bổ số tiền giảm giá của Coupon tỉ lệ thuận theo từng khóa học
     * để hoa hồng giảng viên tính trên giá thực tế nhận được, tránh lỗi doanh thu âm.
     */
    @Transactional
    public Order Create_Order(User user, List<Course> cartCourses, BigDecimal total, Coupon coupon) {
        Order order = new Order();
        order.setUser(user);
        order.setTongTien(total);
        order.setTrangThai("pending");
        order.setCoupon(coupon);
        Order savedOrder = orderRepo.save(order);

        // 1. Tính tổng giá trị gốc của các khóa học trong giỏ hàng
        BigDecimal originalTotal = BigDecimal.ZERO;
        for (Course course : cartCourses) {
            originalTotal = originalTotal.add(course.getGia());
        }

        // 2. Lưu từng OrderItem với giá đã phân bổ sau chiết khấu
        BigDecimal allocatedTotal = BigDecimal.ZERO;
        for (int i = 0; i < cartCourses.size(); i++) {
            Course course = cartCourses.get(i);
            OrderItem item = new OrderItem();
            item.setOrder(savedOrder);
            item.setCourse(course);

            BigDecimal itemActualPrice = course.getGia();
            if (originalTotal.compareTo(BigDecimal.ZERO) > 0 && total.compareTo(originalTotal) < 0) {
                if (i == cartCourses.size() - 1) {
                    // Item cuối cùng: lấy tổng tiền trừ đi phần đã phân bổ để tránh sai số làm tròn
                    itemActualPrice = total.subtract(allocatedTotal);
                } else {
                    itemActualPrice = course.getGia().multiply(total)
                            .divide(originalTotal, 2, java.math.RoundingMode.HALF_UP);
                    allocatedTotal = allocatedTotal.add(itemActualPrice);
                }
            }
            
            // Đảm bảo giá thực tế không âm
            if (itemActualPrice.compareTo(BigDecimal.ZERO) < 0) {
                itemActualPrice = BigDecimal.ZERO;
            }

            item.setGia(itemActualPrice);
            orderItemRepo.save(item);
        }

        return savedOrder;
    }

    /**
     * Hoàn thành thanh toán: Cập nhật 'paid', lưu Payment, tăng daDung của Coupon, và kích hoạt 'Enrollment'.
     * Áp dụng khoá bi quan (Pessimistic Lock) để chống double-submit và race condition của Coupon.
     */
    @Transactional
    public void Complete_Payment(Long orderId, String phuongThuc) {
        // 1. Sử dụng findByIdWithLock để khóa bản ghi Order tránh double-submit
        Order order = orderRepo.findByIdWithLock(orderId).orElse(null);
        if (order == null || !"pending".equals(order.getTrangThai())) {
            return;
        }

        // 2. Chuyển trạng thái Order -> paid
        order.setTrangThai("paid");
        orderRepo.save(order);

        // 3. Cập nhật số lượt dùng của Coupon kèm khóa bi quan và kiểm tra giới hạn lượt dùng
        if (order.getCoupon() != null) {
            Coupon coupon = couponRepo.findByIdWithLock(order.getCoupon().getId())
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy mã giảm giá đã áp dụng!"));
            
            // Kiểm tra ngày hết hạn thực tế lúc thanh toán
            if (coupon.getNgayHetHan() != null && coupon.getNgayHetHan().isBefore(java.time.LocalDate.now())) {
                throw new IllegalStateException("Mã giảm giá '" + coupon.getMaCode() + "' đã hết hạn!");
            }

            // Kiểm tra số lượng lượt dùng tối đa thực tế lúc thanh toán
            if (coupon.getDaDung() >= coupon.getSoLuong()) {
                throw new IllegalStateException("Mã giảm giá '" + coupon.getMaCode() + "' đã hết lượt sử dụng!");
            }
            
            coupon.setDaDung(coupon.getDaDung() + 1);
            couponRepo.save(coupon);
        }

        // 3. Ghi nhận giao dịch Payments
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPhuongThuc(phuongThuc);
        payment.setTrangThai("success");
        payment.setMaGiaoDich("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setSoTien(order.getTongTien());
        payment.setPayDate(LocalDateTime.now());
        paymentRepo.save(payment);

        // 4. Kích hoạt học tập (Tạo Enrollment cho từng khóa học) và Chia hoa hồng (Instructor Earning)
        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);
        for (OrderItem item : items) {
            Course course = item.getCourse();
            
            // Chia hoa hồng cho giảng viên nếu có giảng viên
            if (course.getTeacher() != null) {
                BigDecimal rate = new BigDecimal(course.getCommissionRate()).divide(new BigDecimal(100));
                BigDecimal earningAmount = item.getGia().multiply(rate);
                
                InstructorEarning earning = new InstructorEarning();
                earning.setTeacher(course.getTeacher());
                earning.setOrderItem(item);
                earning.setTongTien(item.getGia());
                earning.setTienNhan(earningAmount);
                earningRepo.save(earning);
            }

            // Kích hoạt học tập
            boolean exists = enrollmentRepo.existsByUserIdAndCourseId(order.getUser().getId(), course.getId());
            if (!exists) {
                Enrollment enrollment = new Enrollment();
                enrollment.setUser(order.getUser());
                enrollment.setCourse(course);
                enrollment.setTrangThai("in_progress");
                enrollmentRepo.save(enrollment);
            }

            // Gửi thông báo mua khóa học cho Giảng viên và Admin
            notificationService.Create_Purchase_Notification(order.getUser(), course, item.getGia());
        }

        // 5. Tự động sinh hóa đơn thanh toán
        invoiceService.Generate_Invoice_For_Order(order);
    }

    /**
     * Hủy đơn hàng hoặc thanh toán thất bại: Chuyển 'cancelled'.
     */
    @Transactional
    public void Cancel_Order(Long orderId) {
        Order order = Get_Order_ById(orderId);
        if (order != null && "pending".equals(order.getTrangThai())) {
            order.setTrangThai("cancelled");
            orderRepo.save(order);

            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setPhuongThuc("bank_transfer");
            payment.setTrangThai("failed");
            payment.setSoTien(order.getTongTien());
            payment.setPayDate(LocalDateTime.now());
            paymentRepo.save(payment);
        }
    }
}
