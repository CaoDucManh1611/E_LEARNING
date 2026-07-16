package com.example.doan.Repository;

import com.example.doan.Model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository xử lý các truy vấn bảng invoices trong cơ sở dữ liệu.
 * File: Repository/InvoiceRepository.java
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByOrderId(Long orderId);
}
