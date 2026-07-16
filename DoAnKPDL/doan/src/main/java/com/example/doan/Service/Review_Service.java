package com.example.doan.Service;

import com.example.doan.Model.Review;
import com.example.doan.Repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lớp nghiệp vụ Review_Service quản trị các thao tác liên quan tới Đánh giá khóa học.
 * Tuân thủ quy tắc đặt tên Snake_Case của dự án.
 * File: Service/Review_Service.java
 */
@Service
public class Review_Service {

    private final ReviewRepository reviewRepo;

    public Review_Service(ReviewRepository reviewRepo) {
        this.reviewRepo = reviewRepo;
    }

    // 1. Lưu đánh giá mới
    @Transactional
    public Review Save_Review(Review review) {
        // Mặc định ban đầu trạng thái là visible
        review.setTrangThai("visible");
        return reviewRepo.save(review);
    }

    // 2. Lấy danh sách đánh giá công khai theo khóa học
    public List<Review> Get_Visible_Reviews_By_Course(Long courseId) {
        return reviewRepo.findByCourseIdAndTrangThaiOrderByCreatedAtDesc(courseId, "visible");
    }

    // 3. Lấy toàn bộ đánh giá (dành cho Admin)
    public List<Review> Get_All_Reviews() {
        return reviewRepo.findAll();
    }

    // 4. Tìm kiếm đánh giá theo ID
    public Review Get_ById(Long id) {
        return reviewRepo.findById(id).orElse(null);
    }

    // 5. Cập nhật trạng thái hiển thị (Duyệt ẩn/hiện)
    @Transactional
    public void Update_Status(Long id, String status) {
        Review review = Get_ById(id);
        if (review != null) {
            review.setTrangThai(status);
            reviewRepo.save(review);
        }
    }

    // 6. Xóa đánh giá
    @Transactional
    public void Delete_Review(Long id) {
        reviewRepo.deleteById(id);
    }

    // 7. Kiểm tra xem user đã đánh giá khóa học chưa
    public boolean Has_Reviewed(Long userId, Long courseId) {
        return reviewRepo.existsByUserIdAndCourseId(userId, courseId);
    }

    // 8. Tính điểm sao trung bình cho khóa học (chỉ tính các review visible)
    public double Get_Average_Stars(Long courseId) {
        List<Review> reviews = Get_Visible_Reviews_By_Course(courseId);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (Review r : reviews) {
            sum += r.getSoSao();
        }
        // Làm tròn đến 1 chữ số thập phân
        return Math.round((sum / reviews.size()) * 10.0) / 10.0;
    }

    // 9. Lấy tổng số lượng đánh giá của khóa học
    public int Get_Review_Count(Long courseId) {
        return Get_Visible_Reviews_By_Course(courseId).size();
    }
}
