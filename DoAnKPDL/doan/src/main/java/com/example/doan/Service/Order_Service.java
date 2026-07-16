package com.example.doan.Service;

import com.example.doan.Model.*;
import com.example.doan.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service xử lý đơn đặt hàng và giao dịch thanh toán.
 * Tích hợp giảm trừ số lượt dùng của Coupon khi thanh toán thành công.
 * File: Service/Order_Service.java
 */
@Service
public class Order_Service {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final PaymentRepository paymentRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final CouponRepository couponRepo;
    private final Invoice_Service invoiceService;

    public Order_Service(OrderRepository orderRepo,
                         OrderItemRepository orderItemRepo,
                         PaymentRepository paymentRepo,
                         EnrollmentRepository enrollmentRepo,
                         CouponRepository couponRepo,
                         Invoice_Service invoiceService) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.paymentRepo = paymentRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.couponRepo = couponRepo;
        this.invoiceService = invoiceService;
    }

    public Order Get_Order_ById(Long id) {
        return orderRepo.findById(id).orElse(null);
    }

    /**
     * Tạo đơn hàng mới ở trạng thái 'pending' kèm thông tin mã giảm giá áp dụng.
     */
    @Transactional
    public Order Create_Order(User user, List<Course> cartCourses, BigDecimal total, Coupon coupon) {
        Order order = new Order();
        order.setUser(user);
        order.setTongTien(total);
        order.setTrangThai("pending");
        order.setCoupon(coupon);
        Order savedOrder = orderRepo.save(order);

        for (Course course : cartCourses) {
            OrderItem item = new OrderItem();
            item.setOrder(savedOrder);
            item.setCourse(course);
            item.setGia(course.getGia());
            orderItemRepo.save(item);
        }

        return savedOrder;
    }

    /**
     * Hoàn thành thanh toán: Cập nhật 'paid', lưu Payment, tăng daDung của Coupon, và kích hoạt 'Enrollment'.
     */
    @Transactional
    public void Complete_Payment(Long orderId, String phuongThuc) {
        Order order = Get_Order_ById(orderId);
        if (order == null || !"pending".equals(order.getTrangThai())) {
            return;
        }

        // 1. Chuyển trạng thái Order -> paid
        order.setTrangThai("paid");
        orderRepo.save(order);

        // 2. Cập nhật số lượt dùng của Coupon (nếu có)
        if (order.getCoupon() != null) {
            Coupon coupon = order.getCoupon();
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

        // 4. Kích hoạt học tập (Tạo Enrollment cho từng khóa học)
        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);
        for (OrderItem item : items) {
            boolean exists = enrollmentRepo.existsByUserIdAndCourseId(order.getUser().getId(), item.getCourse().getId());
            if (!exists) {
                Enrollment enrollment = new Enrollment();
                enrollment.setUser(order.getUser());
                enrollment.setCourse(item.getCourse());
                enrollment.setTrangThai("in_progress");
                enrollmentRepo.save(enrollment);
            }
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
