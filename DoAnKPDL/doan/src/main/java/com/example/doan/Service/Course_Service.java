package com.example.doan.Service;

import com.example.doan.Model.Course;
import com.example.doan.Repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Dịch vụ xử lý nghiệp vụ Khóa học (Course).
 * Tuân thủ phong cách E_LEARNING-main.
 * File: Service/Course_Service.java
 */
@Service
public class Course_Service {

    private final CourseRepository courseRepo;

    public Course_Service(CourseRepository courseRepo) {
        this.courseRepo = courseRepo;
    }

    public List<Course> Get_All_Courses() {
        return courseRepo.findAll();
    }

    public List<Course> Get_Active_Courses() {
        return courseRepo.findByTrangThai("active");
    }

    public Course Get_ById(Long id) {
        return courseRepo.findById(id).orElse(null);
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

    public void Delete(Long id) {
        courseRepo.deleteById(id);
    }
}
