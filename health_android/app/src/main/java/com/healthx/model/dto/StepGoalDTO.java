package com.healthx.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 步数目标数据传输对象
 */
public class StepGoalDTO {
    
    private Long id;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("targetSteps")
    private Integer targetSteps;
    
    @JsonProperty("startDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonProperty("endDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    @JsonProperty("isActive")
    private Boolean isActive;
    
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
    public StepGoalDTO() {
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
    
    public Integer getTargetSteps() {
        return targetSteps;
    }
    
    public void setTargetSteps(Integer targetSteps) {
        this.targetSteps = targetSteps;
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
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean active) {
        isActive = active;
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