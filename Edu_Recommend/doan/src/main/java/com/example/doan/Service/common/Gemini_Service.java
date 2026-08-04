package com.example.doan.Service.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class Gemini_Service {
    private static final Logger log = LoggerFactory.getLogger(Gemini_Service.class);

    private final RestTemplate restTemplate;
    private final String apiKey;

    @Value("${gemini.api.model:gemini-2.5-flash}")
    private String modelName;

    public Gemini_Service(RestTemplate restTemplate,
                          @Value("${gemini.api.key:}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && !apiKey.equals("YOUR_GEMINI_API_KEY");
    }

    @SuppressWarnings("unchecked")
    public String generateReply(String userMessage) {
        if (!isConfigured()) {
            log.warn("Gemini API key chưa được cấu hình hoặc đang dùng placeholder.");
            return null;
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;

        // Xây dựng request body cho Gemini API
        Map<String, Object> requestBody = new HashMap<>();

        // 1. Contents (Tin nhắn người dùng)
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", userMessage);
        
        Map<String, Object> contentObj = new HashMap<>();
        contentObj.put("parts", List.of(textPart));
        requestBody.put("contents", List.of(contentObj));

        // 2. System Instruction (Prompt hệ thống định hình hành vi Chatbot)
        Map<String, Object> systemPart = new HashMap<>();
        systemPart.put("text", "Bạn là trợ lý ảo tư vấn học tập thông minh tên là EduRecommend. " +
                "Hãy trả lời người học bằng Tiếng Việt một cách thân thiện, ngắn gọn và hữu ích. " +
                "Định hướng câu trả lời của bạn tập trung xoay quanh các dịch vụ của EduRecommend bao gồm: " +
                "gợi ý lộ trình học tập bằng AI (KNN + Apriori), mua bán khóa học trực tuyến, chính sách hoàn tiền (chỉ được duyệt nếu mua dưới 7 ngày và tiến độ học dưới 20%), " +
                "giảng viên có thể tải lên bài giảng và nhận hoa hồng doanh thu, học viên nhận chứng chỉ hoàn thành khóa học 100%. " +
                "Nếu câu hỏi hoàn toàn ngoài lề lĩnh vực học tập và lập trình, hãy khéo léo dẫn dắt người dùng về lại chủ đề khóa học.");
        
        Map<String, Object> systemInstructionObj = new HashMap<>();
        systemInstructionObj.put("parts", List.of(systemPart));
        requestBody.put("systemInstruction", systemInstructionObj);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            log.info("Đang gọi Gemini API ({})", modelName);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                    if (content != null && content.containsKey("parts")) {
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        if (!parts.isEmpty()) {
                            return (String) parts.get(0).get("text");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi kết nối Gemini API: {}", e.getMessage());
        }
        return null;
    }
}
