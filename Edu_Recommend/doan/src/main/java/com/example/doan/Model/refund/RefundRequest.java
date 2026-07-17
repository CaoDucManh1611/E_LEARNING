package com.example.doan.Model.refund;
import com.example.doan.Model.user.User;
import com.example.doan.Model.order.Order;
import com.example.doan.Model.order.Payment;
import com.example.doan.Model.course.Course;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "refund_requests")
public class RefundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "ly_do", columnDefinition = "TEXT")
    private String lyDo;

    @Column(name = "trang_thai", nullable = false)
    private String trangThai = "requested"; // requested | approved | rejected

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "xu_ly_at")
    private LocalDateTime xuLyAt;

    public RefundRequest() {
    }

    public RefundRequest(Order order, String lyDo) {
        this.order = order;
        this.lyDo = lyDo;
        this.trangThai = "requested";
    }

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

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
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

    public LocalDateTime getXuLyAt() {
        return xuLyAt;
    }

    public void setXuLyAt(LocalDateTime xuLyAt) {
        this.xuLyAt = xuLyAt;
    }

    @Override
    public String toString() {
        return "RefundRequest{" +
                "id=" + id +
                ", orderId=" + (order != null ? order.getId() : "null") +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
