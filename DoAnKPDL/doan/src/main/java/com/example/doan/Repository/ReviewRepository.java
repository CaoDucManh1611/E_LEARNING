package com.example.doan.Repository;

import com.example.doan.Model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository xử lý các truy vấn bảng reviews trong cơ sở dữ liệu.
 * File: Repository/ReviewRepository.java
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByCourseIdAndTrangThaiOrderByCreatedAtDesc(Long courseId, String trangThai);

    List<Review> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    List<Review> findByTrangThaiOrderByCreatedAtDesc(String trangThai);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
