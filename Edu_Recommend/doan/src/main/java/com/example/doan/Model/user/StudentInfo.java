package com.example.doan.Model.user;
import com.example.doan.Model.user.User;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "student_info")
public class StudentInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String hoTen;

    @Column(nullable = false)
    private String email;

    @Column
    private String soDienThoai;

    @Column
    private String khoaHocQuan;

    @Column
    private String urlKhoaHoc;

    @Column
    private LocalDateTime thoiGianDangKy;

    @PrePersist
    public void prePersist() {
        this.thoiGianDangKy = LocalDateTime.now();
    }

    public StudentInfo() {
    }

    public StudentInfo(Long id, String hoTen, String email, String soDienThoai, String khoaHocQuan, String urlKhoaHoc, LocalDateTime thoiGianDangKy) {
        this.id = id;
        this.hoTen = hoTen;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.khoaHocQuan = khoaHocQuan;
        this.urlKhoaHoc = urlKhoaHoc;
        this.thoiGianDangKy = thoiGianDangKy;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String v) { this.hoTen = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String v) { this.soDienThoai = v; }
    public String getKhoaHocQuan() { return khoaHocQuan; }
    public void setKhoaHocQuan(String v) { this.khoaHocQuan = v; }
    public String getUrlKhoaHoc() { return urlKhoaHoc; }
    public void setUrlKhoaHoc(String v) { this.urlKhoaHoc = v; }
    public LocalDateTime getThoiGianDangKy() { return thoiGianDangKy; }
    public void setThoiGianDangKy(LocalDateTime thoiGianDangKy) { this.thoiGianDangKy = thoiGianDangKy; }
}
