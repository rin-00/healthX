package com.healthx.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 步数统计数据传输对象
 */
public class StepStatisticsDTO {
    
    private Long id;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("statisticType")
    private String statisticType; // WEEKLY/MONTHLY
    
    @JsonProperty("year")
    private Integer year;
    
    @JsonProperty("period")
    private Integer period; // 周数(1-53)或月份(1-12)
    
    @JsonProperty("totalSteps")
    private Integer totalSteps;
    
    @JsonProperty("avgSteps")
    private BigDecimal avgSteps;
    
    @JsonProperty("maxSteps")
    private Integer maxSteps;
    
    @JsonProperty("minSteps")
    private Integer minSteps;
    
    @JsonProperty("recordedDays")
    private Integer recordedDays;
    
    @JsonProperty("startDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonProperty("endDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;
    
    // 构造方法
    public StepStatisticsDTO() {
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getStatisticType() {
        return statisticType;
    }
    
    public void setStatisticType(String statisticType) {
        this.statisticType = statisticType;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public Integer getPeriod() {
        return period;
    }
    
    public void setPeriod(Integer period) {
        this.period = period;
    }
    
    public Integer getTotalSteps() {
        return totalSteps;
    }
    
    public void setTotalSteps(Integer totalSteps) {
        this.totalSteps = totalSteps;
    }
    
    public BigDecimal getAvgSteps() {
        return avgSteps;
    }
    
    public void setAvgSteps(BigDecimal avgSteps) {
        this.avgSteps = avgSteps;
    }
    
    public Integer getMaxSteps() {
        return maxSteps;
    }
    
    public void setMaxSteps(Integer maxSteps) {
        this.maxSteps = maxSteps;
    }
    
    public Integer getMinSteps() {
        return minSteps;
    }
    
    public void setMinSteps(Integer minSteps) {
        this.minSteps = minSteps;
    }
    
    public Integer getRecordedDays() {
        return recordedDays;
    }
    
    public void setRecordedDays(Integer recordedDays) {
        this.recordedDays = recordedDays;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
} 