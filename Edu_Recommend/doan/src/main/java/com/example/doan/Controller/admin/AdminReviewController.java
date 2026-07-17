package com.example.doan.Controller.admin;

import com.example.doan.Model.review.Review;
import com.example.doan.Service.review.Review_Service;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final Review_Service reviewService;

    public AdminReviewController(Review_Service reviewService) {
        this.reviewService = reviewService;
    }

    // 1. Hiển thị danh sách tất cả các đánh giá
    @GetMapping
    public String list(Model model) {
        List<Review> reviews = reviewService.Get_All_Reviews();
        model.addAttribute("reviews", reviews);
        return "admin/reviews/list";
    }

    // 2. Thay đổi trạng thái hiển thị (Hiện/Ẩn)
    @PostMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable("id") Long id) {
        Review review = reviewService.Get_ById(id);
        if (review != null) {
            String newStatus = "visible".equals(review.getTrangThai()) ? "hidden" : "visible";
            reviewService.Update_Status(id, newStatus);
        }
        return "redirect:/admin/reviews";
    }

    // 3. Xóa đánh giá spam
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        reviewService.Delete_Review(id);
        return "redirect:/admin/reviews";
    }
}
