package com.example.doan.Model.order;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_code", nullable = false, unique = true)
    private String maCode;

    @Column(name = "loai_giam", nullable = false)
    private String loaiGiam; // percent | fixed

    @Column(name = "gia_tri", nullable = false)
    private BigDecimal giaTri;

    @Column(name = "so_luong", nullable = false)
    private int soLuong;

    @Column(name = "da_dung", nullable = false)
    private int daDung = 0;

    @Column(name = "ngay_het_han")
    private LocalDate ngayHetHan;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // 1. Constructor mặc định
    public Coupon() {
    }

    // 2. Constructor đầy đủ
    public Coupon(Long id, String maCode, String loaiGiam, BigDecimal giaTri, int soLuong, int daDung, LocalDate ngayHetHan, LocalDateTime createdAt) {
        this.id = id;
        this.maCode = maCode;
        this.loaiGiam = loaiGiam;
        this.giaTri = giaTri;
        this.soLuong = soLuong;
        this.daDung = daDung;
        this.ngayHetHan = ngayHetHan;
        this.createdAt = createdAt;
    }

    // 3. Getters và Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaCode() {
        return maCode;
    }

    public void setMaCode(String maCode) {
        this.maCode = maCode;
    }

    public String getLoaiGiam() {
        return loaiGiam;
    }

    public void setLoaiGiam(String loaiGiam) {
        this.loaiGiam = loaiGiam;
    }

    public BigDecimal getGiaTri() {
        return giaTri;
    }

    public void setGiaTri(BigDecimal giaTri) {
        this.giaTri = giaTri;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public int getDaDung() {
        return daDung;
    }

    public void setDaDung(int daDung) {
        this.daDung = daDung;
    }

    public LocalDate getNgayHetHan() {
        return ngayHetHan;
    }

    public void setNgayHetHan(LocalDate ngayHetHan) {
        this.ngayHetHan = ngayHetHan;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // 4. toString
    @Override
    public String toString() {
        return "Coupon{" +
                "id=" + id +
                ", maCode='" + maCode + '\'' +
                ", loaiGiam='" + loaiGiam + '\'' +
                ", giaTri=" + giaTri +
                '}';
    }
}
