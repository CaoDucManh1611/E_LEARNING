package com.example.doan.Controller.student;

import com.example.doan.Model.order.Order;
import com.example.doan.Model.order.OrderItem;
import com.example.doan.Model.order.Invoice;
import com.example.doan.Model.user.User;
import com.example.doan.Model.refund.RefundRequest;
import com.example.doan.Repository.order.OrderRepository;
import com.example.doan.Repository.order.OrderItemRepository;
import com.example.doan.Repository.refund.RefundRequestRepository;
import com.example.doan.Service.refund.Refund_Service;
import com.example.doan.Service.user.User_Service;
import com.example.doan.Service.order.Invoice_Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Controller
@RequestMapping("/student")
public class StudentRefundController {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final RefundRequestRepository refundRequestRepo;
    private final Refund_Service refundService;
    private final User_Service userService;
    private final Invoice_Service invoiceService;

    public StudentRefundController(OrderRepository orderRepo,
                                   OrderItemRepository orderItemRepo,
                                   RefundRequestRepository refundRequestRepo,
                                   Refund_Service refundService,
                                   User_Service userService,
                                   Invoice_Service invoiceService) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.refundRequestRepo = refundRequestRepo;
        this.refundService = refundService;
        this.userService = userService;
        this.invoiceService = invoiceService;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return userService.FindUserByEmail(auth.getName());
        }
        return null;
    }

    /**
     * Hiển thị danh sách đơn hàng đã mua của học viên.
     */
    @GetMapping("/orders")
    public String viewOrders(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Order> orders = orderRepo.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        
        // Map để lưu trữ danh sách các item cho từng đơn hàng
        Map<Long, List<OrderItem>> orderItemsMap = new HashMap<>();
        // Map lưu trạng thái có đủ điều kiện hoàn tiền hay không
        Map<Long, Boolean> canRefundMap = new HashMap<>();
        // Map lưu lý do nếu đã có yêu cầu hoàn tiền
        Map<Long, RefundRequest> refundRequestsMap = new HashMap<>();

        for (Order order : orders) {
            List<OrderItem> items = orderItemRepo.findByOrderId(order.getId());
            orderItemsMap.put(order.getId(), items);
            canRefundMap.put(order.getId(), refundService.Can_Request_Refund(order));
            
            Optional<RefundRequest> reqOpt = refundRequestRepo.findByOrderId(order.getId());
            reqOpt.ifPresent(refundRequest -> refundRequestsMap.put(order.getId(), refundRequest));
        }

        model.addAttribute("orders", orders);
        model.addAttribute("orderItemsMap", orderItemsMap);
        model.addAttribute("canRefundMap", canRefundMap);
        model.addAttribute("refundRequestsMap", refundRequestsMap);

        return "student/orders";
    }

    /**
     * Gửi yêu cầu hoàn tiền.
     */
    @PostMapping("/orders/{orderId}/refund")
    public String requestRefund(@PathVariable("orderId") Long orderId,
                                @RequestParam("lyDo") String lyDo,
                                RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            refundService.Create_Refund_Request(orderId, lyDo);
            redirectAttributes.addFlashAttribute("successMsg", "Gửi yêu cầu hoàn tiền thành công! Vui lòng chờ Admin phê duyệt.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Gửi yêu cầu thất bại: " + e.getMessage());
        }

        return "redirect:/student/orders";
    }

    /**
     * Hiển thị hóa đơn của đơn hàng.
     */
    @GetMapping("/orders/{orderId}/invoice")
    public String viewInvoice(@PathVariable("orderId") Long orderId, Model model) {
        User currentUser = getCurrentUser();
        Order order = orderRepo.findById(orderId).orElse(null);
        
        if (currentUser == null || order == null || !order.getUser().getId().equals(currentUser.getId())) {
            return "redirect:/access-deny";
        }

        // Lấy hóa đơn liên kết hoặc tự sinh nếu chưa có (phòng trường hợp đơn hàng cũ đã thanh toán trước khi cập nhật tính năng)
        Invoice invoice = invoiceService.Get_Invoice_By_Order(orderId)
                .orElseGet(() -> invoiceService.Generate_Invoice_For_Order(order));

        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);

        model.addAttribute("order", order);
        model.addAttribute("invoice", invoice);
        model.addAttribute("items", items);

        return "student/invoice";
    }
}
