package com.healthx.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.healthx.model.Diet;
import com.healthx.repository.DietRepository;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import java.util.List;

public class DietViewModel extends AndroidViewModel {
    
    private DietRepository repository;
    private MediatorLiveData<List<Diet>> diets = new MediatorLiveData<>();
    private MutableLiveData<String> currentMealType = new MutableLiveData<>();
    private MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>();
    private LiveData<Double> totalCaloriesToday;
    private long userId = 1; // 默认值
    
    public DietViewModel(@NonNull Application application) {
        super(application);
        repository = new DietRepository(application);
        
        // 获取用户ID，实际开发中应该从用户会话中获取
        // 这里为简化处理，使用1作为默认值
        
        // 初始化默认值
        selectedDate.setValue(LocalDate.now());
        currentMealType.setValue("ALL");
        
        // 加载用户的今日饮食记录
        loadDietsForToday(userId);
        
        // 获取今日总卡路里
        totalCaloriesToday = repository.getTotalCaloriesForToday(userId);
    }
    
    // 设置用户ID
    public void setUserId(long userId) {
        this.userId = userId;
        // 重新加载数据
        loadDietsForToday(userId);
        totalCaloriesToday = repository.getTotalCaloriesForToday(userId);
    }
    
    // 加载特定日期的饮食记录
    public void loadDietsForDate(long userId, LocalDate date) {
        selectedDate.setValue(date);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        String mealType = currentMealType.getValue();
        if (mealType != null && !mealType.equals("ALL")) {
            loadDietsByMealType(userId, mealType);
        } else {
            LiveData<List<Diet>> source = repository.getDietsByUserIdAndDateRange(userId, startOfDay, endOfDay);
            diets.addSource(source, diets::setValue);
        }
    }
    
    // 加载今日的饮食记录
    public void loadDietsForToday(long userId) {
        loadDietsForDate(userId, LocalDate.now());
    }
    
    // 按餐次加载饮食记录
    public void loadDietsByMealType(long userId, String mealType) {
        currentMealType.setValue(mealType);
        
        if (mealType.equals("ALL")) {
            loadDietsForDate(userId, selectedDate.getValue());
            return;
        }
        
        LiveData<List<Diet>> source = repository.getDietsByUserIdAndMealType(userId, mealType);
        diets.addSource(source, filteredDiets -> {
            if (filteredDiets != null) {
                // 过滤出所选日期的记录
                LocalDate date = selectedDate.getValue();
                if (date != null) {
                    List<Diet> filtered = filteredDiets.stream()
                            .filter(diet -> {
                                LocalDate dietDate = diet.getEatenAt().toLocalDate();
                                return dietDate.equals(date);
                            })
                            .collect(java.util.stream.Collectors.toList());
                    diets.setValue(filtered);
                } else {
                    diets.setValue(filteredDiets);
                }
            } else {
                diets.setValue(null);
            }
        });
    }
    
    // 从服务器刷新数据
    public void refreshDietsFromServer(long userId) {
        repository.fetchUserDietsFromServer(userId);
    }
    
    // 添加饮食记录
    public void addDiet(Diet diet) {
        diet.setSyncStatus(0); // 标记为未同步
        repository.insert(diet);
        repository.syncUnsyncedData(); // 尝试同步到服务器
    }
    
    // 更新饮食记录
    public void updateDiet(Diet diet) {
        diet.setSyncStatus(2); // 标记为需要更新
        repository.update(diet);
        repository.syncUnsyncedData(); // 尝试同步到服务器
    }
    
    // 删除饮食记录
    public void deleteDiet(Diet diet) {
        try {
            // 无论远程ID是否存在，优先本地删除
            repository.delete(diet);
            
            // 如果有远程ID，尝试从服务器删除
            if (diet.getRemoteId() != null) {
                repository.deleteDietOnServer(diet);
            }
            
            // 刷新今日总卡路里数据
            totalCaloriesToday = repository.getTotalCaloriesForToday(userId);
        } catch (Exception e) {
            Log.e("DietViewModel", "删除饮食记录异常: " + e.getMessage(), e);
        }
    }
    
    // Getters
    public LiveData<List<Diet>> getDiets() {
        return diets;
    }
    
    public LiveData<String> getCurrentMealType() {
        return currentMealType;
    }
    
    public LiveData<LocalDate> getSelectedDate() {
        return selectedDate;
    }
    
    public LiveData<Double> getTotalCaloriesToday() {
        return totalCaloriesToday;
    }
} 