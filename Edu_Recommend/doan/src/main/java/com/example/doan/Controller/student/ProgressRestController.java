package com.example.doan.Controller.student;

import com.example.doan.Service.enrollment.Progress_Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/progress")
public class ProgressRestController {

    private final Progress_Service progressService;

    public ProgressRestController(Progress_Service progressService) {
        this.progressService = progressService;
    }

    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleProgress(
            @RequestParam("enrollmentId") Long enrollmentId,
            @RequestParam("lessonId") Long lessonId,
            @RequestParam("hoanThanh") boolean hoanThanh) {

        int newPercent = progressService.Toggle_Lesson_Progress(enrollmentId, lessonId, hoanThanh);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("percent", newPercent);

        return ResponseEntity.ok(response);
    }
}
