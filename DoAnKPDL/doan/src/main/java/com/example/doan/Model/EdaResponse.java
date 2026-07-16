package com.example.doan.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

/**
 * File: Model/EdaResponse.java
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdaResponse {

    private boolean success;
    private String  dataset;
    private String  error;
    private Data    data;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean v) { this.success = v; }
    public String getDataset() { return dataset; }
    public void setDataset(String v) { this.dataset = v; }
    public String getError() { return error; }
    public void setError(String v) { this.error = v; }
    public Data getData() { return data; }
    public void setData(Data v) { this.data = v; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        private Map<String, Object> stats;
        private List<Chart>         charts;

        public Map<String, Object> getStats()  { return stats; }
        public void setStats(Map<String, Object> v) { this.stats = v; }
        public List<Chart> getCharts()  { return charts; }
        public void setCharts(List<Chart> v) { this.charts = v; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Chart {
        private String title;
        private String image;

        public String getTitle() { return title; }
        public void setTitle(String v) { this.title = v; }
        public String getImage() { return image; }
        public void setImage(String v) { this.image = v; }
    }
}
