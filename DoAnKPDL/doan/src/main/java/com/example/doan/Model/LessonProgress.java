package com.example.doan.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Thực thể LessonProgress ánh xạ tới bảng 'lesson_progress' trong CSDL.
 * Tuân thủ phong cách viết tay của E_LEARNING-main và sử dụng font Inter.
 * File: Model/LessonProgress.java
 */
@Entity
@Table(name = "lesson_progress")
public class LessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "hoan_thanh")
    private boolean hoanThanh = false;

    @Column(name = "hoan_thanh_at")
    private LocalDateTime hoanThanhAt;

    // 1. Constructor mặc định
    public LessonProgress() {
    }

    // 2. Constructor đầy đủ
    public LessonProgress(Long id, Enrollment enrollment, Lesson lesson, boolean hoanThanh, LocalDateTime hoanThanhAt) {
        this.id = id;
        this.enrollment = enrollment;
        this.lesson = lesson;
        this.hoanThanh = hoanThanh;
        this.hoanThanhAt = hoanThanhAt;
    }

    // 3. Getters và Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public boolean isHoanThanh() {
        return hoanThanh;
    }

    public void setHoanThanh(boolean hoanThanh) {
        this.hoanThanh = hoanThanh;
    }

    public LocalDateTime getHoanThanhAt() {
        return hoanThanhAt;
    }

    public void setHoanThanhAt(LocalDateTime hoanThanhAt) {
        this.hoanThanhAt = hoanThanhAt;
    }

    // 4. toString
    @Override
    public String toString() {
        return "LessonProgress{" +
                "id=" + id +
                ", hoanThanh=" + hoanThanh +
                ", hoanThanhAt=" + hoanThanhAt +
                '}';
    }
}
