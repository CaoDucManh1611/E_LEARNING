package com.example.doan.Repository;

import com.example.doan.Model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository xử lý các truy vấn bảng notifications trong cơ sở dữ liệu.
 * File: Repository/NotificationRepository.java
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Lấy danh sách thông báo của một người dùng, sắp xếp mới nhất lên đầu
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Đếm số lượng thông báo chưa đọc của người dùng
    long countByUserIdAndDaDocFalse(Long userId);
}
