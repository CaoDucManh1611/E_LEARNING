package com.example.doan.Service.course;

import com.example.doan.Model.course.Lesson;
import com.example.doan.Repository.course.LessonRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class Lesson_Service {

    private final LessonRepository lessonRepo;

    public Lesson_Service(LessonRepository lessonRepo) {
        this.lessonRepo = lessonRepo;
    }

    public List<Lesson> Get_All_Lessons() {
        return lessonRepo.findAll();
    }

    public List<Lesson> Get_Lessons_By_Course(Long courseId) {
        return lessonRepo.findByCourseIdOrderByThuTuAsc(courseId);
    }

    public Lesson Get_ById(Long id) {
        return lessonRepo.findById(id).orElse(null);
    }

    public Lesson Create(Lesson lesson) {
        return lessonRepo.save(lesson);
    }

    public Lesson Update(Lesson lessonm) {
        Lesson lessoncu = Get_ById(lessonm.getId());
        if (lessoncu != null) {
            lessoncu.setTieuDe(lessonm.getTieuDe());
            lessoncu.setVideoUrl(lessonm.getVideoUrl());
            lessoncu.setThuTu(lessonm.getThuTu());
            lessoncu.setThoiLuongPhut(lessonm.getThoiLuongPhut());
            lessoncu.setCourse(lessonm.getCourse());
            return lessonRepo.save(lessoncu);
        }
        return null;
    }

    public void Delete(Long id) {
        lessonRepo.deleteById(id);
    }
}
