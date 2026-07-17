package com.example.doan.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Kiểm tra xem thư mục d:/EduRecommend/uploads/ có tồn tại trên máy cục bộ này không
        String cleanPath = "d:/EduRecommend/uploads/";
        File oldDir = new File(cleanPath);
        if (!oldDir.exists() || !oldDir.isDirectory()) {
            // Nếu không có (máy khác hoặc khi deploy), dùng thư mục tương đối
            cleanPath = uploadDir;
        }

        if (!cleanPath.endsWith("/") && !cleanPath.endsWith("\\")) {
            cleanPath += "/";
        }
        
        // Tự động tạo thư mục nếu chưa tồn tại
        File dir = new File(cleanPath);
        if (!dir.exists()) {
            dir.mkdirs();
            // Tạo các thư mục con cho ảnh và video
            new File(cleanPath + "images").mkdirs();
            new File(cleanPath + "videos").mkdirs();
        }
        
        String pathUri = dir.getAbsoluteFile().toURI().toString();
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(pathUri);
    }
}
