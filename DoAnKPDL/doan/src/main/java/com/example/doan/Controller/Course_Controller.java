package com.example.doan.Controller;

import com.example.doan.Model.Category;
import com.example.doan.Model.Course;
import com.example.doan.Service.Category_Service;
import com.example.doan.Service.Course_Service;
import com.example.doan.Service.FileUpload_Service;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * MVC Controller quản trị Khóa học (chỉ dành cho Admin).
 * Tuân thủ phong cách E_LEARNING-main.
 * File: Controller/Course_Controller.java
 */
@Controller
@RequestMapping("/admin/courses")
public class Course_Controller {

    private final Course_Service courseService;
    private final Category_Service categoryService;
    private final FileUpload_Service fileUploadService;

    public Course_Controller(Course_Service courseService, 
                             Category_Service categoryService, 
                             FileUpload_Service fileUploadService) {
        this.courseService = courseService;
        this.categoryService = categoryService;
        this.fileUploadService = fileUploadService;
    }

    // 1. Hiển thị danh sách khóa học
    @GetMapping
    public String list(Model model) {
        List<Course> courses = courseService.Get_All_Courses();
        model.addAttribute("courses", courses);
        return "admin/courses/list";
    }

    // 2. Form tạo khóa học mới
    @GetMapping("/create")
    public String getCreate(Model model) {
        List<Category> categories = categoryService.Get_All_Categories();
        model.addAttribute("course", new Course());
        model.addAttribute("categories", categories);
        model.addAttribute("isUpdate", false);
        return "admin/courses/form";
    }

    // 3. Xử lý lưu tạo mới có tải file ảnh
    @PostMapping("/create")
    public String postCreate(@ModelAttribute("course") Course course,
                             @RequestParam("imageFile") MultipartFile imageFile) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String path = fileUploadService.Save_File(imageFile, "images");
                course.setHinhAnh(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        courseService.Create(course);
        return "redirect:/admin/courses";
    }

    // 4. Form cập nhật khóa học
    @GetMapping("/update/{id}")
    public String getUpdate(@PathVariable("id") Long id, Model model) {
        Course course = courseService.Get_ById(id);
        if (course == null) {
            return "redirect:/admin/courses";
        }
        List<Category> categories = categoryService.Get_All_Categories();
        model.addAttribute("course", course);
        model.addAttribute("categories", categories);
        model.addAttribute("isUpdate", true);
        return "admin/courses/form";
    }

    // 5. Xử lý lưu cập nhật có tải file ảnh
    @PostMapping("/update")
    public String postUpdate(@ModelAttribute("course") Course course,
                             @RequestParam("imageFile") MultipartFile imageFile) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String path = fileUploadService.Save_File(imageFile, "images");
                course.setHinhAnh(path);
            } else {
                Course old = courseService.Get_ById(course.getId());
                if (old != null) {
                    course.setHinhAnh(old.getHinhAnh());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        courseService.Update(course);
        return "redirect:/admin/courses";
    }

    // 6. Xử lý xóa khóa học
    @PostMapping("/delete/{id}")
    public String postDelete(@PathVariable("id") Long id) {
        courseService.Delete(id);
        return "redirect:/admin/courses";
    }
}
