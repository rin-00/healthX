package com.healthx.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Index;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 步数目标实体类
 */
@Entity(tableName = "step_goals",
        indices = {
                @Index(value = {"user_id"})
        })
public class StepGoal {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "target_steps")
    private int targetSteps;

    @ColumnInfo(name = "start_date")
    private LocalDate startDate;

    @ColumnInfo(name = "end_date")
    private LocalDate endDate;

    @ColumnInfo(name = "is_active")
    private boolean isActive;

    @ColumnInfo(name = "created_at")
    private LocalDateTime createdAt;

    @ColumnInfo(name = "updated_at")
    private LocalDateTime updatedAt;

    @ColumnInfo(name = "remote_id")
    private Long remoteId;

    @ColumnInfo(name = "sync_status")
    private int syncStatus; // 0-未同步，1-已同步，2-同步失败

    // 构造方法
    public StepGoal() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
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

    public int getTargetSteps() {
        return targetSteps;
    }

    public void setTargetSteps(int targetSteps) {
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
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

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }
} 