package com.example.doan.Controller.shared;

import com.example.doan.Model.user.User;
import com.example.doan.Service.user.User_Service;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class User_Controller {

    private final User_Service userService;

    public User_Controller(User_Service userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String getRegister(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String postRegister(@ModelAttribute("user") User user, 
                               BindingResult bindingResult, 
                               Model model) {
        
        User savedUser = userService.DangKy(user);
        
        if (savedUser == null) {
            bindingResult.rejectValue("email", "error.user", "Email này đã được đăng ký bởi tài khoản khác!");
            model.addAttribute("user", user);
            return "auth/register";
        }

        return "redirect:/login";
    }

    @GetMapping("/access-deny")
    public String accessDeny() {
        return "auth/deny";
    }
}
