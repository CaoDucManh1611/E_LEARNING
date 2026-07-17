package com.example.doan.Repository.user;

import com.example.doan.Model.user.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    Optional<StudentProfile> findByUserId(Long userId);
}
