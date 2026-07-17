package com.example.doan.Repository.order;

import com.example.doan.Model.order.InstructorEarning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstructorEarningRepository extends JpaRepository<InstructorEarning, Long> {
    List<InstructorEarning> findByTeacherId(Long teacherId);
}
