package com.example.doan.Repository.refund;

import com.example.doan.Model.refund.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {

    List<RefundRequest> findByOrderUserIdOrderByCreatedAtDesc(Long userId);

    Optional<RefundRequest> findByOrderId(Long orderId);

    List<RefundRequest> findAllByOrderByCreatedAtDesc();
}
