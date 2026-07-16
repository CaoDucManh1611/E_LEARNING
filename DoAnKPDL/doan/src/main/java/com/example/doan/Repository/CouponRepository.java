package com.example.doan.Repository;

import com.example.doan.Model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * File: Repository/CouponRepository.java
 */
@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    
    Optional<Coupon> findByMaCode(String maCode);
}
