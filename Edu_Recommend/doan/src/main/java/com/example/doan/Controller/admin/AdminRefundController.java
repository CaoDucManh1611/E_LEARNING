package com.example.doan.Controller.admin;

import com.example.doan.Model.refund.RefundRequest;
import com.example.doan.Model.order.OrderItem;
import com.example.doan.Repository.refund.RefundRequestRepository;
import com.example.doan.Repository.order.OrderItemRepository;
import com.example.doan.Service.refund.Refund_Service;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/admin/refunds")
public class AdminRefundController {

    private final Refund_Service refundService;
    private final RefundRequestRepository refundRequestRepo;
    private final OrderItemRepository orderItemRepo;

    public AdminRefundController(Refund_Service refundService,
                                 RefundRequestRepository refundRequestRepo,
                                 OrderItemRepository orderItemRepo) {
        this.refundService = refundService;
        this.refundRequestRepo = refundRequestRepo;
        this.orderItemRepo = orderItemRepo;
    }

    /**
     * Hiển thị danh sách toàn bộ yêu cầu hoàn tiền.
     */
    @GetMapping
    public String viewRefunds(Model model) {
        List<RefundRequest> requests = refundService.Get_All_Refund_Requests();
        
        Map<Long, List<OrderItem>> orderItemsMap = new HashMap<>();
        for (RefundRequest req : requests) {
            List<OrderItem> items = orderItemRepo.findByOrderId(req.getOrder().getId());
            orderItemsMap.put(req.getOrder().getId(), items);
        }

        model.addAttribute("requests", requests);
        model.addAttribute("orderItemsMap", orderItemsMap);
        
        return "admin/refunds/list";
    }

    /**
     * Chấp nhận hoàn tiền.
     */
    @PostMapping("/{id}/approve")
    public String approveRefund(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            refundService.Approve_Refund(id);
            redirectAttributes.addFlashAttribute("successMsg", "Đã phê duyệt hoàn tiền và thu hồi quyền học tập thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Phê duyệt thất bại: " + e.getMessage());
        }
        return "redirect:/admin/refunds";
    }

    /**
     * Từ chối hoàn tiền.
     */
    @PostMapping("/{id}/reject")
    public String rejectRefund(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            refundService.Reject_Refund(id);
            redirectAttributes.addFlashAttribute("successMsg", "Đã từ chối yêu cầu hoàn tiền!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Từ chối thất bại: " + e.getMessage());
        }
        return "redirect:/admin/refunds";
    }
}
