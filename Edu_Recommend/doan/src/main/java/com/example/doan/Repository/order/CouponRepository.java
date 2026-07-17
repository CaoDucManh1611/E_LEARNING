package com.example.doan.Repository.order;

import com.example.doan.Model.order.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    
    Optional<Coupon> findByMaCode(String maCode);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT c FROM Coupon c WHERE c.id = :id")
    java.util.Optional<Coupon> findByIdWithLock(@org.springframework.data.repository.query.Param("id") Long id);
}
