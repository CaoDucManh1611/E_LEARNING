package com.example.doan.Controller;

import com.example.doan.Model.Certificate;
import com.example.doan.Model.Enrollment;
import com.example.doan.Model.User;
import com.example.doan.Repository.EnrollmentRepository;
import com.example.doan.Service.Progress_Service;
import com.example.doan.Service.User_Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller điều phối hiển thị chứng chỉ tốt nghiệp của Học viên.
 * File: Controller/CertificateController.java
 */
@Controller
@RequestMapping("/student")
public class CertificateController {

    private final Progress_Service progressService;
    private final EnrollmentRepository enrollmentRepo;
    private final User_Service userService;

    public CertificateController(Progress_Service progressService,
                                 EnrollmentRepository enrollmentRepo,
                                 User_Service userService) {
        this.progressService = progressService;
        this.enrollmentRepo = enrollmentRepo;
        this.userService = userService;
    }

    @GetMapping("/certificate/{enrollmentId}")
    public String viewCertificate(@PathVariable("enrollmentId") Long enrollmentId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        User currentUser = userService.FindUserByEmail(auth.getName());
        if (currentUser == null) {
            return "redirect:/login";
        }

        Enrollment enrollment = enrollmentRepo.findById(enrollmentId).orElse(null);
        if (enrollment == null || !enrollment.getUser().getId().equals(currentUser.getId())) {
            return "redirect:/access-deny";
        }

        Certificate certificate = progressService.Issue_Certificate(enrollment);
        if (certificate == null) {
            return "redirect:/student/courses/" + enrollment.getCourse().getId(); // Chưa hoàn thành 100%
        }

        model.addAttribute("certificate", certificate);
        model.addAttribute("enrollment", enrollment);
        return "student/certificate";
    }
}
