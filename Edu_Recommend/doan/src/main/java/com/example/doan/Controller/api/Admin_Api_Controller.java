package com.example.doan.Controller.api;

import com.example.doan.Model.course.Category;
import com.example.doan.Model.course.Course;
import com.example.doan.Model.course.Lesson;
import com.example.doan.Model.order.Coupon;
import com.example.doan.Model.order.InstructorEarning;
import com.example.doan.Model.order.Order;
import com.example.doan.Model.order.OrderItem;
import com.example.doan.Model.order.Payment;
import com.example.doan.Model.refund.RefundRequest;
import com.example.doan.Model.review.Review;
import com.example.doan.Model.user.StudentInfo;
import com.example.doan.Model.user.User;
import com.example.doan.Repository.order.InstructorEarningRepository;
import com.example.doan.Repository.order.OrderItemRepository;
import com.example.doan.Repository.order.OrderRepository;
import com.example.doan.Repository.order.PaymentRepository;
import com.example.doan.Repository.refund.RefundRequestRepository;
import com.example.doan.Repository.user.StudentInfoRepository;
import com.example.doan.Service.course.Category_Service;
import com.example.doan.Service.course.Course_Service;
import com.example.doan.Service.course.Lesson_Service;
import com.example.doan.Service.notification.Notification_Service;
import com.example.doan.Service.order.Coupon_Service;
import com.example.doan.Service.refund.Refund_Service;
import com.example.doan.Service.review.Review_Service;
import com.example.doan.Service.user.User_Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class Admin_Api_Controller {

    private final Category_Service categoryService;
    private final Course_Service courseService;
    private final Lesson_Service lessonService;
    private final Coupon_Service couponService;
    private final User_Service userService;
    private final Review_Service reviewService;
    private final Refund_Service refundService;
    private final Notification_Service notificationService;
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final PaymentRepository paymentRepo;
    private final RefundRequestRepository refundRepo;
    private final InstructorEarningRepository earningRepo;
    private final StudentInfoRepository studentInfoRepo;

    public Admin_Api_Controller(Category_Service categoryService,
                                Course_Service courseService,
                                Lesson_Service lessonService,
                                Coupon_Service couponService,
                                User_Service userService,
                                Review_Service reviewService,
                                Refund_Service refundService,
                                Notification_Service notificationService,
                                OrderRepository orderRepo,
                                OrderItemRepository orderItemRepo,
                                PaymentRepository paymentRepo,
                                RefundRequestRepository refundRepo,
                                InstructorEarningRepository earningRepo,
                                StudentInfoRepository studentInfoRepo) {
        this.categoryService = categoryService;
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.couponService = couponService;
        this.userService = userService;
        this.reviewService = reviewService;
        this.refundService = refundService;
        this.notificationService = notificationService;
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.paymentRepo = paymentRepo;
        this.refundRepo = refundRepo;
        this.earningRepo = earningRepo;
        this.studentInfoRepo = studentInfoRepo;
    }

    // ==================== CATEGORY API ====================

    @GetMapping("/categories")
    public ResponseEntity<?> categories() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", categoryService.Get_All_Categories().stream().map(Api_Mapper::category).toList()
        ));
    }

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@RequestBody Map<String, Object> body) {
        Category category = new Category();
        category.setTenDanhMuc(getString(body, "tenDanhMuc"));
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tạo danh mục thành công!",
                "data", Api_Mapper.category(categoryService.Create(category))
        ));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable("id") Long id,
                                            @RequestBody Map<String, Object> body) {
        Category category = new Category();
        category.setId(id);
        category.setTenDanhMuc(getString(body, "tenDanhMuc"));
        Category updated = categoryService.Update(category);
        if (updated == null) return ResponseEntity.status(404).body(Api_Mapper.error("Không tìm thấy danh mục"));
        return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật danh mục thành công!", "data", Api_Mapper.category(updated)));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable("id") Long id) {
        boolean ok = categoryService.Delete(id);
        if (!ok) {
            return ResponseEntity.badRequest().body(Api_Mapper.error("Không thể xóa danh mục này vì đang có khóa học trực thuộc!"));
        }
        return ResponseEntity.ok(Api_Mapper.ok("Đã xóa danh mục thành công!"));
    }

    // ==================== COURSE API ====================

    @GetMapping("/courses")
    public ResponseEntity<?> courses() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", courseService.Get_All_Courses().stream().map(Api_Mapper::course).toList()
        ));
    }

    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(@RequestBody Map<String, Object> body) {
        Course course = mapCourse(body, new Course());
        Course saved = courseService.Create(course);
        return ResponseEntity.ok(Map.of("success", true, "message", "Tạo khóa học thành công!", "data", Api_Mapper.course(saved)));
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable("id") Long id,
                                          @RequestBody Map<String, Object> body) {
        Course old = courseService.Get_ById(id);
        if (old == null) return ResponseEntity.status(404).body(Api_Mapper.error("Không tìm thấy khóa học"));

        Course course = mapCourse(body, old);
        course.setId(id);
        Course updated = courseService.Update(course);
        return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật khóa học thành công!", "data", Api_Mapper.course(updated)));
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable("id") Long id,
                                          @RequestParam(value = "reason", required = false) String reason) {
        if (reason != null && !reason.trim().isEmpty()) {
            courseService.Delete_With_Refund(id, reason);
        } else {
            courseService.Delete(id);
        }
        return ResponseEntity.ok(Api_Mapper.ok("Đã xóa khóa học."));
    }

    @PostMapping("/courses/{id}/approve")
    public ResponseEntity<?> approveCourse(@PathVariable("id") Long id) {
        Course course = courseService.Get_ById(id);
        if (course == null) return ResponseEntity.status(404).body(Api_Mapper.error("Không tìm thấy khóa học"));

        if ("pending_review".equals(course.getTrangThai())) {
            course.setTrangThai("active");
            courseService.Update(course);
            notificationService.Create_Course_Approved_Notification(course);
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã duyệt khóa học!", "data", Api_Mapper.course(course)));
    }

    @PostMapping("/courses/{id}/reject")
    public ResponseEntity<?> rejectCourse(@PathVariable("id") Long id) {
        Course course = courseService.Get_ById(id);
        if (course == null) return ResponseEntity.status(404).body(Api_Mapper.error("Không tìm thấy khóa học"));

        if ("pending_review".equals(course.getTrangThai())) {
            course.setTrangThai("draft");
            courseService.Update(course);
            notificationService.Create_Course_Rejected_Notification(course);
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã từ chối khóa học!", "data", Api_Mapper.course(course)));
    }

    // ==================== LESSON API ====================

    @GetMapping("/courses/{courseId}/lessons")
    public ResponseEntity<?> lessons(@PathVariable("courseId") Long courseId) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", lessonService.Get_Lessons_By_Course(courseId).stream().map(Api_Mapper::lesson).toList()
        ));
    }

    @PostMapping("/courses/{courseId}/lessons")
    public ResponseEntity<?> createLesson(@PathVariable("courseId") Long courseId,
                                          @RequestBody Map<String, Object> body) {
        Course course = courseService.Get_ById(courseId);
        if (course == null) return ResponseEntity.status(404).body(Api_Mapper.error("Không tìm thấy khóa học"));

        Lesson lesson = mapLesson(body, new Lesson());
        lesson.setCourse(course);
        Lesson saved = lessonService.Create(lesson);
        return ResponseEntity.ok(Map.of("success", true, "message", "Thêm bài học thành công!", "data", Api_Mapper.lesson(saved)));
    }

    @PutMapping("/courses/{courseId}/lessons/{id}")
    public ResponseEntity<?> updateLesson(@PathVariable("courseId") Long courseId,
                                          @PathVariable("id") Long id,
                                          @RequestBody Map<String, Object> body) {
        Course course = courseService.Get_ById(courseId);
        Lesson old = lessonService.Get_ById(id);
        if (course == null || old == null) return ResponseEntity.status(404).body(Api_Mapper.error("Không tìm thấy khóa học hoặc bài học"));
        if (old.getCourse() == null || !old.getCourse().getId().equals(courseId)) {
            return ResponseEntity.status(404).body(Api_Mapper.error("Bài học không thuộc khóa học này"));
        }

        Lesson lesson = mapLesson(body, old);
        lesson.setId(id);
        lesson.setCourse(course);
        Lesson updated = lessonService.Update(lesson);
        return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật bài học thành công!", "data", Api_Mapper.lesson(updated)));
    }

    @DeleteMapping("/courses/{courseId}/lessons/{id}")
    public ResponseEntity<?> deleteLesson(@PathVariable("courseId") Long courseId,
                                          @PathVariable("id") Long id) {
        Lesson lesson = lessonService.Get_ById(id);
        if (lesson == null || lesson.getCourse() == null || !lesson.getCourse().getId().equals(courseId)) {
            return ResponseEntity.status(404).body(Api_Mapper.error("Bài học không thuộc khóa học này"));
        }

        lessonService.Delete(id);
        return ResponseEntity.ok(Api_Mapper.ok("Đã xóa bài học."));
    }

    // ==================== COUPON API ====================

    @GetMapping("/coupons")
    public ResponseEntity<?> coupons() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", couponService.Get_All_Coupons().stream().map(Api_Mapper::coupon).toList()
        ));
    }

    @PostMapping("/coupons")
    public ResponseEntity<?> saveCoupon(@RequestBody Coupon coupon) {
        if (coupon.getMaCode() != null) {
            coupon.setMaCode(coupon.getMaCode().trim().toUpperCase());
        }
        Coupon saved = couponService.Save_Coupon(coupon);
        return ResponseEntity.ok(Map.of("success", true, "message", "Lưu mã giảm giá thành công!", "data", Api_Mapper.coupon(saved)));
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<?> updateCoupon(@PathVariable("id") Long id,
                                          @RequestBody Coupon coupon) {
        coupon.setId(id);
        if (coupon.getMaCode() != null) {
            coupon.setMaCode(coupon.getMaCode().trim().toUpperCase());
        }
        Coupon saved = couponService.Save_Coupon(coupon);
        return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật mã giảm giá thành công!", "data", Api_Mapper.coupon(saved)));
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable("id") Long id) {
        couponService.Delete_Coupon(id);
        return ResponseEntity.ok(Api_Mapper.ok("Đã xóa mã giảm giá."));
    }

    // ==================== USER API ====================

    @GetMapping("/users")
    public ResponseEntity<?> users() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", userService.Get_All_Users().stream().map(Api_Mapper::user).toList()
        ));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> changeRole(@PathVariable("id") Long id,
                                        @RequestBody Map<String, Object> body) {
        User targetUser = userService.Get_ById(id);
        if (targetUser == null) return ResponseEntity.status(404).body(Api_Mapper.error("Không tìm thấy người dùng"));
        if (targetUser.getEmail().equals(getLoggedInUsername())) {
            return ResponseEntity.badRequest().body(Api_Mapper.error("Không thể tự thay đổi phân quyền của chính mình."));
        }

        userService.ChangeRole(id, getString(body, "role"));
        return ResponseEntity.ok(Api_Mapper.ok("Đã thay đổi quyền thành công."));
    }

    @PutMapping("/users/{id}/lock")
    public ResponseEntity<?> toggleLock(@PathVariable("id") Long id) {
        User targetUser = userService.Get_ById(id);
        if (targetUser == null) return ResponseEntity.status(404).body(Api_Mapper.error("Không tìm thấy người dùng"));
        if (targetUser.getEmail().equals(getLoggedInUsername())) {
            return ResponseEntity.badRequest().body(Api_Mapper.error("Hệ thống chặn: Không thể tự khóa tài khoản của chính mình."));
        }

        userService.ToggleLock(id);
        return ResponseEntity.ok(Api_Mapper.ok("Trạng thái khóa tài khoản đã thay đổi."));
    }

    // ==================== REVIEW / REFUND / REVENUE API ====================

    @GetMapping("/reviews")
    public ResponseEntity<?> reviews() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", reviewService.Get_All_Reviews().stream().map(Api_Mapper::review).toList()
        ));
    }

    @PutMapping("/reviews/{id}/toggle")
    public ResponseEntity<?> toggleReview(@PathVariable("id") Long id) {
        Review review = reviewService.Get_ById(id);
        if (review == null) return ResponseEntity.status(404).body(Api_Mapper.error("Không tìm thấy đánh giá"));

        String newStatus = "visible".equals(review.getTrangThai()) ? "hidden" : "visible";
        reviewService.Update_Status(id, newStatus);
        return ResponseEntity.ok(Api_Mapper.ok("Đã cập nhật trạng thái đánh giá."));
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable("id") Long id) {
        reviewService.Delete_Review(id);
        return ResponseEntity.ok(Api_Mapper.ok("Đã xóa đánh giá."));
    }

    @GetMapping("/refunds")
    public ResponseEntity<?> refunds() {
        List<Map<String, Object>> data = refundService.Get_All_Refund_Requests().stream()
                .map(r -> Api_Mapper.refund(r, orderItemRepo.findByOrderId(r.getOrder().getId())))
                .toList();
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @PutMapping("/refunds/{id}/approve")
    public ResponseEntity<?> approveRefund(@PathVariable("id") Long id) {
        try {
            refundService.Approve_Refund(id);
            return ResponseEntity.ok(Api_Mapper.ok("Đã phê duyệt hoàn tiền và thu hồi quyền học tập thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Api_Mapper.error("Phê duyệt thất bại: " + e.getMessage()));
        }
    }

    @PutMapping("/refunds/{id}/reject")
    public ResponseEntity<?> rejectRefund(@PathVariable("id") Long id) {
        try {
            refundService.Reject_Refund(id);
            return ResponseEntity.ok(Api_Mapper.ok("Đã từ chối yêu cầu hoàn tiền!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Api_Mapper.error("Từ chối thất bại: " + e.getMessage()));
        }
    }

    @GetMapping("/revenue")
    public ResponseEntity<?> revenue() {
        List<Order> allOrders = orderRepo.findAll();
        List<Order> paidOrders = allOrders.stream()
                .filter(o -> "paid".equals(o.getTrangThai())
                        || "refund_requested".equals(o.getTrangThai())
                        || "refunded".equals(o.getTrangThai()))
                .toList();
        List<Order> refundedOrders = allOrders.stream()
                .filter(o -> "refunded".equals(o.getTrangThai()))
                .toList();
        List<InstructorEarning> earnings = earningRepo.findAll();
        List<Payment> payments = paymentRepo.findAll().stream()
                .filter(p -> "success".equals(p.getTrangThai()))
                .toList();
        List<RefundRequest> refundRequests = refundRepo.findAll();

        BigDecimal totalRevenue = paidOrders.stream().map(Order::getTongTien).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRefunded = refundedOrders.stream().map(Order::getTongTien).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTeacherShare = earnings.stream().map(InstructorEarning::getTienNhan).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSystemNet = totalRevenue.subtract(totalRefunded).subtract(totalTeacherShare);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("totalRevenue", totalRevenue);
        result.put("totalPaidOrders", paidOrders.size());
        result.put("totalRefunded", totalRefunded);
        result.put("totalTeacherShare", totalTeacherShare);
        result.put("totalSystemNet", totalSystemNet);
        result.put("paidOrders", paidOrders.stream().map(o -> Api_Mapper.order(o, orderItemRepo.findByOrderId(o.getId()))).toList());
        result.put("earnings", earnings.stream().map(Api_Mapper::earning).toList());
        result.put("payments", payments.stream().map(Api_Mapper::payment).toList());
        result.put("refundRequests", refundRequests.stream().map(r -> Api_Mapper.refund(r, orderItemRepo.findByOrderId(r.getOrder().getId()))).toList());
        return ResponseEntity.ok(result);
    }

    // ==================== STUDENT INFO / CONSULTATION API ====================

    @GetMapping("/student-info")
    public ResponseEntity<?> studentInfoList() {
        List<StudentInfo> list = studentInfoRepo.findAll(
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "thoiGianDangKy"
                )
        );
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", list.stream().map(Api_Mapper::studentInfo).toList()
        ));
    }

    @DeleteMapping("/student-info/{id}")
    public ResponseEntity<?> deleteStudentInfo(@PathVariable("id") Long id) {
        studentInfoRepo.deleteById(id);
        return ResponseEntity.ok(Api_Mapper.ok("Đã xóa thông tin tư vấn."));
    }

    private Course mapCourse(Map<String, Object> body, Course course) {
        course.setTenKhoaHoc(getString(body, "tenKhoaHoc"));
        course.setMoTa(getString(body, "moTa"));
        course.setGia(getBigDecimal(body, "gia"));
        course.setCapDo(getString(body, "capDo"));
        course.setHinhAnh(getString(body, "hinhAnh"));
        course.setTrangThai(getString(body, "trangThai") != null ? getString(body, "trangThai") : "active");
        course.setCommissionRate(getInteger(body, "commissionRate") != null ? getInteger(body, "commissionRate") : 70);

        Long categoryId = getLong(body, "categoryId");
        if (categoryId != null) {
            course.setCategory(categoryService.Get_ById(categoryId));
        }
        Long teacherId = getLong(body, "teacherId");
        if (teacherId != null) {
            course.setTeacher(userService.Get_ById(teacherId));
        }
        return course;
    }

    private Lesson mapLesson(Map<String, Object> body, Lesson lesson) {
        lesson.setTieuDe(getString(body, "tieuDe"));
        lesson.setVideoUrl(getString(body, "videoUrl"));
        Integer thuTu = getInteger(body, "thuTu");
        lesson.setThuTu(thuTu != null ? thuTu : 1);
        lesson.setThoiLuongPhut(getInteger(body, "thoiLuongPhut"));
        return lesson;
    }

    private String getLoggedInUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return null;
    }

    private String getString(Map<String, Object> body, String key) {
        Object value = body.get(key);
        return value != null ? value.toString() : null;
    }

    private Long getLong(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null || value.toString().isBlank()) return null;
        return Long.valueOf(value.toString());
    }

    private Integer getInteger(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null || value.toString().isBlank()) return null;
        return Integer.valueOf(value.toString());
    }

    private BigDecimal getBigDecimal(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null || value.toString().isBlank()) return BigDecimal.ZERO;
        return new BigDecimal(value.toString());
    }
}
