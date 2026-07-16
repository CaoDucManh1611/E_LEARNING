package com.example.doan.Repository;

import com.example.doan.Model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository xử lý các truy vấn bảng cart_items trong cơ sở dữ liệu.
 * File: Repository/CartItemRepository.java
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserId(Long userId);

    Optional<CartItem> findByUserIdAndCourseId(Long userId, Long courseId);

    @Transactional
    void deleteByUserId(Long userId);

    @Transactional
    void deleteByUserIdAndCourseId(Long userId, Long courseId);
}
