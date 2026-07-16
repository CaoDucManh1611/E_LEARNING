package com.example.doan.Model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Thực thể Payment ánh xạ tới bảng 'payments' trong MySQL.
 * Tuân thủ phong cách viết tay của E_LEARNING-main.
 * File: Model/Payment.java
 */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "phuong_thuc")
    private String phuongThuc = "bank_transfer";

    @Column(name = "trang_thai")
    private String trangThai = "pending"; // pending | success | failed

    @Column(name = "ma_giao_dich")
    private String maGiaoDich;

    @Column(name = "so_tien", nullable = false)
    private BigDecimal soTien;

    @Column(name = "pay_date")
    private LocalDateTime payDate;

    // 1. Constructor mặc định
    public Payment() {
    }

    // 2. Constructor đầy đủ
    public Payment(Long id, Order order, String phuongThuc, String trangThai, String maGiaoDich, BigDecimal soTien, LocalDateTime payDate) {
        this.id = id;
        this.order = order;
        this.phuongThuc = phuongThuc;
        this.trangThai = trangThai;
        this.maGiaoDich = maGiaoDich;
        this.soTien = soTien;
        this.payDate = payDate;
    }

    // 3. Getters và Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getPhuongThuc() {
        return phuongThuc;
    }

    public void setPhuongThuc(String phuongThuc) {
        this.phuongThuc = phuongThuc;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getMaGiaoDich() {
        return maGiaoDich;
    }

    public void setMaGiaoDich(String maGiaoDich) {
        this.maGiaoDich = maGiaoDich;
    }

    public BigDecimal getSoTien() {
        return soTien;
    }

    public void setSoTien(BigDecimal soTien) {
        this.soTien = soTien;
    }

    public LocalDateTime getPayDate() {
        return payDate;
    }

    public void setPayDate(LocalDateTime payDate) {
        this.payDate = payDate;
    }

    // 4. toString
    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", trangThai='" + trangThai + '\'' +
                ", soTien=" + soTien +
                '}';
    }
}
