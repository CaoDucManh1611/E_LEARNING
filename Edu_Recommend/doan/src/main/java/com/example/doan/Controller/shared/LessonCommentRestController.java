package com.example.doan.Controller.shared;

import com.example.doan.Model.course.Lesson;
import com.example.doan.Model.review.LessonComment;
import com.example.doan.Model.user.User;
import com.example.doan.Service.review.LessonComment_Service;
import com.example.doan.Service.course.Lesson_Service;
import com.example.doan.Service.user.User_Service;
import com.example.doan.Service.notification.Notification_Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/lessons")
public class LessonCommentRestController {

    private final LessonComment_Service commentService;
    private final Lesson_Service lessonService;
    private final User_Service userService;
    private final Notification_Service notificationService;

    public LessonCommentRestController(LessonComment_Service commentService,
                                       Lesson_Service lessonService,
                                       User_Service userService,
                                       Notification_Service notificationService) {
        this.commentService = commentService;
        this.lessonService = lessonService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    // 1. Lấy danh sách bình luận của bài giảng
    @GetMapping("/{lessonId}/comments")
    public ResponseEntity<List<Map<String, Object>>> getComments(@PathVariable("lessonId") Long lessonId) {
        List<LessonComment> rootComments = commentService.Get_Root_Comments_By_Lesson(lessonId);
        List<Map<String, Object>> response = new ArrayList<>();

        for (LessonComment c : rootComments) {
            response.add(serializeComment(c));
        }

        return ResponseEntity.ok(response);
    }

    // 2. Gửi bình luận hoặc phản hồi mới
    @PostMapping("/{lessonId}/comments")
    public ResponseEntity<?> postComment(@PathVariable("lessonId") Long lessonId,
                                         @RequestParam("noiDung") String noiDung,
                                         @RequestParam(value = "parentId", required = false) Long parentId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Vui lòng đăng nhập!"));
        }

        User currentUser = userService.FindUserByEmail(auth.getName());
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Người dùng không tồn tại!"));
        }

        Lesson lesson = lessonService.Get_ById(lessonId);
        if (lesson == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Bài học không tồn tại!"));
        }

        if (noiDung == null || noiDung.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Nội dung không được để trống!"));
        }

        LessonComment comment = new LessonComment();
        comment.setLesson(lesson);
        comment.setUser(currentUser);
        comment.setNoiDung(noiDung.trim());

        if (parentId != null) {
            LessonComment parent = commentService.Get_ById(parentId);
            if (parent != null) {
                comment.setParent(parent);
            }
        }

        LessonComment saved = commentService.Save_Comment(comment);
        
        // Tạo thông báo tự động
        String snippet = saved.getNoiDung().length() > 60 
                ? saved.getNoiDung().substring(0, 60) + "..." 
                : saved.getNoiDung();
        if (saved.getParent() != null) {
            notificationService.Create_Reply_Notification(
                saved.getParent().getUser(), 
                currentUser, 
                lesson, 
                snippet
            );
        } else {
            notificationService.Create_Question_Notification(
                currentUser, 
                lesson, 
                snippet
            );
        }

        // Trả về đối tượng JSON đã chuẩn hóa để client cập nhật UI tức thì
        return ResponseEntity.ok(serializeComment(saved));
    }

    // Tiện ích chuyển đổi thực thể sang Map định dạng JSON (tránh LazyInit/Cyclic Reference)
    private Map<String, Object> serializeComment(LessonComment c) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", c.getId());
        map.put("noiDung", c.getNoiDung());
        
        // Thời gian định dạng thân thiện
        String dateStr = c.getCreatedAt() != null 
                ? c.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) 
                : "Vừa xong";
        map.put("createdAt", dateStr);

        // Thông tin người gửi
        Map<String, Object> u = new HashMap<>();
        u.put("hoTen", c.getUser().getHoTen());
        u.put("role", c.getUser().getRole());
        map.put("user", u);

        // Đệ quy serialize các phản hồi con (replies)
        List<Map<String, Object>> reps = new ArrayList<>();
        if (c.getReplies() != null) {
            for (LessonComment r : c.getReplies()) {
                reps.add(serializeComment(r));
            }
        }
        map.put("replies", reps);

        return map;
    }
}
