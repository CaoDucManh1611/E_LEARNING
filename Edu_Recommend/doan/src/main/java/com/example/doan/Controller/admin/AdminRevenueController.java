package com.example.doan.Controller.admin;

import com.example.doan.Model.order.InstructorEarning;
import com.example.doan.Model.order.Order;
import com.example.doan.Model.order.Payment;
import com.example.doan.Model.refund.RefundRequest;
import com.example.doan.Repository.order.InstructorEarningRepository;
import com.example.doan.Repository.order.OrderRepository;
import com.example.doan.Repository.order.PaymentRepository;
import com.example.doan.Repository.refund.RefundRequestRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/admin/revenue")
public class AdminRevenueController {

    private final OrderRepository orderRepo;
    private final InstructorEarningRepository earningRepo;
    private final PaymentRepository paymentRepo;
    private final RefundRequestRepository refundRepo;

    public AdminRevenueController(OrderRepository orderRepo,
                                  InstructorEarningRepository earningRepo,
                                  PaymentRepository paymentRepo,
                                  RefundRequestRepository refundRepo) {
        this.orderRepo = orderRepo;
        this.earningRepo = earningRepo;
        this.paymentRepo = paymentRepo;
        this.refundRepo = refundRepo;
    }

    // 1. Hiển thị trang báo cáo doanh thu tổng hợp cho Admin
    @GetMapping
    public String getRevenueReport(Model model) {
        List<Order> allOrders = orderRepo.findAll();

        // 1. Lọc các đơn hàng đã từng thanh toán thành công
        List<Order> paidOrders = allOrders.stream()
                .filter(o -> "paid".equals(o.getTrangThai()) 
                        || "refund_requested".equals(o.getTrangThai()) 
                        || "refunded".equals(o.getTrangThai()))
                .collect(Collectors.toList());

        // 2. Tính tổng doanh thu hệ thống (Total Revenue)
        BigDecimal totalRevenue = paidOrders.stream()
                .map(Order::getTongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Đếm số lượng đơn hàng thành công
        int totalPaidOrders = paidOrders.size();

        // 4. Tính tổng doanh thu hoàn tiền (Refunded)
        List<Order> refundedOrders = allOrders.stream()
                .filter(o -> "refunded".equals(o.getTrangThai()))
                .collect(Collectors.toList());
        BigDecimal totalRefunded = refundedOrders.stream()
                .map(Order::getTongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. Tính tổng hoa hồng của giảng viên
        List<InstructorEarning> earnings = earningRepo.findAll();
        BigDecimal totalTeacherShare = earnings.stream()
                .map(InstructorEarning::getTienNhan)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 6. Tính tổng doanh thu thuần giữ lại của hệ thống (System Net)
        // Hệ thống Net = Doanh thu - Hoàn tiền - Hoa hồng giảng viên
        BigDecimal totalSystemNet = totalRevenue.subtract(totalRefunded).subtract(totalTeacherShare);

        // 7. Lấy danh sách giao dịch payments thành công
        List<Payment> payments = paymentRepo.findAll().stream()
                .filter(p -> "success".equals(p.getTrangThai()))
                .collect(Collectors.toList());

        // 8. Lấy danh sách yêu cầu hoàn tiền
        List<RefundRequest> refundRequests = refundRepo.findAll();

        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalPaidOrders", totalPaidOrders);
        model.addAttribute("totalRefunded", totalRefunded);
        model.addAttribute("totalTeacherShare", totalTeacherShare);
        model.addAttribute("totalSystemNet", totalSystemNet);

        model.addAttribute("paidOrders", paidOrders);
        model.addAttribute("earnings", earnings);
        model.addAttribute("payments", payments);
        model.addAttribute("refundRequests", refundRequests);

        return "admin/revenue";
    }
}
