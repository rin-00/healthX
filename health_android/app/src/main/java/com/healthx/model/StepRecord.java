package com.healthx.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Index;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 步数记录实体类
 */
@Entity(tableName = "step_records",
        indices = {
                @Index(value = {"user_id", "record_date"}, unique = true)
        })
public class StepRecord {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "step_count")
    private int stepCount;

    @ColumnInfo(name = "distance")
    private BigDecimal distance;

    @ColumnInfo(name = "calories_burned")
    private BigDecimal caloriesBurned;

    @ColumnInfo(name = "record_date")
    private LocalDate recordDate;

    @ColumnInfo(name = "source")
    private String source;

    @ColumnInfo(name = "created_at")
    private LocalDateTime createdAt;

    @ColumnInfo(name = "updated_at")
    private LocalDateTime updatedAt;

    @ColumnInfo(name = "remote_id")
    private Long remoteId;

    @ColumnInfo(name = "sync_status")
    private int syncStatus; // 0-未同步，1-已同步，2-同步失败

    // 构造方法
    public StepRecord() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.syncStatus = 0;
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

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
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

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }
} 