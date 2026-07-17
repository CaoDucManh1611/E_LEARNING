package com.example.doan.Controller.admin;

import com.example.doan.Model.user.User;
import com.example.doan.Service.user.User_Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final User_Service userService;

    public AdminUserController(User_Service userService) {
        this.userService = userService;
    }

    private String getLoggedInUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return null;
    }

    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.Get_All_Users();
        model.addAttribute("users", users);
        return "admin/users/list";
    }

    @PostMapping("/role/{id}")
    public String changeRole(@PathVariable("id") Long id, 
                             @RequestParam("role") String role,
                             RedirectAttributes redirectAttributes) {
        User targetUser = userService.Get_ById(id);
        if (targetUser != null) {
            String loggedInEmail = getLoggedInUsername();
            if (targetUser.getEmail().equals(loggedInEmail)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể tự thay đổi phân quyền của chính mình.");
            } else {
                userService.ChangeRole(id, role);
                redirectAttributes.addFlashAttribute("successMessage", "Đã thay đổi quyền thành công.");
            }
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/lock/{id}")
    public String toggleLock(@PathVariable("id") Long id,
                             RedirectAttributes redirectAttributes) {
        User targetUser = userService.Get_ById(id);
        if (targetUser != null) {
            String loggedInEmail = getLoggedInUsername();
            if (targetUser.getEmail().equals(loggedInEmail)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Hệ thống chặn: Không thể tự khóa tài khoản của chính mình.");
            } else {
                userService.ToggleLock(id);
                redirectAttributes.addFlashAttribute("successMessage", "Trạng thái khóa tài khoản đã thay đổi.");
            }
        }
        return "redirect:/admin/users";
    }
}
