package com.healthx.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;
import com.healthx.database.converter.DateTimeConverter;

import java.io.Serializable;
import org.threeten.bp.LocalDateTime;

@Entity(tableName = "exercises")
@TypeConverters(DateTimeConverter.class)
public class Exercise implements Serializable {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private long userId;
    
    @NonNull
    @SerializedName("exerciseName")
    private String exerciseName;
    
    @SerializedName("duration")
    private int duration; // 运动时长（分钟）
    
    @SerializedName("caloriesBurned")
    private double caloriesBurned; // 消耗的卡路里
    
    @SerializedName("exerciseType")
    private String exerciseType; // 运动类型（如：有氧、力量、柔韧性等）
    
    @SerializedName("intensity")
    private String intensity; // 运动强度（低、中、高）
    
    @NonNull
    @SerializedName("exercisedAt")
    private LocalDateTime exercisedAt; // 运动时间
    
    @NonNull
    @SerializedName("createdAt")
    private LocalDateTime createdAt;
    
    // 远程ID
    @SerializedName("remoteId")
    private Long remoteId;
    
    // 同步状态：0-未同步，1-已同步，2-需要更新，3-需要删除
    private int syncStatus;
    
    public Exercise() {
        this.createdAt = LocalDateTime.now();
        this.syncStatus = 0;
    }
    
    @Ignore
    public Exercise(long userId, @NonNull String exerciseName, int duration, double caloriesBurned, 
                   String exerciseType, String intensity, @NonNull LocalDateTime exercisedAt) {
        this.userId = userId;
        this.exerciseName = exerciseName;
        this.duration = duration;
        this.caloriesBurned = caloriesBurned;
        this.exerciseType = exerciseType;
        this.intensity = intensity;
        this.exercisedAt = exercisedAt;
        this.createdAt = LocalDateTime.now();
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
    
    @NonNull
    public String getExerciseName() {
        return exerciseName;
    }
    
    public void setExerciseName(@NonNull String exerciseName) {
        this.exerciseName = exerciseName;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public double getCaloriesBurned() {
        return caloriesBurned;
    }
    
    public void setCaloriesBurned(double caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }
    
    public String getExerciseType() {
        return exerciseType;
    }
    
    public void setExerciseType(String exerciseType) {
        this.exerciseType = exerciseType;
    }
    
    public String getIntensity() {
        return intensity;
    }
    
    public void setIntensity(String intensity) {
        this.intensity = intensity;
    }
    
    @NonNull
    public LocalDateTime getExercisedAt() {
        return exercisedAt;
    }
    
    public void setExercisedAt(@NonNull LocalDateTime exercisedAt) {
        this.exercisedAt = exercisedAt;
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