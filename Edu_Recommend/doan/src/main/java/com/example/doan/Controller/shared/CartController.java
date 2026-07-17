package com.example.doan.Controller.shared;

import com.example.doan.Model.course.Course;
import com.example.doan.Model.order.Coupon;
import com.example.doan.Service.order.Cart_Service;
import com.example.doan.Service.course.Course_Service;
import com.example.doan.Service.order.Coupon_Service;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;


@Controller
@RequestMapping("/cart")
public class CartController {

    private final Cart_Service cartService;
    private final Course_Service courseService;
    private final Coupon_Service couponService;

    public CartController(Cart_Service cartService, 
                          Course_Service courseService,
                          Coupon_Service couponService) {
        this.cartService = cartService;
        this.courseService = courseService;
        this.couponService = couponService;
    }

    // 1. Hiển thị giỏ hàng và tính toán tiền giảm giá
    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        List<Course> cartCourses = cartService.Get_Cart_Courses(session);
        BigDecimal total = cartService.Get_Cart_Total(session);

        Coupon appliedCoupon = (Coupon) session.getAttribute("appliedCoupon");
        BigDecimal discount = cartService.Calculate_Discount(session, appliedCoupon);
        BigDecimal discountedTotal = cartService.Get_Cart_Discounted_Total(session, appliedCoupon);

        model.addAttribute("cartCourses", cartCourses);
        model.addAttribute("total", total);
        model.addAttribute("appliedCoupon", appliedCoupon);
        model.addAttribute("discount", discount);
        model.addAttribute("discountedTotal", discountedTotal);

        return "student/cart";
    }

    // 2. Thêm khóa học vào giỏ hàng
    @GetMapping("/add/{courseId}")
    public String addToCart(@PathVariable("courseId") Long courseId, 
                            HttpSession session, 
                            RedirectAttributes redirectAttributes) {
        try {
            Course course = courseService.Get_ById(courseId);
            if (course != null && "active".equals(course.getTrangThai())) {
                cartService.Add_To_Cart(session, courseId);
                redirectAttributes.addFlashAttribute("successMsg", "Đã thêm khóa học vào giỏ hàng!");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/cart";
    }

    // 3. Xóa khóa học khỏi giỏ hàng
    @GetMapping("/remove/{courseId}")
    public String removeFromCart(@PathVariable("courseId") Long courseId, HttpSession session) {
        cartService.Remove_From_Cart(session, courseId);
        return "redirect:/cart";
    }

    // 4. Áp dụng mã giảm giá vào session
    @PostMapping("/apply-coupon")
    public String applyCoupon(@RequestParam("couponCode") String couponCode, 
                              HttpSession session, 
                              RedirectAttributes redirectAttributes) {
        try {
            Coupon coupon = couponService.Validate_Coupon(couponCode);
            session.setAttribute("appliedCoupon", coupon);
            redirectAttributes.addFlashAttribute("successMsg", "Áp dụng mã giảm giá thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/cart";
    }

    // 5. Gỡ bỏ mã giảm giá khỏi session
    @GetMapping("/remove-coupon")
    public String removeCoupon(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("appliedCoupon");
        redirectAttributes.addFlashAttribute("successMsg", "Đã gỡ mã giảm giá!");
        return "redirect:/cart";
    }
}
