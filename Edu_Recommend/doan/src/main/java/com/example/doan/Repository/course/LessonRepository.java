package com.example.doan.Repository.course;

import com.example.doan.Model.course.Course;
import com.example.doan.Model.course.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    
    // Tìm bài học thuộc khóa học sắp xếp theo thứ tự từ nhỏ đến lớn
    List<Lesson> findByCourseOrderByThuTuAsc(Course course);
    
    // Tìm bài học theo ID khóa học sắp xếp theo thứ tự
    List<Lesson> findByCourseIdOrderByThuTuAsc(Long courseId);
}
