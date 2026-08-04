package com.example.doan.Controller.api;

import com.example.doan.Model.notification.Notification;
import com.example.doan.Model.user.User;
import com.example.doan.Service.notification.Notification_Service;
import com.example.doan.Service.user.User_Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/v1/notifications")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class Notification_Api_Controller {

    private final Notification_Service notificationService;
    private final User_Service userService;

    public Notification_Api_Controller(Notification_Service notificationService,
                                       User_Service userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<?> list() {
        User user = getCurrentUser();
        if (user == null) return unauthorized();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "unreadCount", notificationService.Get_Unread_Count(user.getId()),
                "data", notificationService.Get_Notifications_By_User(user.getId()).stream()
                        .map(Api_Mapper::notification)
                        .toList()
        ));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> read(@PathVariable("id") Long id) {
        User user = getCurrentUser();
        if (user == null) return unauthorized();

        Notification notification = notificationService.Get_ById(id);
        if (!canAccess(notification, user)) {
            return ResponseEntity.status(403).body(Api_Mapper.error("Không có quyền thao tác thông báo này"));
        }

        notificationService.Mark_As_Read(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "url", notification.getUrl()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        User user = getCurrentUser();
        if (user == null) return unauthorized();

        Notification notification = notificationService.Get_ById(id);
        if (!canAccess(notification, user)) {
            return ResponseEntity.status(403).body(Api_Mapper.error("Không có quyền xóa thông báo này"));
        }

        notificationService.Delete(id);
        return ResponseEntity.ok(Api_Mapper.ok("Đã xóa thông báo."));
    }

    private boolean canAccess(Notification notification, User user) {
        return notification != null
                && notification.getUser() != null
                && notification.getUser().getId().equals(user.getId());
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return userService.FindUserByEmail(auth.getName());
        }
        return null;
    }

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(401).body(Api_Mapper.error("Chưa đăng nhập"));
    }
}
