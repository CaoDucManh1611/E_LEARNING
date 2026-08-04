package com.example.doan.Controller.api;

import com.example.doan.Model.course.Category;
import com.example.doan.Model.course.Course;
import com.example.doan.Model.course.Lesson;
import com.example.doan.Model.enrollment.Certificate;
import com.example.doan.Model.enrollment.Enrollment;
import com.example.doan.Model.enrollment.LessonProgress;
import com.example.doan.Model.notification.Notification;
import com.example.doan.Model.order.Coupon;
import com.example.doan.Model.order.InstructorEarning;
import com.example.doan.Model.order.Invoice;
import com.example.doan.Model.order.Order;
import com.example.doan.Model.order.OrderItem;
import com.example.doan.Model.order.Payment;
import com.example.doan.Model.refund.RefundRequest;
import com.example.doan.Model.review.Review;
import com.example.doan.Model.review.LessonComment;
import com.example.doan.Model.user.User;
import com.example.doan.Model.user.StudentInfo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Api_Mapper {

    private Api_Mapper() {
    }

    public static Map<String, Object> ok(String message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("success", true);
        map.put("message", message);
        return map;
    }

    public static Map<String, Object> error(String message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("success", false);
        map.put("message", message);
        return map;
    }

    public static Map<String, Object> user(User u) {
        if (u == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", u.getId());
        map.put("hoTen", u.getHoTen());
        map.put("email", u.getEmail());
        map.put("role", u.getRole());
        map.put("soDienThoai", u.getSoDienThoai());
        map.put("isLocked", u.isLocked());
        map.put("createdAt", u.getCreatedAt());
        return map;
    }

    public static Map<String, Object> userMini(User u) {
        if (u == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", u.getId());
        map.put("hoTen", u.getHoTen());
        map.put("role", u.getRole());
        return map;
    }

    public static Map<String, Object> studentInfo(StudentInfo s) {
        if (s == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", s.getId());
        map.put("hoTen", s.getHoTen());
        map.put("email", s.getEmail());
        map.put("soDienThoai", s.getSoDienThoai());
        map.put("khoaHocQuan", s.getKhoaHocQuan());
        map.put("urlKhoaHoc", s.getUrlKhoaHoc());
        map.put("thoiGianDangKy", s.getThoiGianDangKy());
        return map;
    }

    public static Map<String, Object> category(Category c) {
        if (c == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        map.put("tenDanhMuc", c.getTenDanhMuc());
        return map;
    }

    public static Map<String, Object> course(Course c) {
        if (c == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        map.put("tenKhoaHoc", c.getTenKhoaHoc());
        map.put("moTa", c.getMoTa());
        map.put("gia", c.getGia());
        map.put("capDo", c.getCapDo());
        map.put("hinhAnh", c.getHinhAnh());
        map.put("trangThai", c.getTrangThai());
        map.put("commissionRate", c.getCommissionRate());
        map.put("averageStars", c.getAverageStars());
        map.put("reviewCount", c.getReviewCount());
        map.put("createdAt", c.getCreatedAt());
        map.put("category", category(c.getCategory()));
        map.put("teacher", userMini(c.getTeacher()));
        return map;
    }

    public static Map<String, Object> lesson(Lesson l) {
        if (l == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", l.getId());
        map.put("courseId", l.getCourse() != null ? l.getCourse().getId() : null);
        map.put("tieuDe", l.getTieuDe());
        map.put("videoUrl", l.getVideoUrl());
        map.put("thuTu", l.getThuTu());
        map.put("thoiLuongPhut", l.getThoiLuongPhut());
        return map;
    }

    public static Map<String, Object> coupon(Coupon c) {
        if (c == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        map.put("maCode", c.getMaCode());
        map.put("loaiGiam", c.getLoaiGiam());
        map.put("giaTri", c.getGiaTri());
        map.put("soLuong", c.getSoLuong());
        map.put("daDung", c.getDaDung());
        map.put("ngayHetHan", c.getNgayHetHan());
        map.put("createdAt", c.getCreatedAt());
        return map;
    }

    public static Map<String, Object> order(Order o, List<OrderItem> items) {
        if (o == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", o.getId());
        map.put("user", userMini(o.getUser()));
        map.put("coupon", coupon(o.getCoupon()));
        map.put("tongTien", o.getTongTien());
        map.put("trangThai", o.getTrangThai());
        map.put("createdAt", o.getCreatedAt());
        map.put("items", items != null ? items.stream().map(Api_Mapper::orderItem).toList() : List.of());
        return map;
    }

    public static Map<String, Object> orderItem(OrderItem i) {
        if (i == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", i.getId());
        map.put("orderId", i.getOrder() != null ? i.getOrder().getId() : null);
        map.put("course", course(i.getCourse()));
        map.put("gia", i.getGia());
        return map;
    }

    public static Map<String, Object> invoice(Invoice i) {
        if (i == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", i.getId());
        map.put("orderId", i.getOrder() != null ? i.getOrder().getId() : null);
        map.put("soHoaDon", i.getSoHoaDon());
        map.put("ngayXuat", i.getNgayXuat());
        return map;
    }

    public static Map<String, Object> review(Review r) {
        if (r == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", r.getId());
        map.put("user", userMini(r.getUser()));
        map.put("course", course(r.getCourse()));
        map.put("soSao", r.getSoSao());
        map.put("noiDung", r.getNoiDung());
        map.put("trangThai", r.getTrangThai());
        map.put("createdAt", r.getCreatedAt());
        return map;
    }

    public static Map<String, Object> enrollment(Enrollment e) {
        if (e == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", e.getId());
        map.put("user", userMini(e.getUser()));
        map.put("course", course(e.getCourse()));
        map.put("tienDoPercent", e.getTienDoPercent());
        map.put("trangThai", e.getTrangThai());
        map.put("ngayDangKy", e.getNgayDangKy());
        map.put("ngayHoanThanh", e.getNgayHoanThanh());
        return map;
    }

    public static Map<String, Object> progress(LessonProgress p) {
        if (p == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("enrollmentId", p.getEnrollment() != null ? p.getEnrollment().getId() : null);
        map.put("lesson", lesson(p.getLesson()));
        map.put("hoanThanh", p.isHoanThanh());
        map.put("hoanThanhAt", p.getHoanThanhAt());
        return map;
    }

    public static Map<String, Object> certificate(Certificate c) {
        if (c == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        map.put("enrollment", enrollment(c.getEnrollment()));
        map.put("maXacThuc", c.getMaXacThuc());
        map.put("ngayCap", c.getNgayCap());
        return map;
    }

    public static Map<String, Object> lessonComment(LessonComment c) {
        if (c == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        map.put("noiDung", c.getNoiDung());
        map.put("createdAt", c.getCreatedAt());
        map.put("user", userMini(c.getUser()));
        map.put("replies", c.getReplies() != null
                ? c.getReplies().stream().map(Api_Mapper::lessonComment).toList()
                : List.of());
        return map;
    }

    public static Map<String, Object> notification(Notification n) {
        if (n == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", n.getId());
        map.put("user", userMini(n.getUser()));
        map.put("sender", userMini(n.getSender()));
        map.put("tieuDe", n.getTieuDe());
        map.put("noiDung", n.getNoiDung());
        map.put("url", n.getUrl());
        map.put("daDoc", n.isDaDoc());
        map.put("createdAt", n.getCreatedAt());
        return map;
    }

    public static Map<String, Object> refund(RefundRequest r, List<OrderItem> items) {
        if (r == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", r.getId());
        map.put("order", order(r.getOrder(), items));
        map.put("lyDo", r.getLyDo());
        map.put("trangThai", r.getTrangThai());
        map.put("createdAt", r.getCreatedAt());
        map.put("xuLyAt", r.getXuLyAt());
        return map;
    }

    public static Map<String, Object> payment(Payment p) {
        if (p == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("orderId", p.getOrder() != null ? p.getOrder().getId() : null);
        map.put("phuongThuc", p.getPhuongThuc());
        map.put("trangThai", p.getTrangThai());
        map.put("maGiaoDich", p.getMaGiaoDich());
        map.put("soTien", p.getSoTien());
        map.put("payDate", p.getPayDate());
        return map;
    }

    public static Map<String, Object> earning(InstructorEarning e) {
        if (e == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", e.getId());
        map.put("teacher", userMini(e.getTeacher()));
        map.put("orderItem", orderItem(e.getOrderItem()));
        map.put("tongTien", e.getTongTien());
        map.put("tienNhan", e.getTienNhan());
        map.put("thoiGian", e.getThoiGian());
        return map;
    }
}
