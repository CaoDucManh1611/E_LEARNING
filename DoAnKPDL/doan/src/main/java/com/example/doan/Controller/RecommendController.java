package com.example.doan.Controller;

import com.example.doan.Model.RecommendResponse;
import com.example.doan.Model.StudentProfile;
import com.example.doan.Model.StudentRequest;
import com.example.doan.Model.User;
import com.example.doan.Repository.StudentProfileRepository;
import com.example.doan.Repository.UserRepository;
import com.example.doan.Service.FlaskApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * File: Controller/RecommendController.java
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RecommendController {

    private static final Logger log = LoggerFactory.getLogger(RecommendController.class);

    private final FlaskApiService flaskApiService;
    private final StudentProfileRepository profileRepo;
    private final UserRepository userRepo;

    public RecommendController(FlaskApiService flaskApiService,
                               StudentProfileRepository profileRepo,
                               UserRepository userRepo) {
        this.flaskApiService = flaskApiService;
        this.profileRepo = profileRepo;
        this.userRepo = userRepo;
    }

    // ── Lấy profile học viên hiện tại ──────────────────────────────────────
    @GetMapping("/profile/me")
    public ResponseEntity<?> getMyProfile() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
        }
        Optional<StudentProfile> opt = profileRepo.findByUserId(user.getId());
        if (opt.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Chưa có hồ sơ"));
        }
        return ResponseEntity.ok(Map.of("success", true, "profile", opt.get()));
    }

    // ── Lưu / cập nhật profile học viên ────────────────────────────────────
    @PostMapping("/profile/save")
    public ResponseEntity<?> saveMyProfile(@RequestBody StudentProfile incoming) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
        }

        // Upsert: tìm profile cũ hoặc tạo mới
        StudentProfile profile = profileRepo.findByUserId(user.getId()).orElse(new StudentProfile());
        profile.setUser(user);

        // Ánh xạ 17 đặc trưng từ request body
        profile.setHoursStudied(incoming.getHoursStudied());
        profile.setAttendance(incoming.getAttendance());
        profile.setPreviousScores(incoming.getPreviousScores());
        profile.setSleepHours(incoming.getSleepHours());
        profile.setTutoringSessions(incoming.getTutoringSessions());
        profile.setExtracurricularActivities(incoming.getExtracurricularActivities());
        profile.setLearningDisabilities(incoming.getLearningDisabilities());
        profile.setFamilyIncome(incoming.getFamilyIncome());
        profile.setParentalInvolvement(incoming.getParentalInvolvement());
        profile.setInternetAccess(incoming.getInternetAccess());
        profile.setSocialMediaUsage(incoming.getSocialMediaUsage());
        profile.setDistanceFromHome(incoming.getDistanceFromHome());
        profile.setAccessToResources(incoming.getAccessToResources());
        profile.setParentalEducationLevel(incoming.getParentalEducationLevel());
        profile.setPhysicalActivity(incoming.getPhysicalActivity());
        profile.setMotivationLevel(incoming.getMotivationLevel());
        profile.setPeerInfluence(incoming.getPeerInfluence());
        profile.setGender(incoming.getGender());
        if (incoming.getGroupLabel() != null) {
            profile.setGroupLabel(incoming.getGroupLabel());
        }

        StudentProfile saved = profileRepo.save(profile);
        log.info("Lưu profile học viên: userId={}", user.getId());
        return ResponseEntity.ok(Map.of("success", true, "userId", saved.getUserId()));
    }

    // ── Gợi ý AI ────────────────────────────────────────────────────────────
    @PostMapping("/recommend")
    public ResponseEntity<RecommendResponse> recommend(@RequestBody StudentRequest request) {
        log.info("Nhận yêu cầu gợi ý: skills={}, topN={}", request.getInputSkills(), request.getTopN());
        RecommendResponse response = flaskApiService.getRecommendations(request);
        if (response != null && response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(503).body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean flaskOk = flaskApiService.checkHealth();
        Map<String, Object> result = Map.of(
                "springboot", "ok",
                "flask_api",  flaskOk ? "ok" : "unreachable",
                "status",     flaskOk ? "healthy" : "degraded"
        );
        return flaskOk
                ? ResponseEntity.ok(result)
                : ResponseEntity.status(503).body(result);
    }

    @GetMapping("/skills")
    public ResponseEntity<Map<String, Object>> skills() {
        List<String> skills = flaskApiService.getTopSkills();
        return ResponseEntity.ok(Map.of(
                "total",  skills.size(),
                "skills", skills
        ));
    }

    // ── Helper: lấy user đang đăng nhập ────────────────────────────────────
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return userRepo.findByEmail(auth.getName());
    }
}
