package com.example.doan.Controller.api;

import com.example.doan.Model.course.Lesson;
import com.example.doan.Model.review.LessonComment;
import com.example.doan.Model.user.User;
import com.example.doan.Service.course.Lesson_Service;
import com.example.doan.Service.notification.Notification_Service;
import com.example.doan.Service.review.LessonComment_Service;
import com.example.doan.Service.user.User_Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/v1/lessons")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class Lesson_Comment_Api_Controller {

    private final LessonComment_Service commentService;
    private final Lesson_Service lessonService;
    private final User_Service userService;
    private final Notification_Service notificationService;

    public Lesson_Comment_Api_Controller(LessonComment_Service commentService,
                                         Lesson_Service lessonService,
                                         User_Service userService,
                                         Notification_Service notificationService) {
        this.commentService = commentService;
        this.lessonService = lessonService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    // 1. Lấy danh sách bình luận của bài học
    @GetMapping("/{lessonId}/comments")
    public ResponseEntity<?> comments(@PathVariable("lessonId") Long lessonId) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", commentService.Get_Root_Comments_By_Lesson(lessonId).stream()
                        .map(Api_Mapper::lessonComment)
                        .toList()
        ));
    }

    // 2. Gửi bình luận hoặc phản hồi
    @PostMapping("/{lessonId}/comments")
    public ResponseEntity<?> postComment(@PathVariable("lessonId") Long lessonId,
                                         @RequestBody Map<String, Object> body) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Api_Mapper.error("Vui lòng đăng nhập!"));
        }

        Lesson lesson = lessonService.Get_ById(lessonId);
        if (lesson == null) {
            return ResponseEntity.badRequest().body(Api_Mapper.error("Bài học không tồn tại!"));
        }

        String noiDung = body.get("noiDung") != null ? body.get("noiDung").toString().trim() : "";
        if (noiDung.isEmpty()) {
            return ResponseEntity.badRequest().body(Api_Mapper.error("Nội dung không được để trống!"));
        }

        LessonComment comment = new LessonComment();
        comment.setLesson(lesson);
        comment.setUser(currentUser);
        comment.setNoiDung(noiDung);

        Long parentId = body.get("parentId") != null && !body.get("parentId").toString().isBlank()
                ? Long.valueOf(body.get("parentId").toString())
                : null;
        if (parentId != null) {
            LessonComment parent = commentService.Get_ById(parentId);
            if (parent != null) {
                comment.setParent(parent);
            }
        }

        LessonComment saved = commentService.Save_Comment(comment);
        String snippet = saved.getNoiDung().length() > 60
                ? saved.getNoiDung().substring(0, 60) + "..."
                : saved.getNoiDung();

        if (saved.getParent() != null) {
            notificationService.Create_Reply_Notification(saved.getParent().getUser(), currentUser, lesson, snippet);
        } else {
            notificationService.Create_Question_Notification(currentUser, lesson, snippet);
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Gửi bình luận thành công!",
                "data", Api_Mapper.lessonComment(saved)
        ));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return userService.FindUserByEmail(auth.getName());
        }
        return null;
    }
}
