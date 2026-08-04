package com.example.doan.Controller.api;

import com.example.doan.Model.user.User;
import com.example.doan.Repository.user.StudentInfoRepository;
import com.example.doan.Repository.user.StudentProfileRepository;
import com.example.doan.Repository.user.UserRepository;
import com.example.doan.Service.common.FlaskApiService;
import com.example.doan.Service.common.Gemini_Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class Recommend_Api_Controller_Test {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FlaskApiService flaskApiService;

    @MockitoBean
    private Gemini_Service geminiService;

    @MockitoBean
    private StudentProfileRepository profileRepo;

    @MockitoBean
    private StudentInfoRepository studentInfoRepo;

    @MockitoBean
    private UserRepository userRepo;

    @Test
    void testHealthEndpoint_healthy() throws Exception {
        when(flaskApiService.checkHealth()).thenReturn(true);

        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"healthy\"")));
    }

    @Test
    void testHealthEndpoint_degraded() throws Exception {
        when(flaskApiService.checkHealth()).thenReturn(false);

        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(containsString("\"status\":\"degraded\"")));
    }

    @Test
    void testSkillsEndpoint() throws Exception {
        when(flaskApiService.getTopSkills()).thenReturn(List.of("Java", "Python"));

        mockMvc.perform(get("/api/v1/skills"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Java")))
                .andExpect(content().string(containsString("Python")));
    }

    @Test
    void testChatAI_offlineFallback() throws Exception {
        when(geminiService.isConfigured()).thenReturn(false);

        mockMvc.perform(post("/api/v1/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"hoàn tiền\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Chính sách hoàn tiền")));
    }

    @Test
    void testChatAI_onlineResponse() throws Exception {
        when(geminiService.isConfigured()).thenReturn(true);
        when(geminiService.generateReply("hello")).thenReturn("Xin chào, tôi có thể giúp gì?");

        mockMvc.perform(post("/api/v1/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Xin chào, tôi có thể giúp gì?")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetMyProfile_unregistered() throws Exception {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        when(userRepo.findByEmail("test@example.com")).thenReturn(mockUser);
        when(profileRepo.findByUserId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/profile/me"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Chưa có hồ sơ")));
    }
}
