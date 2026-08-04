package com.example.doan.Controller.api;

import com.example.doan.Model.course.Course;
import com.example.doan.Model.course.Lesson;
import com.example.doan.Model.enrollment.Enrollment;
import com.example.doan.Model.notification.Notification;
import com.example.doan.Model.order.InstructorEarning;
import com.example.doan.Model.review.Review;
import com.example.doan.Model.user.User;
import com.example.doan.Repository.enrollment.EnrollmentRepository;
import com.example.doan.Repository.order.InstructorEarningRepository;
import com.example.doan.Repository.review.ReviewRepository;
import com.example.doan.Service.course.Category_Service;
import com.example.doan.Service.course.Course_Service;
import com.example.doan.Service.course.Lesson_Service;
import com.example.doan.Service.notification.Notification_Service;
import com.example.doan.Service.user.User_Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/teacher")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class Teacher_Api_Controller {

    private final Course_Service courseService;
    private final Category_Service categoryService;
    private final Lesson_Service lessonService;
    private final User_Service userService;
    private final Notification_Service notificationService;
    private final InstructorEarningRepository earningRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final ReviewRepository reviewRepo;

    public Teacher_Api_Controller(Course_Service courseService,
                                  Category_Service categoryService,
                                  Lesson_Service lessonService,
                                  User_Service userService,
                                  Notification_Service notificationService,
                                  InstructorEarningRepository earningRepo,
                                  EnrollmentRepository enrollmentRepo,
                                  ReviewRepository reviewRepo) {
        this.courseService = courseService;
        this.categoryService = categoryService;
        this.lessonService = lessonService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.earningRepo = earningRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.reviewRepo = reviewRepo;
    }

    // 1. Dashboard giảng viên
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return unauthorized();

        List<InstructorEarning> earnings = earningRepo.findByTeacherId(teacher.getId());
        BigDecimal totalEarning = earnings.stream()
                .map(InstructorEarning::getTienNhan)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Enrollment> teacherEnrollments = getTeacherEnrollments(teacher);
        List<Review> teacherReviews = getTeacherReviews(teacher);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("totalEarning", totalEarning);
        result.put("earningCount", earnings.size());
        result.put("studentCount", teacherEnrollments.size());
        result.put("reviewCount", teacherReviews.size());
        result.put("earnings", earnings.stream().map(Api_Mapper::earning).toList());
        return ResponseEntity.ok(result);
    }

    // 2. Khóa học do giảng viên tạo
    @GetMapping("/courses")
    public ResponseEntity<?> myCourses() {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return unauthorized();

        List<Course> myCourses = courseService.Get_All_Courses().stream()
                .filter(c -> c.getTeacher() != null && c.getTeacher().getId().equals(teacher.getId()))
                .toList();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", myCourses.stream().map(Api_Mapper::course).toList(),
                "categories", categoryService.Get_All_Categories().stream().map(Api_Mapper::category).toList()
        ));
    }

    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(@RequestBody Map<String, Object> body) {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return unauthorized();

        Course course = mapCourse(body, new Course());
        course.setTeacher(teacher);
        course.setTrangThai("pending_review");
        course.setCommissionRate(70);

        Course saved = courseService.Create(course);
        notificationService.Create_Course_Pending_Notification(teacher, saved);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã gửi yêu cầu tạo khóa học! Vui lòng chờ Admin duyệt.", "data", Api_Mapper.course(saved)));
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable("id") Long id,
                                          @RequestBody Map<String, Object> body) {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return unauthorized();

        Course existingCourse = courseService.Get_ById(id);
        if (!checkOwnership(existingCourse, teacher)) {
            return ResponseEntity.status(403).body(Api_Mapper.error("Bạn không có quyền sửa khóa học này."));
        }

        mapCourse(body, existingCourse);
        Course updated = courseService.Update(existingCourse);
        return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật khóa học thành công!", "data", Api_Mapper.course(updated)));
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable("id") Long id,
                                          @RequestParam(value = "reason", required = false) String reason) {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return unauthorized();

        Course course = courseService.Get_ById(id);
        if (!checkOwnership(course, teacher)) {
            return ResponseEntity.status(403).body(Api_Mapper.error("Bạn không có quyền xóa khóa học này."));
        }

        if (reason != null && !reason.trim().isEmpty()) {
            courseService.Delete_With_Refund(id, reason);
        } else {
            courseService.Delete(id);
        }
        return ResponseEntity.ok(Api_Mapper.ok("Đã xóa khóa học."));
    }

    // 3. Bài học thuộc khóa học của giảng viên
    @GetMapping("/courses/{courseId}/lessons")
    public ResponseEntity<?> lessons(@PathVariable("courseId") Long courseId) {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return unauthorized();

        Course course = courseService.Get_ById(courseId);
        if (!checkOwnership(course, teacher)) {
            return ResponseEntity.status(403).body(Api_Mapper.error("Không tìm thấy hoặc bạn không có quyền truy cập khóa học này."));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "course", Api_Mapper.course(course),
                "lessons", lessonService.Get_Lessons_By_Course(courseId).stream().map(Api_Mapper::lesson).toList()
        ));
    }

    @PostMapping("/courses/{courseId}/lessons")
    public ResponseEntity<?> createLesson(@PathVariable("courseId") Long courseId,
                                          @RequestBody Map<String, Object> body) {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return unauthorized();

        Course course = courseService.Get_ById(courseId);
        if (!checkOwnership(course, teacher)) {
            return ResponseEntity.status(403).body(Api_Mapper.error("Không có quyền thực hiện."));
        }

        Lesson lesson = mapLesson(body, new Lesson());
        lesson.setCourse(course);
        Lesson saved = lessonService.Create(lesson);
        return ResponseEntity.ok(Map.of("success", true, "message", "Thêm bài học thành công!", "data", Api_Mapper.lesson(saved)));
    }

    @PutMapping("/courses/{courseId}/lessons/{id}")
    public ResponseEntity<?> updateLesson(@PathVariable("courseId") Long courseId,
                                          @PathVariable("id") Long id,
                                          @RequestBody Map<String, Object> body) {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return unauthorized();

        Course course = courseService.Get_ById(courseId);
        Lesson existing = lessonService.Get_ById(id);
        if (!checkOwnership(course, teacher)) {
            return ResponseEntity.status(403).body(Api_Mapper.error("Không có quyền thực hiện."));
        }
        if (existing == null || existing.getCourse() == null || !existing.getCourse().getId().equals(courseId)) {
            return ResponseEntity.status(404).body(Api_Mapper.error("Bài học không tồn tại."));
        }

        mapLesson(body, existing);
        Lesson updated = lessonService.Update(existing);
        return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật bài học thành công!", "data", Api_Mapper.lesson(updated)));
    }

    @DeleteMapping("/courses/{courseId}/lessons/{id}")
    public ResponseEntity<?> deleteLesson(@PathVariable("courseId") Long courseId,
                                          @PathVariable("id") Long id) {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return unauthorized();

        Course course = courseService.Get_ById(courseId);
        if (!checkOwnership(course, teacher)) {
            return ResponseEntity.status(403).body(Api_Mapper.error("Không có quyền thực hiện."));
        }

        lessonService.Delete(id);
        return ResponseEntity.ok(Api_Mapper.ok("Đã xóa bài học."));
    }

    // 4. Review, báo cáo, khóa học đang học
    @GetMapping("/reviews")
    public ResponseEntity<?> reviews() {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return unauthorized();
        return ResponseEntity.ok(Map.of("success", true, "data", getTeacherReviews(teacher).stream().map(Api_Mapper::review).toList()));
    }

    @GetMapping("/reports")
    public ResponseEntity<?> reports() {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return unauthorized();

        List<Enrollment> enrollments = getTeacherEnrollments(teacher);
        List<InstructorEarning> earnings = earningRepo.findByTeacherId(teacher.getId());
        BigDecimal totalEarning = earnings.stream()
                .map(InstructorEarning::getTienNhan)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long uniqueStudentsCount = enrollments.stream().map(Enrollment::getUser).distinct().count();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("enrollments", enrollments.stream().map(Api_Mapper::enrollment).toList());
        result.put("earnings", earnings.stream().map(Api_Mapper::earning).toList());
        result.put("totalEarning", totalEarning);
        result.put("uniqueStudentsCount", uniqueStudentsCount);
        result.put("registeredCount", enrollments.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my-courses")
    public ResponseEntity<?> ownedAndBoughtCourses() {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return unauthorized();

        List<Course> ownedCourses = courseService.Get_Active_Courses().stream()
                .filter(c -> c.getTeacher() != null && c.getTeacher().getId().equals(teacher.getId()))
                .toList();
        List<Course> boughtCourses = enrollmentRepo.findByUserId(teacher.getId()).stream()
                .map(Enrollment::getCourse)
                .filter(c -> c != null && (c.getTeacher() == null || !c.getTeacher().getId().equals(teacher.getId())))
                .toList();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "ownedCourses", ownedCourses.stream().map(Api_Mapper::course).toList(),
                "boughtCourses", boughtCourses.stream().map(Api_Mapper::course).toList()
        ));
    }

    // 5. Thông báo giảng viên
    @GetMapping("/notifications")
    public ResponseEntity<?> notifications() {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return unauthorized();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", notificationService.Get_Notifications_By_User(teacher.getId()).stream().map(Api_Mapper::notification).toList()
        ));
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<?> readNotification(@PathVariable("id") Long id) {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return unauthorized();

        Notification n = notificationService.Get_ById(id);
        if (n == null || !n.getUser().getId().equals(teacher.getId())) {
            return ResponseEntity.status(403).body(Api_Mapper.error("Forbidden"));
        }
        notificationService.Mark_As_Read(id);
        return ResponseEntity.ok(Map.of("success", true, "url", n.getUrl()));
    }

    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable("id") Long id) {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return unauthorized();

        Notification n = notificationService.Get_ById(id);
        if (n == null || !n.getUser().getId().equals(teacher.getId())) {
            return ResponseEntity.status(403).body(Api_Mapper.error("Forbidden"));
        }
        notificationService.Delete(id);
        return ResponseEntity.ok(Api_Mapper.ok("Deleted"));
    }

    private List<Enrollment> getTeacherEnrollments(User teacher) {
        return enrollmentRepo.findAll().stream()
                .filter(e -> e.getCourse() != null
                        && e.getCourse().getTeacher() != null
                        && e.getCourse().getTeacher().getId().equals(teacher.getId())
                        && e.getUser() != null
                        && "student".equals(e.getUser().getRole()))
                .toList();
    }

    private List<Review> getTeacherReviews(User teacher) {
        return reviewRepo.findAll().stream()
                .filter(r -> r.getCourse() != null
                        && r.getCourse().getTeacher() != null
                        && r.getCourse().getTeacher().getId().equals(teacher.getId()))
                .toList();
    }

    private boolean checkOwnership(Course course, User teacher) {
        return course != null && teacher != null && course.getTeacher() != null && course.getTeacher().getId().equals(teacher.getId());
    }

    private User getLoggedInTeacher() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return userService.FindUserByEmail(auth.getName());
        }
        return null;
    }

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(401).body(Api_Mapper.error("Chưa đăng nhập"));
    }

    private Course mapCourse(Map<String, Object> body, Course course) {
        if (body.containsKey("tenKhoaHoc")) course.setTenKhoaHoc(getString(body, "tenKhoaHoc"));
        if (body.containsKey("moTa")) course.setMoTa(getString(body, "moTa"));
        if (body.containsKey("gia")) course.setGia(new BigDecimal(getString(body, "gia")));
        if (body.containsKey("capDo")) course.setCapDo(getString(body, "capDo"));
        if (body.containsKey("hinhAnh")) course.setHinhAnh(getString(body, "hinhAnh"));
        if (body.containsKey("categoryId")) course.setCategory(categoryService.Get_ById(Long.valueOf(getString(body, "categoryId"))));
        return course;
    }

    private Lesson mapLesson(Map<String, Object> body, Lesson lesson) {
        if (body.containsKey("tieuDe")) lesson.setTieuDe(getString(body, "tieuDe"));
        if (body.containsKey("videoUrl")) lesson.setVideoUrl(getString(body, "videoUrl"));
        if (body.containsKey("thuTu")) lesson.setThuTu(Integer.parseInt(getString(body, "thuTu")));
        if (body.containsKey("thoiLuongPhut")) lesson.setThoiLuongPhut(Integer.parseInt(getString(body, "thoiLuongPhut")));
        return lesson;
    }

    private String getString(Map<String, Object> body, String key) {
        Object value = body.get(key);
        return value != null ? value.toString() : null;
    }
}
