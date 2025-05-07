package com.healthx.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.healthx.model.User;
import com.healthx.model.WeightRecord;
import com.healthx.repository.Resource;
import com.healthx.repository.UserRepository;
import com.healthx.repository.WeightRepository;
import com.healthx.util.DateTimeUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 体重管理ViewModel
 */
public class WeightViewModel extends AndroidViewModel {
    
    private final WeightRepository weightRepository;
    private final UserRepository userRepository;
    private final ExecutorService executorService;
    
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Float> latestWeight = new MutableLiveData<>();
    private final MutableLiveData<Float> latestBmi = new MutableLiveData<>();
    private final MutableLiveData<String> bmiStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasRecordToday = new MutableLiveData<>(false);
    
    private User currentUser;
    
    public WeightViewModel(@NonNull Application application) {
        super(application);
        weightRepository = new WeightRepository(application);
        userRepository = new UserRepository(application);
        executorService = Executors.newFixedThreadPool(2);
        
        // 初始化加载当前用户
        loadCurrentUser();
    }
    
    private void loadCurrentUser() {
        executorService.execute(() -> {
            currentUser = userRepository.getDefaultUser();
            // 加载最新体重记录
            loadLatestWeightRecord();
            // 检查今天是否已有记录
            checkTodayRecord();
        });
    }
    
    /**
     * 加载最新体重记录
     */
    private void loadLatestWeightRecord() {
        if (currentUser == null) return;
        
        executorService.execute(() -> {
            WeightRecord latestRecord = weightRepository.getLatestByUserId(currentUser.getId());
            if (latestRecord != null) {
                latestWeight.postValue(latestRecord.getWeight());
                latestBmi.postValue(latestRecord.getBmi());
                bmiStatus.postValue(latestRecord.getBmiStatus());
            } else {
                // 如果没有体重记录，使用用户个人信息中的体重
                if (currentUser.getWeight() > 0) {
                    // 将Double转为float
                    latestWeight.postValue(currentUser.getWeight().floatValue());
                    
                    // 计算BMI（如果有身高）
                    if (currentUser.getHeight() > 0) {
                        float bmi = WeightRepository.calculateBMI(
                                currentUser.getWeight().floatValue(), 
                                currentUser.getHeight().floatValue());
                        latestBmi.postValue(bmi);
                        bmiStatus.postValue(WeightRepository.getBmiStatus(bmi));
                    }
                }
            }
        });
    }
    
    /**
     * 检查今天是否已有体重记录
     */
    private void checkTodayRecord() {
        if (currentUser == null) return;
        
        executorService.execute(() -> {
            boolean hasTodayRecord = weightRepository.hasRecordForToday(currentUser.getId());
            hasRecordToday.postValue(hasTodayRecord);
        });
    }
    
