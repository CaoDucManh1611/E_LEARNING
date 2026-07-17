package com.example.doan.Model.review;
import com.example.doan.Model.user.User;
import com.example.doan.Model.course.Course;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "so_sao", nullable = false)
    private int soSao;

    @Column(name = "noi_dung", columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "trang_thai", nullable = false)
    private String trangThai = "visible"; // visible | hidden

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Default Constructor
    public Review() {
    }

    // Parameterized Constructor
    public Review(Long id, User user, Course course, int soSao, String noiDung, String trangThai, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.course = course;
        this.soSao = soSao;
        this.noiDung = noiDung;
        this.trangThai = trangThai;
        this.createdAt = createdAt;
    }

    // Getters and Setters
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

    public int getSoSao() {
        return soSao;
    }

    public void setSoSao(int soSao) {
        this.soSao = soSao;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", soSao=" + soSao +
                ", noiDung='" + noiDung + '\'' +
                ", trangThai='" + trangThai + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
