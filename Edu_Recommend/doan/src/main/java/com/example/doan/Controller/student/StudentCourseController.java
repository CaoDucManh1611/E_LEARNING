package com.example.doan.Controller.student;

import com.example.doan.Model.course.Category;
import com.example.doan.Model.course.Course;
import com.example.doan.Model.enrollment.Enrollment;
import com.example.doan.Model.course.Lesson;
import com.example.doan.Model.enrollment.LessonProgress;
import com.example.doan.Model.review.Review;
import com.example.doan.Model.notification.Notification;
import com.example.doan.Model.user.User;
import com.example.doan.Repository.enrollment.EnrollmentRepository;
import com.example.doan.Service.course.Category_Service;
import com.example.doan.Service.course.Course_Service;
import com.example.doan.Service.course.Lesson_Service;
import com.example.doan.Service.user.User_Service;
import com.example.doan.Service.common.FlaskApiService;
import com.example.doan.Service.enrollment.Progress_Service;
import com.example.doan.Service.review.Review_Service;
import com.example.doan.Service.notification.Notification_Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;


@Controller
@RequestMapping("/student")
public class StudentCourseController {

    private final Course_Service courseService;
    private final Category_Service categoryService;
    private final Lesson_Service lessonService;
    private final User_Service userService;
    private final EnrollmentRepository enrollmentRepo;
    private final FlaskApiService flaskApiService;
    private final Progress_Service progressService;
    private final Review_Service reviewService;
    private final Notification_Service notificationService;

    public StudentCourseController(Course_Service courseService,
                                   Category_Service categoryService,
                                   Lesson_Service lessonService,
                                   User_Service userService,
                                   EnrollmentRepository enrollmentRepo,
                                   FlaskApiService flaskApiService,
                                   Progress_Service progressService,
                                   Review_Service reviewService,
                                   Notification_Service notificationService) {
        this.courseService = courseService;
        this.categoryService = categoryService;
        this.lessonService = lessonService;
        this.userService = userService;
        this.enrollmentRepo = enrollmentRepo;
        this.flaskApiService = flaskApiService;
        this.progressService = progressService;
        this.reviewService = reviewService;
        this.notificationService = notificationService;
    }

