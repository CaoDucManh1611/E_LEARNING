package com.example.doan.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Service lưu trữ tệp tin tải lên (Hình ảnh/Video) vào thư mục cục bộ.
 * Tuân thủ phong cách E_LEARNING-main.
 * File: Service/FileUpload_Service.java
 */
@Service
public class FileUpload_Service {

    private final String uploadRoot = "d:/NHOM3/uploads/";

    /**
     * Lưu tệp tin tải lên.
     * @param file Đối tượng file tải lên
     * @param subFolder Thư mục con ('images' hoặc 'videos')
     * @return Đường dẫn URL tương đối để hiển thị (ví dụ: '/uploads/images/abc.jpg')
     */
    public String Save_File(MultipartFile file, String subFolder) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Tạo thư mục đích nếu chưa có
        File destinationDir = new File(uploadRoot + subFolder);
        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }

        // Tạo tên tệp độc nhất tránh trùng lặp
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        
        String uniqueName = UUID.randomUUID().toString() + extension;
        Path filePath = Paths.get(uploadRoot + subFolder + "/" + uniqueName);

        // Lưu file vật lý vào đĩa cứng
        Files.write(filePath, file.getBytes());

        // Trả về URL tương đối
        return "/uploads/" + subFolder + "/" + uniqueName;
    }
}
