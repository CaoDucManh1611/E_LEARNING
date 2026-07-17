package com.example.doan.Model.order;
import com.example.doan.Model.user.User;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người mua hàng (Học viên)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Column(name = "tong_tien", nullable = false)
    private BigDecimal tongTien;

    @Column(name = "trang_thai", nullable = false)
    private String trangThai = "pending"; // pending | paid | cancelled

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // 1. Constructor mặc định
    public Order() {
    }

    // 2. Constructor đầy đủ
    public Order(Long id, User user, Coupon coupon, BigDecimal tongTien, String trangThai, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.coupon = coupon;
        this.tongTien = tongTien;
        this.trangThai = trangThai;
        this.createdAt = createdAt;
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

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
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

    // 4. toString
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", tongTien=" + tongTien +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
