package com.example.doan.Controller.shared;

import com.example.doan.Model.enrollment.Enrollment;
import com.example.doan.Repository.enrollment.EnrollmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
public class CourseRestController {

    private final EnrollmentRepository enrollmentRepo;

    public CourseRestController(EnrollmentRepository enrollmentRepo) {
        this.enrollmentRepo = enrollmentRepo;
    }

    @GetMapping("/{id}/enrollment-count")
    public ResponseEntity<Map<String, Object>> getEnrollmentCount(@PathVariable("id") Long id) {
        // Find all enrollments, filter by course ID
        // Note: Better to add countByCourseId in repository, but we can do it via findAll for now
        List<Enrollment> enrollments = enrollmentRepo.findAll().stream()
                .filter(e -> e.getCourse() != null && e.getCourse().getId().equals(id))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("count", enrollments.size());
        return ResponseEntity.ok(response);
    }
}
