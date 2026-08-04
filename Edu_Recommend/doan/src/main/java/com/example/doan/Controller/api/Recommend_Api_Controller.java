package com.example.doan.Controller.api;

import com.example.doan.Model.recommend.EdaResponse;
import com.example.doan.Model.recommend.RecommendResponse;
import com.example.doan.Model.user.StudentInfo;
import com.example.doan.Model.user.StudentProfile;
import com.example.doan.Model.user.StudentRequest;
import com.example.doan.Model.user.User;
import com.example.doan.Repository.user.StudentInfoRepository;
import com.example.doan.Repository.user.StudentProfileRepository;
import com.example.doan.Repository.user.UserRepository;
import com.example.doan.Service.common.FlaskApiService;
import com.example.doan.Service.common.Gemini_Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class Recommend_Api_Controller {

    private final FlaskApiService flaskApiService;
    private final StudentProfileRepository profileRepo;
    private final StudentInfoRepository studentInfoRepo;
    private final UserRepository userRepo;
    private final Gemini_Service geminiService;

    public Recommend_Api_Controller(FlaskApiService flaskApiService,
                                    StudentProfileRepository profileRepo,
                                    StudentInfoRepository studentInfoRepo,
                                    UserRepository userRepo,
                                    Gemini_Service geminiService) {
        this.flaskApiService = flaskApiService;
        this.profileRepo = profileRepo;
        this.studentInfoRepo = studentInfoRepo;
        this.userRepo = userRepo;
        this.geminiService = geminiService;
    }

    // 1. Hồ sơ học viên cho mô hình AI
    @GetMapping("/profile/me")
    public ResponseEntity<?> getMyProfile() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Api_Mapper.error("Chưa đăng nhập"));
        }
        Optional<StudentProfile> opt = profileRepo.findByUserId(user.getId());
        if (opt.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Chưa có hồ sơ"));
        }
        return ResponseEntity.ok(Map.of("success", true, "profile", opt.get()));
    }

    @PostMapping("/profile")
    public ResponseEntity<?> saveMyProfile(@RequestBody StudentProfile incoming) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Api_Mapper.error("Chưa đăng nhập"));
        }

        StudentProfile profile = profileRepo.findByUserId(user.getId()).orElse(new StudentProfile());
        profile.setUser(user);
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
        return ResponseEntity.ok(Map.of("success", true, "message", "Lưu hồ sơ thành công!", "userId", saved.getUserId()));
    }

    // 2. Gợi ý AI / health / skills / EDA
    @PostMapping("/recommend")
    public ResponseEntity<RecommendResponse> recommend(@RequestBody StudentRequest request) {
        RecommendResponse response = flaskApiService.getRecommendations(request);
        if (response != null && response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(503).body(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean flaskOk = flaskApiService.checkHealth();
        Map<String, Object> result = Map.of(
                "springboot", "ok",
                "flask_api", flaskOk ? "ok" : "unreachable",
                "status", flaskOk ? "healthy" : "degraded"
        );
        return flaskOk ? ResponseEntity.ok(result) : ResponseEntity.status(503).body(result);
    }

    @GetMapping("/skills")
    public ResponseEntity<Map<String, Object>> skills() {
        List<String> skills = flaskApiService.getTopSkills();
        return ResponseEntity.ok(Map.of("total", skills.size(), "skills", skills));
    }

    @GetMapping("/eda")
    public ResponseEntity<EdaResponse> eda(@RequestParam(defaultValue = "students") String dataset) {
        EdaResponse response = flaskApiService.getEda(dataset);
        if (response != null && response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(503).body(response);
    }

    // 3. Chat AI
    @PostMapping("/ai/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "").trim();
        
        String reply = null;
        if (geminiService.isConfigured()) {
            reply = geminiService.generateReply(message);
        }
        
        if (reply == null) {
            reply = getAIResponseOffline(message.toLowerCase());
        }
        
        return Map.of("reply", reply);
    }

    // 4. Lưu thông tin tư vấn/khóa học quan tâm
    @PostMapping("/student-info")
    public ResponseEntity<?> saveStudentInfo(@RequestBody StudentInfo info) {
        try {
            StudentInfo saved = studentInfoRepo.save(info);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", saved.getId(),
                    "message", "Đã lưu thông tin!"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Api_Mapper.error(e.getMessage()));
        }
    }

    private String getAIResponseOffline(String msg) {
        if (msg.contains("hoàn tiền") || msg.contains("refund") || msg.contains("trả tiền") || msg.contains("hủy")) {
            return "Chính sách hoàn tiền: bạn có thể yêu cầu hoàn tiền nếu đơn hàng đã thanh toán, mua dưới 7 ngày và tiến độ học dưới 20%.";
        }
        if (msg.contains("lộ trình") || msg.contains("recommend") || msg.contains("gợi ý") || msg.contains("ai")) {
            return "Hệ thống gợi ý AI phân tích giờ học, chuyên cần, điểm số, giấc ngủ và kỹ năng đầu vào để đề xuất khóa học phù hợp.";
        }
        if (msg.contains("giảng viên") || msg.contains("teacher") || msg.contains("doanh thu")) {
            return "Giảng viên có thể tạo khóa học, quản lý bài học, xem học viên, đánh giá và doanh thu thực nhận sau hoa hồng.";
        }
        if (msg.contains("đánh giá") || msg.contains("sao") || msg.contains("review")) {
            return "Học viên đã mua khóa học có thể đánh giá 1 lần bằng số sao và nhận xét. Admin/Giảng viên không tự đánh giá khóa học.";
        }
        return "Xin chào! Tôi là trợ lý EduRecommend. Bạn có thể hỏi về khóa học, gợi ý AI, hoàn tiền, giảng viên hoặc đánh giá.";
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return userRepo.findByEmail(auth.getName());
    }
}
