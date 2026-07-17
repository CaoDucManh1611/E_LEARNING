package com.example.doan.Service.notification;

import com.example.doan.Model.course.Course;
import com.example.doan.Model.course.Lesson;
import com.example.doan.Model.order.Order;
import com.example.doan.Model.notification.Notification;
import com.example.doan.Model.user.User;
import com.example.doan.Repository.notification.NotificationRepository;
import com.example.doan.Repository.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


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

    // 6. Tạo thông báo cho Giáo viên khi có Câu hỏi mới (Question)
    @Transactional
    public void Create_Question_Notification(User student, Lesson lesson, String snippet) {
        User teacher = lesson.getCourse().getTeacher();
        
        if (teacher != null) {
            if (!teacher.getId().equals(student.getId())) {
                Notification n = new Notification();
                n.setUser(teacher);
                n.setSender(student);
                n.setTieuDe("Câu hỏi mới từ học viên " + student.getHoTen());
                n.setNoiDung(student.getHoTen() + " đã gửi thắc mắc ở bài học '" + lesson.getTieuDe() + "': \"" + snippet + "\"");
                n.setUrl("/student/courses/" + lesson.getCourse().getId() + "/learn?lessonId=" + lesson.getId());
                n.setDaDoc(false);
                notifRepo.save(n);
            }
        } else {
            // Fallback for courses without teacher (Admin courses)
            List<User> admins = userRepo.findAll().stream()
                    .filter(u -> "admin".equals(u.getRole()))
                    .toList();

            for (User admin : admins) {
                if (admin.getId().equals(student.getId())) continue;
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

    // 7. Tạo thông báo cho Giáo viên khi có đánh giá (Review) mới
    @Transactional
    public void Create_Review_Notification(User student, Course course, int stars, String comment) {
        User teacher = course.getTeacher();
        if (teacher != null && !teacher.getId().equals(student.getId())) {
            Notification n = new Notification();
            n.setUser(teacher);
            n.setSender(student);
            n.setTieuDe("Đánh giá mới: " + stars + " sao");
            n.setNoiDung(student.getHoTen() + " đã đánh giá khóa học '" + course.getTenKhoaHoc() + "': \"" + comment + "\"");
            // Link to the course detail page so teacher can see the review
            n.setUrl("/student/courses/" + course.getId());
            n.setDaDoc(false);
            notifRepo.save(n);
        }
    }

    // 8. Tạo thông báo cho Admin khi có yêu cầu duyệt khóa học mới
    @Transactional
    public void Create_Course_Pending_Notification(User teacher, Course course) {
        List<User> admins = userRepo.findAll().stream()
                .filter(u -> "admin".equals(u.getRole()))
                .toList();

        for (User admin : admins) {
            Notification n = new Notification();
            n.setUser(admin);
            n.setSender(teacher);
            n.setTieuDe("Yêu cầu duyệt khóa học mới");
            n.setNoiDung("Giảng viên " + teacher.getHoTen() + " đã gửi yêu cầu duyệt khóa học mới: '" + course.getTenKhoaHoc() + "'");
            n.setUrl("/admin/courses");
            n.setDaDoc(false);
            notifRepo.save(n);
        }
    }

    // 9. Tạo thông báo cho Giảng viên khi khóa học được phê duyệt
    @Transactional
    public void Create_Course_Approved_Notification(Course course) {
        User teacher = course.getTeacher();
        if (teacher != null) {
            Notification n = new Notification();
            n.setUser(teacher);
            n.setTieuDe("Khóa học đã được duyệt");
            n.setNoiDung("Khóa học '" + course.getTenKhoaHoc() + "' của bạn đã được Admin duyệt và bắt đầu hoạt động trên trang chủ.");
            n.setUrl("/teacher/courses");
            n.setDaDoc(false);
            notifRepo.save(n);
        }
    }

    // 10. Tạo thông báo cho Giảng viên khi khóa học bị từ chối duyệt
    @Transactional
    public void Create_Course_Rejected_Notification(Course course) {
        User teacher = course.getTeacher();
        if (teacher != null) {
            Notification n = new Notification();
            n.setUser(teacher);
            n.setTieuDe("Yêu cầu duyệt khóa học bị từ chối");
            n.setNoiDung("Khóa học '" + course.getTenKhoaHoc() + "' của bạn đã bị từ chối phê duyệt và chuyển về trạng thái nháp.");
            n.setUrl("/teacher/courses");
            n.setDaDoc(false);
            notifRepo.save(n);
        }
    }

    // 11. Xóa thông báo
    @Transactional
    public void Delete(Long id) {
        notifRepo.deleteById(id);
    }

    // 12. Tạo thông báo khi có khách hàng mua khóa học
    //     → Gửi cho: Giảng viên của khóa học + Tất cả Admin
    @Transactional
    public void Create_Purchase_Notification(User buyer, Course course, java.math.BigDecimal price) {
        String buyerName = buyer.getHoTen();
        String courseName = course.getTenKhoaHoc();

        // Thông báo cho Giảng viên sở hữu khóa học (nếu có)
        User teacher = course.getTeacher();
        if (teacher != null && !teacher.getId().equals(buyer.getId())) {
            Notification n = new Notification();
            n.setUser(teacher);
            n.setSender(buyer);
            n.setTieuDe("Khóa học vừa được mua!");
            n.setNoiDung(buyerName + " vừa đăng ký khóa học '"
                    + courseName + "' với giá "
                    + String.format("%,.0f", price) + "đ. Hoa hồng đã được ghi nhận.");
            n.setUrl("/teacher/dashboard");
            n.setDaDoc(false);
            notifRepo.save(n);
        }

        // Thông báo cho tất cả Admin
        List<User> admins = userRepo.findAll().stream()
                .filter(u -> "admin".equals(u.getRole()))
                .toList();
        for (User admin : admins) {
            if (admin.getId().equals(buyer.getId())) continue;
            Notification n = new Notification();
            n.setUser(admin);
            n.setSender(buyer);
            n.setTieuDe("Đơn hàng mới: " + courseName);
            n.setNoiDung(buyerName + " vừa mua khóa học '"
                    + courseName + "' với giá "
                    + String.format("%,.0f", price) + "đ.");
            n.setUrl("/admin");
            n.setDaDoc(false);
            notifRepo.save(n);
        }

        // Thông báo xác nhận cho chính người mua
        Notification confirm = new Notification();
        confirm.setUser(buyer);
        confirm.setTieuDe("✅ Đăng ký khóa học thành công!");
        confirm.setNoiDung("Bạn đã mua thành công khóa học '" + courseName
                + "' với giá " + String.format("%,.0f", price) + "đ. Chúc bạn học tốt!");
        confirm.setUrl("/student/courses/" + course.getId() + "/learn");
        confirm.setDaDoc(false);
        notifRepo.save(confirm);
    }

    // 13. Thông báo khi học viên yêu cầu hoàn tiền → Admin
    @Transactional
    public void Create_Refund_Request_Notification(User student, Long orderId, String courseNames) {
        List<User> admins = userRepo.findAll().stream()
                .filter(u -> "admin".equals(u.getRole()))
                .toList();

        for (User admin : admins) {
            Notification n = new Notification();
            n.setUser(admin);
            n.setSender(student);
            n.setTieuDe("Yêu cầu hoàn tiền mới!");
            n.setNoiDung(student.getHoTen() + " vừa gửi yêu cầu hoàn tiền cho đơn hàng #"
                    + orderId + " (Khóa học: " + courseNames + ").");
            n.setUrl("/admin/refunds");
            n.setDaDoc(false);
            notifRepo.save(n);
        }
    }

    // 14. Thông báo kết quả hoàn tiền → Học viên
    @Transactional
    public void Create_Refund_Result_Notification(User student, String courseNames, boolean approved) {
        Notification n = new Notification();
        n.setUser(student);

        if (approved) {
            n.setTieuDe("Yêu cầu hoàn tiền được chấp thuận");
            n.setNoiDung("Yêu cầu hoàn tiền cho khóa học '" + courseNames
                    + "' đã được Admin phê duyệt. Tiền sẽ được hoàn lại trong 3-5 ngày làm việc.");
            n.setUrl("/student/orders");
        } else {
            n.setTieuDe("Yêu cầu hoàn tiền bị từ chối");
            n.setNoiDung("Yêu cầu hoàn tiền cho khóa học '" + courseNames
                    + "' đã bị Admin từ chối. Bạn vẫn có thể tiếp tục học khóa học này.");
            n.setUrl("/student/orders");
        }
        n.setDaDoc(false);
        notifRepo.save(n);
    }
}
