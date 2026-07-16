package com.example.doan.Controller;

import com.example.doan.Model.Course;
import com.example.doan.Model.User;
import com.example.doan.Repository.EnrollmentRepository;
import com.example.doan.Repository.StudentInfoRepository;
import com.example.doan.Service.Category_Service;
import com.example.doan.Service.Course_Service;
import com.example.doan.Service.FlaskApiService;
import com.example.doan.Service.Lesson_Service;
import com.example.doan.Service.User_Service;
import com.example.doan.Service.Review_Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Controller phục vụ các trang chung và trang quản trị Admin.
 * File: Controller/PageController.java
 */
@Controller
public class PageController {

    private final FlaskApiService flaskApiService;
    private final Category_Service categoryService;
    private final Course_Service courseService;
    private final Lesson_Service lessonService;
    private final StudentInfoRepository studentInfoRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final User_Service userService;
    private final Review_Service reviewService;

    public PageController(FlaskApiService flaskApiService,
                          Category_Service categoryService,
                          Course_Service courseService,
                          Lesson_Service lessonService,
                          StudentInfoRepository studentInfoRepo,
                          EnrollmentRepository enrollmentRepo,
                          User_Service userService,
                          Review_Service reviewService) {
        this.flaskApiService = flaskApiService;
        this.categoryService = categoryService;
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.studentInfoRepo = studentInfoRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.userService = userService;
        this.reviewService = reviewService;
    }

    @GetMapping("/index")
    public String index() {
        return "redirect:/index.html";
    }

    // Trang chủ mới: Landing Page giới thiệu hệ thống mua bán khóa học
    @GetMapping("/")
    public String home(Model model) {
        boolean flaskOk = flaskApiService.checkHealth();
        model.addAttribute("flaskStatus", flaskOk ? "online" : "offline");

        // Lấy 3 khóa học mới nhất hoạt động
        List<Course> latestCourses = courseService.Get_Active_Courses().stream()
                .sorted(Comparator.comparing(Course::getId).reversed())
                .limit(3)
                .toList();

        for (Course c : latestCourses) {
            c.setAverageStars(reviewService.Get_Average_Stars(c.getId()));
            c.setReviewCount(reviewService.Get_Review_Count(c.getId()));
        }
        model.addAttribute("latestCourses", latestCourses);

        // Lấy các khóa học đã mua để ẩn giỏ hàng
        List<Long> enrolledCourseIds = new ArrayList<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            User currentUser = userService.FindUserByEmail(auth.getName());
            if (currentUser != null) {
                enrolledCourseIds = enrollmentRepo.findByUserId(currentUser.getId()).stream()
                        .map(e -> e.getCourse().getId())
                        .toList();
            }
        }
        model.addAttribute("enrolledCourseIds", enrolledCourseIds);

        return "home";
    }

    @GetMapping("/courses")
    public String courses(Model model) {
        return "courses";
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        model.addAttribute("categoryCount", categoryService.Get_All_Categories().size());
        model.addAttribute("courseCount", courseService.Get_All_Courses().size());
        model.addAttribute("lessonCount", lessonService.Get_All_Lessons().size());
        model.addAttribute("consultationCount", studentInfoRepo.count());
        
        var list = studentInfoRepo.findAll(
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "thoiGianDangKy"
                )
        );
        model.addAttribute("consultations", list);
        return "admin/dashboard";
    }

    @PostMapping("/admin/consultation/delete/{id}")
    public String deleteConsultation(@PathVariable("id") Long id) {
        studentInfoRepo.deleteById(id);
        return "redirect:/admin";
    }
}
