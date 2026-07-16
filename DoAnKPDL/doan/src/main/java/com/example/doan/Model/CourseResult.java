package com.example.doan.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * File: Model/CourseResult.java
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseResult {

    @JsonProperty("ten_khoa_hoc")
    private String tenKhoaHoc;

    @JsonProperty("to_chuc")
    private String toChuc;

    @JsonProperty("cap_do")
    private String capDo;

    @JsonProperty("diem_danh_gia")
    private Double diemDanhGia;

    @JsonProperty("loai_chung_chi")
    private String loaiChungChi;

    @JsonProperty("duong_dan")
    private String duongDan;

    @JsonProperty("thoi_gian_hoc")
    private String thoiGianHoc;

    @JsonProperty("ky_nang_khop")
    private List<String> kyNangKhop;

    @JsonProperty("skill_match")
    private int skillMatch;

    public String getTenKhoaHoc() { return tenKhoaHoc; }
    public void setTenKhoaHoc(String tenKhoaHoc) { this.tenKhoaHoc = tenKhoaHoc; }
    public String getToChuc() { return toChuc; }
    public void setToChuc(String toChuc) { this.toChuc = toChuc; }
    public String getCapDo() { return capDo; }
    public void setCapDo(String capDo) { this.capDo = capDo; }
    public Double getDiemDanhGia() { return diemDanhGia; }
    public void setDiemDanhGia(Double diemDanhGia) { this.diemDanhGia = diemDanhGia; }
    public String getLoaiChungChi() { return loaiChungChi; }
    public void setLoaiChungChi(String loaiChungChi) { this.loaiChungChi = loaiChungChi; }
    public String getDuongDan() { return duongDan; }
    public void setDuongDan(String duongDan) { this.duongDan = duongDan; }
    public String getThoiGianHoc() { return thoiGianHoc; }
    public void setThoiGianHoc(String thoiGianHoc) { this.thoiGianHoc = thoiGianHoc; }
    public List<String> getKyNangKhop() { return kyNangKhop; }
    public void setKyNangKhop(List<String> kyNangKhop) { this.kyNangKhop = kyNangKhop; }
    public int getSkillMatch() { return skillMatch; }
    public void setSkillMatch(int skillMatch) { this.skillMatch = skillMatch; }
}
