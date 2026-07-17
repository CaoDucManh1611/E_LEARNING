package com.example.doan.Model.course;
import com.example.doan.Model.course.Course;

import jakarta.persistence.*;


@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "tieu_de", nullable = false)
    private String tieuDe;

    @Column(name = "video_url")
    private String videoUrl; // Link nhúng YouTube

    @Column(name = "thu_tu", nullable = false)
    private int thuTu;

    @Column(name = "thoi_luong_phut")
    private Integer thoiLuongPhut;

    // 1. Constructor mặc định
    public Lesson() {
    }

    // 2. Constructor đầy đủ tham số
    public Lesson(Long id, Course course, String tieuDe, String videoUrl, int thuTu, Integer thoiLuongPhut) {
        this.id = id;
        this.course = course;
        this.tieuDe = tieuDe;
        this.videoUrl = videoUrl;
        this.thuTu = thuTu;
        this.thoiLuongPhut = thoiLuongPhut;
    }

    // 3. Getters và Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public int getThuTu() {
        return thuTu;
    }

    public void setThuTu(int thuTu) {
        this.thuTu = thuTu;
    }

    public Integer getThoiLuongPhut() {
        return thoiLuongPhut;
    }

    public void setThoiLuongPhut(Integer thoiLuongPhut) {
        this.thoiLuongPhut = thoiLuongPhut;
    }

    // 4. toString
    @Override
    public String toString() {
        return "Lesson{" +
                "id=" + id +
                ", tieuDe='" + tieuDe + '\'' +
                ", thuTu=" + thuTu +
                '}';
    }
}
