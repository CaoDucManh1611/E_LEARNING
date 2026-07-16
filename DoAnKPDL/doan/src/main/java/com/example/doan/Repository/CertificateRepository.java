package com.example.doan.Repository;

import com.example.doan.Model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * File: Repository/CertificateRepository.java
 */
@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    
    Optional<Certificate> findByEnrollmentId(Long enrollmentId);
    
    Optional<Certificate> findByMaXacThuc(String maXacThuc);
}
