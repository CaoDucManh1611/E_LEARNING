package com.example.doan.Controller.api;

import com.example.doan.Model.course.Category;
import com.example.doan.Model.course.Course;
import com.example.doan.Model.course.Lesson;
import com.example.doan.Model.review.Review;
import com.example.doan.Model.user.User;
import com.example.doan.Repository.enrollment.EnrollmentRepository;
import com.example.doan.Service.course.Category_Service;
import com.example.doan.Service.course.Course_Service;
import com.example.doan.Service.course.Lesson_Service;
import com.example.doan.Service.review.Review_Service;
import com.example.doan.Service.user.User_Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class Catalog_Api_Controller {

    private final Course_Service courseService;
    private final Category_Service categoryService;
    private final Lesson_Service lessonService;
    private final Review_Service reviewService;
    private final User_Service userService;
    private final EnrollmentRepository enrollmentRepo;

    public Catalog_Api_Controller(Course_Service courseService,
                                  Category_Service categoryService,
                                  Lesson_Service lessonService,
                                  Review_Service reviewService,
                                  User_Service userService,
                                  EnrollmentRepository enrollmentRepo) {
        this.courseService = courseService;
        this.categoryService = categoryService;
        this.lessonService = lessonService;
        this.reviewService = reviewService;
        this.userService = userService;
        this.enrollmentRepo = enrollmentRepo;
    }

    // 1. Danh sách danh mục công khai
    @GetMapping("/categories")
    public ResponseEntity<?> categories() {
        List<Map<String, Object>> data = categoryService.Get_All_Categories().stream()
                .map(Api_Mapper::category)
                .toList();
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    // 2. Danh sách khóa học công khai, có lọc/tìm kiếm/sắp xếp như controller Thymeleaf cũ
    @GetMapping("/courses")
    public ResponseEntity<?> courses(@RequestParam(value = "search", required = false) String search,
                                     @RequestParam(value = "categoryId", required = false) Long categoryId,
                                     @RequestParam(value = "capDo", required = false) String capDo,
                                     @RequestParam(value = "sort", required = false) String sort) {
        Stream<Course> courseStream = courseService.Get_All_Courses().stream()
                .filter(c -> "active".equals(c.getTrangThai()));

        if (search != null && !search.trim().isEmpty()) {
            String query = search.trim().toLowerCase();
            courseStream = courseStream.filter(c -> c.getTenKhoaHoc().toLowerCase().contains(query)
                    || (c.getMoTa() != null && c.getMoTa().toLowerCase().contains(query)));
        }

        if (categoryId != null) {
            courseStream = courseStream.filter(c -> c.getCategory() != null && c.getCategory().getId().equals(categoryId));
        }

        if (capDo != null && !capDo.trim().isEmpty() && !"all".equalsIgnoreCase(capDo)) {
            courseStream = courseStream.filter(c -> capDo.equalsIgnoreCase(c.getCapDo()));
        }

        if (sort != null && !sort.trim().isEmpty()) {
            switch (sort) {
                case "price_asc":
                    courseStream = courseStream.sorted(Comparator.comparing(Course::getGia));
                    break;
                case "price_desc":
                    courseStream = courseStream.sorted(Comparator.comparing(Course::getGia).reversed());
                    break;
                case "latest":
                default:
                    courseStream = courseStream.sorted(Comparator.comparing(Course::getId).reversed());
                    break;
            }
        }

        List<Course> courses = courseStream.toList();
        for (Course c : courses) {
            c.setAverageStars(reviewService.Get_Average_Stars(c.getId()));
            c.setReviewCount(reviewService.Get_Review_Count(c.getId()));
        }

        List<Long> enrolledCourseIds = getCurrentUser() != null
                ? enrollmentRepo.findByUserId(getCurrentUser().getId()).stream().map(e -> e.getCourse().getId()).toList()
                : List.of();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("data", courses.stream().map(Api_Mapper::course).toList());
        result.put("categories", categoryService.Get_All_Categories().stream().map(Api_Mapper::category).toList());
        result.put("enrolledCourseIds", enrolledCourseIds);
        return ResponseEntity.ok(result);
    }

    // 3. Chi tiết khóa học công khai
    @GetMapping("/courses/{id}")
    public ResponseEntity<?> courseDetail(@PathVariable("id") Long id) {
        Course course = courseService.Get_ById(id);
        if (course == null || !"active".equals(course.getTrangThai())) {
            return ResponseEntity.status(404).body(Api_Mapper.error("Không tìm thấy khóa học"));
        }

        course.setAverageStars(reviewService.Get_Average_Stars(id));
        course.setReviewCount(reviewService.Get_Review_Count(id));

        User currentUser = getCurrentUser();
        boolean isEnrolled = false;
        boolean hasReviewed = false;
        boolean canReview = false;
        if (currentUser != null) {
            boolean hasBought = enrollmentRepo.existsByUserIdAndCourseId(currentUser.getId(), id);
            boolean isMyCourse = "teacher".equals(currentUser.getRole())
                    && course.getTeacher() != null
                    && course.getTeacher().getId().equals(currentUser.getId());
            isEnrolled = hasBought || "admin".equals(currentUser.getRole()) || isMyCourse;
            hasReviewed = reviewService.Has_Reviewed(currentUser.getId(), id);
            canReview = hasBought && !hasReviewed && !"admin".equals(currentUser.getRole()) && !"teacher".equals(currentUser.getRole());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("course", Api_Mapper.course(course));
        result.put("lessons", lessonService.Get_Lessons_By_Course(id).stream().map(Api_Mapper::lesson).toList());
        result.put("reviews", reviewService.Get_Visible_Reviews_By_Course(id).stream().map(Api_Mapper::review).toList());
        result.put("isEnrolled", isEnrolled);
        result.put("hasReviewed", hasReviewed);
        result.put("canReview", canReview);
        return ResponseEntity.ok(result);
    }

    // 4. Danh sách bài học và đánh giá tách riêng cho frontend cần gọi nhỏ
    @GetMapping("/courses/{courseId}/lessons")
    public ResponseEntity<?> courseLessons(@PathVariable("courseId") Long courseId) {
        List<Lesson> lessons = lessonService.Get_Lessons_By_Course(courseId);
        return ResponseEntity.ok(Map.of("success", true, "data", lessons.stream().map(Api_Mapper::lesson).toList()));
    }

    @GetMapping("/courses/{courseId}/reviews")
    public ResponseEntity<?> courseReviews(@PathVariable("courseId") Long courseId) {
        List<Review> reviews = reviewService.Get_Visible_Reviews_By_Course(courseId);
        return ResponseEntity.ok(Map.of("success", true, "data", reviews.stream().map(Api_Mapper::review).toList()));
    }

    // 5. Số học viên đã đăng ký khóa học
    @GetMapping("/courses/{id}/enrollment-count")
    public ResponseEntity<?> enrollmentCount(@PathVariable("id") Long id) {
        long count = enrollmentRepo.findAll().stream()
                .filter(e -> e.getCourse() != null && e.getCourse().getId().equals(id))
                .count();
        return ResponseEntity.ok(Map.of("success", true, "count", count));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return userService.FindUserByEmail(auth.getName());
        }
        return null;
    }
}
