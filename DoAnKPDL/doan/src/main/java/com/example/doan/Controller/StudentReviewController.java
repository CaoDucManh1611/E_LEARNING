package com.example.doan.Controller;

import com.example.doan.Model.Course;
import com.example.doan.Model.Review;
import com.example.doan.Model.User;
import com.example.doan.Repository.EnrollmentRepository;
import com.example.doan.Service.Course_Service;
import com.example.doan.Service.Review_Service;
import com.example.doan.Service.User_Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller tiếp nhận yêu cầu gửi Đánh giá & Nhận xét khóa học từ phía Học viên.
 * File: Controller/StudentReviewController.java
 */
@Controller
@RequestMapping("/student/courses/{courseId}/review")
public class StudentReviewController {

    private final Review_Service reviewService;
    private final Course_Service courseService;
    private final User_Service userService;
    private final EnrollmentRepository enrollmentRepo;

    public StudentReviewController(Review_Service reviewService,
                                   Course_Service courseService,
                                   User_Service userService,
                                   EnrollmentRepository enrollmentRepo) {
        this.reviewService = reviewService;
        this.courseService = courseService;
        this.userService = userService;
        this.enrollmentRepo = enrollmentRepo;
    }

    @PostMapping
    public String submitReview(@PathVariable("courseId") Long courseId,
                               @RequestParam("soSao") int soSao,
                               @RequestParam(value = "noiDung", required = false) String noiDung,
                               RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }

        User currentUser = userService.FindUserByEmail(auth.getName());
        if (currentUser == null) {
            return "redirect:/login";
        }

        Course course = courseService.Get_ById(courseId);
        if (course == null) {
            return "redirect:/student/courses";
        }

        // Ràng buộc 1: Phải sở hữu khóa học mới được đánh giá
        boolean isEnrolled = enrollmentRepo.existsByUserIdAndCourseId(currentUser.getId(), courseId);
        if (!isEnrolled) {
            redirectAttributes.addFlashAttribute("errorMsg", "Bạn cần sở hữu khóa học này để có thể đánh giá!");
            return "redirect:/student/courses/" + courseId;
        }

        // Ràng buộc đặc biệt: Admin/Giáo viên không được tự đánh giá khóa học của mình
        if ("admin".equals(currentUser.getRole())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Giáo viên không được tự đánh giá khóa học của mình!");
            return "redirect:/student/courses/" + courseId;
        }

        // Ràng buộc 2: Mỗi người chỉ được đánh giá 1 lần duy nhất
        boolean hasReviewed = reviewService.Has_Reviewed(currentUser.getId(), courseId);
        if (hasReviewed) {
            redirectAttributes.addFlashAttribute("errorMsg", "Bạn đã đánh giá khóa học này rồi!");
            return "redirect:/student/courses/" + courseId;
        }

        // Tạo mới Review
        Review review = new Review();
        review.setUser(currentUser);
        review.setCourse(course);
        review.setSoSao(soSao);
        // Tránh khoảng trắng
        review.setNoiDung(noiDung != null ? noiDung.trim() : "");
        reviewService.Save_Review(review);

        redirectAttributes.addFlashAttribute("successMsg", "Đăng tải đánh giá khóa học thành công!");
        return "redirect:/student/courses/" + courseId;
    }
}
