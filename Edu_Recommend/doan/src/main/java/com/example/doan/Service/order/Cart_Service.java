package com.example.doan.Service.order;

import com.example.doan.Model.course.Course;
import com.example.doan.Model.order.Coupon;
import com.example.doan.Model.user.User;
import com.example.doan.Model.order.CartItem;
import com.example.doan.Repository.course.CourseRepository;
import com.example.doan.Repository.user.UserRepository;
import com.example.doan.Repository.order.CartItemRepository;
import com.example.doan.Repository.enrollment.EnrollmentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service
public class Cart_Service {

    private static final String CART_SESSION_KEY = "cart";
    private final CourseRepository courseRepo;
    private final UserRepository userRepo;
    private final CartItemRepository cartItemRepo;
    private final EnrollmentRepository enrollmentRepo;

    public Cart_Service(CourseRepository courseRepo,
                        UserRepository userRepo,
                        CartItemRepository cartItemRepo,
                        EnrollmentRepository enrollmentRepo) {
        this.courseRepo = courseRepo;
        this.userRepo = userRepo;
        this.cartItemRepo = cartItemRepo;
        this.enrollmentRepo = enrollmentRepo;
    }

    private User getCurrentUser() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return userRepo.findByEmail(auth.getName());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Long> getCartItems(HttpSession session) {
        List<Long> cart = (List<Long>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    public List<Long> Get_Db_Cart_Items(Long userId) {
        return cartItemRepo.findByUserId(userId).stream()
                .map(item -> item.getCourse().getId())
                .toList();
    }

    @Transactional
    public void Save_Db_Cart_Item(Long userId, Long courseId) {
        if (!cartItemRepo.findByUserIdAndCourseId(userId, courseId).isPresent()) {
            User user = userRepo.findById(userId).orElse(null);
            Course course = courseRepo.findById(courseId).orElse(null);
            if (user != null && course != null) {
                cartItemRepo.save(new CartItem(user, course));
            }
        }
    }

    /**
     * Thêm khóa học vào giỏ hàng (nếu chưa tồn tại).
     * Kiểm tra xem học viên đã sở hữu khóa học hay chưa để ngăn chặn mua trùng.
     */
    public void Add_To_Cart(HttpSession session, Long courseId) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            boolean isEnrolled = enrollmentRepo.existsByUserIdAndCourseId(currentUser.getId(), courseId);
            if (isEnrolled) {
                throw new IllegalArgumentException("Bạn đã đăng ký/sở hữu khóa học này rồi!");
            }
        }

        List<Long> cart = getCartItems(session);
        if (!cart.contains(courseId)) {
            cart.add(courseId);
        }
        if (currentUser != null) {
            Save_Db_Cart_Item(currentUser.getId(), courseId);
        }
    }

    /**
     * Xóa khóa học khỏi giỏ hàng.
     */
    public void Remove_From_Cart(HttpSession session, Long courseId) {
        List<Long> cart = getCartItems(session);
        cart.remove(courseId);
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            cartItemRepo.deleteByUserIdAndCourseId(currentUser.getId(), courseId);
        }
    }

    /**
     * Lấy danh sách các khóa học thực tế đang nằm trong giỏ hàng.
     */
    public List<Course> Get_Cart_Courses(HttpSession session) {
        List<Long> courseIds = getCartItems(session);
        List<Course> courses = new ArrayList<>();
        for (Long id : courseIds) {
            courseRepo.findById(id).ifPresent(courses::add);
        }
        return courses;
    }

    /**
     * Tính tổng số tiền các sản phẩm trong giỏ hàng.
     */
    public BigDecimal Get_Cart_Total(HttpSession session) {
        List<Course> courses = Get_Cart_Courses(session);
        BigDecimal total = BigDecimal.ZERO;
        for (Course course : courses) {
            total = total.add(course.getGia());
        }
        return total;
    }

    /**
     * Tính số tiền được giảm giá của Coupon.
     */
    public BigDecimal Calculate_Discount(HttpSession session, Coupon coupon) {
        if (coupon == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = Get_Cart_Total(session);
        BigDecimal discount = BigDecimal.ZERO;

        if ("percent".equalsIgnoreCase(coupon.getLoaiGiam())) {
            BigDecimal factor = coupon.getGiaTri().divide(BigDecimal.valueOf(100));
            discount = total.multiply(factor);
        } else if ("fixed".equalsIgnoreCase(coupon.getLoaiGiam())) {
            discount = coupon.getGiaTri();
        }

        if (discount.compareTo(total) > 0) {
            discount = total;
        }

        return discount;
    }

    /**
     * Tổng tiền sau giảm giá.
     */
    public BigDecimal Get_Cart_Discounted_Total(HttpSession session, Coupon coupon) {
        BigDecimal total = Get_Cart_Total(session);
        BigDecimal discount = Calculate_Discount(session, coupon);
        return total.subtract(discount);
    }

    /**
     * Làm trống giỏ hàng.
     */
    public void Clear_Cart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            cartItemRepo.deleteByUserId(currentUser.getId());
        }
    }
}
