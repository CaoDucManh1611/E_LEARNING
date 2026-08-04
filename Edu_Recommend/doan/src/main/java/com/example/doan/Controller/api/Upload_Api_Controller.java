package com.example.doan.Controller.api;

import com.example.doan.Service.common.FileUpload_Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;


@RestController
@RequestMapping("/api/v1/uploads")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class Upload_Api_Controller {

    private final FileUpload_Service fileUploadService;

    public Upload_Api_Controller(FileUpload_Service fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping("/images")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String path = fileUploadService.Save_File(file, "images");
            return ResponseEntity.ok(Map.of("success", true, "path", path));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Api_Mapper.error(e.getMessage()));
        }
    }

    @PostMapping("/videos")
    public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            String path = fileUploadService.Save_File(file, "videos");
            return ResponseEntity.ok(Map.of("success", true, "path", path));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Api_Mapper.error(e.getMessage()));
        }
    }
}
