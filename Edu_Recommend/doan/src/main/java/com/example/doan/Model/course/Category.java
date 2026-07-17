package com.example.doan.Model.course;

import jakarta.persistence.*;


@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_danh_muc", nullable = false)
    private String tenDanhMuc;

    // 1. Constructor mặc định
    public Category() {
    }

    // 2. Constructor đầy đủ tham số
    public Category(Long id, String tenDanhMuc) {
        this.id = id;
        this.tenDanhMuc = tenDanhMuc;
    }

    // 3. Getters và Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenDanhMuc() {
        return tenDanhMuc;
    }

    public void setTenDanhMuc(String tenDanhMuc) {
        this.tenDanhMuc = tenDanhMuc;
    }

    // 4. toString
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", tenDanhMuc='" + tenDanhMuc + '\'' +
                '}';
    }
}
