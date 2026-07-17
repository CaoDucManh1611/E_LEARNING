package com.example.doan.Service.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;


@Service
public class FileUpload_Service {

    @Value("${app.upload.dir}")
    private String uploadRoot;

    /**
     * Lưu tệp tin tải lên có kiểm tra định dạng và giới hạn dung lượng.
     * @param file Đối tượng file tải lên
     * @param subFolder Thư mục con ('images' hoặc 'videos')
     * @return Đường dẫn URL tương đối để hiển thị (ví dụ: '/uploads/images/abc.jpg')
     */
    public String Save_File(MultipartFile file, String subFolder) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Kiểm tra xem thư mục d:/EduRecommend/uploads/ có tồn tại trên máy cục bộ này không
        String cleanRoot = "d:/EduRecommend/uploads/";
        File oldDir = new File(cleanRoot);
        if (!oldDir.exists() || !oldDir.isDirectory()) {
            // Nếu không có, dùng thư mục tương đối
            cleanRoot = uploadRoot;
        }

        if (!cleanRoot.endsWith("/") && !cleanRoot.endsWith("\\")) {
            cleanRoot += "/";
        }

        // Tạo tên tệp độc nhất tránh trùng lặp và lấy đuôi mở rộng
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        } else {
            throw new IllegalArgumentException("Tệp tin tải lên không hợp lệ (không có phần mở rộng)!");
        }

        long fileSize = file.getSize();

        // 1. Kiểm tra Whitelist & Dung lượng
        if ("images".equalsIgnoreCase(subFolder)) {
            List<String> allowedImgExts = List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
            if (!allowedImgExts.contains(extension)) {
                throw new IllegalArgumentException("Định dạng hình ảnh không hợp lệ! Hệ thống chỉ nhận: .jpg, .jpeg, .png, .gif, .webp");
            }
            long maxImgSize = 5 * 1024 * 1024; // 5MB
            if (fileSize > maxImgSize) {
                throw new IllegalArgumentException("Dung lượng hình ảnh quá lớn! Kích thước tối đa cho phép là 5MB.");
            }
        } else if ("videos".equalsIgnoreCase(subFolder)) {
            List<String> allowedVidExts = List.of(".mp4", ".mov", ".avi", ".mkv");
            if (!allowedVidExts.contains(extension)) {
                throw new IllegalArgumentException("Định dạng video không hợp lệ! Hệ thống chỉ nhận: .mp4, .mov, .avi, .mkv");
            }
            long maxVidSize = 200 * 1024 * 1024; // 200MB
            if (fileSize > maxVidSize) {
                throw new IllegalArgumentException("Dung lượng video quá lớn! Kích thước tối đa cho phép là 200MB.");
            }
        } else {
            throw new IllegalArgumentException("Thư mục con lưu trữ không hợp lệ!");
        }

        // Tạo thư mục đích nếu chưa có
        File destinationDir = new File(cleanRoot + subFolder);
        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }
        
        String uniqueName = UUID.randomUUID().toString() + extension;
        Path filePath = Paths.get(cleanRoot + subFolder + "/" + uniqueName);

        // Lưu file vật lý vào đĩa cứng
        Files.write(filePath, file.getBytes());

        // Trả về URL tương đối
        return "/uploads/" + subFolder + "/" + uniqueName;
    }
}
