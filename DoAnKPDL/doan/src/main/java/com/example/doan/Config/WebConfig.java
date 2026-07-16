package com.example.doan.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Cấu hình Web MVC để ánh xạ đường dẫn tĩnh ngoài classpath.
 * Ánh xạ URL '/uploads/**' trỏ tới thư mục vật lý 'd:/NHOM3/uploads/'.
 * File: Config/WebConfig.java
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = "d:/NHOM3/uploads/";
        
        // Tự động tạo thư mục nếu chưa tồn tại
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
            // Tạo các thư mục con cho ảnh và video
            new File(uploadPath + "images").mkdirs();
            new File(uploadPath + "videos").mkdirs();
        }
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
