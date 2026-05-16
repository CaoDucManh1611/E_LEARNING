package com.example.demo.Config;

import com.example.demo.Service.SinhVien_Service;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    UserDetailsService userDetailsService (SinhVien_Service svsv)
    {
        return new CustomUserDetailsService(svsv);
    }
    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider (UserDetailsService userDetailsService, PasswordEncoder passwordEncoder)
    {
        DaoAuthenticationProvider dao = new DaoAuthenticationProvider(userDetailsService);
        dao.setPasswordEncoder(passwordEncoder);
        return dao;
    }
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        http.authorizeHttpRequests((requests) -> requests.requestMatchers("/Home", "/register").permitAll().anyRequest().authenticated());
        http.formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/Home").failureUrl("/login?error").permitAll());
        return http.build();
    }

}