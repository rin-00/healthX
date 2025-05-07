package com.healthx.model;

import com.google.gson.annotations.SerializedName;
import org.threeten.bp.LocalDateTime;

/**
 * 用于与服务器通信的DTO类
 */
public class SleepRecordDTO {
    
    @SerializedName("id")
    private Long id;
    
    @SerializedName("userId")
    private Long userId;
    
    @SerializedName("startTime")
    private LocalDateTime startTime;
    
    @SerializedName("endTime")
    private LocalDateTime endTime;
    
    @SerializedName("duration")
    private Integer duration;
    
    @SerializedName("createdAt")
    private LocalDateTime createdAt;
    
    // 无参构造函数
    public SleepRecordDTO() {
    }
    
    // 从SleepRecord转换的构造函数
    public SleepRecordDTO(SleepRecord sleepRecord) {
        this.id = sleepRecord.getRemoteId();
        this.userId = sleepRecord.getUserId();
        this.startTime = sleepRecord.getStartTime();
        this.endTime = sleepRecord.getEndTime();
        this.duration = sleepRecord.getDuration();
        this.createdAt = sleepRecord.getCreatedAt();
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
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // 转换为实体对象
    public SleepRecord toEntity() {
        SleepRecord entity = new SleepRecord();
        entity.setRemoteId(this.id);
        entity.setUserId(this.userId);
        entity.setStartTime(this.startTime);
        entity.setEndTime(this.endTime);
        entity.setDuration(this.duration);
        entity.setCreatedAt(this.createdAt);
        entity.setSyncStatus(1); // 已同步
        return entity;
    }
} 