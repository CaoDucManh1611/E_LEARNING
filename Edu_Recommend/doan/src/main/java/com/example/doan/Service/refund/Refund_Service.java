package com.example.doan.Service.refund;

import com.example.doan.Model.order.Order;
import com.example.doan.Model.order.OrderItem;
import com.example.doan.Model.enrollment.Enrollment;
import com.example.doan.Model.refund.RefundRequest;
import com.example.doan.Model.order.InstructorEarning;
import com.example.doan.Repository.order.OrderRepository;
import com.example.doan.Repository.order.OrderItemRepository;
import com.example.doan.Repository.enrollment.EnrollmentRepository;
import com.example.doan.Repository.refund.RefundRequestRepository;
import com.example.doan.Repository.order.InstructorEarningRepository;
import com.example.doan.Service.notification.Notification_Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
public class Refund_Service {

    private final RefundRequestRepository refundRequestRepo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final InstructorEarningRepository earningRepo;
    private final Notification_Service notificationService;

    public Refund_Service(RefundRequestRepository refundRequestRepo,
                          OrderRepository orderRepo,
                          OrderItemRepository orderItemRepo,
                          EnrollmentRepository enrollmentRepo,
                          InstructorEarningRepository earningRepo,
                          Notification_Service notificationService) {
        this.refundRequestRepo = refundRequestRepo;
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.earningRepo = earningRepo;
        this.notificationService = notificationService;
    }

    /**
     * Kiểm tra đơn hàng có đủ điều kiện hoàn tiền không:
     * 1. Phải ở trạng thái "paid"
     * 2. Thời gian mua dưới 7 ngày
     * 3. Tiến độ học của tất cả các khóa trong đơn hàng phải dưới 20%
     */
    public boolean Can_Request_Refund(Order order) {
        if (order == null || !"paid".equals(order.getTrangThai())) {
            return false;
        }

        // 1. Kiểm tra 7 ngày
        if (order.getCreatedAt() != null) {
            LocalDateTime limitDate = order.getCreatedAt().plusDays(7);
            if (LocalDateTime.now().isAfter(limitDate)) {
                return false;
            }
        }

        // 2. Kiểm tra tiến độ học < 20% cho tất cả các khóa học trong đơn
        List<OrderItem> items = orderItemRepo.findByOrderId(order.getId());
        if (items.isEmpty()) {
            return false;
        }

        for (OrderItem item : items) {
            Optional<Enrollment> enrollOpt = enrollmentRepo.findByUserIdAndCourseId(order.getUser().getId(), item.getCourse().getId());
            if (enrollOpt.isPresent()) {
                Enrollment enrollment = enrollOpt.get();
                if (enrollment.getTienDoPercent() >= 20) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Học viên tạo yêu cầu hoàn tiền cho đơn hàng.
     */
    @Transactional
    public void Create_Refund_Request(Long orderId, String lyDo) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng!"));
        
        if (!Can_Request_Refund(order)) {
            throw new IllegalStateException("Đơn hàng này không đủ điều kiện hoàn tiền!");
        }

        // Kiểm tra xem đã có yêu cầu hoàn tiền cho đơn này chưa
        Optional<RefundRequest> existing = refundRequestRepo.findByOrderId(orderId);
        if (existing.isPresent()) {
            throw new IllegalStateException("Đã tồn tại yêu cầu hoàn tiền cho đơn hàng này!");
        }

        // Lưu yêu cầu
        RefundRequest request = new RefundRequest(order, lyDo);
        refundRequestRepo.save(request);

        // Chuyển trạng thái đơn hàng sang 'refund_requested'
        order.setTrangThai("refund_requested");
        orderRepo.save(order);

        // Thông báo cho Admin
        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);
        String courseNames = items.stream()
                .map(i -> i.getCourse() != null ? i.getCourse().getTenKhoaHoc() : "")
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.joining(", "));
        notificationService.Create_Refund_Request_Notification(order.getUser(), orderId, courseNames);
    }

    /**
     * Lấy danh sách yêu cầu hoàn tiền của học viên
     */
    public List<RefundRequest> Get_Refund_Requests_By_User(Long userId) {
        return refundRequestRepo.findByOrderUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Lấy tất cả yêu cầu hoàn tiền (dành cho Admin)
     */
    public List<RefundRequest> Get_All_Refund_Requests() {
        return refundRequestRepo.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Admin duyệt chấp thuận hoàn tiền:
     * - Order -> 'refunded'
     * - RefundRequest -> 'approved'
     * - Xóa các Enrollment tương ứng trong đơn
     * - Thu hồi doanh thu phân bổ của giảng viên tương ứng với các mặt hàng
     */
    @Transactional
    public void Approve_Refund(Long requestId) {
        RefundRequest request = refundRequestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu hoàn tiền!"));

        if (!"requested".equals(request.getTrangThai())) {
            throw new IllegalStateException("Yêu cầu này đã được xử lý từ trước!");
        }

        request.setTrangThai("approved");
        request.setXuLyAt(LocalDateTime.now());
        refundRequestRepo.save(request);

        Order order = request.getOrder();
        order.setTrangThai("refunded");
        orderRepo.save(order);

        // Thu hồi quyền học tập: Xóa Enrollment của học viên cho các khóa học trong đơn
        List<OrderItem> items = orderItemRepo.findByOrderId(order.getId());
        for (OrderItem item : items) {
            Optional<Enrollment> enrollOpt = enrollmentRepo.findByUserIdAndCourseId(order.getUser().getId(), item.getCourse().getId());
            enrollOpt.ifPresent(enrollmentRepo::delete);

            // Thu hồi doanh thu phân bổ của giảng viên tương ứng với mặt hàng này
            List<InstructorEarning> earnings = earningRepo.findAll().stream()
                    .filter(earn -> earn.getOrderItem().getId().equals(item.getId()))
                    .toList();
            for (InstructorEarning earning : earnings) {
                earningRepo.delete(earning);
            }
        }

        // Thông báo cho Học viên: hoàn tiền được duyệt
        String approvedCourseNames = items.stream()
                .map(i -> i.getCourse() != null ? i.getCourse().getTenKhoaHoc() : "")
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.joining(", "));
        notificationService.Create_Refund_Result_Notification(order.getUser(), approvedCourseNames, true);
    }

    /**
     * Admin từ chối hoàn tiền:
     * - Order -> 'paid' (trở lại trạng thái đã mua bình thường)
     * - RefundRequest -> 'rejected'
     */
    @Transactional
    public void Reject_Refund(Long requestId) {
        RefundRequest request = refundRequestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu hoàn tiền!"));

        if (!"requested".equals(request.getTrangThai())) {
            throw new IllegalStateException("Yêu cầu này đã được xử lý từ trước!");
        }

        request.setTrangThai("rejected");
        request.setXuLyAt(LocalDateTime.now());
        refundRequestRepo.save(request);

        Order order = request.getOrder();
        order.setTrangThai("paid");
        orderRepo.save(order);

        // Thông báo cho Học viên: hoàn tiền bị từ chối
        List<OrderItem> rejItems = orderItemRepo.findByOrderId(order.getId());
        String rejectedCourseNames = rejItems.stream()
                .map(i -> i.getCourse() != null ? i.getCourse().getTenKhoaHoc() : "")
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.joining(", "));
        notificationService.Create_Refund_Result_Notification(order.getUser(), rejectedCourseNames, false);
    }
}
