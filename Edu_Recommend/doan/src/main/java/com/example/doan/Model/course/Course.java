package com.example.doan.Model.course;
import com.example.doan.Model.user.User;
import com.example.doan.Model.course.Category;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher; // Giảng viên sở hữu khóa học (được sử dụng ở Giai đoạn 2, Giai đoạn 1 tạm thời để NULL)

    @Column(name = "ten_khoa_hoc", nullable = false)
    private String tenKhoaHoc;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "gia", nullable = false)
    private BigDecimal gia;

    @Column(name = "cap_do")
    private String capDo; // Beginner | Intermediate | Advanced

    @Column(name = "hinh_anh")
    private String hinhAnh;

    @Column(name = "trang_thai", nullable = false)
    private String trangThai = "active"; // active | hidden

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private double averageStars;

    @Transient
    private int reviewCount;

    @Column(name = "commission_rate", nullable = false)
    private Integer commissionRate = 70; // Giảng viên nhận 70% mặc định

    // 1. Constructor mặc định
    public Course() {
    }

    // 2. Constructor đầy đủ tham số
    public Course(Long id, Category category, User teacher, String tenKhoaHoc, String moTa, BigDecimal gia, String capDo, String hinhAnh, String trangThai, Integer commissionRate, LocalDateTime createdAt) {
        this.id = id;
        this.category = category;
        this.teacher = teacher;
        this.tenKhoaHoc = tenKhoaHoc;
        this.moTa = moTa;
        this.gia = gia;
        this.capDo = capDo;
        this.hinhAnh = hinhAnh;
        this.trangThai = trangThai;
        this.commissionRate = commissionRate;
        this.createdAt = createdAt;
    }

    // 3. Getters và Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public String getTenKhoaHoc() {
        return tenKhoaHoc;
    }

    public void setTenKhoaHoc(String tenKhoaHoc) {
        this.tenKhoaHoc = tenKhoaHoc;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public BigDecimal getGia() {
        return gia;
    }

    public void setGia(BigDecimal gia) {
        this.gia = gia;
    }

    public String getCapDo() {
        return capDo;
    }

    public void setCapDo(String capDo) {
        this.capDo = capDo;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Integer getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(Integer commissionRate) {
        this.commissionRate = commissionRate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public double getAverageStars() {
        return averageStars;
    }

    public void setAverageStars(double averageStars) {
        this.averageStars = averageStars;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    // 4. toString
    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", tenKhoaHoc='" + tenKhoaHoc + '\'' +
                ", gia=" + gia +
                ", capDo='" + capDo + '\'' +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
