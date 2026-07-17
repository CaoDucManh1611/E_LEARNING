package com.example.doan.Service.user;

import com.example.doan.Model.user.User;
import com.example.doan.Repository.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
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

    public void ChangeRole(Long userId, String newRole) {
        User user = Get_ById(userId);
        if (user != null) {
            user.setRole(newRole);
            userRepo.save(user);
        }
    }

    public void ToggleLock(Long userId) {
        User user = Get_ById(userId);
        if (user != null) {
            user.setLocked(!user.isLocked());
            userRepo.save(user);
        }
    }

    public void Delete_User(Long id) {
        userRepo.deleteById(id);
    }
}
