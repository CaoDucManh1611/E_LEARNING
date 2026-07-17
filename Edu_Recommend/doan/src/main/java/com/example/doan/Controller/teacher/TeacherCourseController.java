package com.example.doan.Controller.teacher;

import com.example.doan.Model.course.Course;
import com.example.doan.Model.user.User;
import com.example.doan.Service.course.Category_Service;
import com.example.doan.Service.course.Course_Service;
import com.example.doan.Service.common.FileUpload_Service;
import com.example.doan.Service.user.User_Service;
import com.example.doan.Service.notification.Notification_Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/teacher/courses")
public class TeacherCourseController {

    private final Course_Service courseService;
    private final Category_Service categoryService;
    private final User_Service userService;
    private final FileUpload_Service fileUploadService;
    private final Notification_Service notificationService;

    public TeacherCourseController(Course_Service courseService, Category_Service categoryService, User_Service userService, FileUpload_Service fileUploadService, Notification_Service notificationService) {
        this.courseService = courseService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.fileUploadService = fileUploadService;
        this.notificationService = notificationService;
    }

    private User getLoggedInTeacher() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return userService.FindUserByEmail(auth.getName());
        }
        return null;
    }

    @GetMapping
    public String listMyCourses(Model model) {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return "redirect:/login";

        // Lọc khóa học theo teacher
        List<Course> myCourses = courseService.Get_All_Courses().stream()
                .filter(c -> c.getTeacher() != null && c.getTeacher().getId().equals(teacher.getId()))
                .collect(Collectors.toList());

        model.addAttribute("courses", myCourses);
        return "teacher/course-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("categories", categoryService.Get_All_Categories());
        model.addAttribute("isUpdate", false);
        return "teacher/course-form";
    }

    @PostMapping("/create")
    public String createCourse(@ModelAttribute Course course,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               RedirectAttributes redirectAttributes) {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return "redirect:/login";

        course.setTeacher(teacher);
        
        // Mặc định khóa học tạo mới bởi GV sẽ vào trạng thái "pending_review" để Admin duyệt
        course.setTrangThai("pending_review");
        // Hoa hồng có thể được Admin set lại sau, mặc định 70
        course.setCommissionRate(70);

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String path = fileUploadService.Save_File(imageFile, "images");
                course.setHinhAnh(path);
            }
            Course saved = courseService.Create(course);
            // Gửi thông báo cho Admin
            notificationService.Create_Course_Pending_Notification(teacher, saved);
            
            redirectAttributes.addFlashAttribute("successMessage", "Đã gửi yêu cầu tạo khóa học! Vui lòng chờ Admin duyệt.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi tạo khóa học: " + e.getMessage());
            return "redirect:/teacher/courses/create";
        }
        return "redirect:/teacher/courses";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        User teacher = getLoggedInTeacher();
        Course course = courseService.Get_ById(id);
        
        // IDOR Protection: GV chỉ được sửa khóa của mình
        if (course == null || course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền sửa khóa học này.");
            return "redirect:/teacher/courses";
        }

        model.addAttribute("course", course);
        model.addAttribute("categories", categoryService.Get_All_Categories());
        model.addAttribute("isUpdate", true);
        return "teacher/course-form";
    }

    @PostMapping("/edit/{id}")
    public String updateCourse(@PathVariable("id") Long id,
                               @ModelAttribute Course updatedCourse,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               RedirectAttributes redirectAttributes) {
        User teacher = getLoggedInTeacher();
        Course existingCourse = courseService.Get_ById(id);
        
        if (existingCourse == null || existingCourse.getTeacher() == null || !existingCourse.getTeacher().getId().equals(teacher.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền sửa khóa học này.");
            return "redirect:/teacher/courses";
        }

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String path = fileUploadService.Save_File(imageFile, "images");
                existingCourse.setHinhAnh(path);
            }

            existingCourse.setTenKhoaHoc(updatedCourse.getTenKhoaHoc());
            existingCourse.setMoTa(updatedCourse.getMoTa());
            existingCourse.setGia(updatedCourse.getGia());
            existingCourse.setCapDo(updatedCourse.getCapDo());
            existingCourse.setCategory(updatedCourse.getCategory());
            
            // Có thể khi sửa thì yêu cầu duyệt lại, hoặc nếu đã active thì vẫn giữ active. 
            // Ở đây tạm cho phép sửa thoải mái nếu đã được duyệt, hoặc đổi về pending_review nếu muốn khắt khe.
            
            courseService.Update(existingCourse);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật khóa học thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật khóa học: " + e.getMessage());
        }

        return "redirect:/teacher/courses";
    }

    @PostMapping("/delete/{id}")
    public String deleteCourse(@PathVariable("id") Long id, @RequestParam(value = "reason", required = false) String reason, RedirectAttributes redirectAttributes) {
        User teacher = getLoggedInTeacher();
        Course course = courseService.Get_ById(id);

        if (course == null || course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xóa khóa học này.");
            return "redirect:/teacher/courses";
        }

        if (reason != null && !reason.trim().isEmpty()) {
            courseService.Delete_With_Refund(id, reason);
        } else {
            courseService.Delete(id);
        }
        
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa khóa học.");
        return "redirect:/teacher/courses";
    }
}
