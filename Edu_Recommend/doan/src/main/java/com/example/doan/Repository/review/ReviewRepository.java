package com.example.doan.Repository.review;

import com.example.doan.Model.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByCourseIdAndTrangThaiOrderByCreatedAtDesc(Long courseId, String trangThai);

    List<Review> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    List<Review> findByTrangThaiOrderByCreatedAtDesc(String trangThai);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
