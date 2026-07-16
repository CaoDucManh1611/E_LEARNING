package com.example.doan.Service;

import com.example.doan.Model.Lesson;
import com.example.doan.Model.Notification;
import com.example.doan.Model.User;
import com.example.doan.Repository.NotificationRepository;
import com.example.doan.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lớp nghiệp vụ Notification_Service quản lý hệ thống thông báo hỏi đáp.
 * Tuân thủ phong cách Snake_Case.
 * File: Service/Notification_Service.java
 */
@Service
public class Notification_Service {

    private final NotificationRepository notifRepo;
    private final UserRepository userRepo;

    public Notification_Service(NotificationRepository notifRepo, UserRepository userRepo) {
        this.notifRepo = notifRepo;
        this.userRepo = userRepo;
    }

    // 1. Lưu thông báo mới
    @Transactional
    public Notification Save_Notification(Notification n) {
        return notifRepo.save(n);
    }

    // 2. Lấy tất cả thông báo của người dùng
    public List<Notification> Get_Notifications_By_User(Long userId) {
        return notifRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Lấy thông báo theo ID
    public Notification Get_ById(Long id) {
        return notifRepo.findById(id).orElse(null);
    }

    // 3. Đếm thông báo chưa đọc
    public long Get_Unread_Count(Long userId) {
        return notifRepo.countByUserIdAndDaDocFalse(userId);
    }

    // 4. Đánh dấu đã đọc
    @Transactional
    public void Mark_As_Read(Long id) {
        Notification n = notifRepo.findById(id).orElse(null);
        if (n != null) {
            n.setDaDoc(true);
            notifRepo.save(n);
        }
    }

    // 5. Tạo thông báo tự động khi có trả lời (Reply)
    @Transactional
    public void Create_Reply_Notification(User targetUser, User sender, Lesson lesson, String snippet) {
        // Không thông báo nếu tự trả lời chính mình
        if (targetUser.getId().equals(sender.getId())) {
            return;
        }
        Notification n = new Notification();
        n.setUser(targetUser);
        n.setSender(sender);
        n.setTieuDe("Phản hồi mới từ " + sender.getHoTen());
        n.setNoiDung(sender.getHoTen() + " đã trả lời thắc mắc của bạn ở bài học '" + lesson.getTieuDe() + "': \"" + snippet + "\"");
        n.setUrl("/student/courses/" + lesson.getCourse().getId() + "/learn?lessonId=" + lesson.getId());
        n.setDaDoc(false);
        notifRepo.save(n);
    }

    // 6. Tạo thông báo cho Giáo viên (Tất cả Admin) khi có Câu hỏi mới (Question)
    @Transactional
    public void Create_Question_Notification(User student, Lesson lesson, String snippet) {
        List<User> admins = userRepo.findAll().stream()
                .filter(u -> "admin".equals(u.getRole()))
                .toList();

        for (User admin : admins) {
            // Không thông báo nếu admin tự hỏi chính mình
            if (admin.getId().equals(student.getId())) {
                continue;
            }
            Notification n = new Notification();
            n.setUser(admin);
            n.setSender(student);
            n.setTieuDe("Câu hỏi mới từ học viên " + student.getHoTen());
            n.setNoiDung(student.getHoTen() + " đã gửi thắc mắc ở bài học '" + lesson.getTieuDe() + "': \"" + snippet + "\"");
            n.setUrl("/student/courses/" + lesson.getCourse().getId() + "/learn?lessonId=" + lesson.getId());
            n.setDaDoc(false);
            notifRepo.save(n);
        }
    }
}
