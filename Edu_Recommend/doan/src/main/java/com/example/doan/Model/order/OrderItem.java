package com.example.doan.Model.order;
import com.example.doan.Model.course.Course;
import com.example.doan.Model.order.Order;

import jakarta.persistence.*;
import java.math.BigDecimal;


@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "gia", nullable = false)
    private BigDecimal gia;

    // 1. Constructor mặc định
    public OrderItem() {
    }

    // 2. Constructor đầy đủ
    public OrderItem(Long id, Order order, Course course, BigDecimal gia) {
        this.id = id;
        this.order = order;
        this.course = course;
        this.gia = gia;
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

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public BigDecimal getGia() {
        return gia;
    }

    public void setGia(BigDecimal gia) {
        this.gia = gia;
    }

    // 4. toString
    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", gia=" + gia +
                '}';
    }
}
