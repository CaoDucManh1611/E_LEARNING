package com.example.doan.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;


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
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            response.sendRedirect("/");
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(requests -> requests
                // 1. Cho phép công khai tất cả mọi người truy cập
                .requestMatchers(
                    "/", "/index", "/index.html", "/login", "/register",
                    "/api/recommend", "/api/skills", "/api/health", "/api/eda",
                    "/api/student-info", "/api/ai/**",
                    "/css/**", "/js/**", "/images/**", "/uploads/**"
                ).permitAll()
                
                // 2. Bảo vệ trang quản trị Admin và các API quản lý sinh viên
                .requestMatchers("/admin.html", "/admin/**", "/api/student-info/all", "/api/student-info/*").hasRole("admin")
                
                // 3. Bảo vệ trang quản trị của Giảng viên (Giai đoạn 2)
                .requestMatchers("/teacher/**").hasRole("teacher")
                
                // 4. Các request còn lại bắt buộc phải đăng nhập
                .anyRequest().authenticated()
            )
            
            // Cấu hình Form Login
            .formLogin(form -> form
                .loginPage("/login")              // Link dẫn đến trang login
                .successHandler(customAuthenticationSuccessHandler()) // Điều hướng động dựa trên Role
                .failureUrl("/login?error")       // Thất bại
                .permitAll()
            )
            
            // Cấu hình Logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout") // Thành công chuyển về login
                .permitAll()
            )
            
            // Xử lý trang báo lỗi phân quyền 403
            .exceptionHandling(e -> e.accessDeniedPage("/access-deny"))
            
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
