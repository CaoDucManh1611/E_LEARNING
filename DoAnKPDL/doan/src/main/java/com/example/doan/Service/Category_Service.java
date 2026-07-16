package com.example.doan.Service;

import com.example.doan.Model.Category;
import com.example.doan.Repository.CategoryRepository;
import com.example.doan.Repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Dịch vụ xử lý nghiệp vụ Danh mục (Category).
 * Tuân thủ phong cách E_LEARNING-main.
 * File: Service/Category_Service.java
 */
@Service
public class Category_Service {

    private final CategoryRepository categoryRepo;
    private final CourseRepository courseRepo;

    public Category_Service(CategoryRepository categoryRepo, CourseRepository courseRepo) {
        this.categoryRepo = categoryRepo;
        this.courseRepo = courseRepo;
    }

    public List<Category> Get_All_Categories() {
        return categoryRepo.findAll();
    }

    public Category Get_ById(Long id) {
        return categoryRepo.findById(id).orElse(null);
    }

    public Category Create(Category cat) {
        return categoryRepo.save(cat);
    }

    public Category Update(Category catm) {
        Category catcu = Get_ById(catm.getId());
        if (catcu != null) {
            catcu.setTenDanhMuc(catm.getTenDanhMuc());
            return categoryRepo.save(catcu);
        }
        return null;
    }

    /**
     * Nghiệp vụ xóa danh mục.
     * Chỉ cho phép xóa nếu danh mục không chứa bất kỳ khóa học nào.
     * Trả về true nếu xóa thành công, false nếu có khóa học thuộc danh mục này.
     */
    public boolean Delete(Long id) {
        Category cat = Get_ById(id);
        if (cat != null) {
            // Kiểm tra xem có khóa học nào thuộc danh mục này không
            long count = courseRepo.findAll().stream()
                    .filter(c -> c.getCategory() != null && c.getCategory().getId().equals(id))
                    .count();
            if (count > 0) {
                return false; // Không được xóa vì đang chứa khóa học
            }
            categoryRepo.deleteById(id);
            return true;
        }
        return false;
    }
}
