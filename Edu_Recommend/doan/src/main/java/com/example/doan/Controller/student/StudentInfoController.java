package com.example.doan.Controller.student;

import com.example.doan.Model.user.StudentInfo;
import com.example.doan.Repository.user.StudentInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class StudentInfoController {

    private static final Logger log = LoggerFactory.getLogger(StudentInfoController.class);
    private final StudentInfoRepository repo;

    public StudentInfoController(StudentInfoRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/student-info")
    public ResponseEntity<Map<String, Object>> save(@RequestBody StudentInfo info) {
        try {
            StudentInfo saved = repo.save(info);
            log.info("Lưu sinh viên: {} | khóa: {}", saved.getEmail(), saved.getKhoaHocQuan());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", saved.getId(),
                    "message", "Đã lưu thông tin!"
            ));
        } catch (Exception e) {
            log.error("Lỗi lưu sinh viên: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/student-info/all")
    public ResponseEntity<List<StudentInfo>> getAll() {
        List<StudentInfo> list = repo.findAll(
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "thoiGianDangKy"
                )
        );
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/student-info/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            repo.deleteById(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
