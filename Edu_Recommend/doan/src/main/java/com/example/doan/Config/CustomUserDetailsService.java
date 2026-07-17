package com.example.doan.Config;

import com.example.doan.Model.user.User;
import com.example.doan.Service.user.User_Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final User_Service userService;

    public CustomUserDetailsService(User_Service userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Tìm tài khoản email dưới database MySQL
        User user = userService.FindUserByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("Không tìm thấy tài khoản người dùng: " + username);
        }

        // Trả về đối tượng User của Spring Security với cờ khóa tài khoản
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                true, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                !user.isLocked(), // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}
