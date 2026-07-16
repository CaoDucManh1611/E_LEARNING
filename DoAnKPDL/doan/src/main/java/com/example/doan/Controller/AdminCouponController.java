package com.example.doan.Controller;

import com.example.doan.Model.Coupon;
import com.example.doan.Service.Coupon_Service;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller phục vụ Admin quản trị CRUD Mã giảm giá (Coupons).
 * File: Controller/AdminCouponController.java
 */
@Controller
@RequestMapping("/admin/coupons")
public class AdminCouponController {

    private final Coupon_Service couponService;

    public AdminCouponController(Coupon_Service couponService) {
        this.couponService = couponService;
    }

    // 1. Hiển thị danh sách mã giảm giá
    @GetMapping
    public String list(Model model) {
        List<Coupon> coupons = couponService.Get_All_Coupons();
        model.addAttribute("coupons", coupons);
        return "admin/coupons/list";
    }

    // 2. Form thêm mới
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("coupon", new Coupon());
        return "admin/coupons/form";
    }

    // 3. Form chỉnh sửa
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Coupon coupon = couponService.Get_ById(id);
        if (coupon == null) {
            return "redirect:/admin/coupons";
        }
        model.addAttribute("coupon", coupon);
        return "admin/coupons/form";
    }

    // 4. Lưu mã giảm giá (Thêm mới/Cập nhật)
    @PostMapping("/save")
    public String save(@ModelAttribute("coupon") Coupon coupon) {
        // Chuẩn hóa mã viết hoa
        if (coupon.getMaCode() != null) {
            coupon.setMaCode(coupon.getMaCode().trim().toUpperCase());
        }
        couponService.Save_Coupon(coupon);
        return "redirect:/admin/coupons";
    }

    // 5. Xóa mã giảm giá
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        couponService.Delete_Coupon(id);
        return "redirect:/admin/coupons";
    }
}
