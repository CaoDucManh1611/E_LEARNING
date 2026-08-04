package com.example.doan.Controller.api;

import com.example.doan.Model.course.Course;
import com.example.doan.Model.order.Coupon;
import com.example.doan.Model.order.Order;
import com.example.doan.Model.user.User;
import com.example.doan.Repository.enrollment.EnrollmentRepository;
import com.example.doan.Repository.order.OrderItemRepository;
import com.example.doan.Service.course.Course_Service;
import com.example.doan.Service.order.Cart_Service;
import com.example.doan.Service.order.Coupon_Service;
import com.example.doan.Service.order.Order_Service;
import com.example.doan.Service.user.User_Service;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class Cart_Checkout_Api_Controller {

    private final Cart_Service cartService;
    private final Course_Service courseService;
    private final Coupon_Service couponService;
    private final Order_Service orderService;
    private final User_Service userService;
    private final EnrollmentRepository enrollmentRepo;
    private final OrderItemRepository orderItemRepo;

    public Cart_Checkout_Api_Controller(Cart_Service cartService,
                                        Course_Service courseService,
                                        Coupon_Service couponService,
                                        Order_Service orderService,
                                        User_Service userService,
                                        EnrollmentRepository enrollmentRepo,
                                        OrderItemRepository orderItemRepo) {
        this.cartService = cartService;
        this.courseService = courseService;
        this.couponService = couponService;
        this.orderService = orderService;
        this.userService = userService;
        this.enrollmentRepo = enrollmentRepo;
        this.orderItemRepo = orderItemRepo;
    }

    // 1. Xem giỏ hàng
    @GetMapping("/cart")
    public ResponseEntity<?> viewCart(HttpSession session) {
        return ResponseEntity.ok(cartData(session));
    }

    // 2. Thêm khóa học vào giỏ hàng
    @PostMapping("/cart/items/{courseId}")
    public ResponseEntity<?> addToCart(@PathVariable("courseId") Long courseId,
                                       HttpSession session) {
        try {
            Course course = courseService.Get_ById(courseId);
            if (course == null || !"active".equals(course.getTrangThai())) {
                return ResponseEntity.status(404).body(Api_Mapper.error("Khóa học không tồn tại hoặc chưa hoạt động"));
            }

            cartService.Add_To_Cart(session, courseId);
            Map<String, Object> result = cartData(session);
            result.put("message", "Đã thêm khóa học vào giỏ hàng!");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Api_Mapper.error(e.getMessage()));
        }
    }

    // 3. Xóa khóa học khỏi giỏ hàng
    @DeleteMapping("/cart/items/{courseId}")
    public ResponseEntity<?> removeFromCart(@PathVariable("courseId") Long courseId,
                                            HttpSession session) {
        cartService.Remove_From_Cart(session, courseId);
        Map<String, Object> result = cartData(session);
        result.put("message", "Đã xóa khóa học khỏi giỏ hàng.");
        return ResponseEntity.ok(result);
    }

    // 4. Áp dụng/gỡ mã giảm giá
    @PostMapping("/cart/coupon")
    public ResponseEntity<?> applyCoupon(@RequestBody Map<String, Object> body,
                                         HttpSession session) {
        try {
            String couponCode = body.get("couponCode") != null ? body.get("couponCode").toString() : "";
            Coupon coupon = couponService.Validate_Coupon(couponCode);
            session.setAttribute("appliedCoupon", coupon);

            Map<String, Object> result = cartData(session);
            result.put("message", "Áp dụng mã giảm giá thành công!");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Api_Mapper.error(e.getMessage()));
        }
    }

    @DeleteMapping("/cart/coupon")
    public ResponseEntity<?> removeCoupon(HttpSession session) {
        session.removeAttribute("appliedCoupon");
        Map<String, Object> result = cartData(session);
        result.put("message", "Đã gỡ mã giảm giá!");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/cart")
    public ResponseEntity<?> clearCart(HttpSession session) {
        cartService.Clear_Cart(session);
        session.removeAttribute("appliedCoupon");
        return ResponseEntity.ok(cartData(session));
    }

    // 5. Tạo đơn hàng pending từ giỏ hàng
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(HttpSession session) {
        User currentUser = getLoggedInUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Api_Mapper.error("Chưa đăng nhập"));
        }

        List<Course> cartCourses = cartService.Get_Cart_Courses(session);
        if (cartCourses.isEmpty()) {
            return ResponseEntity.badRequest().body(Api_Mapper.error("Giỏ hàng đang trống"));
        }

        for (Course course : cartCourses) {
            boolean isEnrolled = enrollmentRepo.existsByUserIdAndCourseId(currentUser.getId(), course.getId());
            if (isEnrolled) {
                cartService.Remove_From_Cart(session, course.getId());
                return ResponseEntity.badRequest().body(Api_Mapper.error("Khóa học '" + course.getTenKhoaHoc() + "' đã được bạn sở hữu! Hệ thống tự động gỡ khỏi giỏ hàng."));
            }
        }

        Coupon appliedCoupon = (Coupon) session.getAttribute("appliedCoupon");
        BigDecimal total = cartService.Get_Cart_Discounted_Total(session, appliedCoupon);
        Order order = orderService.Create_Order(currentUser, cartCourses, total, appliedCoupon);

        cartService.Clear_Cart(session);
        session.removeAttribute("appliedCoupon");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "Tạo đơn hàng thành công!");
        result.put("order", Api_Mapper.order(order, orderItemRepo.findByOrderId(order.getId())));
        result.put("paymentSimulatorUrl", "/api/v1/checkout/" + order.getId() + "/success");
        return ResponseEntity.ok(result);
    }

    // 6. Xem/xử lý thanh toán giả lập
    @GetMapping("/checkout/{orderId}")
    public ResponseEntity<?> checkoutOrder(@PathVariable("orderId") Long orderId) {
        User currentUser = getLoggedInUser();
        Order order = orderService.Get_Order_ById(orderId);

        if (currentUser == null) return ResponseEntity.status(401).body(Api_Mapper.error("Chưa đăng nhập"));
        if (order == null || !order.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(Api_Mapper.error("Bạn không có quyền xem đơn hàng này"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "order", Api_Mapper.order(order, orderItemRepo.findByOrderId(orderId))
        ));
    }

    @PutMapping("/checkout/{orderId}/success")
    public ResponseEntity<?> paymentSuccess(@PathVariable("orderId") Long orderId) {
        User currentUser = getLoggedInUser();
        Order order = orderService.Get_Order_ById(orderId);
        if (currentUser == null) return ResponseEntity.status(401).body(Api_Mapper.error("Chưa đăng nhập"));
        if (order == null || !order.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(Api_Mapper.error("Bạn không có quyền thanh toán đơn hàng này"));
        }

        orderService.Complete_Payment(orderId, "bank_transfer");
        order = orderService.Get_Order_ById(orderId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Thanh toán thành công!",
                "order", Api_Mapper.order(order, orderItemRepo.findByOrderId(orderId))
        ));
    }

    @PutMapping("/checkout/{orderId}/cancel")
    public ResponseEntity<?> paymentCancel(@PathVariable("orderId") Long orderId) {
        User currentUser = getLoggedInUser();
        Order order = orderService.Get_Order_ById(orderId);
        if (currentUser == null) return ResponseEntity.status(401).body(Api_Mapper.error("Chưa đăng nhập"));
        if (order == null || !order.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(Api_Mapper.error("Bạn không có quyền hủy đơn hàng này"));
        }

        orderService.Cancel_Order(orderId);
        order = orderService.Get_Order_ById(orderId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã hủy thanh toán.",
                "order", Api_Mapper.order(order, orderItemRepo.findByOrderId(orderId))
        ));
    }

    private Map<String, Object> cartData(HttpSession session) {
        List<Course> cartCourses = cartService.Get_Cart_Courses(session);
        BigDecimal total = cartService.Get_Cart_Total(session);
        Coupon appliedCoupon = (Coupon) session.getAttribute("appliedCoupon");
        BigDecimal discount = cartService.Calculate_Discount(session, appliedCoupon);
        BigDecimal discountedTotal = cartService.Get_Cart_Discounted_Total(session, appliedCoupon);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("cartCourses", cartCourses.stream().map(Api_Mapper::course).toList());
        result.put("total", total);
        result.put("appliedCoupon", Api_Mapper.coupon(appliedCoupon));
        result.put("discount", discount);
        result.put("discountedTotal", discountedTotal);
        return result;
    }

    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return userService.FindUserByEmail(auth.getName());
        }
        return null;
    }
}
