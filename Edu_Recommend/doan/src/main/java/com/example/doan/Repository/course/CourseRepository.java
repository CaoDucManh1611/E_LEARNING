package com.example.doan.Repository.course;

import com.example.doan.Model.course.Category;
import com.example.doan.Model.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    // Tìm danh sách khóa học đang hoạt động
    List<Course> findByTrangThai(String trangThai);
    
    // Tìm danh sách khóa học theo danh mục và đang hoạt động
    List<Course> findByCategoryAndTrangThai(Category category, String trangThai);
    
    // Tìm danh sách khóa học theo tác giả (teacher) (Dành cho Giai đoạn 2)
    List<Course> findByTeacherId(Long teacherId);
}
