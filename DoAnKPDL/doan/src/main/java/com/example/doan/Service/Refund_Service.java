package com.example.doan.Service;

import com.example.doan.Model.Order;
import com.example.doan.Model.OrderItem;
import com.example.doan.Model.Enrollment;
import com.example.doan.Model.RefundRequest;
import com.example.doan.Repository.OrderRepository;
import com.example.doan.Repository.OrderItemRepository;
import com.example.doan.Repository.EnrollmentRepository;
import com.example.doan.Repository.RefundRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service quản lý logic nghiệp vụ Yêu cầu hoàn tiền (Refund Requests).
 * File: Service/Refund_Service.java
 */
@Service
public class Refund_Service {

    private final RefundRequestRepository refundRequestRepo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final EnrollmentRepository enrollmentRepo;

    public Refund_Service(RefundRequestRepository refundRequestRepo,
                          OrderRepository orderRepo,
                          OrderItemRepository orderItemRepo,
                          EnrollmentRepository enrollmentRepo) {
        this.refundRequestRepo = refundRequestRepo;
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.enrollmentRepo = enrollmentRepo;
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
        }
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
    }
}
