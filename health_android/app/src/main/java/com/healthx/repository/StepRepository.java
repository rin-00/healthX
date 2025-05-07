package com.healthx.repository;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.healthx.database.AppDatabase;
import com.healthx.database.dao.StepGoalDao;
import com.healthx.database.dao.StepRecordDao;
import com.healthx.model.StepGoal;
import com.healthx.model.StepRecord;
import com.healthx.model.dto.StepGoalDTO;
import com.healthx.model.dto.StepRecordDTO;
import com.healthx.model.dto.StepStatisticsDTO;
import com.healthx.network.ApiResponse;
import com.healthx.network.RetrofitClient;
import com.healthx.network.StepApiService;
import com.healthx.util.DateTimeUtils;
import com.healthx.util.PreferenceManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 步数仓库类，负责步数数据的获取和存储
 */
public class StepRepository {
    private static final String TAG = "StepRepository";
    
    private final StepRecordDao stepRecordDao;
    private final StepGoalDao stepGoalDao;
    private final StepApiService stepApiService;
    private final Executor executor;
    private final PreferenceManager preferenceManager;
    
    public StepRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        stepRecordDao = db.stepRecordDao();
        stepGoalDao = db.stepGoalDao();
        stepApiService = RetrofitClient.getInstance(context).create(StepApiService.class);
        executor = Executors.newFixedThreadPool(2);
        preferenceManager = PreferenceManager.getInstance(context);
    }
    
    // 步数记录相关方法
    
    /**
     * 添加步数记录
     * @param stepRecord 步数记录
     * @param callback 回调
     */
    public void addStepRecord(StepRecord stepRecord, final DataCallback<StepRecord> callback) {
        executor.execute(() -> {
            try {
                // 检查是否已存在当日记录
                StepRecord existingRecord = stepRecordDao.getStepRecordByDate(
                        stepRecord.getUserId(), stepRecord.getRecordDate());
                
                if (existingRecord != null) {
                    // 更新已有记录
                    existingRecord.setStepCount(stepRecord.getStepCount());
                    existingRecord.setDistance(stepRecord.getDistance());
                    existingRecord.setCaloriesBurned(stepRecord.getCaloriesBurned());
                    existingRecord.setSource(stepRecord.getSource());
                    existingRecord.setUpdatedAt(LocalDateTime.now());
                    existingRecord.setSyncStatus(0); // 设置为未同步
                    
                    stepRecordDao.update(existingRecord);
                    
                    // 同步到服务器
                    syncStepRecordToServer(existingRecord, callback);
                } else {
                    // 保存新记录
                    long id = stepRecordDao.insert(stepRecord);
                    stepRecord.setId(id);
                    
                    // 同步到服务器
                    syncStepRecordToServer(stepRecord, callback);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error adding step record", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 同步步数记录到服务器
     * @param stepRecord 步数记录
     * @param callback 回调
     */
    private void syncStepRecordToServer(StepRecord stepRecord, final DataCallback<StepRecord> callback) {
        // 如果未登录，直接返回本地数据
        if (!preferenceManager.isLoggedIn()) {
            if (callback != null) {
                callback.onSuccess(stepRecord);
            }
            return;
        }
        
        // 转换为DTO
        StepRecordDTO dto = convertToStepRecordDTO(stepRecord);
        
        // 发送到服务器
        Call<ApiResponse<StepRecordDTO>> call;
        if (stepRecord.getRemoteId() != null) {
            call = stepApiService.updateStepRecord(stepRecord.getRemoteId(), dto);
        } else {
            call = stepApiService.addStepRecord(dto);
        }
        
        call.enqueue(new Callback<ApiResponse<StepRecordDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<StepRecordDTO>> call, Response<ApiResponse<StepRecordDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    executor.execute(() -> {
                        try {
                            // 更新本地记录的远程ID和同步状态
                            StepRecordDTO serverDTO = response.body().getData();
                            stepRecord.setRemoteId(serverDTO.getId());
                            stepRecord.setSyncStatus(1); // 设置为已同步
                            stepRecordDao.update(stepRecord);
                            
                            if (callback != null) {
                                callback.onSuccess(stepRecord);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating step record after sync", e);
                            if (callback != null) {
                                callback.onError(e.getMessage());
                            }
                        }
                    });
                } else {
                    // 标记同步失败
                    executor.execute(() -> {
                        stepRecord.setSyncStatus(2); // 设置为同步失败
                        stepRecordDao.update(stepRecord);
                        
                        if (callback != null) {
                            callback.onSuccess(stepRecord); // 仍然返回本地数据
                        }
                    });
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<StepRecordDTO>> call, Throwable t) {
                Log.e(TAG, "Error syncing step record to server", t);
                
                // 标记同步失败
                executor.execute(() -> {
                    stepRecord.setSyncStatus(2); // 设置为同步失败
                    stepRecordDao.update(stepRecord);
                    
                    if (callback != null) {
                        callback.onSuccess(stepRecord); // 仍然返回本地数据
                    }
                });
            }
        });
    }
    
    /**
     * 获取用户所有步数记录
     * @param userId 用户ID
     * @return 步数记录LiveData
     */
    public LiveData<List<StepRecord>> getUserStepRecords(long userId) {
        return stepRecordDao.getStepRecordsByUserId(userId);
    }
    
    /**
     * 获取用户特定日期的步数记录
     * @param userId 用户ID
     * @param date 日期
     * @param forceRefresh 是否强制刷新
     * @param callback 回调
     */
    public void getStepRecordByDate(long userId, LocalDate date, boolean forceRefresh, final DataCallback<StepRecord> callback) {
        executor.execute(() -> {
            // 先从本地获取
            StepRecord localRecord = stepRecordDao.getStepRecordByDate(userId, date);
            
            // 如果本地有数据且不需要强制刷新，直接返回
            if (localRecord != null && !forceRefresh) {
                if (callback != null) {
                    callback.onSuccess(localRecord);
                }
                return;
            }
            
            // 如果未登录或需要强制刷新，尝试从网络获取
            if (preferenceManager.isLoggedIn()) {
                fetchStepRecordByDateFromServer(userId, date, localRecord, callback);
            } else if (callback != null) {
                // 未登录，返回本地数据
                callback.onSuccess(localRecord);
            }
        });
    }
    
    /**
     * 从服务器获取特定日期的步数记录
     * @param userId 用户ID
     * @param date 日期
     * @param localRecord 本地记录
     * @param callback 回调
     */
    private void fetchStepRecordByDateFromServer(long userId, LocalDate date, StepRecord localRecord, final DataCallback<StepRecord> callback) {
        String dateStr = date.format(DateTimeUtils.API_DATE_FORMAT);
        stepApiService.getUserStepRecordByDate(userId, dateStr).enqueue(new Callback<ApiResponse<StepRecordDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<StepRecordDTO>> call, Response<ApiResponse<StepRecordDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    executor.execute(() -> {
                        try {
                            // 转换为实体
                            StepRecord record = convertToStepRecord(response.body().getData());
                            
                            // 保存到本地数据库
                            if (localRecord != null) {
                                record.setId(localRecord.getId());
                                stepRecordDao.update(record);
                            } else {
                                long id = stepRecordDao.insert(record);
                                record.setId(id);
                            }
                            
                            if (callback != null) {
                                callback.onSuccess(record);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error saving step record from server", e);
                            if (callback != null) {
                                callback.onError(e.getMessage());
                            }
                        }
                    });
                } else {
                    // 服务器没有数据或获取失败，返回本地数据
                    if (callback != null) {
                        callback.onSuccess(localRecord);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<StepRecordDTO>> call, Throwable t) {
                Log.e(TAG, "Error fetching step record from server", t);
                // 返回本地数据
                if (callback != null) {
                    callback.onSuccess(localRecord);
                }
            }
        });
    }
    
    // 工具方法：将StepRecord实体转换为DTO
    private StepRecordDTO convertToStepRecordDTO(StepRecord record) {
        StepRecordDTO dto = new StepRecordDTO();
        dto.setId(record.getRemoteId());
        dto.setUserId(record.getUserId());
        dto.setStepCount(record.getStepCount());
        dto.setDistance(record.getDistance());
        dto.setCaloriesBurned(record.getCaloriesBurned());
        dto.setRecordDate(record.getRecordDate());
        dto.setSource(record.getSource());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setUpdatedAt(record.getUpdatedAt());
        return dto;
    }
    
    // 工具方法：将DTO转换为StepRecord实体
    private StepRecord convertToStepRecord(StepRecordDTO dto) {
        StepRecord record = new StepRecord();
        record.setRemoteId(dto.getId());
        record.setUserId(dto.getUserId());
        record.setStepCount(dto.getStepCount());
        record.setDistance(dto.getDistance());
        record.setCaloriesBurned(dto.getCaloriesBurned());
        record.setRecordDate(dto.getRecordDate());
        record.setSource(dto.getSource());
        record.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());
        record.setUpdatedAt(dto.getUpdatedAt() != null ? dto.getUpdatedAt() : LocalDateTime.now());
        record.setSyncStatus(1); // 从服务器获取的数据标记为已同步
        return record;
    }
    
    // 根据步数计算消耗的卡路里（简单估算）
    public BigDecimal calculateCalories(int steps) {
        // 每1000步消耗约40卡路里（简化估算）
        double calories = steps * 0.04;
        return BigDecimal.valueOf(calories).setScale(2, RoundingMode.HALF_UP);
    }
    
    // 根据步数计算行走距离（简单估算）
    public BigDecimal calculateDistance(int steps) {
        // 假设平均每步0.7米
        double distance = steps * 0.7;
        return BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 数据回调接口
     * @param <T> 数据类型
     */
    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
} 