package com.example.doan.Controller.api;

import com.example.doan.Service.common.FlaskApiService;
import com.example.doan.Service.common.Gemini_Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit test cho Recommend_Api_Controller.
 * Không boot Spring context — test trực tiếp logic bằng Mockito thuần.
 */
class Recommend_Api_Controller_Test {

    private FlaskApiService flaskApiService;
    private Gemini_Service geminiService;
    private Recommend_Api_Controller controller;

    @BeforeEach
    void setUp() {
        flaskApiService = mock(FlaskApiService.class);
        geminiService = mock(Gemini_Service.class);
        // Truyền null cho các repo không cần trong test này
        controller = new Recommend_Api_Controller(
                flaskApiService, null, null, null, geminiService
        );
    }

    // ------------------------------------------------------------------ //
    // GET /api/v1/health
    // ------------------------------------------------------------------ //

    @Test
    void health_returnsHealthy_whenFlaskIsUp() {
        when(flaskApiService.checkHealth()).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.health();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "healthy");
        assertThat(response.getBody()).containsEntry("flask_api", "ok");
    }

    @Test
    void health_returnsDegraded_whenFlaskIsDown() {
        when(flaskApiService.checkHealth()).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = controller.health();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).containsEntry("status", "degraded");
        assertThat(response.getBody()).containsEntry("flask_api", "unreachable");
    }

    // ------------------------------------------------------------------ //
    // GET /api/v1/skills
    // ------------------------------------------------------------------ //

    @Test
    void skills_returnsListOfSkills() {
        when(flaskApiService.getTopSkills()).thenReturn(List.of("Java", "Python", "SQL"));

        ResponseEntity<Map<String, Object>> response = controller.skills();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("skills");
        assertThat(response.getBody()).containsEntry("total", 3);

        @SuppressWarnings("unchecked")
        List<String> skills = (List<String>) response.getBody().get("skills");
        assertThat(skills).containsExactly("Java", "Python", "SQL");
    }

    // ------------------------------------------------------------------ //
    // POST /api/v1/ai/chat
    // ------------------------------------------------------------------ //

    @Test
    void chat_returnsOfflineFallback_whenGeminiNotConfigured() {
        when(geminiService.isConfigured()).thenReturn(false);

        Map<String, String> result = controller.chat(Map.of("message", "hoàn tiền"));

        assertThat(result.get("reply")).contains("Chính sách hoàn tiền");
    }

    @Test
    void chat_returnsGeminiReply_whenGeminiIsConfigured() {
        when(geminiService.isConfigured()).thenReturn(true);
        when(geminiService.generateReply("xin chào")).thenReturn("Xin chào! Tôi có thể giúp gì?");

        Map<String, String> result = controller.chat(Map.of("message", "xin chào"));

        assertThat(result.get("reply")).isEqualTo("Xin chào! Tôi có thể giúp gì?");
    }

    @Test
    void chat_fallsBackToOffline_whenGeminiReturnsNull() {
        when(geminiService.isConfigured()).thenReturn(true);
        when(geminiService.generateReply(anyString())).thenReturn(null);

        Map<String, String> result = controller.chat(Map.of("message", "lộ trình học"));

        assertThat(result.get("reply")).contains("gợi ý AI");
    }

    @Test
    void chat_returnsDefault_forUnknownMessage() {
        when(geminiService.isConfigured()).thenReturn(false);

        Map<String, String> result = controller.chat(Map.of("message", "câu hỏi ngẫu nhiên"));

        assertThat(result.get("reply")).contains("trợ lý EduRecommend");
    }
}
