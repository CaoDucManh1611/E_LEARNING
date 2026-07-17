package com.example.doan.Repository.user;

import com.example.doan.Model.user.StudentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface StudentInfoRepository extends JpaRepository<StudentInfo, Long> {
}
