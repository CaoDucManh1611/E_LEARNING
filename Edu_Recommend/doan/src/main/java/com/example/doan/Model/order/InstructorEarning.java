package com.example.doan.Model.order;
import com.example.doan.Model.user.User;
import com.example.doan.Model.course.Course;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "instructor_earnings")
public class InstructorEarning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(name = "tong_tien", nullable = false)
    private BigDecimal tongTien; // Giá trị khóa học (hoặc giá thanh toán sau coupon nếu tính theo item)

    @Column(name = "tien_nhan", nullable = false)
    private BigDecimal tienNhan; // Thu nhập thực nhận của giảng viên

    @Column(name = "thoi_gian", insertable = false, updatable = false)
    private LocalDateTime thoiGian;

    public InstructorEarning() {
    }

    public InstructorEarning(User teacher, OrderItem orderItem, BigDecimal tongTien, BigDecimal tienNhan) {
        this.teacher = teacher;
        this.orderItem = orderItem;
        this.tongTien = tongTien;
        this.tienNhan = tienNhan;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

    public BigDecimal getTienNhan() {
        return tienNhan;
    }

    public void setTienNhan(BigDecimal tienNhan) {
        this.tienNhan = tienNhan;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(LocalDateTime thoiGian) {
        this.thoiGian = thoiGian;
    }
}
