package com.example.demo.Config;

import com.example.demo.Model.SinhVien;
import com.example.demo.Service.SinhVien_Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final SinhVien_Service svsv;
    public CustomUserDetailsService (SinhVien_Service svsv)
    {
        this.svsv = svsv;
    }
    @Override
    public UserDetails loadUserByUsername (String username) throws UsernameNotFoundException {
        SinhVien sv = this.svsv.FindUserByEmail(username);
        if (sv == null)
        {
            throw new UsernameNotFoundException ("user not found");
        }
        return new User(sv.getEmail(), sv.getPassword(), Collections.singletonList(new SimpleGrantedAuthority(sv.getRole())));
    }
}
