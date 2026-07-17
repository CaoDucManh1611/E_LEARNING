package com.example.doan.Repository.enrollment;

import com.example.doan.Model.enrollment.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    
    List<LessonProgress> findByEnrollmentId(Long enrollmentId);
    
    Optional<LessonProgress> findByEnrollmentIdAndLessonId(Long enrollmentId, Long lessonId);
    
    long countByEnrollmentId(Long enrollmentId);
    
    long countByEnrollmentIdAndHoanThanh(Long enrollmentId, boolean hoanThanh);
}
