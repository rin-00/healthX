package com.healthx.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.healthx.model.StepRecord;
import com.healthx.repository.StepRepository;
import com.healthx.util.PreferenceManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 步数ViewModel，负责处理步数相关的UI逻辑
 */
public class StepViewModel extends ViewModel {
    
    private final StepRepository repository;
    private LiveData<List<StepRecord>> userStepRecords;
    private final MutableLiveData<StepRecord> currentStepRecord = new MutableLiveData<>();
    private final MutableLiveData<Integer> todaySteps = new MutableLiveData<>(0);
    private final MutableLiveData<BigDecimal> todayDistance = new MutableLiveData<>(BigDecimal.ZERO);
    private final MutableLiveData<BigDecimal> todayCalories = new MutableLiveData<>(BigDecimal.ZERO);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    public StepViewModel(StepRepository repository) {
        this.repository = repository;
    }
    
    /**
     * 获取用户所有步数记录
     * @param userId 用户ID
     * @return 步数记录LiveData
     */
    public LiveData<List<StepRecord>> getUserStepRecords(long userId) {
        if (userStepRecords == null) {
            userStepRecords = repository.getUserStepRecords(userId);
        }
        return userStepRecords;
    }
    
    /**
     * 获取今日步数记录
     * @param userId 用户ID
     * @param forceRefresh 是否强制刷新
     */
    public void getTodayStepRecord(long userId, boolean forceRefresh) {
        isLoading.setValue(true);
        
        LocalDate today = LocalDate.now();
        repository.getStepRecordByDate(userId, today, forceRefresh, new StepRepository.DataCallback<StepRecord>() {
            @Override
            public void onSuccess(StepRecord data) {
                currentStepRecord.postValue(data);
                
                if (data != null) {
                    todaySteps.postValue(data.getStepCount());
                    todayDistance.postValue(data.getDistance());
                    todayCalories.postValue(data.getCaloriesBurned());
                } else {
                    todaySteps.postValue(0);
                    todayDistance.postValue(BigDecimal.ZERO);
                    todayCalories.postValue(BigDecimal.ZERO);
                }
                
                isLoading.postValue(false);
            }
            
            @Override
            public void onError(String message) {
                errorMessage.postValue(message);
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * 记录步数
     * @param userId 用户ID
     * @param steps 步数
     * @param source 数据来源
     */
    public void recordSteps(long userId, int steps, String source) {
        isLoading.setValue(true);
        
        StepRecord stepRecord = new StepRecord();
        stepRecord.setUserId(userId);
        stepRecord.setStepCount(steps);
        stepRecord.setDistance(repository.calculateDistance(steps));
        stepRecord.setCaloriesBurned(repository.calculateCalories(steps));
        stepRecord.setRecordDate(LocalDate.now());
        stepRecord.setSource(source);
        
        repository.addStepRecord(stepRecord, new StepRepository.DataCallback<StepRecord>() {
            @Override
            public void onSuccess(StepRecord data) {
                currentStepRecord.postValue(data);
                todaySteps.postValue(data.getStepCount());
                todayDistance.postValue(data.getDistance());
                todayCalories.postValue(data.getCaloriesBurned());
                isLoading.postValue(false);
            }
            
            @Override
            public void onError(String message) {
                errorMessage.postValue(message);
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * 增加步数
     * @param userId 用户ID
     * @param additionalSteps 增加的步数
     * @param source 数据来源
     */
    public void addSteps(long userId, int additionalSteps, String source) {
        // 先获取当前数据
        StepRecord currentRecord = currentStepRecord.getValue();
        
        if (currentRecord != null) {
            // 已有记录，增加步数
            int newSteps = currentRecord.getStepCount() + additionalSteps;
            recordSteps(userId, newSteps, source);
        } else {
            // 没有记录，直接创建
            recordSteps(userId, additionalSteps, source);
        }
    }
    
    // Getters
    public LiveData<StepRecord> getCurrentStepRecord() {
        return currentStepRecord;
    }
    
    public LiveData<Integer> getTodaySteps() {
        return todaySteps;
    }
    
    public LiveData<BigDecimal> getTodayDistance() {
        return todayDistance;
    }
    
    public LiveData<BigDecimal> getTodayCalories() {
        return todayCalories;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
} 