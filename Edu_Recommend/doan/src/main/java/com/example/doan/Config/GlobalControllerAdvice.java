package com.example.doan.Config;

import com.example.doan.Model.user.User;
import com.example.doan.Service.user.User_Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;


@ControllerAdvice
public class GlobalControllerAdvice {

    private final User_Service userService;

    public GlobalControllerAdvice(User_Service userService) {
        this.userService = userService;
    }

    @ModelAttribute
    public void addCurrentUserToModel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            User currentUser = userService.FindUserByEmail(auth.getName());
            if (currentUser != null) {
                model.addAttribute("currentUser", currentUser);
            }
        }
    }
}
