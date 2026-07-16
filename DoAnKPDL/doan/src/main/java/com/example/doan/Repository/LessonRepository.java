package com.example.doan.Repository;

import com.example.doan.Model.Course;
import com.example.doan.Model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository xử lý thao tác với bảng lessons.
 * File: Repository/LessonRepository.java
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    
    // Tìm bài học thuộc khóa học sắp xếp theo thứ tự từ nhỏ đến lớn
    List<Lesson> findByCourseOrderByThuTuAsc(Course course);
    
    // Tìm bài học theo ID khóa học sắp xếp theo thứ tự
    List<Lesson> findByCourseIdOrderByThuTuAsc(Long courseId);
}
