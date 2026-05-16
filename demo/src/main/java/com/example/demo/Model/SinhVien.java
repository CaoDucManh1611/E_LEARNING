package com.example.demo.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "Student") // Tên bảng trong MySQL của bạn là Student
public class SinhVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "tensv")
    private String tensv;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private String role;

    @Column(name = "address") // Chú ý: trong SQL bạn đặt là address (đã sửa lỗi chính tả)
    private String address;

    // 1. Constructor mặc định (bắt buộc phải có cho JPA)
    public SinhVien() {
    }

    // 2. Constructor đầy đủ tham số
    public SinhVien(int id, String tensv, String email, String password, String address, String role) {
        this.id = id;
        this.tensv = tensv;
        this.email = email;
        this.password = password;
        this.role = role;
        this.address = address;
    }

    // 3. Getter và Setter

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTensv() {
        return tensv;
    }

    public void setTensv(String tensv) {
        this.tensv = tensv;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {return role; }

    public void setRole(String role) {
        this.role = role;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // 4. toString để debug cho dễ

    @Override
    public String toString() {
        return "SinhVien{" +
                "id=" + id +
                ", tensv='" + tensv + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}