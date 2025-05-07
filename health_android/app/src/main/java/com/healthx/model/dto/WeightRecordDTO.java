package com.healthx.model.dto;

import com.google.gson.annotations.SerializedName;
import com.healthx.util.DateTimeUtils;

import java.math.BigDecimal;

/**
 * 体重记录数据传输对象，用于前后端数据传输
 * 遵循日期时间格式规范
 */
public class WeightRecordDTO {
    
    private Long id;
    
    private Long userId;
    
    private BigDecimal weight;
    
    private BigDecimal bmi;
    
    private String bmiStatus;
    
    private BigDecimal bodyFatPercentage;
    
    @SerializedName("measurementTime")
    private String measurementTimeStr;
    
    private String note;
    
    @SerializedName("createdAt")
    private String createdAtStr;
    
    @SerializedName("updatedAt")
    private String updatedAtStr;
    
    private Long remoteId;
    
    private Integer syncStatus;
    
    // 默认构造函数
    public WeightRecordDTO() {
    }
    
    // 从WeightRecord转换的构造函数
    public WeightRecordDTO(com.healthx.model.WeightRecord record) {
        this.id = record.getId() > 0 ? record.getId() : null;
        this.userId = record.getUserId();
        this.weight = new BigDecimal(String.valueOf(record.getWeight()));
        this.bmi = record.getBmi() > 0 ? new BigDecimal(String.valueOf(record.getBmi())) : null;
        this.bmiStatus = record.getBmiStatus();
        this.bodyFatPercentage = record.getBodyFatPercentage() != null ? 
                new BigDecimal(String.valueOf(record.getBodyFatPercentage())) : null;
        
        // 转换时间戳为ISO格式字符串，符合规范
        this.measurementTimeStr = DateTimeUtils.timestampToIsoString(record.getMeasurementTime());
        this.createdAtStr = DateTimeUtils.timestampToIsoString(record.getCreatedAt());
        this.updatedAtStr = DateTimeUtils.timestampToIsoString(System.currentTimeMillis());
        
        this.note = record.getNote();
        this.remoteId = record.getRemoteId();
        this.syncStatus = record.getSyncStatus();
    }
    
    // 转换为WeightRecord对象
    public com.healthx.model.WeightRecord toWeightRecord() {
        com.healthx.model.WeightRecord record = new com.healthx.model.WeightRecord();
        
        if (this.id != null) {
            record.setId(this.id);
        }
        
        record.setUserId(this.userId);
        record.setWeight(this.weight != null ? this.weight.floatValue() : 0);
        record.setBmi(this.bmi != null ? this.bmi.floatValue() : 0);
        record.setBmiStatus(this.bmiStatus);
        
        if (this.bodyFatPercentage != null) {
            record.setBodyFatPercentage(this.bodyFatPercentage.floatValue());
        }
        
        // 将ISO格式字符串转回时间戳
        if (this.measurementTimeStr != null) {
            record.setMeasurementTime(DateTimeUtils.isoStringToTimestamp(this.measurementTimeStr));
        }
        
        if (this.createdAtStr != null) {
            record.setCreatedAt(DateTimeUtils.isoStringToTimestamp(this.createdAtStr));
        } else {
            record.setCreatedAt(System.currentTimeMillis());
        }
        
        record.setNote(this.note);
        record.setRemoteId(this.remoteId);
        
        if (this.syncStatus != null) {
            record.setSyncStatus(this.syncStatus);
        }
        
        return record;
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
    
    public BigDecimal getWeight() {
        return weight;
    }
    
    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }
    
    public BigDecimal getBmi() {
        return bmi;
    }
    
    public void setBmi(BigDecimal bmi) {
        this.bmi = bmi;
    }
    
    public String getBmiStatus() {
        return bmiStatus;
    }
    
    public void setBmiStatus(String bmiStatus) {
        this.bmiStatus = bmiStatus;
    }
    
    public BigDecimal getBodyFatPercentage() {
        return bodyFatPercentage;
    }
    
    public void setBodyFatPercentage(BigDecimal bodyFatPercentage) {
        this.bodyFatPercentage = bodyFatPercentage;
    }
    
    public String getMeasurementTimeStr() {
        return measurementTimeStr;
    }
    
    public void setMeasurementTimeStr(String measurementTimeStr) {
        this.measurementTimeStr = measurementTimeStr;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public String getCreatedAtStr() {
        return createdAtStr;
    }
    
    public void setCreatedAtStr(String createdAtStr) {
        this.createdAtStr = createdAtStr;
    }
    
    public String getUpdatedAtStr() {
        return updatedAtStr;
    }
    
    public void setUpdatedAtStr(String updatedAtStr) {
        this.updatedAtStr = updatedAtStr;
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