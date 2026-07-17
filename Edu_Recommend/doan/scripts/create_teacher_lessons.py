import os

def write_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Created {path}")

controller_code = """package com.example.doan.Controller;

import com.example.doan.Model.Course;
import com.example.doan.Model.Lesson;
import com.example.doan.Model.User;
import com.example.doan.Service.Course_Service;
import com.example.doan.Service.FileUpload_Service;
import com.example.doan.Service.Lesson_Service;
import com.example.doan.Service.User_Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/teacher/courses/{courseId}/lessons")
public class TeacherLessonController {

    private final Lesson_Service lessonService;
    private final Course_Service courseService;
    private final FileUpload_Service fileUploadService;
    private final User_Service userService;

    public TeacherLessonController(Lesson_Service lessonService, 
                                   Course_Service courseService,
                                   FileUpload_Service fileUploadService,
                                   User_Service userService) {
        this.lessonService = lessonService;
        this.courseService = courseService;
        this.fileUploadService = fileUploadService;
        this.userService = userService;
    }

    private User getLoggedInTeacher() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return userService.FindUserByEmail(auth.getName());
        }
        return null;
    }

    private boolean checkOwnership(Course course) {
        User teacher = getLoggedInTeacher();
        return course != null && teacher != null && course.getTeacher() != null && course.getTeacher().getId().equals(teacher.getId());
    }

    @GetMapping
    public String listLessons(@PathVariable("courseId") Long courseId, Model model, RedirectAttributes ra) {
        Course course = courseService.Get_ById(courseId);
        if (!checkOwnership(course)) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy hoặc bạn không có quyền truy cập khóa học này.");
            return "redirect:/teacher/courses";
        }
        
        List<Lesson> lessons = lessonService.Get_Lessons_By_Course(courseId);
        model.addAttribute("course", course);
        model.addAttribute("lessons", lessons);
        return "teacher/lessons/list";
    }

    @GetMapping("/create")
    public String showCreateForm(@PathVariable("courseId") Long courseId, Model model, RedirectAttributes ra) {
        Course course = courseService.Get_ById(courseId);
        if (!checkOwnership(course)) {
            ra.addFlashAttribute("errorMessage", "Không có quyền thực hiện.");
            return "redirect:/teacher/courses";
        }
        
        Lesson lesson = new Lesson();
        lesson.setCourse(course);
        model.addAttribute("lesson", lesson);
        model.addAttribute("course", course);
        model.addAttribute("isUpdate", false);
        return "teacher/lessons/form";
    }

    @PostMapping("/create")
    public String createLesson(@PathVariable("courseId") Long courseId,
                               @ModelAttribute("lesson") Lesson lesson,
                               @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                               RedirectAttributes ra) {
        Course course = courseService.Get_ById(courseId);
        if (!checkOwnership(course)) {
            ra.addFlashAttribute("errorMessage", "Không có quyền thực hiện.");
            return "redirect:/teacher/courses";
        }

        try {
            if (videoFile != null && !videoFile.isEmpty()) {
                String path = fileUploadService.Save_File(videoFile, "videos");
                lesson.setVideoUrl(path);
            }
            lesson.setCourse(course);
            lessonService.Create(lesson);
            ra.addFlashAttribute("successMessage", "Thêm bài học thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("errorMessage", "Lỗi lưu file video: " + e.getMessage());
            return "redirect:/teacher/courses/" + courseId + "/lessons/create";
        }
        return "redirect:/teacher/courses/" + courseId + "/lessons";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("courseId") Long courseId, 
                               @PathVariable("id") Long id, 
                               Model model, RedirectAttributes ra) {
        Course course = courseService.Get_ById(courseId);
        if (!checkOwnership(course)) {
            ra.addFlashAttribute("errorMessage", "Không có quyền thực hiện.");
            return "redirect:/teacher/courses";
        }

        Lesson lesson = lessonService.Get_ById(id);
        if (lesson == null || !lesson.getCourse().getId().equals(courseId)) {
            ra.addFlashAttribute("errorMessage", "Bài học không tồn tại.");
            return "redirect:/teacher/courses/" + courseId + "/lessons";
        }

        model.addAttribute("lesson", lesson);
        model.addAttribute("course", course);
        model.addAttribute("isUpdate", true);
        return "teacher/lessons/form";
    }

    @PostMapping("/edit/{id}")
    public String updateLesson(@PathVariable("courseId") Long courseId,
                               @PathVariable("id") Long id,
                               @ModelAttribute("lesson") Lesson updatedLesson,
                               @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                               RedirectAttributes ra) {
        Course course = courseService.Get_ById(courseId);
        if (!checkOwnership(course)) {
            ra.addFlashAttribute("errorMessage", "Không có quyền thực hiện.");
            return "redirect:/teacher/courses";
        }

        Lesson existing = lessonService.Get_ById(id);
        if (existing == null || !existing.getCourse().getId().equals(courseId)) {
            ra.addFlashAttribute("errorMessage", "Bài học không tồn tại.");
            return "redirect:/teacher/courses/" + courseId + "/lessons";
        }

        try {
            if (videoFile != null && !videoFile.isEmpty()) {
                String path = fileUploadService.Save_File(videoFile, "videos");
                existing.setVideoUrl(path);
            }
            existing.setTenBaiHoc(updatedLesson.getTenBaiHoc());
            existing.setNoiDung(updatedLesson.getNoiDung());
            existing.setThuTu(updatedLesson.getThuTu());
            
            lessonService.Update(existing);
            ra.addFlashAttribute("successMessage", "Cập nhật bài học thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("errorMessage", "Lỗi lưu file video: " + e.getMessage());
            return "redirect:/teacher/courses/" + courseId + "/lessons/edit/" + id;
        }
        return "redirect:/teacher/courses/" + courseId + "/lessons";
    }

    @PostMapping("/delete/{id}")
    public String deleteLesson(@PathVariable("courseId") Long courseId,
                               @PathVariable("id") Long id,
                               RedirectAttributes ra) {
        Course course = courseService.Get_ById(courseId);
        if (!checkOwnership(course)) {
            ra.addFlashAttribute("errorMessage", "Không có quyền thực hiện.");
            return "redirect:/teacher/courses";
        }

        lessonService.Delete(id);
        ra.addFlashAttribute("successMessage", "Đã xóa bài học.");
        return "redirect:/teacher/courses/" + courseId + "/lessons";
    }
}
"""

