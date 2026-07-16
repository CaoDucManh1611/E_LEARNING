package com.example.doan.Repository;

import com.example.doan.Model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * File: Repository/EnrollmentRepository.java
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    
    // Kiểm tra học viên đã đăng ký học khóa này chưa
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    
    // Tìm các khóa học đã đăng ký của học viên
    List<Enrollment> findByUserId(Long userId);

    java.util.Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);
}
