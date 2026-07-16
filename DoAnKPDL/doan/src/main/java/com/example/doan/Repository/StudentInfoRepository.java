package com.example.doan.Repository;

import com.example.doan.Model.StudentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * File: Repository/StudentInfoRepository.java
 */
@Repository
public interface StudentInfoRepository extends JpaRepository<StudentInfo, Long> {
}
