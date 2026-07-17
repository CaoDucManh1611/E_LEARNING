package com.example.doan.Service.review;

import com.example.doan.Model.review.LessonComment;
import com.example.doan.Repository.review.LessonCommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class LessonComment_Service {

    private final LessonCommentRepository commentRepo;

    public LessonComment_Service(LessonCommentRepository commentRepo) {
        this.commentRepo = commentRepo;
    }

    // 1. Lưu câu hỏi hoặc phản hồi mới
    @Transactional
    public LessonComment Save_Comment(LessonComment comment) {
        return commentRepo.save(comment);
    }

    // 2. Lấy danh sách bình luận gốc của bài học
    public List<LessonComment> Get_Root_Comments_By_Lesson(Long lessonId) {
        return commentRepo.findByLessonIdAndParentIsNullOrderByCreatedAtDesc(lessonId);
    }

    // 3. Tìm bình luận theo ID
    public LessonComment Get_ById(Long id) {
        return commentRepo.findById(id).orElse(null);
    }

    // 4. Xóa bình luận
    @Transactional
    public void Delete_Comment(Long id) {
        commentRepo.deleteById(id);
    }
}
