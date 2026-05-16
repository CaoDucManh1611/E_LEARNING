package com.example.demo.Repository;

import com.example.demo.Model.SinhVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SinhVienRepository extends JpaRepository <SinhVien, Integer> {
    Optional <SinhVien> findByEmail (String email);
}
