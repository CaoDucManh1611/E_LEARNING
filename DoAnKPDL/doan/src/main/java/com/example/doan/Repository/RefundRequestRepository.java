package com.example.doan.Repository;

import com.example.doan.Model.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository xử lý các truy vấn bảng refund_requests trong cơ sở dữ liệu.
 * File: Repository/RefundRequestRepository.java
 */
@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {

    List<RefundRequest> findByOrderUserIdOrderByCreatedAtDesc(Long userId);

    Optional<RefundRequest> findByOrderId(Long orderId);

    List<RefundRequest> findAllByOrderByCreatedAtDesc();
}
