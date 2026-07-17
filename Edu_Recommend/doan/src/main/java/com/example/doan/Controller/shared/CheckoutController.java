package com.example.doan.Controller.shared;

import com.example.doan.Model.course.Course;
import com.example.doan.Model.order.Coupon;
import com.example.doan.Model.order.Order;
import com.example.doan.Model.user.User;
import com.example.doan.Service.order.Cart_Service;
import com.example.doan.Service.order.Order_Service;
import com.example.doan.Service.user.User_Service;
import com.example.doan.Repository.enrollment.EnrollmentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;


@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final Cart_Service cartService;
    private final Order_Service orderService;
    private final User_Service userService;
    private final EnrollmentRepository enrollmentRepo;

    public CheckoutController(Cart_Service cartService, 
                              Order_Service orderService, 
                              User_Service userService,
                              EnrollmentRepository enrollmentRepo) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.userService = userService;
        this.enrollmentRepo = enrollmentRepo;
    }

    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return userService.FindUserByEmail(auth.getName());
        }
        return null;
    }

    // 1. Tiến hành thanh toán: Tạo Order pending từ giỏ hàng Session và Coupon chiết khấu
    @PostMapping
    public String proceedToCheckout(HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = getLoggedInUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Course> cartCourses = cartService.Get_Cart_Courses(session);
        if (cartCourses.isEmpty()) {
            return "redirect:/cart";
        }

        // Kiểm tra chặn mua lại khóa học đã sở hữu
        for (Course course : cartCourses) {
            boolean isEnrolled = enrollmentRepo.existsByUserIdAndCourseId(currentUser.getId(), course.getId());
            if (isEnrolled) {
                cartService.Remove_From_Cart(session, course.getId());
                redirectAttributes.addFlashAttribute("errorMsg", "Khóa học '" + course.getTenKhoaHoc() + "' đã được bạn sở hữu! Hệ thống tự động gỡ khỏi giỏ hàng.");
                return "redirect:/cart";
            }
        }

        // Lấy thông tin mã giảm giá đang áp dụng trong session
        Coupon appliedCoupon = (Coupon) session.getAttribute("appliedCoupon");
        BigDecimal total = cartService.Get_Cart_Discounted_Total(session, appliedCoupon);
        
        // Tạo đơn hàng pending lưu thông tin Coupon
        Order order = orderService.Create_Order(currentUser, cartCourses, total, appliedCoupon);
        
        // Xóa giỏ hàng và mã giảm giá đã áp dụng sau khi chốt tạo đơn hàng thành công
        cartService.Clear_Cart(session);
        session.removeAttribute("appliedCoupon");

        return "redirect:/checkout/simulator/" + order.getId();
    }

    // 2. Giao diện Cổng thanh toán giả lập
    @GetMapping("/simulator/{orderId}")
    public String viewSimulator(@PathVariable("orderId") Long orderId, Model model) {
        User currentUser = getLoggedInUser();
        Order order = orderService.Get_Order_ById(orderId);
        
        if (currentUser == null || order == null || !order.getUser().getId().equals(currentUser.getId())) {
            return "redirect:/access-deny";
        }

        if (!"pending".equals(order.getTrangThai())) {
            return "redirect:/student/courses";
        }

        model.addAttribute("order", order);
        return "student/simulator";
    }

    // 3. Xử lý khi thanh toán thành công (Success)
    @PostMapping("/simulator/success/{orderId}")
    public String paymentSuccess(@PathVariable("orderId") Long orderId) {
        orderService.Complete_Payment(orderId, "bank_transfer");
        return "redirect:/checkout/success/" + orderId;
    }

    // 4. Xử lý khi thanh toán thất bại/Hủy bỏ (Cancel)
    @PostMapping("/simulator/cancel/{orderId}")
    public String paymentCancel(@PathVariable("orderId") Long orderId) {
        orderService.Cancel_Order(orderId);
        return "redirect:/checkout/failed/" + orderId;
    }

    // 5. Giao diện báo thành công
    @GetMapping("/success/{orderId}")
    public String getSuccess(@PathVariable("orderId") Long orderId, Model model) {
        User currentUser = getLoggedInUser();
        Order order = orderService.Get_Order_ById(orderId);
        if (currentUser == null || order == null || !order.getUser().getId().equals(currentUser.getId())) {
            return "redirect:/access-deny";
        }
        model.addAttribute("order", order);
        return "student/success";
    }

    // 6. Giao diện báo thất bại
    @GetMapping("/failed/{orderId}")
    public String getFailed(@PathVariable("orderId") Long orderId, Model model) {
        User currentUser = getLoggedInUser();
        Order order = orderService.Get_Order_ById(orderId);
        if (currentUser == null || order == null || !order.getUser().getId().equals(currentUser.getId())) {
            return "redirect:/access-deny";
        }
        model.addAttribute("order", order);
        return "student/failed";
    }
}
