package com.example.doan.Model.recommend;
import com.example.doan.Model.course.CourseResult;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendResponse {

    private boolean success;
    private String error;
    private Data data;

    @JsonProperty("grade")
    private String grade;

    @JsonProperty("predicted_score")
    private Integer predictedScore;

    @JsonProperty("recommendations")
    private List<Recommendation> recommendations;

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public Integer getPredictedScore() { return predictedScore; }
    public void setPredictedScore(Integer predictedScore) { this.predictedScore = predictedScore; }
    public List<Recommendation> getRecommendations() { return recommendations; }
    public void setRecommendations(List<Recommendation> recommendations) { this.recommendations = recommendations; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Recommendation {
        @JsonProperty("course_name")
        private String courseName;
        @JsonProperty("category")
        private String category;
        @JsonProperty("difficulty")
        private String difficulty;
        @JsonProperty("rating")
        private Double rating;
        @JsonProperty("url")
        private String url;
        @JsonProperty("match_score")
        private Double matchScore;
        @JsonProperty("matched_skills")
        private List<String> matchedSkills;

        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public Double getMatchScore() { return matchScore; }
        public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }
        public List<String> getMatchedSkills() { return matchedSkills; }
        public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }
    }

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
