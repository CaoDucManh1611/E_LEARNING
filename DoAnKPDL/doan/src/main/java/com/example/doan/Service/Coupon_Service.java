package com.example.doan.Service;

import com.example.doan.Model.Coupon;
import com.example.doan.Repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service quản lý nghiệp vụ mã giảm giá (Coupons).
 * Tuân thủ phong cách E_LEARNING-main.
 * File: Service/Coupon_Service.java
 */
@Service
public class Coupon_Service {

    private final CouponRepository couponRepo;

    public Coupon_Service(CouponRepository couponRepo) {
        this.couponRepo = couponRepo;
    }

    public List<Coupon> Get_All_Coupons() {
        return couponRepo.findAll();
    }

    public Coupon Get_ById(Long id) {
        return couponRepo.findById(id).orElse(null);
    }

    @Transactional
    public Coupon Save_Coupon(Coupon coupon) {
        return couponRepo.save(coupon);
    }

    @Transactional
    public void Delete_Coupon(Long id) {
        couponRepo.deleteById(id);
    }

    /**
     * Xác thực tính hợp lệ của mã giảm giá.
     * Trả về thực thể Coupon nếu hợp lệ, ngược lại ném ngoại lệ hoặc trả về null.
     */
    public Coupon Validate_Coupon(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã giảm giá không được để trống!");
        }

        Optional<Coupon> opt = couponRepo.findByMaCode(code.trim().toUpperCase());
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Mã giảm giá không tồn tại!");
        }

        Coupon coupon = opt.get();

        // 1. Kiểm tra ngày hết hạn
        if (coupon.getNgayHetHan() != null && coupon.getNgayHetHan().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Mã giảm giá đã hết hạn sử dụng!");
        }

        // 2. Kiểm tra số lượng lượt dùng
        if (coupon.getDaDung() >= coupon.getSoLuong()) {
            throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng!");
        }

        return coupon;
    }
}
