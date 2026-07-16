package com.example.doan.Repository;

import com.example.doan.Model.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * File: Repository/StudentProfileRepository.java
 */
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    Optional<StudentProfile> findByUserId(Long userId);
}
