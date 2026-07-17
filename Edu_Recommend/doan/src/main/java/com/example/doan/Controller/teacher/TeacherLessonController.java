package com.example.doan.Controller.teacher;

import com.example.doan.Model.course.Course;
import com.example.doan.Model.course.Lesson;
import com.example.doan.Model.user.User;
import com.example.doan.Service.course.Course_Service;
import com.example.doan.Service.common.FileUpload_Service;
import com.example.doan.Service.course.Lesson_Service;
import com.example.doan.Service.user.User_Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/teacher/courses/{courseId}/lessons")
public class TeacherLessonController {

    private final Lesson_Service lessonService;
    private final Course_Service courseService;
    private final FileUpload_Service fileUploadService;
    private final User_Service userService;

    public TeacherLessonController(Lesson_Service lessonService, 
                                   Course_Service courseService,
                                   FileUpload_Service fileUploadService,
                                   User_Service userService) {
        this.lessonService = lessonService;
        this.courseService = courseService;
        this.fileUploadService = fileUploadService;
        this.userService = userService;
    }

    private User getLoggedInTeacher() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return userService.FindUserByEmail(auth.getName());
        }
        return null;
    }

    private boolean checkOwnership(Course course) {
        User teacher = getLoggedInTeacher();
        return course != null && teacher != null && course.getTeacher() != null && course.getTeacher().getId().equals(teacher.getId());
    }

    @GetMapping
    public String listLessons(@PathVariable("courseId") Long courseId, Model model, RedirectAttributes ra) {
        Course course = courseService.Get_ById(courseId);
        if (!checkOwnership(course)) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy hoặc bạn không có quyền truy cập khóa học này.");
            return "redirect:/teacher/courses";
        }
        
        List<Lesson> lessons = lessonService.Get_Lessons_By_Course(courseId);
        model.addAttribute("course", course);
        model.addAttribute("lessons", lessons);
        return "teacher/lessons/list";
    }

    @GetMapping("/create")
    public String showCreateForm(@PathVariable("courseId") Long courseId, Model model, RedirectAttributes ra) {
        Course course = courseService.Get_ById(courseId);
        if (!checkOwnership(course)) {
            ra.addFlashAttribute("errorMessage", "Không có quyền thực hiện.");
            return "redirect:/teacher/courses";
        }
        
        Lesson lesson = new Lesson();
        lesson.setCourse(course);
        model.addAttribute("lesson", lesson);
        model.addAttribute("course", course);
        model.addAttribute("isUpdate", false);
        return "teacher/lessons/form";
    }

    @PostMapping("/create")
    public String createLesson(@PathVariable("courseId") Long courseId,
                               @ModelAttribute("lesson") Lesson lesson,
                               @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                               RedirectAttributes ra) {
        Course course = courseService.Get_ById(courseId);
        if (!checkOwnership(course)) {
            ra.addFlashAttribute("errorMessage", "Không có quyền thực hiện.");
            return "redirect:/teacher/courses";
        }

        try {
            if (videoFile != null && !videoFile.isEmpty()) {
                String path = fileUploadService.Save_File(videoFile, "videos");
                lesson.setVideoUrl(path);
            }
            lesson.setCourse(course);
            lessonService.Create(lesson);
            ra.addFlashAttribute("successMessage", "Thêm bài học thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("errorMessage", "Lỗi lưu file video: " + e.getMessage());
            return "redirect:/teacher/courses/" + courseId + "/lessons/create";
        }
        return "redirect:/teacher/courses/" + courseId + "/lessons";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("courseId") Long courseId, 
                               @PathVariable("id") Long id, 
                               Model model, RedirectAttributes ra) {
        Course course = courseService.Get_ById(courseId);
        if (!checkOwnership(course)) {
            ra.addFlashAttribute("errorMessage", "Không có quyền thực hiện.");
            return "redirect:/teacher/courses";
        }

        Lesson lesson = lessonService.Get_ById(id);
        if (lesson == null || !lesson.getCourse().getId().equals(courseId)) {
            ra.addFlashAttribute("errorMessage", "Bài học không tồn tại.");
            return "redirect:/teacher/courses/" + courseId + "/lessons";
        }

        model.addAttribute("lesson", lesson);
        model.addAttribute("course", course);
        model.addAttribute("isUpdate", true);
        return "teacher/lessons/form";
    }

    @PostMapping("/edit/{id}")
    public String updateLesson(@PathVariable("courseId") Long courseId,
                               @PathVariable("id") Long id,
                               @ModelAttribute("lesson") Lesson updatedLesson,
                               @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                               RedirectAttributes ra) {
        Course course = courseService.Get_ById(courseId);
        if (!checkOwnership(course)) {
            ra.addFlashAttribute("errorMessage", "Không có quyền thực hiện.");
            return "redirect:/teacher/courses";
        }

        Lesson existing = lessonService.Get_ById(id);
        if (existing == null || !existing.getCourse().getId().equals(courseId)) {
            ra.addFlashAttribute("errorMessage", "Bài học không tồn tại.");
            return "redirect:/teacher/courses/" + courseId + "/lessons";
        }

        try {
            if (videoFile != null && !videoFile.isEmpty()) {
                String path = fileUploadService.Save_File(videoFile, "videos");
                existing.setVideoUrl(path);
            }
            existing.setTieuDe(updatedLesson.getTieuDe());
            existing.setThoiLuongPhut(updatedLesson.getThoiLuongPhut());
            existing.setThuTu(updatedLesson.getThuTu());
            
            lessonService.Update(existing);
            ra.addFlashAttribute("successMessage", "Cập nhật bài học thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("errorMessage", "Lỗi lưu file video: " + e.getMessage());
            return "redirect:/teacher/courses/" + courseId + "/lessons/edit/" + id;
        }
        return "redirect:/teacher/courses/" + courseId + "/lessons";
    }

    @PostMapping("/delete/{id}")
    public String deleteLesson(@PathVariable("courseId") Long courseId,
                               @PathVariable("id") Long id,
                               RedirectAttributes ra) {
        Course course = courseService.Get_ById(courseId);
        if (!checkOwnership(course)) {
            ra.addFlashAttribute("errorMessage", "Không có quyền thực hiện.");
            return "redirect:/teacher/courses";
        }

        lessonService.Delete(id);
        ra.addFlashAttribute("successMessage", "Đã xóa bài học.");
        return "redirect:/teacher/courses/" + courseId + "/lessons";
    }
}
