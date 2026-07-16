package com.example.doan.Service;

import com.example.doan.Model.User;
import com.example.doan.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * File: Service/User_Service.java
 */
@Service
public class User_Service {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public User_Service(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> Get_All_Users() {
        return userRepo.findAll();
    }

    public User Get_ById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    public User FindUserByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public User DangKy(User user) {
        User ktr = userRepo.findByEmail(user.getEmail());
        if (ktr != null) {
            return null;
        }
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setRole("student");
        return userRepo.save(user);
    }

    public void Delete_User(Long id) {
        userRepo.deleteById(id);
    }
}
