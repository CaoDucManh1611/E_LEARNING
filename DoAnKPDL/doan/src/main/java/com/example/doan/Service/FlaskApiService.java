package com.example.doan.Service;

import com.example.doan.Model.EdaResponse;
import com.example.doan.Model.RecommendResponse;
import com.example.doan.Model.StudentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * File: Service/FlaskApiService.java
 */
@Service
public class FlaskApiService {

    private static final Logger log = LoggerFactory.getLogger(FlaskApiService.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public FlaskApiService(RestTemplate restTemplate,
                           @Value("${flask.api.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public RecommendResponse getRecommendations(StudentRequest req) {
        String url = baseUrl + "/recommend";

        Map<String, Object> body = new HashMap<>();
        body.put("encoded_vector", req.toEncodedVector());
        body.put("input_skills",   req.getInputSkills() != null ? req.getInputSkills() : List.of());
        body.put("top_n",          req.getTopN());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("ngrok-skip-browser-warning", "true");

        try {
            log.info("POST {} | skills={}", url, req.getInputSkills());
            ResponseEntity<RecommendResponse> resp = restTemplate.exchange(
                    url, HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    RecommendResponse.class
            );
            return resp.getBody();

        } catch (Exception e) {
            log.error("Flask API lỗi: {}", e.getMessage());
            RecommendResponse err = new RecommendResponse();
            err.setSuccess(false);
            err.setError("Không kết nối được Flask API: " + e.getMessage());
            return err;
        }
    }

    public boolean checkHealth() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("ngrok-skip-browser-warning", "true");
            ResponseEntity<Map> resp = restTemplate.exchange(
                    baseUrl + "/health", HttpMethod.GET,
                    new HttpEntity<>(headers), Map.class
            );
            return resp.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.warn("Health check thất bại: {}", e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getTopSkills() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("ngrok-skip-browser-warning", "true");
            ResponseEntity<Map> resp = restTemplate.exchange(
                    baseUrl + "/skills", HttpMethod.GET,
                    new HttpEntity<>(headers), Map.class
            );
            Map<String, Object> respBody = resp.getBody();
            if (respBody != null && respBody.containsKey("skills")) {
                return (List<String>) respBody.get("skills");
            }
        } catch (Exception e) {
            log.warn("Không lấy được skills: {}", e.getMessage());
        }
        return List.of();
    }

    public EdaResponse getEda(String dataset) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("ngrok-skip-browser-warning", "true");

            String url = baseUrl + "/eda?dataset=" + dataset;
            ResponseEntity<EdaResponse> resp = restTemplate.exchange(
                    url, HttpMethod.GET,
                    new HttpEntity<>(headers),
                    EdaResponse.class
            );
            return resp.getBody();

        } catch (Exception e) {
            log.error("EDA API lỗi: {}", e.getMessage());
            EdaResponse err = new EdaResponse();
            err.setSuccess(false);
            err.setError("Không lấy được EDA: " + e.getMessage());
            return err;
        }
    }
}
