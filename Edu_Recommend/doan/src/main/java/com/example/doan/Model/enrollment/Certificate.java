package com.example.doan.Model.enrollment;
import com.example.doan.Model.user.User;
import com.example.doan.Model.course.Course;

import jakarta.persistence.*;
import java.time.LocalDate;


@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false, unique = true)
    private Enrollment enrollment;

    @Column(name = "ma_xac_thuc", nullable = false, unique = true)
    private String maXacThuc;

    @Column(name = "ngay_cap", insertable = false, updatable = false)
    private LocalDate ngayCap;

    // 1. Constructor mặc định
    public Certificate() {
    }

    // 2. Constructor đầy đủ
    public Certificate(Long id, Enrollment enrollment, String maXacThuc, LocalDate ngayCap) {
        this.id = id;
        this.enrollment = enrollment;
        this.maXacThuc = maXacThuc;
        this.ngayCap = ngayCap;
    }

    // 3. Getters và Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    public String getMaXacThuc() {
        return maXacThuc;
    }

    public void setMaXacThuc(String maXacThuc) {
        this.maXacThuc = maXacThuc;
    }

    public LocalDate getNgayCap() {
        return ngayCap;
    }

    public void setNgayCap(LocalDate ngayCap) {
        this.ngayCap = ngayCap;
    }

    // 4. toString
    @Override
    public String toString() {
        return "Certificate{" +
                "id=" + id +
                ", maXacThuc='" + maXacThuc + '\'' +
                ", ngayCap=" + ngayCap +
                '}';
    }
}
