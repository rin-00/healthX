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

@Entity(tableName = "diets")
@TypeConverters(DateTimeConverter.class)
public class Diet implements Serializable {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private long userId;
    
    @NonNull
    @SerializedName("foodName")
    private String foodName;
    
    @SerializedName("calories")
    private double calories;
    
    @SerializedName("protein")
    private double protein;
    
    @SerializedName("carbs")
    private double carbs;
    
    @SerializedName("fat")
    private double fat;
    
    @NonNull
    @SerializedName("mealType")
    private String mealType;
    
    @NonNull
    @SerializedName("eatenAt")
    private LocalDateTime eatenAt;
    
    @NonNull
    @SerializedName("createdAt")
    private LocalDateTime createdAt;
    
    // Remote ID from server
    @SerializedName("remoteId")
    private Long remoteId;
    
    // 同步状态：0-未同步，1-已同步，2-需要更新，3-需要删除
    private int syncStatus;
    
    public Diet() {
        this.createdAt = LocalDateTime.now();
        this.syncStatus = 0;
    }
    
    @Ignore
    public Diet(long userId, @NonNull String foodName, double calories, double protein, double carbs, double fat, @NonNull String mealType, @NonNull LocalDateTime eatenAt) {
        this.userId = userId;
        this.foodName = foodName;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.mealType = mealType;
        this.eatenAt = eatenAt;
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
    public String getFoodName() {
        return foodName;
    }
    
    public void setFoodName(@NonNull String foodName) {
        this.foodName = foodName;
    }
    
    public double getCalories() {
        return calories;
    }
    
    public void setCalories(double calories) {
        this.calories = calories;
    }
    
    public double getProtein() {
        return protein;
    }
    
    public void setProtein(double protein) {
        this.protein = protein;
    }
    
    public double getCarbs() {
        return carbs;
    }
    
    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }
    
    public double getFat() {
        return fat;
    }
    
    public void setFat(double fat) {
        this.fat = fat;
    }
    
    @NonNull
    public String getMealType() {
        return mealType;
    }
    
    public void setMealType(@NonNull String mealType) {
        this.mealType = mealType;
    }
    
    @NonNull
    public LocalDateTime getEatenAt() {
        return eatenAt;
    }
    
    public void setEatenAt(@NonNull LocalDateTime eatenAt) {
        this.eatenAt = eatenAt;
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