package com.healthx.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.healthx.model.User;
import com.healthx.model.WeightRecord;
import com.healthx.repository.UserRepository;
import com.healthx.repository.WeightRepository;
import com.healthx.util.DateTimeUtils;

import java.util.Date;
import java.util.List;

/**
 * 体重记录ViewModel - 重构后
 */
public class WeightViewModel extends AndroidViewModel {
    private final WeightRepository repository;
    private final UserRepository userRepository;
    
    // UI状态数据
    private final MutableLiveData<Float> latestWeight = new MutableLiveData<>();
    private final MutableLiveData<Float> latestBmi = new MutableLiveData<>();
    private final MutableLiveData<Integer> bmiStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasRecordToday = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    
    // 当前用户
    private User currentUser;
    
    // 体重统计数据
    private float[] weightStats = new float[3]; // [最大值, 最小值, 平均值]
    
    public WeightViewModel(@NonNull Application application) {
        super(application);
        repository = new WeightRepository(application);
        userRepository = new UserRepository(application);
        
        // 加载当前用户
        loadCurrentUser();
    }
    
    // 加载当前用户
    private void loadCurrentUser() {
        userRepository.getCurrentUser(user -> {
            currentUser = user;
            if (user != null) {
                // 用户加载完成后加载体重数据
                loadLatestWeightRecord();
                checkTodayRecord();
            }
        });
    }
    
    // 加载最新体重记录
    public void loadLatestWeightRecord() {
        if (currentUser == null) return;
        
        new Thread(() -> {
            WeightRecord latest = repository.getLatestByUserId(currentUser.getId());
            if (latest != null) {
                latestWeight.postValue(latest.getWeight());
                if (latest.getBmi() > 0) {
                    latestBmi.postValue(latest.getBmi());
                    bmiStatus.postValue(latest.getBmiStatus());
                }
            }
        }).start();
    }
    
    // 检查今天是否已有记录
    public void checkTodayRecord() {
        if (currentUser == null) return;
        
        new Thread(() -> {
            boolean hasRecord = repository.hasRecordForToday(currentUser.getId());
            hasRecordToday.postValue(hasRecord);
        }).start();
    }
    
    // 获取用户所有体重记录
    public LiveData<List<WeightRecord>> getWeightRecords() {
        if (currentUser == null) {
            return new MutableLiveData<>();
        }
        return repository.getByUserId(currentUser.getId());
    }
    
    // 获取用户指定日期范围的体重记录
    public LiveData<List<WeightRecord>> getWeightRecordsByDateRange(Date startDate, Date endDate) {
        if (currentUser == null) {
            return new MutableLiveData<>();
        }
        return repository.getByUserIdAndDateRange(
                currentUser.getId(),
                startDate.getTime(),
                endDate.getTime());
    }
    
    // 获取用户最近30天的体重记录
    public LiveData<List<WeightRecord>> getLast30DaysWeightRecords() {
        if (currentUser == null) {
            return new MutableLiveData<>();
        }
        
        Date startDate = DateTimeUtils.startOfDay(DateTimeUtils.addDays(new Date(), -30));
        return repository.getLast30DaysByUserId(currentUser.getId(), startDate.getTime());
    }
    
    /**
     * 添加体重记录
     */
    public void addWeightRecord(float weight, String note) {
        if (currentUser == null) {
            toastMessage.setValue("请先设置用户信息");
            return;
        }
        
        // 创建新的体重记录
        WeightRecord record = new WeightRecord();
        record.setUserId(currentUser.getId());
        record.setWeight(weight);
        record.setNote(note);
        record.setMeasurementTime(System.currentTimeMillis());
        record.setCreatedAt(System.currentTimeMillis());
        record.setSyncStatus(0); // 未同步
        
        // 如果有身高数据，计算BMI
        if (currentUser.getHeight() > 0) {
            float bmi = WeightRepository.calculateBMI(
                    weight, 
                    currentUser.getHeight().floatValue());
            record.setBmi(bmi);
            record.setBmiStatus(WeightRepository.getBmiStatus(bmi));
        }
        
        // 保存记录并尝试同步
        repository.insert(record);
        repository.syncUnsyncedData();
        
        // 更新UI状态
        latestWeight.setValue(weight);
        if (record.getBmi() > 0) {
            latestBmi.setValue(record.getBmi());
            bmiStatus.setValue(record.getBmiStatus());
        }
        hasRecordToday.setValue(true);
        toastMessage.setValue("体重记录已保存");
    }
    
    /**
     * 更新体重记录
     */
    public void updateWeightRecord(WeightRecord record, float weight, String note) {
        if (currentUser == null) return;
        
        record.setWeight(weight);
        record.setNote(note);
        
        // 如果有身高数据，重新计算BMI
        if (currentUser.getHeight() > 0) {
            float bmi = WeightRepository.calculateBMI(
                    weight, 
                    currentUser.getHeight().floatValue());
            record.setBmi(bmi);
            record.setBmiStatus(WeightRepository.getBmiStatus(bmi));
        }
        
        // 保存记录并尝试同步
        repository.update(record);
        repository.syncUnsyncedData();
        
        // 如果是最新记录，更新UI状态
        if (isLatestRecord(record)) {
            latestWeight.setValue(weight);
            if (record.getBmi() > 0) {
                latestBmi.setValue(record.getBmi());
                bmiStatus.setValue(record.getBmiStatus());
            }
        }
        
        toastMessage.setValue("体重记录已更新");
    }
    
    /**
     * 删除体重记录
     */
    public void deleteWeightRecord(WeightRecord record) {
        if (currentUser == null) return;
        
        boolean isToday = DateTimeUtils.isSameDay(new Date(record.getMeasurementTime()), new Date());
        boolean isLatest = isLatestRecord(record);
        
        repository.delete(record);
        repository.syncUnsyncedData();
        
        // 更新UI状态
        if (isToday) {
            checkTodayRecord();
        }
        
        if (isLatest) {
            loadLatestWeightRecord();
        }
        
        toastMessage.setValue("体重记录已删除");
    }
    
    /**
     * 从服务器刷新数据
     */
    public void refreshWeightRecordsFromServer() {
        if (currentUser == null) return;
        
        isLoading.setValue(true);
        
        // 从服务器获取数据
        repository.fetchWeightRecordsFromServer(currentUser.getId());
        
        // 延迟后重新加载数据并更新UI
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            loadLatestWeightRecord();
            checkTodayRecord();
            isLoading.setValue(false);
        }, 1000);
    }
    
    /**
     * 同步未同步的数据
     */
    public void syncWeightRecords() {
        if (currentUser == null) return;
        
        isLoading.setValue(true);
        toastMessage.setValue("正在同步体重数据...");
        
        // 同步数据
        repository.syncUnsyncedData();
        
        // 延迟后更新UI状态
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isLoading.setValue(false);
        }, 1500);
    }
    
    /**
     * 判断给定记录是否是最新记录
     */
    private boolean isLatestRecord(WeightRecord record) {
        if (currentUser == null) return false;
        
        WeightRecord latest = repository.getLatestByUserId(currentUser.getId());
        return latest != null && latest.getId() == record.getId();
    }
    
    // Getter方法
    public LiveData<Float> getLatestWeight() {
        return latestWeight;
    }
    
    public LiveData<Float> getLatestBmi() {
        return latestBmi;
    }
    
    public LiveData<Integer> getBmiStatus() {
        return bmiStatus;
    }
    
    public LiveData<Boolean> getHasRecordToday() {
        return hasRecordToday;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getToastMessage() {
        return toastMessage;
    }
    
    public float[] getWeightStats() {
        return weightStats;
    }
} 