list_html = """<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Danh Sách Bài Học — EduRecommend</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet"/>
    <style>
        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
        :root {
            --bg:       #faf7f2;
            --surface:  #ffffff;
            --border:   #e4ddd3;
            --green:    #2d6a4f;
            --green2:   #40916c;
            --green-lt: #d8f3dc;
            --text:     #1c2b22;
            --muted:    #7a8c80;
            --accent:   #b7791f;
            --red:      #dc2626;
            --shadow:   0 2px 16px rgba(45,106,79,.06);
        }
        body { background: var(--bg); color: var(--text); font-family: 'Inter', sans-serif; min-height: 100vh; display: flex; }
        
        .sidebar { width: 260px; background: var(--surface); border-right: 1px solid var(--border); display: flex; flex-direction: column; padding: 30px 20px; position: fixed; top: 0; bottom: 0; left: 0; z-index: 100; }
        .logo { font-weight: 800; font-size: 1.3rem; letter-spacing: -.5px; color: var(--green); margin-bottom: 40px; text-align: center; }
        .logo em { font-style: normal; color: var(--accent); }
        .menu-title { font-size: 0.72rem; text-transform: uppercase; font-weight: 700; color: var(--muted); letter-spacing: 1px; margin-bottom: 12px; padding-left: 8px; }
        .nav-links { display: flex; flex-direction: column; gap: 6px; flex-grow: 1; }
        .nav-link { display: flex; align-items: center; gap: 12px; color: var(--text); text-decoration: none; font-size: 0.9rem; font-weight: 500; padding: 12px 16px; border-radius: 10px; transition: all .18s; }
        .nav-link:hover { background: var(--green-lt); color: var(--green); }
        .nav-link.active { background: var(--green); color: white; font-weight: 600; }
        .nav-link-logout { margin-top: auto; background: #fef2f2; color: var(--red); font-weight: 600; }
        .nav-link-logout:hover { background: var(--red); color: white; }

        .main-content { margin-left: 260px; flex: 1; padding: 40px 48px; min-height: 100vh; }
        .page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 30px; }
        .page-title { font-size: 1.6rem; font-weight: 800; letter-spacing: -.5px; color: var(--text); margin-bottom: 6px; }
        .page-sub { font-size: 0.88rem; color: var(--muted); }
        
        .btn-create { display: inline-flex; align-items: center; gap: 8px; background: var(--green); color: white; padding: 12px 20px; border-radius: 10px; font-weight: 600; font-size: 0.9rem; text-decoration: none; transition: all 0.2s; border: none; cursor: pointer; }
        .btn-create:hover { background: var(--green2); transform: translateY(-1px); box-shadow: 0 4px 12px rgba(45,106,79,.2); }
        
        .table-wrap { background: var(--surface); border: 1px solid var(--border); border-radius: 16px; overflow: hidden; box-shadow: var(--shadow); margin-top: 20px; }
        table { width: 100%; border-collapse: collapse; text-align: left; }
        th { background: rgba(216,243,220,.35); padding: 14px 20px; font-size: 0.75rem; font-weight: 700; text-transform: uppercase; color: var(--green); border-bottom: 1px solid var(--border); }
        td { padding: 16px 20px; font-size: 0.9rem; border-bottom: 1px solid var(--border); color: var(--text); vertical-align: middle; }
        tr:last-child td { border-bottom: none; }
        
        .btn-action { padding: 6px 12px; border-radius: 6px; text-decoration: none; font-weight: 600; font-size: 0.8rem; display: inline-block; border: none; cursor: pointer; }
        .btn-edit { background: #fef08a; color: #854d0e; }
        .btn-edit:hover { background: #fde047; }
        .btn-delete { background: #fecaca; color: #991b1b; }
        .btn-delete:hover { background: #fca5a5; }
        
        .empty-state { text-align: center; padding: 40px; color: var(--muted); }
        .alert { padding: 12px 20px; border-radius: 8px; margin-bottom: 20px; font-weight: 500; }
        .alert-success { background: #dcfce7; color: #166534; }
        .alert-danger { background: #fee2e2; color: #991b1b; }
        .video-link { color: var(--accent); text-decoration: underline; font-weight: 600; }
    </style>
</head>
<body>

<div class="sidebar">
    <div class="logo">Edu<em>Recommend</em></div>
    <div class="menu-title">Điều hướng</div>
    <div class="nav-links">
        <a href="/teacher" class="nav-link"><span>📊</span> Tổng quan</a>
        <a href="/teacher/courses" class="nav-link active"><span>📚</span> Khóa học của tôi</a>
        
        <div class="menu-title" style="margin-top: 20px;">Học viên</div>
        <a href="/" class="nav-link"><span>🏠</span> Về Trang chủ</a>
        <a href="/logout" class="nav-link nav-link-logout"><span>🚪</span> Đăng xuất</a>
    </div>
</div>

<div class="main-content">
    <div class="page-header">
        <div>
            <div style="margin-bottom: 10px;">
                <a href="/teacher/courses" style="color: var(--muted); text-decoration: none; font-size: 0.85rem; font-weight: 600;">← Quay lại danh sách khóa học</a>
            </div>
            <h1 class="page-title">Bài Học: <span th:text="${course.tenKhoaHoc}"></span></h1>
            <p class="page-sub">Quản lý nội dung và video bài học thuộc khóa này.</p>
        </div>
        <a th:href="@{/teacher/courses/{id}/lessons/create(id=${course.id})}" class="btn-create">
            <span>➕</span> Thêm Bài Học Mới
        </a>
    </div>

    <div th:if="${successMessage}" class="alert alert-success" th:text="${successMessage}"></div>
    <div th:if="${errorMessage}" class="alert alert-danger" th:text="${errorMessage}"></div>

    <div class="table-wrap">
        <table>
            <thead>
            <tr>
                <th width="80">Thứ Tự</th>
                <th>Tên Bài Học</th>
                <th>Video</th>
                <th width="150" style="text-align: center;">Thao Tác</th>
            </tr>
            </thead>
            <tbody>
            <tr th:if="${#lists.isEmpty(lessons)}">
                <td colspan="4" class="empty-state">Chưa có bài học nào được tạo.</td>
            </tr>
            <tr th:each="lesson : ${lessons}">
                <td th:text="${lesson.thuTu}">1</td>
                <td th:text="${lesson.tenBaiHoc}" style="font-weight: 600;">Bài 1</td>
                <td>
                    <a th:if="${lesson.videoUrl != null}" th:href="${lesson.videoUrl}" target="_blank" class="video-link">Xem Video</a>
                    <span th:if="${lesson.videoUrl == null}" style="color: var(--muted); font-size: 0.8rem; font-style: italic;">Không có video</span>
                </td>
                <td style="text-align: center;">
                    <a th:href="@{/teacher/courses/{courseId}/lessons/edit/{id}(courseId=${course.id}, id=${lesson.id})}" class="btn-action btn-edit">Sửa</a>
                    <form th:action="@{/teacher/courses/{courseId}/lessons/delete/{id}(courseId=${course.id}, id=${lesson.id})}" method="POST" style="display:inline;">
                        <button type="submit" class="btn-action btn-delete" onclick="return confirm('Bạn có chắc chắn muốn xóa bài học này không?');">Xóa</button>
                    </form>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</div>

</body>
</html>
"""

