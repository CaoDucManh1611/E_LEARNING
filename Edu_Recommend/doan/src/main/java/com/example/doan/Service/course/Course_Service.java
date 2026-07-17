package com.example.doan.Service.course;

import com.example.doan.Model.course.Course;
import com.example.doan.Repository.course.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import com.example.doan.Model.course.*;
import com.example.doan.Model.user.*;
import com.example.doan.Model.order.*;
import com.example.doan.Model.enrollment.*;
import com.example.doan.Model.review.*;
import com.example.doan.Model.refund.*;
import com.example.doan.Model.notification.*;
import com.example.doan.Model.recommend.*;
import com.example.doan.Repository.course.*;
import com.example.doan.Repository.user.*;
import com.example.doan.Repository.order.*;
import com.example.doan.Repository.enrollment.*;
import com.example.doan.Repository.review.*;
import com.example.doan.Repository.refund.*;
import com.example.doan.Repository.notification.*;
import org.springframework.transaction.annotation.Transactional;



@Service
public class Course_Service {
    private final CourseRepository courseRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final OrderItemRepository orderItemRepo;
    private final OrderRepository orderRepo;
    private final RefundRequestRepository refundRepo;
    private final InstructorEarningRepository earningRepo;
    private final NotificationRepository notifRepo;

    public Course_Service(CourseRepository courseRepo,
                          EnrollmentRepository enrollmentRepo,
                          OrderItemRepository orderItemRepo,
                          OrderRepository orderRepo,
                          RefundRequestRepository refundRepo,
                          InstructorEarningRepository earningRepo,
                          NotificationRepository notifRepo) {
        this.courseRepo = courseRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.orderItemRepo = orderItemRepo;
        this.orderRepo = orderRepo;
        this.refundRepo = refundRepo;
        this.earningRepo = earningRepo;
        this.notifRepo = notifRepo;
    }

    public List<Course> Get_All_Courses() {
        return courseRepo.findAll().stream()
                .filter(c -> !"deleted".equals(c.getTrangThai()))
                .toList();
    }

    public List<Course> Get_Active_Courses() {
        return courseRepo.findByTrangThai("active");
    }

    public Course Get_ById(Long id) {
        Course c = courseRepo.findById(id).orElse(null);
        if (c != null && "deleted".equals(c.getTrangThai())) {
            return null; // Do not return deleted courses
        }
        return c;
    }

    public Course Create(Course course) {
        if (course.getTrangThai() == null) {
            course.setTrangThai("active");
        }
        return courseRepo.save(course);
    }

    public Course Update(Course coursem) {
        Course coursecu = Get_ById(coursem.getId());
        if (coursecu != null) {
            coursecu.setTenKhoaHoc(coursem.getTenKhoaHoc());
            coursecu.setMoTa(coursem.getMoTa());
            coursecu.setGia(coursem.getGia());
            coursecu.setCapDo(coursem.getCapDo());
            coursecu.setHinhAnh(coursem.getHinhAnh());
            coursecu.setTrangThai(coursem.getTrangThai());
            coursecu.setCategory(coursem.getCategory());
            return courseRepo.save(coursecu);
        }
        return null;
    }

    @Transactional
    public void Delete_With_Refund(Long courseId, String reason) {
        Course course = Get_ById(courseId);
        if (course == null) return;

        // 1. Get all enrollments for this course
        List<Enrollment> enrollments = enrollmentRepo.findAll().stream()
                .filter(e -> e.getCourse() != null && e.getCourse().getId().equals(courseId))
                .toList();

        // Notify Teacher if exists
        User teacher = course.getTeacher();
        if (teacher != null) {
            Notification notif = new Notification();
            notif.setUser(teacher);
            notif.setTieuDe("Khóa học bị gỡ bỏ: " + course.getTenKhoaHoc());
            notif.setNoiDung("Khóa học '" + course.getTenKhoaHoc() + "' của bạn đã bị gỡ bỏ khỏi hệ thống. Lý do: " + reason + ". Hệ thống đã tự động tiến hành hoàn tiền cho học viên đã mua và khấu trừ doanh thu tương ứng.");
            notif.setUrl("/teacher/courses");
            notif.setDaDoc(false);
            notifRepo.save(notif);
        }

        // 2. For each enrollment, process refund
        for (Enrollment e : enrollments) {
            User student = e.getUser();
            
            // Notify Student
            Notification notif = new Notification();
            notif.setUser(student);
            notif.setTieuDe("Khóa học đã bị hủy: " + course.getTenKhoaHoc());
            notif.setNoiDung("Khóa học '" + course.getTenKhoaHoc() + "' bạn đã mua đã bị gỡ bỏ khỏi hệ thống. Lý do: " + reason + ". Hệ thống đã tự động xử lý hoàn tiền cho bạn.");
            notif.setUrl("/student/orders");
            notif.setDaDoc(false);
            notifRepo.save(notif);

            // Find order item for this student and course
            List<Order> studentOrders = orderRepo.findByUserId(student.getId());
            for (Order order : studentOrders) {
                if (!"paid".equals(order.getTrangThai()) && !"completed".equals(order.getTrangThai())) continue;
                
                List<OrderItem> items = orderItemRepo.findByOrderId(order.getId());
                for (OrderItem item : items) {
                    if (item.getCourse().getId().equals(courseId)) {
                        // Create approved refund request
                        RefundRequest refund = new RefundRequest(order, "Tự động hoàn tiền do khóa học bị xóa. Lý do: " + reason);
                        refund.setTrangThai("approved");
                        refund.setXuLyAt(java.time.LocalDateTime.now());
                        refundRepo.save(refund);
                        
                        // Deduct earning from teacher if exists
                        List<InstructorEarning> earnings = earningRepo.findAll().stream()
                                .filter(earn -> earn.getOrderItem().getId().equals(item.getId()))
                                .toList();
                        for (InstructorEarning earning : earnings) {
                            earningRepo.delete(earning); // Or create negative earning
                        }
                    }
                }
            }
            // 3. Delete enrollment
            enrollmentRepo.delete(e);
        }

        // 4. Soft delete course
        course.setTrangThai("deleted");
        courseRepo.save(course);
    }

    @Transactional
    public void Delete(Long id) {
        Course course = courseRepo.findById(id).orElse(null);
        if (course == null) return;

        long enrollmentCount = enrollmentRepo.findAll().stream()
                .filter(e -> e.getCourse() != null && e.getCourse().getId().equals(id))
                .count();

        if (enrollmentCount > 0) {
            Delete_With_Refund(id, "Hệ thống tự động gỡ bỏ khóa học");
        } else {
            course.setTrangThai("deleted");
            courseRepo.save(course);

            // Notify Teacher if exists
            User teacher = course.getTeacher();
            if (teacher != null) {
                Notification notif = new Notification();
                notif.setUser(teacher);
                notif.setTieuDe("Khóa học bị gỡ bỏ: " + course.getTenKhoaHoc());
                notif.setNoiDung("Khóa học '" + course.getTenKhoaHoc() + "' của bạn đã bị gỡ bỏ khỏi hệ thống.");
                notif.setUrl("/teacher/courses");
                notif.setDaDoc(false);
                notifRepo.save(notif);
            }
        }
    }
}