    // 1. Xem danh sách khóa học công khai (Lọc theo danh mục, cấp độ, tìm kiếm và sắp xếp)
    @GetMapping("/courses")
    public String list(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "capDo", required = false) String capDo,
            @RequestParam(value = "sort", required = false) String sort,
            Model model) {

        List<Course> courses = courseService.Get_All_Courses();
        Stream<Course> courseStream = courses.stream()
                .filter(c -> "active".equals(c.getTrangThai()));

        // Lọc theo tìm kiếm
        if (search != null && !search.trim().isEmpty()) {
            String query = search.trim().toLowerCase();
            courseStream = courseStream.filter(c -> c.getTenKhoaHoc().toLowerCase().contains(query)
                    || (c.getMoTa() != null && c.getMoTa().toLowerCase().contains(query)));
        }

        // Lọc theo danh mục
        if (categoryId != null) {
            courseStream = courseStream.filter(c -> c.getCategory() != null && c.getCategory().getId().equals(categoryId));
        }

        // Lọc theo cấp độ
        if (capDo != null && !capDo.trim().isEmpty() && !"all".equalsIgnoreCase(capDo)) {
            courseStream = courseStream.filter(c -> capDo.equalsIgnoreCase(c.getCapDo()));
        }

        // Sắp xếp
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

        List<Course> filteredCourses = courseStream.toList();
        for (Course c : filteredCourses) {
            c.setAverageStars(reviewService.Get_Average_Stars(c.getId()));
            c.setReviewCount(reviewService.Get_Review_Count(c.getId()));
        }
        
        List<Category> categories = categoryService.Get_All_Categories();

        List<Long> enrolledCourseIds = new java.util.ArrayList<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            User currentUser = userService.FindUserByEmail(auth.getName());
            if (currentUser != null) {
                enrolledCourseIds = enrollmentRepo.findByUserId(currentUser.getId()).stream()
                        .map(e -> e.getCourse().getId())
                        .toList();
            }
        }

        model.addAttribute("courses", filteredCourses);
        model.addAttribute("categories", categories);
        model.addAttribute("search", search);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedCapDo", capDo);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("enrolledCourseIds", enrolledCourseIds);

        return "student/courses/list";
    }

    // 2. Xem chi tiết khóa học và danh sách bài học
    @GetMapping("/courses/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Course course = courseService.Get_ById(id);
        if (course == null || !"active".equals(course.getTrangThai())) {
            return "redirect:/student/courses";
        }

        course.setAverageStars(reviewService.Get_Average_Stars(id));
        course.setReviewCount(reviewService.Get_Review_Count(id));

        List<Lesson> lessons = lessonService.Get_Lessons_By_Course(id);
        List<Review> reviews = reviewService.Get_Visible_Reviews_By_Course(id);

        model.addAttribute("course", course);
        model.addAttribute("lessons", lessons);
        model.addAttribute("reviews", reviews);

        boolean isEnrolled = false;
        boolean hasReviewed = false;
        boolean canReview = false;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            User currentUser = userService.FindUserByEmail(auth.getName());
            if (currentUser != null) {
                boolean hasBought = enrollmentRepo.existsByUserIdAndCourseId(currentUser.getId(), id);
                boolean isMyCourse = "teacher".equals(currentUser.getRole()) && course.getTeacher() != null && course.getTeacher().getId().equals(currentUser.getId());
                isEnrolled = hasBought || "admin".equals(currentUser.getRole()) || isMyCourse;
                hasReviewed = reviewService.Has_Reviewed(currentUser.getId(), id);
                canReview = hasBought && !hasReviewed && !"admin".equals(currentUser.getRole()) && !"teacher".equals(currentUser.getRole());
            }
        }
        model.addAttribute("isEnrolled", isEnrolled);
        model.addAttribute("hasReviewed", hasReviewed);
        model.addAttribute("canReview", canReview);

        return "student/courses/detail";
    }

    // 3. Khóa học của tôi (Khóa học đã sở hữu)
    @GetMapping("/my-courses")
    public String myCourses(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        User currentUser = userService.FindUserByEmail(auth.getName());
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Enrollment> enrollments = enrollmentRepo.findByUserId(currentUser.getId());
        List<Course> myCourses = enrollments.stream().map(Enrollment::getCourse).toList();

        // Lấy thông báo của người nhận
        List<Notification> notifications = notificationService.Get_Notifications_By_User(currentUser.getId());
        long unreadCount = notificationService.Get_Unread_Count(currentUser.getId());

        model.addAttribute("myCourses", myCourses);
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        return "student/my-courses";
    }

    // Click vào thông báo (đánh dấu đã đọc và chuyển hướng)
    @GetMapping("/notifications/{id}/click")
    public String clickNotification(@PathVariable("id") Long id) {
        Notification n = notificationService.Get_ById(id);
        if (n != null) {
            notificationService.Mark_As_Read(id);
            return "redirect:" + n.getUrl();
        }
        return "redirect:/student/my-courses";
    }

    // 4. Lớp học trực tuyến (Classroom)
    @GetMapping("/courses/{id}/learn")
    public String learn(@PathVariable("id") Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        User currentUser = userService.FindUserByEmail(auth.getName());
        if (currentUser == null) {
            return "redirect:/login";
        }

        Enrollment enrollment = enrollmentRepo.findByUserId(currentUser.getId()).stream()
                .filter(e -> e.getCourse().getId().equals(id))
                .findFirst()
                .orElse(null);

        if (enrollment == null) {
            Course courseCheck = courseService.Get_ById(id);
            boolean isTeacherOfCourse = "teacher".equals(currentUser.getRole()) && courseCheck != null 
                && courseCheck.getTeacher() != null 
                && courseCheck.getTeacher().getId().equals(currentUser.getId());
                
            if ("admin".equals(currentUser.getRole()) || isTeacherOfCourse) {
                // Tự động tham gia khóa học cho Admin hoặc Giảng viên của khóa học
                enrollment = new Enrollment();
                enrollment.setUser(currentUser);
                enrollment.setCourse(courseCheck);
                enrollment.setTienDoPercent(0);
                enrollment.setTrangThai("in_progress");
                enrollment = enrollmentRepo.save(enrollment);
            } else {
                return "redirect:/student/courses/" + id;
            }
        }

        Course course = courseService.Get_ById(id);
        List<Lesson> lessons = lessonService.Get_Lessons_By_Course(id);
        
        // Lấy hoặc tự động khởi tạo danh sách tiến trình các bài học
        List<LessonProgress> progressList = progressService.Get_Or_Create_Progress(enrollment);

        model.addAttribute("course", course);
        model.addAttribute("lessons", lessons);
        model.addAttribute("enrollment", enrollment);
        model.addAttribute("progressList", progressList);
        return "student/courses/learn";
    }

    // 5. Giao diện khảo sát gợi ý lộ trình học tập AI
    @GetMapping("/recommend")
    public String recommendPage(Model model) {
        boolean flaskOk = flaskApiService.checkHealth();
        model.addAttribute("flaskStatus", flaskOk ? "online" : "offline");
        return "student/recommend";
    }

    // 6. Xóa thông báo
    @ PostMapping("/notifications/{id}/delete")
    @ResponseBody
    public String deleteNotification(@PathVariable("id") Long id) {
        notificationService.Delete(id);
        return "success";
    }
}
