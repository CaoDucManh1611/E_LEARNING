package com.example.doan.Controller;

import com.example.doan.Model.Category;
import com.example.doan.Service.Category_Service;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * MVC Controller quản trị Danh mục (chỉ dành cho Admin).
 * Tuân thủ phong cách E_LEARNING-main.
 * File: Controller/Category_Controller.java
 */
@Controller
@RequestMapping("/admin/categories")
public class Category_Controller {

    private final Category_Service categoryService;

    public Category_Controller(Category_Service categoryService) {
        this.categoryService = categoryService;
    }

    // 1. Hiển thị danh sách danh mục
    @GetMapping
    public String list(Model model) {
        List<Category> categories = categoryService.Get_All_Categories();
        model.addAttribute("categories", categories);
        model.addAttribute("category", new Category()); // Cần cho form th:object trong list.html
        return "admin/categories/list";
    }

    // 2. Form tạo danh mục mới
    @GetMapping("/create")
    public String getCreate(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("isUpdate", false);
        return "admin/categories/form";
    }

    // 3. Xử lý lưu tạo mới
    @PostMapping("/create")
    public String postCreate(@ModelAttribute("category") Category category) {
        categoryService.Create(category);
        return "redirect:/admin/categories";
    }

    // 4. Form cập nhật danh mục
    @GetMapping("/update/{id}")
    public String getUpdate(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.Get_ById(id);
        if (category == null) {
            return "redirect:/admin/categories";
        }
        model.addAttribute("category", category);
        model.addAttribute("isUpdate", true);
        return "admin/categories/form";
    }

    // 5. Xử lý lưu cập nhật
    @PostMapping("/update")
    public String postUpdate(@ModelAttribute("category") Category category) {
        categoryService.Update(category);
        return "redirect:/admin/categories";
    }

    // 6. Xử lý xóa danh mục
    @PostMapping("/delete/{id}")
    public String postDelete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        boolean ok = categoryService.Delete(id);
        if (!ok) {
            // Gửi thông báo lỗi qua Flash Attribute nếu không xóa được do ràng buộc khóa học
            redirectAttributes.addFlashAttribute("errorMsg", "Không thể xóa danh mục này vì đang có khóa học trực thuộc!");
        } else {
            redirectAttributes.addFlashAttribute("successMsg", "Đã xóa danh mục thành công!");
        }
        return "redirect:/admin/categories";
    }
}
