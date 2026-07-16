package com.example.doan.Repository;

import com.example.doan.Model.LessonComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository xử lý các truy vấn bảng lesson_comments trong cơ sở dữ liệu.
 * File: Repository/LessonCommentRepository.java
 */
@Repository
public interface LessonCommentRepository extends JpaRepository<LessonComment, Long> {

    // Tìm kiếm các bình luận gốc (không có parent) của một bài giảng, sắp xếp mới nhất lên đầu
    List<LessonComment> findByLessonIdAndParentIsNullOrderByCreatedAtDesc(Long lessonId);
}