    /**
     * 添加体重记录
     */
    public void addWeightRecord(float weight, String note) {
        if (currentUser == null) {
            toastMessage.setValue("请先设置用户信息");
            return;
        }
        
        isLoading.setValue(true);
        
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
        
        // 保存记录
        executorService.execute(() -> {
            LiveData<Resource<WeightRecord>> result = weightRepository.insert(record);
            
            // 创建一次性观察者，避免内存泄漏
            androidx.lifecycle.Observer<Resource<WeightRecord>> observer = resource -> {
                isLoading.postValue(false);
                
                if (resource.getStatus() == Resource.Status.SUCCESS) {
                    toastMessage.postValue("体重记录已保存");
                    
                    // 更新状态
                    latestWeight.postValue(weight);
                    if (record.getBmi() > 0) {
                        latestBmi.postValue(record.getBmi());
                        bmiStatus.postValue(record.getBmiStatus());
                    }
                    hasRecordToday.postValue(true);
                } else if (resource.getStatus() == Resource.Status.ERROR) {
                    toastMessage.postValue(resource.getMessage());
                }
            };
            
            // 在主线程中添加观察者
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                result.observeForever(observer);
            });
        });
    }
    
    /**
     * 更新体重记录
     */
    public void updateWeightRecord(WeightRecord record, float weight, String note) {
        if (currentUser == null) return;
        
        isLoading.setValue(true);
        
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
        
        // 保存记录
        executorService.execute(() -> {
            LiveData<Resource<WeightRecord>> result = weightRepository.update(record);
            
            // 创建一次性观察者，避免内存泄漏
            androidx.lifecycle.Observer<Resource<WeightRecord>> observer = resource -> {
                isLoading.postValue(false);
                
                if (resource.getStatus() == Resource.Status.SUCCESS) {
                    toastMessage.postValue("体重记录已更新");
                    
                    // 如果是最新记录，更新状态
                    if (isLatestRecord(record)) {
                        latestWeight.postValue(weight);
                        if (record.getBmi() > 0) {
                            latestBmi.postValue(record.getBmi());
                            bmiStatus.postValue(record.getBmiStatus());
                        }
                    }
                } else if (resource.getStatus() == Resource.Status.ERROR) {
                    toastMessage.postValue(resource.getMessage());
                }
            };
            
            // 在主线程中添加观察者
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                result.observeForever(observer);
            });
        });
    }
    
    /**
     * 删除体重记录
     */
    public void deleteWeightRecord(WeightRecord record) {
        if (currentUser == null) return;
        
        isLoading.setValue(true);
        
        boolean isToday = DateTimeUtils.isSameDay(new Date(record.getMeasurementTime()), new Date());
        boolean isLatest = isLatestRecord(record);
        
        executorService.execute(() -> {
            LiveData<Resource<Boolean>> result = weightRepository.delete(record);
            
            // 创建一次性观察者，避免内存泄漏
            androidx.lifecycle.Observer<Resource<Boolean>> observer = resource -> {
                isLoading.postValue(false);
                
                if (resource.getStatus() == Resource.Status.SUCCESS) {
                    toastMessage.postValue("体重记录已删除");
                    
                    // 更新状态
                    if (isToday) {
                        checkTodayRecord();
                    }
                    
                    if (isLatest) {
                        loadLatestWeightRecord();
                    }
                } else if (resource.getStatus() == Resource.Status.ERROR) {
                    toastMessage.postValue(resource.getMessage());
                }
            };
            
            // 在主线程中添加观察者
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                result.observeForever(observer);
            });
        });
    }
    
    /**
     * 同步体重记录数据
     */
    public LiveData<Resource<Boolean>> syncWeightRecords() {
        if (currentUser == null) {
            MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
            result.setValue(Resource.error("用户未设置", false));
            return result;
        }
        
        isLoading.setValue(true);
        
        // 返回同步结果
        LiveData<Resource<Boolean>> syncResult = weightRepository.syncData(currentUser.getId());
        
        // 通过Transformations监听结果变化
        return Transformations.map(syncResult, resource -> {
            isLoading.postValue(false);
            
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                toastMessage.postValue("数据同步完成");
                // 重新加载数据
                loadLatestWeightRecord();
                checkTodayRecord();
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                toastMessage.postValue("同步失败: " + resource.getMessage());
            }
            return resource;
        });
    }
    
    /**
     * 判断是否为最新记录
     */
    private boolean isLatestRecord(WeightRecord record) {
        WeightRecord latestRecord = weightRepository.getLatestByUserId(currentUser.getId());
        return latestRecord != null && latestRecord.getId() == record.getId();
    }
    
    /**
     * 获取用户的所有体重记录
     */
    public LiveData<List<WeightRecord>> getAllWeightRecords() {
        if (currentUser == null) return new MutableLiveData<>();
        return weightRepository.getByUserId(currentUser.getId());
    }
    
    /**
     * 获取用户最近30天的体重记录
     */
    public LiveData<List<WeightRecord>> getLast30DaysWeightRecords() {
        if (currentUser == null) return new MutableLiveData<>();
        return weightRepository.getLast30DaysByUserId(currentUser.getId());
    }
    
    /**
     * 获取用户指定日期范围的体重记录
     */
    public LiveData<List<WeightRecord>> getWeightRecordsByDateRange(Date startDate, Date endDate) {
        if (currentUser == null) return new MutableLiveData<>();
        return weightRepository.getByUserIdAndDateRange(currentUser.getId(), startDate, endDate);
    }
    
    // 各种LiveData的getter方法
    public LiveData<String> getToastMessage() {
        return toastMessage;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<Float> getLatestWeight() {
        return latestWeight;
    }
    
    public LiveData<Float> getLatestBmi() {
        return latestBmi;
    }
    
    public LiveData<String> getBmiStatus() {
        return bmiStatus;
    }
    
    public LiveData<Boolean> getHasRecordToday() {
        return hasRecordToday;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
} 