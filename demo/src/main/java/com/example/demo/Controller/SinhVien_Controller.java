package com.example.demo.Controller;


import com.example.demo.Model.SinhVien;
import com.example.demo.Service.SinhVien_Service;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class SinhVien_Controller {
    private final SinhVien_Service svsv;
    public SinhVien_Controller(SinhVien_Service svsv)
    {
        this.svsv = svsv;
    }
    @GetMapping("/Home")
    public String Get_DSSV (Model model)
    {
        List<SinhVien> sv = svsv.Get_SinhVien();
        model.addAttribute("SinhVien", sv);
        return "/Home";
    }
    @GetMapping("/Create")
    public String Get_Create (Model model)
    {
        model.addAttribute("SinhVien", new SinhVien());
        return "/Create";
    }
    @PostMapping("/Create")
    public String Post_Create (@ModelAttribute SinhVien sv, Model model, BindingResult bindingResult)
    {
        SinhVien svm = svsv.Create(sv);
        if (svm != null)
        {
            bindingResult.rejectValue("email", "error.sinhVien", "Email nay da ton tai!");
        }

        model.addAttribute("SinhVien", svm);
        return "redirect:/Home";
    }
    @GetMapping("/Update/user/{id}")
    public String Get_Update (@PathVariable int id,  Model model)
    {
        SinhVien svm = svsv.Get_Id(id);
        model.addAttribute("SinhVien", svm);
        return "/Update";
    }
    @PostMapping("/Update")
    public String Post_Update (@ModelAttribute SinhVien sv, Model model)
    {
        SinhVien svm = svsv.Update_SinhVien(sv);
        model.addAttribute("SinhVien", svm);
        return "redirect:/Home";
    }
    @PostMapping("/Delete/user/{id}")
    public String Post_Delete (@PathVariable int id,  Model model)
    {
        svsv.Delete(id);
        return "redirect:/Home";
    }
    @GetMapping("TimKiem")
    public String Get_TimKiem (Model model)
    {
        return "TimKiem";
    }
    @PostMapping("TimKiem")
    public String Post_TimKiem (String name, Model model )
    {
        SinhVien sv = svsv.TimKiem(name);
        model.addAttribute("SinhVien", sv);
        return "Timkiem2";
    }
    /////Spring Sercurity
    @GetMapping("login")
    public  String Login ()
    {
        return "auth/login";
    }

    @GetMapping("/register")
    public String Get_Register (Model model)
    {
        model.addAttribute("SinhVien", new SinhVien());
        return "auth/register";
    }
    @PostMapping("/register")
    public String Post_Register (@ModelAttribute SinhVien sv, BindingResult bindingResult, Model model)
    {
        SinhVien svm = svsv.DangKy(sv);
        if (svm == null)
        {
            bindingResult.rejectValue("email", "error.sinhVien", "Email nay da ton tai!");
            model.addAttribute("SinhVien", svm);
            return "auth/register";
        }

        return "redirect:/login";

    }


    @GetMapping("/findByName")
    public String findByName (Model model)
    {
        return "testFindByName";

    }
    @PostMapping("/findByName")
    public String findByName (String name, Model model)

    {
        SinhVien sv = svsv.FindByName(name);
        model.addAttribute("us", sv);
        return "testFindByName";

    }


    //findByName



}
