package com.example.doan.Controller.teacher;

import com.example.doan.Model.notification.Notification;
import com.example.doan.Model.user.User;
import com.example.doan.Service.notification.Notification_Service;
import com.example.doan.Service.user.User_Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/teacher/notifications")
public class TeacherNotificationController {

    private final Notification_Service notificationService;
    private final User_Service userService;

    public TeacherNotificationController(Notification_Service notificationService, User_Service userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    private User getLoggedInTeacher() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return userService.FindUserByEmail(auth.getName());
        }
        return null;
    }

    @GetMapping
    public String listNotifications(Model model) {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return "redirect:/login";

        List<Notification> notifications = notificationService.Get_Notifications_By_User(teacher.getId());
        model.addAttribute("notifications", notifications);
        return "teacher/notifications/list";
    }

    @GetMapping("/{id}/click")
    public String clickNotification(@PathVariable("id") Long id) {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return "redirect:/login";

        Notification n = notificationService.Get_ById(id);
        if (n != null && n.getUser().getId().equals(teacher.getId())) {
            notificationService.Mark_As_Read(id);
            return "redirect:" + n.getUrl();
        }
        return "redirect:/teacher/notifications";
    }

    // Xóa thông báo — chỉ xóa được thông báo của chính mình
    @PostMapping("/{id}/delete")
    @ResponseBody
    public ResponseEntity<String> deleteNotification(@PathVariable("id") Long id) {
        User teacher = getLoggedInTeacher();
        if (teacher == null) return ResponseEntity.status(401).body("Unauthorized");

        Notification n = notificationService.Get_ById(id);
        if (n == null || !n.getUser().getId().equals(teacher.getId())) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        notificationService.Delete(id);
        return ResponseEntity.ok("Deleted");
    }
}

