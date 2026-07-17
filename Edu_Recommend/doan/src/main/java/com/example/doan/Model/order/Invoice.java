package com.example.doan.Model.order;
import com.example.doan.Model.user.User;
import com.example.doan.Model.order.Order;
import com.example.doan.Model.order.Payment;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "so_hoa_don", nullable = false, unique = true)
    private String soHoaDon;

    @Column(name = "ngay_xuat", insertable = false, updatable = false)
    private LocalDateTime ngayXuat;

    public Invoice() {
    }

    public Invoice(Order order, String soHoaDon) {
        this.order = order;
        this.soHoaDon = soHoaDon;
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

    public String getSoHoaDon() {
        return soHoaDon;
    }

    public void setSoHoaDon(String soHoaDon) {
        this.soHoaDon = soHoaDon;
    }

    public LocalDateTime getNgayXuat() {
        return ngayXuat;
    }

    public void setNgayXuat(LocalDateTime ngayXuat) {
        this.ngayXuat = ngayXuat;
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", soHoaDon='" + soHoaDon + '\'' +
                ", orderId=" + (order != null ? order.getId() : "null") +
                '}';
    }
}
