package com.example.doan.Controller.shared;

import com.example.doan.Model.course.Course;
import com.example.doan.Model.course.Lesson;
import com.example.doan.Service.course.Course_Service;
import com.example.doan.Service.common.FileUpload_Service;
import com.example.doan.Service.course.Lesson_Service;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Controller
@RequestMapping("/admin/courses/{courseId}/lessons")
public class Lesson_Controller {

    private final Lesson_Service lessonService;
    private final Course_Service courseService;
    private final FileUpload_Service fileUploadService;

    public Lesson_Controller(Lesson_Service lessonService, 
                             Course_Service courseService,
                             FileUpload_Service fileUploadService) {
        this.lessonService = lessonService;
        this.courseService = courseService;
        this.fileUploadService = fileUploadService;
    }

    // 1. Hiển thị danh sách bài học thuộc khóa học
    @GetMapping
    public String list(@PathVariable("courseId") Long courseId, Model model) {
        Course course = courseService.Get_ById(courseId);
        if (course == null) {
            return "redirect:/admin/courses";
        }
        List<Lesson> lessons = lessonService.Get_Lessons_By_Course(courseId);
        model.addAttribute("course", course);
        model.addAttribute("lessons", lessons);
        return "admin/lessons/list";
    }

    // 2. Form tạo bài học mới
    @GetMapping("/create")
    public String getCreate(@PathVariable("courseId") Long courseId, Model model) {
        Course course = courseService.Get_ById(courseId);
        if (course == null) {
            return "redirect:/admin/courses";
        }
        Lesson lesson = new Lesson();
        lesson.setCourse(course); // Liên kết khóa học
        model.addAttribute("lesson", lesson);
        model.addAttribute("course", course);
        model.addAttribute("isUpdate", false);
        return "admin/lessons/form";
    }

    // 3. Xử lý lưu tạo bài học mới có tải file video
    @PostMapping("/create")
    public String postCreate(@PathVariable("courseId") Long courseId, 
                             @ModelAttribute("lesson") Lesson lesson,
                             @RequestParam("videoFile") MultipartFile videoFile) {
        Course course = courseService.Get_ById(courseId);
        if (course != null) {
            lesson.setCourse(course);
            try {
                if (videoFile != null && !videoFile.isEmpty()) {
                    String path = fileUploadService.Save_File(videoFile, "videos");
                    lesson.setVideoUrl(path);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            lessonService.Create(lesson);
        }
        return "redirect:/admin/courses/" + courseId + "/lessons";
    }

    // 4. Form cập nhật bài học
    @GetMapping("/update/{id}")
    public String getUpdate(@PathVariable("courseId") Long courseId, 
                            @PathVariable("id") Long id, 
                            Model model) {
        Course course = courseService.Get_ById(courseId);
        Lesson lesson = lessonService.Get_ById(id);
        if (course == null || lesson == null) {
            return "redirect:/admin/courses";
        }
        model.addAttribute("lesson", lesson);
        model.addAttribute("course", course);
        model.addAttribute("isUpdate", true);
        return "admin/lessons/form";
    }

    // 5. Xử lý lưu cập nhật có tải file video
    @PostMapping("/update")
    public String postUpdate(@PathVariable("courseId") Long courseId, 
                             @ModelAttribute("lesson") Lesson lesson,
                             @RequestParam("videoFile") MultipartFile videoFile) {
        Course course = courseService.Get_ById(courseId);
        if (course != null) {
            lesson.setCourse(course);
            try {
                if (videoFile != null && !videoFile.isEmpty()) {
                    String path = fileUploadService.Save_File(videoFile, "videos");
                    lesson.setVideoUrl(path);
                } else {
                    Lesson old = lessonService.Get_ById(lesson.getId());
                    if (old != null) {
                        lesson.setVideoUrl(old.getVideoUrl());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            lessonService.Update(lesson);
        }
        return "redirect:/admin/courses/" + courseId + "/lessons";
    }

    // 6. Xử lý xóa bài học
    @PostMapping("/delete/{id}")
    public String postDelete(@PathVariable("courseId") Long courseId, 
                             @PathVariable("id") Long id) {
        lessonService.Delete(id);
        return "redirect:/admin/courses/" + courseId + "/lessons";
    }
}
