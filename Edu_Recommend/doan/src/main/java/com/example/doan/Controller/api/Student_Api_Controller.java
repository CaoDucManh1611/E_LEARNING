package com.example.doan.Controller.api;

import com.example.doan.Model.course.Course;
import com.example.doan.Model.enrollment.Certificate;
import com.example.doan.Model.enrollment.Enrollment;
import com.example.doan.Model.enrollment.LessonProgress;
import com.example.doan.Model.notification.Notification;
import com.example.doan.Model.order.Invoice;
import com.example.doan.Model.order.Order;
import com.example.doan.Model.order.OrderItem;
import com.example.doan.Model.refund.RefundRequest;
import com.example.doan.Model.review.Review;
import com.example.doan.Model.user.User;
import com.example.doan.Repository.enrollment.EnrollmentRepository;
import com.example.doan.Repository.order.OrderItemRepository;
import com.example.doan.Repository.order.OrderRepository;
import com.example.doan.Repository.refund.RefundRequestRepository;
import com.example.doan.Service.course.Course_Service;
import com.example.doan.Service.course.Lesson_Service;
import com.example.doan.Service.enrollment.Progress_Service;
import com.example.doan.Service.notification.Notification_Service;
import com.example.doan.Service.order.Invoice_Service;
import com.example.doan.Service.refund.Refund_Service;
import com.example.doan.Service.review.Review_Service;
import com.example.doan.Service.user.User_Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1/student")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class Student_Api_Controller {

    private final User_Service userService;
    private final Course_Service courseService;
    private final Lesson_Service lessonService;
    private final Progress_Service progressService;
    private final Review_Service reviewService;
    private final Refund_Service refundService;
    private final Invoice_Service invoiceService;
    private final Notification_Service notificationService;
    private final EnrollmentRepository enrollmentRepo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final RefundRequestRepository refundRequestRepo;

    public Student_Api_Controller(User_Service userService,
                                  Course_Service courseService,
                                  Lesson_Service lessonService,
                                  Progress_Service progressService,
                                  Review_Service reviewService,
                                  Refund_Service refundService,
                                  Invoice_Service invoiceService,
                                  Notification_Service notificationService,
                                  EnrollmentRepository enrollmentRepo,
                                  OrderRepository orderRepo,
                                  OrderItemRepository orderItemRepo,
                                  RefundRequestRepository refundRequestRepo) {
        this.userService = userService;
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.progressService = progressService;
        this.reviewService = reviewService;
        this.refundService = refundService;
        this.invoiceService = invoiceService;
        this.notificationService = notificationService;
        this.enrollmentRepo = enrollmentRepo;
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.refundRequestRepo = refundRequestRepo;
    }

    // 1. Khóa học đã sở hữu
    @GetMapping("/my-courses")
    public ResponseEntity<?> myCourses() {
        User currentUser = getCurrentUser();
        if (currentUser == null) return unauthorized();

        List<Enrollment> enrollments = enrollmentRepo.findByUserId(currentUser.getId());
        List<Notification> notifications = notificationService.Get_Notifications_By_User(currentUser.getId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("myCourses", enrollments.stream().map(Enrollment::getCourse).map(Api_Mapper::course).toList());
        result.put("enrollments", enrollments.stream().map(Api_Mapper::enrollment).toList());
        result.put("notifications", notifications.stream().map(Api_Mapper::notification).toList());
        result.put("unreadCount", notificationService.Get_Unread_Count(currentUser.getId()));
        return ResponseEntity.ok(result);
    }

    // 2. Dữ liệu lớp học trực tuyến
    @GetMapping("/courses/{id}/learn")
    public ResponseEntity<?> learn(@PathVariable("id") Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return unauthorized();

        Enrollment enrollment = enrollmentRepo.findByUserId(currentUser.getId()).stream()
                .filter(e -> e.getCourse().getId().equals(id))
                .findFirst()
                .orElse(null);

        Course courseCheck = courseService.Get_ById(id);
        if (courseCheck == null) {
            return ResponseEntity.status(404).body(Api_Mapper.error("Không tìm thấy khóa học"));
        }

        if (enrollment == null) {
            boolean isTeacherOfCourse = "teacher".equals(currentUser.getRole()) && courseCheck != null
                    && courseCheck.getTeacher() != null
                    && courseCheck.getTeacher().getId().equals(currentUser.getId());

            if ("admin".equals(currentUser.getRole()) || isTeacherOfCourse) {
                enrollment = new Enrollment();
                enrollment.setUser(currentUser);
                enrollment.setCourse(courseCheck);
                enrollment.setTienDoPercent(0);
                enrollment.setTrangThai("in_progress");
                enrollment = enrollmentRepo.save(enrollment);
            } else {
                return ResponseEntity.status(403).body(Api_Mapper.error("Bạn chưa sở hữu khóa học này"));
            }
        }

        List<LessonProgress> progressList = progressService.Get_Or_Create_Progress(enrollment);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("course", Api_Mapper.course(courseCheck));
        result.put("lessons", lessonService.Get_Lessons_By_Course(id).stream().map(Api_Mapper::lesson).toList());
        result.put("enrollment", Api_Mapper.enrollment(enrollment));
        result.put("progressList", progressList.stream().map(Api_Mapper::progress).toList());
        return ResponseEntity.ok(result);
    }

    // 3. Cập nhật tiến độ bài học
    @PostMapping("/progress/toggle")
    public ResponseEntity<?> toggleProgress(@RequestBody Map<String, Object> body) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return unauthorized();

        Long enrollmentId = getLong(body, "enrollmentId");
        Long lessonId = getLong(body, "lessonId");
        boolean hoanThanh = Boolean.parseBoolean(String.valueOf(body.get("hoanThanh")));

        Enrollment enrollment = enrollmentRepo.findById(enrollmentId).orElse(null);
        if (enrollment == null || enrollment.getUser() == null || !enrollment.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(Api_Mapper.error("Bạn không có quyền cập nhật tiến độ này"));
        }

        int newPercent = progressService.Toggle_Lesson_Progress(enrollmentId, lessonId, hoanThanh);
        return ResponseEntity.ok(Map.of("success", true, "percent", newPercent));
    }

    // 4. Gửi đánh giá khóa học
    @PostMapping("/courses/{courseId}/reviews")
    public ResponseEntity<?> submitReview(@PathVariable("courseId") Long courseId,
                                          @RequestBody Map<String, Object> body) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return unauthorized();

        Course course = courseService.Get_ById(courseId);
        if (course == null) return ResponseEntity.status(404).body(Api_Mapper.error("Không tìm thấy khóa học"));

        boolean isEnrolled = enrollmentRepo.existsByUserIdAndCourseId(currentUser.getId(), courseId);
        if (!isEnrolled) {
            return ResponseEntity.status(403).body(Api_Mapper.error("Bạn cần sở hữu khóa học này để có thể đánh giá!"));
        }
        if ("admin".equals(currentUser.getRole()) || "teacher".equals(currentUser.getRole())) {
            return ResponseEntity.badRequest().body(Api_Mapper.error("Admin/Giáo viên không được đánh giá khóa học."));
        }
        if (reviewService.Has_Reviewed(currentUser.getId(), courseId)) {
            return ResponseEntity.badRequest().body(Api_Mapper.error("Bạn đã đánh giá khóa học này rồi!"));
        }

        Review review = new Review();
        review.setUser(currentUser);
        review.setCourse(course);
        review.setSoSao(getInteger(body, "soSao") != null ? getInteger(body, "soSao") : 5);
        review.setNoiDung(getString(body, "noiDung") != null ? getString(body, "noiDung").trim() : "");
        Review saved = reviewService.Save_Review(review);

        notificationService.Create_Review_Notification(currentUser, course, saved.getSoSao(), saved.getNoiDung());
        return ResponseEntity.ok(Map.of("success", true, "message", "Đăng tải đánh giá khóa học thành công!", "data", Api_Mapper.review(saved)));
    }

    // 5. Đơn hàng đã mua, trạng thái hoàn tiền và item từng đơn
    @GetMapping("/orders")
    public ResponseEntity<?> orders() {
        User currentUser = getCurrentUser();
        if (currentUser == null) return unauthorized();

        List<Order> orders = orderRepo.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        List<Map<String, Object>> data = orders.stream().map(order -> {
            List<OrderItem> items = orderItemRepo.findByOrderId(order.getId());
            Optional<RefundRequest> refund = refundRequestRepo.findByOrderId(order.getId());

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("order", Api_Mapper.order(order, items));
            map.put("canRefund", refundService.Can_Request_Refund(order));
            map.put("refundRequest", refund.map(r -> Api_Mapper.refund(r, items)).orElse(null));
            return map;
        }).toList();

        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    // 6. Gửi yêu cầu hoàn tiền
    @PostMapping("/orders/{orderId}/refund")
    public ResponseEntity<?> requestRefund(@PathVariable("orderId") Long orderId,
                                           @RequestBody Map<String, Object> body) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return unauthorized();

        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null || !order.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(Api_Mapper.error("Bạn không có quyền thao tác đơn hàng này"));
        }

        try {
            refundService.Create_Refund_Request(orderId, getString(body, "lyDo"));
            return ResponseEntity.ok(Api_Mapper.ok("Gửi yêu cầu hoàn tiền thành công! Vui lòng chờ Admin phê duyệt."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Api_Mapper.error("Gửi yêu cầu thất bại: " + e.getMessage()));
        }
    }

    // 7. Xem hóa đơn
    @GetMapping("/orders/{orderId}/invoice")
    public ResponseEntity<?> invoice(@PathVariable("orderId") Long orderId) {
        User currentUser = getCurrentUser();
        Order order = orderRepo.findById(orderId).orElse(null);

        if (currentUser == null) return unauthorized();
        if (order == null || !order.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(Api_Mapper.error("Bạn không có quyền xem hóa đơn này"));
        }

        Invoice invoice = invoiceService.Get_Invoice_By_Order(orderId)
                .orElseGet(() -> invoiceService.Generate_Invoice_For_Order(order));
        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("order", Api_Mapper.order(order, items));
        result.put("invoice", Api_Mapper.invoice(invoice));
        result.put("items", items.stream().map(Api_Mapper::orderItem).toList());
        return ResponseEntity.ok(result);
    }

    // 8. Thông báo cá nhân
    @GetMapping("/notifications")
    public ResponseEntity<?> notifications() {
        User currentUser = getCurrentUser();
        if (currentUser == null) return unauthorized();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "unreadCount", notificationService.Get_Unread_Count(currentUser.getId()),
                "data", notificationService.Get_Notifications_By_User(currentUser.getId()).stream().map(Api_Mapper::notification).toList()
        ));
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<?> readNotification(@PathVariable("id") Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return unauthorized();

        Notification n = notificationService.Get_ById(id);
        if (n == null || !n.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(Api_Mapper.error("Không có quyền thao tác thông báo này"));
        }
        notificationService.Mark_As_Read(id);
        return ResponseEntity.ok(Map.of("success", true, "url", n.getUrl()));
    }

    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable("id") Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return unauthorized();

        Notification n = notificationService.Get_ById(id);
        if (n == null || !n.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(Api_Mapper.error("Không có quyền xóa thông báo này"));
        }
        notificationService.Delete(id);
        return ResponseEntity.ok(Api_Mapper.ok("Đã xóa thông báo."));
    }

    // 9. Cấp/xem chứng chỉ khi học viên đạt 100%
    @GetMapping("/certificate/{enrollmentId}")
    public ResponseEntity<?> certificate(@PathVariable("enrollmentId") Long enrollmentId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return unauthorized();

        Enrollment enrollment = enrollmentRepo.findById(enrollmentId).orElse(null);
        if (enrollment == null || enrollment.getUser() == null || !enrollment.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(Api_Mapper.error("Bạn không có quyền xem chứng chỉ này"));
        }

        Certificate certificate = progressService.Issue_Certificate(enrollment);
        if (certificate == null) {
            return ResponseEntity.badRequest().body(Api_Mapper.error("Bạn cần hoàn thành 100% khóa học để nhận chứng chỉ"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "certificate", Api_Mapper.certificate(certificate),
                "enrollment", Api_Mapper.enrollment(enrollment)
        ));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return userService.FindUserByEmail(auth.getName());
        }
        return null;
    }

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(401).body(Api_Mapper.error("Chưa đăng nhập"));
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
}
