package com.example.doan.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(CustomUserDetailsService customUserDetailsService) {
        return customUserDetailsService;
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService, 
                                                               PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);       // Xác định cách băm mật khẩu
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(requests -> requests
                // RESTful API v1 dành cho frontend VueJS
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/api/v1/auth/**", "/api/v1/health", "/api/v1/skills", "/api/v1/recommend", "/api/v1/eda", "/api/v1/ai/**", "/api/v1/student-info").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/categories/**", "/api/v1/courses/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/lessons/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("admin")
                .requestMatchers("/api/v1/teacher/**").hasRole("teacher")
                .requestMatchers("/api/v1/student/**", "/api/v1/cart/**", "/api/v1/checkout/**", "/api/v1/profile/**", "/api/v1/notifications/**", "/api/v1/lessons/**", "/api/v1/uploads/**").authenticated()

                // Vue SPA routes/static files are public; API security is handled above.
                .anyRequest().permitAll()
            )
            .formLogin(form -> form.disable())
            
            // Cấu hình Logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout") // Thành công chuyển về login
                .permitAll()
            )
            
            // Xử lý lỗi phân quyền: API trả JSON, web Thymeleaf vẫn về trang deny
            .exceptionHandling(e -> e
                .defaultAuthenticationEntryPointFor((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"Chua dang nhap\"}");
                }, request -> request.getRequestURI().startsWith("/api/v1/"))
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (request.getRequestURI().startsWith("/api/v1/")) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"success\":false,\"message\":\"Khong co quyen truy cap\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"success\":false,\"message\":\"Khong co quyen truy cap\"}");
                    }
                })
            )
            
            // Giới hạn Session
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired")
            )
            
            // Ghi nhớ đăng nhập (Remember Me) bằng token
            .rememberMe(remember -> remember
                .key("EduRecommendSecretKey")
                .userDetailsService(userDetailsService)
                .tokenValiditySeconds(14 * 24 * 60 * 60) // 14 ngày
            );

        return http.build();
    }
}
