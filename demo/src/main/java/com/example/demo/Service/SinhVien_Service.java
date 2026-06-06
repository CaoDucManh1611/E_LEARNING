package com.example.demo.Service;


import com.example.demo.Model.SinhVien;
import com.example.demo.Repository.SinhVienRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SinhVien_Service {
    private final SinhVienRepository svrepo;
    private final PasswordEncoder passwordEncoder;
    public SinhVien_Service(SinhVienRepository svrepo, PasswordEncoder passwordEncoder)
    {
        this.svrepo = svrepo;
        this.passwordEncoder = passwordEncoder;
    }

    public List<SinhVien>Get_SinhVien ()
    {
        return svrepo.findAll();
    }
    public SinhVien Create (SinhVien sv)
    {
        SinhVien ktr_email  =  svrepo.findByEmail(sv.getEmail());
        if (ktr_email == null)
        {
            return null;
        }
        sv.setPassword(passwordEncoder.encode(sv.getPassword()));
        return svrepo.save(sv);
    }
    public SinhVien Get_Id (int id)
    {
        return svrepo.findById(id).get();
    }
    public SinhVien Update_SinhVien(SinhVien svm) {
        SinhVien svcu = Get_Id(svm.getId());

        if (svcu != null) {
            svcu.setTensv(svm.getTensv());
            svcu.setEmail(svm.getEmail());
            svcu.setAddress(svm.getAddress());
            svcu.setPassword(passwordEncoder.encode(svm.getPassword()));
            svcu.setRole(svm.getRole());
            return svrepo.save(svcu);
        }
        return null;
    }
    public void Delete (int id)
    {
        svrepo.deleteById(id);
    }
    public SinhVien TimKiem (String name)
    {
        SinhVien svtk = Get_SinhVien().stream().filter(st -> st.getTensv().equals(name)).findFirst().orElse(null);
        if (svtk == null)
        {
            System.out.printf("Khong tim thay");
        }
        return svtk;
    }

    public SinhVien FindByName (String name)
    {
        return svrepo.findByTensv(name);
    }

    public SinhVien FindUserByEmail (String email)
    {
        return svrepo.findByEmail(email);
    }
    /////Spring sercurity
    public SinhVien DangKy (SinhVien sv)
    {
        SinhVien ktr = svrepo.findByEmail(sv.getEmail());
        if (ktr != null)
        {
            return null;
        }
        sv.setPassword(passwordEncoder.encode(sv.getPassword()));
        return svrepo.save(sv);
    }
}

