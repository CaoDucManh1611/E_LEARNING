package com.example.doan.Controller.api;

import com.example.doan.Model.user.User;
import com.example.doan.Service.user.User_Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class Auth_Api_Controller {

    private final User_Service userService;
    private final AuthenticationManager authenticationManager;

    public Auth_Api_Controller(User_Service userService,
                               AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    // 1. Đăng ký tài khoản học viên mới
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        User savedUser = userService.DangKy(user);
        if (savedUser == null) {
            return ResponseEntity.badRequest().body(Api_Mapper.error("Email này đã được đăng ký bởi tài khoản khác!"));
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đăng ký tài khoản thành công!",
                "user", Api_Mapper.user(savedUser)
        ));
    }

    // 2. Đăng nhập bằng JSON cho VueJS, vẫn dùng session Spring Security
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body,
                                   HttpServletRequest request) {
        String email = body.getOrDefault("email", "").trim();
        String password = body.getOrDefault("password", "");

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );

            User user = userService.FindUserByEmail(email);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đăng nhập thành công!",
                    "user", Api_Mapper.user(user)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Api_Mapper.error("Email hoặc mật khẩu không chính xác!"));
        }
    }

    // 3. Lấy thông tin người dùng đang đăng nhập bằng session Spring Security
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Api_Mapper.error("Chưa đăng nhập"));
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "user", Api_Mapper.user(currentUser)
        ));
    }

    // 4. Đăng xuất session hiện tại
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Api_Mapper.ok("Đăng xuất thành công!"));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return userService.FindUserByEmail(auth.getName());
        }
        return null;
    }
}
