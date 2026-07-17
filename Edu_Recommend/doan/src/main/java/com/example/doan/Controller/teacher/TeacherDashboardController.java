package com.example.doan.Controller.teacher;

import com.example.doan.Model.course.Course;
import com.example.doan.Model.order.InstructorEarning;
import com.example.doan.Model.user.User;
import com.example.doan.Model.enrollment.Enrollment;
import com.example.doan.Model.review.Review;
import com.example.doan.Repository.order.InstructorEarningRepository;
import com.example.doan.Repository.enrollment.EnrollmentRepository;
import com.example.doan.Repository.review.ReviewRepository;
import com.example.doan.Service.course.Course_Service;
import com.example.doan.Service.user.User_Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/teacher")
public class TeacherDashboardController {

    private final InstructorEarningRepository earningRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final ReviewRepository reviewRepo;
    private final User_Service userService;
    private final Course_Service courseService;

    public TeacherDashboardController(InstructorEarningRepository earningRepo,
                                      EnrollmentRepository enrollmentRepo,
                                      ReviewRepository reviewRepo,
                                      User_Service userService,
                                      Course_Service courseService) {
        this.earningRepo = earningRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.reviewRepo = reviewRepo;
        this.userService = userService;
        this.courseService = courseService;
    }

    private User getLoggedInTeacher() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return userService.FindUserByEmail(auth.getName());
        }
        return null;
    }

    // 1. Hiển thị trang tổng quan Dashboard cho Giảng viên
    @GetMapping(value = {"", "/", "/dashboard"})
    public String dashboard(Model model) {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return "redirect:/login";

        List<InstructorEarning> earnings = earningRepo.findByTeacherId(teacher.getId());
        
        BigDecimal totalEarning = BigDecimal.ZERO;
        for (InstructorEarning e : earnings) {
            totalEarning = totalEarning.add(e.getTienNhan());
        }

        // Lấy danh sách học viên đăng ký khóa học của giảng viên này (chỉ lọc những người có vai trò 'student')
        List<Enrollment> teacherEnrollments = enrollmentRepo.findAll().stream()
                .filter(e -> e.getCourse() != null 
                        && e.getCourse().getTeacher() != null 
                        && e.getCourse().getTeacher().getId().equals(teacher.getId())
                        && e.getUser() != null
                        && "student".equals(e.getUser().getRole()))
                .collect(Collectors.toList());

        // Lấy danh sách đánh giá từ học viên về các khóa học của giảng viên này
        List<Review> teacherReviews = reviewRepo.findAll().stream()
                .filter(r -> r.getCourse() != null 
                        && r.getCourse().getTeacher() != null 
                        && r.getCourse().getTeacher().getId().equals(teacher.getId()))
                .collect(Collectors.toList());

        model.addAttribute("totalEarning", totalEarning);
        model.addAttribute("earnings", earnings);
        model.addAttribute("earningCount", earnings.size());
        model.addAttribute("studentCount", teacherEnrollments.size());
        model.addAttribute("reviewCount", teacherReviews.size());

        return "teacher/dashboard";
    }

    // 2. Hiển thị trang danh sách đánh giá của học viên gửi tới các khóa học của giảng viên
    @GetMapping("/reviews")
    public String viewReviews(Model model) {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return "redirect:/login";

        // Lấy các đánh giá của các khóa học thuộc giảng viên
        List<Review> teacherReviews = reviewRepo.findAll().stream()
                .filter(r -> r.getCourse() != null 
                        && r.getCourse().getTeacher() != null 
                        && r.getCourse().getTeacher().getId().equals(teacher.getId()))
                .collect(Collectors.toList());

        model.addAttribute("reviews", teacherReviews);
        return "teacher/reviews";
    }

    // 3. Hiển thị trang báo cáo doanh thu phân bổ và danh sách học viên đăng ký khóa học
    @GetMapping("/reports")
    public String viewReports(Model model) {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return "redirect:/login";

        // 1. Số lượng học viên đăng ký khóa học của mình (chỉ lọc những người có vai trò 'student')
        List<Enrollment> teacherEnrollments = enrollmentRepo.findAll().stream()
                .filter(e -> e.getCourse() != null 
                        && e.getCourse().getTeacher() != null 
                        && e.getCourse().getTeacher().getId().equals(teacher.getId())
                        && e.getUser() != null
                        && "student".equals(e.getUser().getRole()))
                .collect(Collectors.toList());

        // 2. Danh sách doanh thu từ admin phân bổ
        List<InstructorEarning> earnings = earningRepo.findByTeacherId(teacher.getId());
        BigDecimal totalEarning = earnings.stream()
                .map(InstructorEarning::getTienNhan)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Số lượng học viên duy nhất (distinct students)
        long uniqueStudentsCount = teacherEnrollments.stream()
                .map(Enrollment::getUser)
                .distinct()
                .count();

        model.addAttribute("enrollments", teacherEnrollments);
        model.addAttribute("earnings", earnings);
        model.addAttribute("totalEarning", totalEarning);
        model.addAttribute("uniqueStudentsCount", uniqueStudentsCount);
        model.addAttribute("registeredCount", teacherEnrollments.size());

        return "teacher/reports";
    }

    // 4. Trang "Khóa học của tôi" dành riêng cho Giảng viên
    @GetMapping("/my-courses")
    public String myCourses(Model model) {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return "redirect:/login";

        // Khóa học giảng viên sở hữu (do mình tạo)
        List<Course> ownedCourses = courseService.Get_Active_Courses().stream()
                .filter(c -> c.getTeacher() != null && c.getTeacher().getId().equals(teacher.getId()))
                .collect(Collectors.toList());

        // Khóa học giảng viên đã mua từ người khác (through enrollment)
        List<Course> boughtCourses = enrollmentRepo.findByUserId(teacher.getId()).stream()
                .map(Enrollment::getCourse)
                .filter(c -> c != null && (c.getTeacher() == null || !c.getTeacher().getId().equals(teacher.getId())))
                .collect(Collectors.toList());

        model.addAttribute("ownedCourses", ownedCourses);
        model.addAttribute("boughtCourses", boughtCourses);

        return "teacher/my-courses";
    }
}
