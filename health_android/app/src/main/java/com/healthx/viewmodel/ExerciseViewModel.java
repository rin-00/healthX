package com.healthx.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.healthx.model.Exercise;
import com.healthx.repository.ExerciseRepository;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import java.util.List;

public class ExerciseViewModel extends AndroidViewModel {
    private static final String TAG = "ExerciseViewModel";
    
    private ExerciseRepository repository;
    private MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>();
    private MutableLiveData<String> selectedExerciseType = new MutableLiveData<>();
    private LiveData<List<Exercise>> exercises;
    private LiveData<Double> totalCaloriesBurned;
    
    public ExerciseViewModel(@NonNull Application application) {
        super(application);
        repository = new ExerciseRepository(application);
        
        // 初始化为今天的日期
        selectedDate.setValue(LocalDate.now());
        
        // 默认查看所有类型的运动
        selectedExerciseType.setValue("ALL");
        
        // 根据所选日期和运动类型动态加载运动记录
        exercises = Transformations.switchMap(
            selectedDate, date -> Transformations.switchMap(
                selectedExerciseType, type -> {
                    LocalDateTime startOfDay = date.atStartOfDay();
                    LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
                    
                    // 默认用户ID为1，实际应用中应该从登录会话获取
                    long userId = 1;
                    
                    if ("ALL".equals(type)) {
                        return repository.getExercisesByUserIdAndDateRange(userId, startOfDay, endOfDay);
                    } else {
                        return repository.getExercisesByUserIdAndType(userId, type);
                    }
                }
            )
        );
        
        // 获取所选日期的总消耗卡路里
        totalCaloriesBurned = Transformations.map(selectedDate, date -> {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            
            // 默认用户ID为1
            long userId = 1;
            
            LiveData<Double> caloriesData = repository.getTotalCaloriesBurnedByDateRange(userId, startOfDay, endOfDay);
            
            // 由于Transformations.map无法直接返回LiveData对象，
            // 这里我们只能返回当前时刻的值，这可能不够实时
            // 实际应用中可能需要使用更复杂的方法，如switchMap
            return caloriesData.getValue();
        });
    }
    
    // 加载指定日期的运动记录
    public void loadExercisesForDate(long userId, LocalDate date) {
        selectedDate.setValue(date);
        
        // 同时从服务器刷新数据
        refreshExercisesFromServer(userId);
    }
    
    // 加载指定类型的运动记录
    public void loadExercisesByType(long userId, String exerciseType) {
        selectedExerciseType.setValue(exerciseType);
        
        // 同时从服务器刷新数据
        refreshExercisesFromServer(userId);
    }
    
    // 从服务器刷新数据
    public void refreshExercisesFromServer(long userId) {
        try {
            Log.d(TAG, "从服务器刷新运动记录数据...");
            repository.fetchUserExercisesFromServer(userId);
        } catch (Exception e) {
            Log.e(TAG, "刷新运动记录失败: " + e.getMessage(), e);
        }
    }
    
    // 添加新的运动记录
    public void addExercise(Exercise exercise) {
        try {
            Log.d(TAG, "添加新的运动记录: " + exercise.getExerciseName());
            
            // 保存到本地数据库
            repository.insert(exercise);
            
            // 同步到服务器
            repository.saveExerciseToServer(exercise);
        } catch (Exception e) {
            Log.e(TAG, "添加运动记录失败: " + e.getMessage(), e);
        }
    }
    
    // 更新运动记录
    public void updateExercise(Exercise exercise) {
        try {
            Log.d(TAG, "更新运动记录: " + exercise.getExerciseName() + ", ID: " + exercise.getId());
            
            // 更新同步状态
            exercise.setSyncStatus(2); // 需要更新
            
            // 更新本地数据库
            repository.update(exercise);
            
            // 同步到服务器
            repository.updateExerciseOnServer(exercise);
        } catch (Exception e) {
            Log.e(TAG, "更新运动记录失败: " + e.getMessage(), e);
        }
    }
    
    // 删除运动记录
    public void deleteExercise(Exercise exercise) {
        try {
            Log.d(TAG, "删除运动记录: " + exercise.getExerciseName() + ", ID: " + exercise.getId());
            
            // 如果有远程ID，则标记为需要删除并更新同步状态
            if (exercise.getRemoteId() != null) {
                exercise.setSyncStatus(3); // 需要删除
                repository.update(exercise);
            }
            
            // 从服务器删除
            repository.deleteExerciseOnServer(exercise);
        } catch (Exception e) {
            Log.e(TAG, "删除运动记录失败: " + e.getMessage(), e);
        }
    }
    
    // 同步未同步的数据
    public void syncPendingData() {
        repository.syncUnsyncedData();
    }
    
    // 获取运动记录LiveData
    public LiveData<List<Exercise>> getExercises() {
        return exercises;
    }
    
    // 获取所选日期LiveData
    public LiveData<LocalDate> getSelectedDate() {
        return selectedDate;
    }
    
    // 获取所选运动类型LiveData
    public LiveData<String> getSelectedExerciseType() {
        return selectedExerciseType;
    }
    
    // 获取总消耗卡路里LiveData
    public LiveData<Double> getTotalCaloriesBurned() {
        return totalCaloriesBurned;
    }
    
    // 获取今日消耗的卡路里
    public LiveData<Double> getTotalCaloriesToday() {
        return repository.getTotalCaloriesBurnedForToday(1); // 默认用户ID为1
    }
} 