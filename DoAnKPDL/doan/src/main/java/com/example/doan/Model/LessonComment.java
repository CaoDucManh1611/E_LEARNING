package com.example.doan.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể LessonComment ánh xạ tới bảng 'lesson_comments' trong MySQL.
 * Hỗ trợ lưu bình luận/hỏi đáp phân cấp cho từng bài học.
 * File: Model/LessonComment.java
 */
@Entity
@Table(name = "lesson_comments")
public class LessonComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    @JsonIgnore
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private LessonComment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<LessonComment> replies = new ArrayList<>();

    @Column(name = "noi_dung", nullable = false, columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Default Constructor
    public LessonComment() {
    }

    // Parameterized Constructor
    public LessonComment(Long id, Lesson lesson, User user, LessonComment parent, String noiDung, LocalDateTime createdAt) {
        this.id = id;
        this.lesson = lesson;
        this.user = user;
        this.parent = parent;
        this.noiDung = noiDung;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LessonComment getParent() {
        return parent;
    }

    public void setParent(LessonComment parent) {
        this.parent = parent;
    }

    public List<LessonComment> getReplies() {
        return replies;
    }

    public void setReplies(List<LessonComment> replies) {
        this.replies = replies;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "LessonComment{" +
                "id=" + id +
                ", noiDung='" + noiDung + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