form_html = """<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title th:text="${isUpdate} ? 'Cập Nhật Bài Học — EduRecommend' : 'Thêm Bài Học Mới — EduRecommend'">Bài Học</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet"/>
    <style>
        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
        :root {
            --bg:       #faf7f2;
            --surface:  #ffffff;
            --border:   #e4ddd3;
            --green:    #2d6a4f;
            --green2:   #40916c;
            --text:     #1c2b22;
            --muted:    #7a8c80;
            --shadow:   0 10px 30px rgba(45,106,79,.08);
        }
        body { background: var(--bg); color: var(--text); font-family: 'Inter', sans-serif; min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 40px 20px; }
        .form-container { background: var(--surface); border: 1px solid var(--border); border-radius: 20px; width: 100%; max-width: 500px; padding: 40px 30px; box-shadow: var(--shadow); }
        .title { font-size: 1.3rem; font-weight: 800; color: var(--text); margin-bottom: 24px; text-align: center; letter-spacing: -0.5px; }
        .form-group { display: flex; flex-direction: column; gap: 6px; margin-bottom: 18px; }
        .form-group label { font-size: 0.75rem; font-weight: 700; color: var(--muted); text-transform: uppercase; letter-spacing: 0.5px; }
        .form-group input, .form-group textarea { background: var(--bg); border: 1.5px solid var(--border); border-radius: 10px; padding: 12px 14px; font-family: 'Inter', sans-serif; font-size: 0.9rem; color: var(--text); outline: none; transition: border-color 0.18s; }
        .form-group input[type="file"] { background: transparent; border: none; padding: 4px 0; cursor: pointer; }
        .form-group textarea { resize: vertical; min-height: 100px; }
        .form-group input:focus, .form-group textarea:focus { border-color: var(--green2); background: var(--surface); }
        .btn-group { display: flex; gap: 10px; margin-top: 10px; }
        .btn { flex: 1; padding: 13px; border-radius: 11px; font-size: 0.95rem; font-weight: 700; cursor: pointer; font-family: 'Inter', sans-serif; transition: all 0.18s; border: none; text-align: center; text-decoration: none; }
        .btn-submit { background: var(--green); color: white; box-shadow: 0 4px 16px rgba(45,106,79,0.2); }
        .btn-submit:hover { background: var(--green2); }
        .btn-cancel { background: var(--border); color: var(--text); }
        .btn-cancel:hover { background: #d7cdbe; }
        .alert { padding: 12px 20px; border-radius: 8px; margin-bottom: 20px; font-weight: 500; }
        .alert-success { background: #dcfce7; color: #166534; }
        .alert-danger { background: #fee2e2; color: #991b1b; }
    </style>
</head>
<body>

<div class="form-container">
    <h1 class="title" th:text="${isUpdate} ? 'Cập Nhật Bài Học' : 'Thêm Bài Học Mới'">Bài Học</h1>
    <div style="text-align: center; margin-bottom: 20px; font-size: 0.85rem; color: var(--muted);">
        Khóa học: <strong th:text="${course.tenKhoaHoc}"></strong>
    </div>

    <div th:if="${successMessage}" class="alert alert-success" th:text="${successMessage}"></div>
    <div th:if="${errorMessage}" class="alert alert-danger" th:text="${errorMessage}"></div>

    <form th:action="${isUpdate} ? @{/teacher/courses/{courseId}/lessons/edit/{id}(courseId=${course.id}, id=${lesson.id})} : @{/teacher/courses/{courseId}/lessons/create(courseId=${course.id})}" 
          th:object="${lesson}" method="POST" enctype="multipart/form-data">
        
        <input type="hidden" th:field="*{id}" th:if="${isUpdate}"/>
        <input type="hidden" name="course.id" th:value="${course.id}"/>

        <div class="form-group">
            <label for="tenBaiHoc">Tên Bài Học *</label>
            <input type="text" id="tenBaiHoc" th:field="*{tenBaiHoc}" placeholder="Ví dụ: Bài 1: Cài đặt môi trường" required autocomplete="off"/>
        </div>

        <div class="form-group">
            <label for="thuTu">Thứ Tự (Số) *</label>
            <input type="number" id="thuTu" th:field="*{thuTu}" placeholder="Ví dụ: 1" min="1" required/>
        </div>

        <div class="form-group">
            <label for="videoFile">Video Bài Giảng</label>
            <input type="file" id="videoFile" name="videoFile" accept="video/*" />
            <div th:if="${isUpdate && lesson.videoUrl != null}" style="font-size: 0.8rem; color: var(--muted); margin-top: 4px;">
                Đã có video: <a th:href="${lesson.videoUrl}" target="_blank" style="color: var(--green);">Xem thử</a>
            </div>
        </div>

        <div class="form-group">
            <label for="noiDung">Nội Dung / Mô tả</label>
            <textarea id="noiDung" th:field="*{noiDung}" placeholder="Ghi chú bài học..."></textarea>
        </div>

        <div class="btn-group">
            <a th:href="@{/teacher/courses/{id}/lessons(id=${course.id})}" class="btn btn-cancel">Hủy Bỏ</a>
            <button type="submit" class="btn btn-submit">Lưu Lại</button>
        </div>
    </form>
</div>

</body>
</html>
"""

write_file(r'd:\NHOM3\DoAnKPDL\doan\src\main\java\com\example\doan\Controller\TeacherLessonController.java', controller_code)
write_file(r'd:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\teacher\lessons\list.html', list_html)
write_file(r'd:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\teacher\lessons\form.html', form_html)
