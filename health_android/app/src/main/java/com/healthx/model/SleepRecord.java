package com.healthx.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;
import com.healthx.database.converter.DateTimeConverter;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;

import java.io.Serializable;

@Entity(tableName = "sleep_records")
@TypeConverters(DateTimeConverter.class)
public class SleepRecord implements Serializable {
    
    @PrimaryKey(autoGenerate = true)
    @SerializedName("localId")
    private long id;
    
    private long userId;
    
    @NonNull
    @SerializedName("startTime")
    private LocalDateTime startTime; // 睡眠开始时间
    
    @NonNull
    @SerializedName("endTime")
    private LocalDateTime endTime; // 睡眠结束时间
    
    @SerializedName("duration")
    private int duration; // 睡眠时长（分钟）
    
    @NonNull
    @SerializedName("createdAt")
    private LocalDateTime createdAt; // 创建时间
    
    // 远程ID
    @SerializedName("id")
    private Long remoteId;
    
    // 同步状态：0-未同步，1-已同步，2-需要更新，3-需要删除
    private int syncStatus;
    
    // 默认构造函数
    public SleepRecord() {
        this.createdAt = LocalDateTime.now();
        this.syncStatus = 0;
    }
    
    // 带参数的构造函数
    @Ignore
    public SleepRecord(long userId, @NonNull LocalDateTime startTime, @NonNull LocalDateTime endTime) {
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.calculateDuration();
        this.createdAt = LocalDateTime.now();
        this.syncStatus = 0;
    }
    
    // 计算睡眠时长（分钟）
    private void calculateDuration() {
        if (startTime != null && endTime != null) {
            long minutes = Duration.between(startTime, endTime).toMinutes();
            this.duration = (int) minutes;
        }
    }
    
    // Getters and Setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    @NonNull
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(@NonNull LocalDateTime startTime) {
        this.startTime = startTime;
        calculateDuration();
    }
    
    @NonNull
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(@NonNull LocalDateTime endTime) {
        this.endTime = endTime;
        calculateDuration();
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    @NonNull
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(@NonNull LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Long getRemoteId() {
        return remoteId;
    }
    
    public void setRemoteId(Long remoteId) {
        this.remoteId = remoteId;
    }
    
    public int getSyncStatus() {
        return syncStatus;
    }
    
    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }
} 