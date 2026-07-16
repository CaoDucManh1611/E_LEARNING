package com.example.doan.Service;

import com.example.doan.Model.*;
import com.example.doan.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service quản lý tiến trình bài học và cấp chứng chỉ tốt nghiệp.
 * Tuân thủ phong cách E_LEARNING-main.
 * File: Service/Progress_Service.java
 */
@Service
public class Progress_Service {

    private final LessonProgressRepository lessonProgressRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final LessonRepository lessonRepo;
    private final CertificateRepository certificateRepo;

    public Progress_Service(LessonProgressRepository lessonProgressRepo,
                            EnrollmentRepository enrollmentRepo,
                            LessonRepository lessonRepo,
                            CertificateRepository certificateRepo) {
        this.lessonProgressRepo = lessonProgressRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.lessonRepo = lessonRepo;
        this.certificateRepo = certificateRepo;
    }

    /**
     * Đồng bộ tiến độ bài học: Tạo bản ghi chưa hoàn thành cho toàn bộ các bài học trong khóa học.
     */
    @Transactional
    public List<LessonProgress> Get_Or_Create_Progress(Enrollment enrollment) {
        List<Lesson> lessons = lessonRepo.findByCourseIdOrderByThuTuAsc(enrollment.getCourse().getId());
        List<LessonProgress> progressList = lessonProgressRepo.findByEnrollmentId(enrollment.getId());

        if (progressList.size() < lessons.size()) {
            for (Lesson lesson : lessons) {
                boolean exists = progressList.stream().anyMatch(p -> p.getLesson().getId().equals(lesson.getId()));
                if (!exists) {
                    LessonProgress lp = new LessonProgress();
                    lp.setEnrollment(enrollment);
                    lp.setLesson(lesson);
                    lp.setHoanThanh(false);
                    lessonProgressRepo.save(lp);
                }
            }
            progressList = lessonProgressRepo.findByEnrollmentId(enrollment.getId());
        }
        return progressList;
    }

    /**
     * Thay đổi trạng thái hoàn thành bài học và tự động tính toán lại % tiến độ của học viên.
     */
    @Transactional
    public int Toggle_Lesson_Progress(Long enrollmentId, Long lessonId, boolean hoanThanh) {
        Optional<LessonProgress> opt = lessonProgressRepo.findByEnrollmentIdAndLessonId(enrollmentId, lessonId);
        if (opt.isPresent()) {
            LessonProgress lp = opt.get();
            lp.setHoanThanh(hoanThanh);
            lp.setHoanThanhAt(hoanThanh ? LocalDateTime.now() : null);
            lessonProgressRepo.save(lp);

            long total = lessonProgressRepo.countByEnrollmentId(enrollmentId);
            long completed = lessonProgressRepo.countByEnrollmentIdAndHoanThanh(enrollmentId, true);

            int percent = 0;
            if (total > 0) {
                percent = (int) ((completed * 100) / total);
            }

            Enrollment enrollment = enrollmentRepo.findById(enrollmentId).orElse(null);
            if (enrollment != null) {
                enrollment.setTienDoPercent(percent);
                if (percent == 100) {
                    enrollment.setTrangThai("completed");
                    enrollment.setNgayHoanThanh(LocalDateTime.now());
                } else {
                    enrollment.setTrangThai("in_progress");
                    enrollment.setNgayHoanThanh(null);
                }
                enrollmentRepo.save(enrollment);
            }
            return percent;
        }
        return 0;
    }

    /**
     * Cấp chứng chỉ tốt nghiệp cho học viên khi đạt 100% tiến độ.
     */
    @Transactional
    public Certificate Issue_Certificate(Enrollment enrollment) {
        if (enrollment.getTienDoPercent() < 100) {
            return null;
        }

        Optional<Certificate> opt = certificateRepo.findByEnrollmentId(enrollment.getId());
        if (opt.isPresent()) {
            return opt.get();
        }

        Certificate cert = new Certificate();
        cert.setEnrollment(enrollment);
        cert.setMaXacThuc("CERT-" + UUID.randomUUID().toString().substring(0, 13).toUpperCase());
        return certificateRepo.save(cert);
    }
}
