package com.healthx.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 步数记录数据传输对象
 */
public class StepRecordDTO {
    
    private Long id;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("stepCount")
    private Integer stepCount;
    
    @JsonProperty("distance")
    private BigDecimal distance;
    
    @JsonProperty("caloriesBurned")
    private BigDecimal caloriesBurned;
    
    @JsonProperty("recordDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recordDate;
    
    @JsonProperty("source")
    private String source;
    
    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;
    
    @JsonProperty("remoteId")
    private Long remoteId;
    
    @JsonProperty("syncStatus")
    private Integer syncStatus;
    
    // 构造方法
    public StepRecordDTO() {
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
    
    public Integer getStepCount() {
        return stepCount;
    }
    
    public void setStepCount(Integer stepCount) {
        this.stepCount = stepCount;
    }
    
    public BigDecimal getDistance() {
        return distance;
    }
    
    public void setDistance(BigDecimal distance) {
        this.distance = distance;
    }
    
    public BigDecimal getCaloriesBurned() {
        return caloriesBurned;
    }
    
    public void setCaloriesBurned(BigDecimal caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }
    
    public LocalDate getRecordDate() {
        return recordDate;
    }
    
    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
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
    
    public Long getRemoteId() {
        return remoteId;
    }
    
    public void setRemoteId(Long remoteId) {
        this.remoteId = remoteId;
    }
    
    public Integer getSyncStatus() {
        return syncStatus;
    }
    
    public void setSyncStatus(Integer syncStatus) {
        this.syncStatus = syncStatus;
    }
} 