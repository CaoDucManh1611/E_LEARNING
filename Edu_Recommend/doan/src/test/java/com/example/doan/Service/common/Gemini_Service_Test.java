package com.example.doan.Service.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class Gemini_Service_Test {

    @Mock
    private RestTemplate restTemplate;

    private Gemini_Service geminiService;

    @BeforeEach
    void setUp() {
        geminiService = new Gemini_Service(restTemplate, "mock-api-key");
    }

    @Test
    void testIsConfigured_withValidKey() {
        assertTrue(geminiService.isConfigured());
    }

    @Test
    void testIsConfigured_withEmptyKey() {
        Gemini_Service emptyService = new Gemini_Service(restTemplate, "");
        assertFalse(emptyService.isConfigured());

        Gemini_Service nullService = new Gemini_Service(restTemplate, null);
        assertFalse(nullService.isConfigured());

        Gemini_Service placeholderService = new Gemini_Service(restTemplate, "YOUR_GEMINI_API_KEY");
        assertFalse(placeholderService.isConfigured());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGenerateReply_success() {
        Map<String, Object> candidate = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", "Xin chào, tôi là trợ lý ảo.");
        content.put("parts", List.of(part));
        candidate.put("content", content);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("candidates", List.of(candidate));

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        String reply = geminiService.generateReply("Hello");
        assertEquals("Xin chào, tôi là trợ lý ảo.", reply);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGenerateReply_apiError() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenThrow(new RuntimeException("API error"));

        String reply = geminiService.generateReply("Hello");
        assertNull(reply);
    }
}
