package com.healthx.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * 体重记录实体类
 */
@Entity(tableName = "weight_records",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index("userId"),
                @Index("measurementTime")
        })
public class WeightRecord implements Serializable {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "userId")
    private long userId;
    
    @ColumnInfo(name = "weight")
    private float weight;
    
    @ColumnInfo(name = "bmi")
    private float bmi;
    
    @ColumnInfo(name = "bmiStatus")
    private String bmiStatus;
    
    @ColumnInfo(name = "bodyFatPercentage")
    private Float bodyFatPercentage;
    
    @ColumnInfo(name = "measurementTime")
    private long measurementTime; // 存储为时间戳
    
    @ColumnInfo(name = "note")
    private String note;
    
    @ColumnInfo(name = "createdAt")
    private long createdAt;
    
    @ColumnInfo(name = "remoteId")
    private Long remoteId;
    
    @ColumnInfo(name = "syncStatus")
    private int syncStatus = 0; // 0: 未同步, 1: 已同步, 2: 同步失败
    
    // 构造函数
    public WeightRecord() {
        this.createdAt = System.currentTimeMillis();
        this.measurementTime = System.currentTimeMillis();
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
    
    public float getWeight() {
        return weight;
    }
    
    public void setWeight(float weight) {
        this.weight = weight;
    }
    
    public float getBmi() {
        return bmi;
    }
    
    public void setBmi(float bmi) {
        this.bmi = bmi;
    }
    
    public String getBmiStatus() {
        return bmiStatus;
    }
    
    public void setBmiStatus(String bmiStatus) {
        this.bmiStatus = bmiStatus;
    }
    
    public Float getBodyFatPercentage() {
        return bodyFatPercentage;
    }
    
    public void setBodyFatPercentage(Float bodyFatPercentage) {
        this.bodyFatPercentage = bodyFatPercentage;
    }
    
    public long getMeasurementTime() {
        return measurementTime;
    }
    
    public void setMeasurementTime(long measurementTime) {
        this.measurementTime = measurementTime;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
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