package com.example.doan.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * File: Model/RecommendResponse.java
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendResponse {

    private boolean success;
    private String error;
    private Data data;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {

        @JsonProperty("nhom_sinh_vien")
        private String nhomSinhVien;

        @JsonProperty("nhom_id")
        private int nhomId;

        @JsonProperty("do_kho_phu_hop")
        private List<String> doKhoPhuHop;

        @JsonProperty("ky_nang_dau_vao")
        private List<String> kyNangDauVao;

        @JsonProperty("ky_nang_mo_rong")
        private List<String> kyNangMoRong;

        @JsonProperty("khoa_hoc_goi_y")
        private List<CourseResult> khoaHocGoiY;

        public String getNhomSinhVien() { return nhomSinhVien; }
        public void setNhomSinhVien(String nhomSinhVien) { this.nhomSinhVien = nhomSinhVien; }
        public int getNhomId() { return nhomId; }
        public void setNhomId(int nhomId) { this.nhomId = nhomId; }
        public List<String> getDoKhoPhuHop() { return doKhoPhuHop; }
        public void setDoKhoPhuHop(List<String> doKhoPhuHop) { this.doKhoPhuHop = doKhoPhuHop; }
        public List<String> getKyNangDauVao() { return kyNangDauVao; }
        public void setKyNangDauVao(List<String> kyNangDauVao) { this.kyNangDauVao = kyNangDauVao; }
        public List<String> getKyNangMoRong() { return kyNangMoRong; }
        public void setKyNangMoRong(List<String> kyNangMoRong) { this.kyNangMoRong = kyNangMoRong; }
        public List<CourseResult> getKhoaHocGoiY() { return khoaHocGoiY; }
        public void setKhoaHocGoiY(List<CourseResult> khoaHocGoiY) { this.khoaHocGoiY = khoaHocGoiY; }
    }
}
