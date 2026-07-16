package com.example.doan.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Thực thể Enrollment ánh xạ tới bảng 'enrollments' trong MySQL.
 * Tuân thủ phong cách viết tay của E_LEARNING-main.
 * File: Model/Enrollment.java
 */
@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "ngay_dang_ky", insertable = false, updatable = false)
    private LocalDateTime ngayDangKy;

    @Column(name = "tien_do_percent")
    private int tienDoPercent = 0;

    @Column(name = "trang_thai", nullable = false)
    private String trangThai = "in_progress"; // in_progress | completed

    @Column(name = "ngay_hoan_thanh")
    private LocalDateTime ngayHoanThanh;

    // 1. Constructor mặc định
    public Enrollment() {
    }

    // 2. Constructor đầy đủ
    public Enrollment(Long id, User user, Course course, LocalDateTime ngayDangKy, int tienDoPercent, String trangThai, LocalDateTime ngayHoanThanh) {
        this.id = id;
        this.user = user;
        this.course = course;
        this.ngayDangKy = ngayDangKy;
        this.tienDoPercent = tienDoPercent;
        this.trangThai = trangThai;
        this.ngayHoanThanh = ngayHoanThanh;
    }

    // 3. Getters và Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public LocalDateTime getNgayDangKy() {
        return ngayDangKy;
    }

    public void setNgayDangKy(LocalDateTime ngayDangKy) {
        this.ngayDangKy = ngayDangKy;
    }

    public int getTienDoPercent() {
        return tienDoPercent;
    }

    public void setTienDoPercent(int tienDoPercent) {
        this.tienDoPercent = tienDoPercent;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayHoanThanh() {
        return ngayHoanThanh;
    }

    public void setNgayHoanThanh(LocalDateTime ngayHoanThanh) {
        this.ngayHoanThanh = ngayHoanThanh;
    }

    // 4. toString
    @Override
    public String toString() {
        return "Enrollment{" +
                "id=" + id +
                ", tienDoPercent=" + tienDoPercent +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
