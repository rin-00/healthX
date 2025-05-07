package com.healthx.repository;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.healthx.database.AppDatabase;
import com.healthx.database.dao.WeightRecordDao;
import com.healthx.model.WeightRecord;
import com.healthx.model.dto.WeightRecordDTO;
import com.healthx.network.ApiClient;
import com.healthx.network.ApiResponse;
import com.healthx.network.WeightApi;
import com.healthx.util.DateTimeUtils;
import com.healthx.util.NetworkUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 体重记录仓库类
 */
public class WeightRepository {
    private static final String TAG = "WeightRepository";
    
    private final WeightRecordDao weightRecordDao;
    private final WeightApi weightApi;
    private final ExecutorService executorService;
    private final Context context;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    public WeightRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        weightRecordDao = db.weightRecordDao();
        weightApi = ApiClient.getInstance().create(WeightApi.class);
        executorService = Executors.newFixedThreadPool(4);
        this.context = context;
    }
    
    /**
     * 插入体重记录
     */
    public LiveData<Resource<WeightRecord>> insert(WeightRecord record) {
        MutableLiveData<Resource<WeightRecord>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        
        // 创建异步任务检查当日是否已有记录
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                Date date = new Date(record.getMeasurementTime());
                return !hasRecordForDayInternal(record.getUserId(), date);
            }
            
            @Override
            protected void onPostExecute(Boolean canInsert) {
                if (!canInsert) {
                    // 当日已有记录，返回错误
                    result.postValue(Resource.error("当日已有体重记录，不能重复添加", null));
                    return;
                }
                
                // 先保存到本地数据库
                executorService.execute(() -> {
                    long id = weightRecordDao.insert(record);
                    record.setId(id);
                    
                    // 检查网络连接
                    if (NetworkUtils.isNetworkConnected(context)) {
                        // 转换为DTO并发送到服务器
                        WeightRecordDTO recordDTO = new WeightRecordDTO(record);
                        weightApi.addWeightRecord(recordDTO).enqueue(new Callback<ApiResponse<WeightRecordDTO>>() {
                            @Override
                            public void onResponse(@NonNull Call<ApiResponse<WeightRecordDTO>> call, 
                                                  @NonNull Response<ApiResponse<WeightRecordDTO>> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                    WeightRecordDTO remoteRecordDTO = response.body().getData();
                                    if (remoteRecordDTO != null) {
                                        // 更新本地记录的远程ID和同步状态
                                        record.setRemoteId(remoteRecordDTO.getRemoteId());
                                        record.setSyncStatus(1); // 已同步
                                        executorService.execute(() -> weightRecordDao.update(record));
                                    }
                                    result.postValue(Resource.success(record));
                                } else {
                                    String errorMsg = "服务器响应错误";
                                    if (response.body() != null) {
                                        errorMsg = response.body().getMessage();
                                    }
                                    record.setSyncStatus(2); // 同步失败
                                    executorService.execute(() -> weightRecordDao.update(record));
                                    result.postValue(Resource.error(errorMsg, record));
                                }
                            }
                            
                            @Override
                            public void onFailure(@NonNull Call<ApiResponse<WeightRecordDTO>> call, @NonNull Throwable t) {
                                record.setSyncStatus(2); // 同步失败
                                executorService.execute(() -> weightRecordDao.update(record));
                                result.postValue(Resource.error("网络请求失败: " + t.getMessage(), record));
                            }
                        });
                    } else {
                        // 无网络连接，仅保存本地
                        result.postValue(Resource.success(record));
                    }
                });
            }
        }.execute();
        
        return result;
    }
    
    /**
     * 更新体重记录
     */
    public LiveData<Resource<WeightRecord>> update(WeightRecord record) {
        MutableLiveData<Resource<WeightRecord>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        
        // 先更新本地数据库
        executorService.execute(() -> {
            record.setSyncStatus(2); // 需要更新
            weightRecordDao.update(record);
            
            // 检查网络连接
            if (NetworkUtils.isNetworkConnected(context)) {
                // 转换为DTO并发送到服务器
                WeightRecordDTO recordDTO = new WeightRecordDTO(record);
                
                if (record.getRemoteId() != null) {
                    // 有远程ID，执行更新操作
                    weightApi.updateWeightRecord(record.getRemoteId(), recordDTO).enqueue(new Callback<ApiResponse<WeightRecordDTO>>() {
                        @Override
                        public void onResponse(@NonNull Call<ApiResponse<WeightRecordDTO>> call, 
                                              @NonNull Response<ApiResponse<WeightRecordDTO>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                record.setSyncStatus(1); // 已同步
                                executorService.execute(() -> weightRecordDao.update(record));
                                result.postValue(Resource.success(record));
                            } else {
                                String errorMsg = "服务器响应错误";
                                if (response.body() != null) {
                                    errorMsg = response.body().getMessage();
                                }
                                result.postValue(Resource.error(errorMsg, record));
                            }
                        }
                        
                        @Override
                        public void onFailure(@NonNull Call<ApiResponse<WeightRecordDTO>> call, @NonNull Throwable t) {
                            result.postValue(Resource.error("网络请求失败: " + t.getMessage(), record));
                        }
                    });
                } else {
                    // 无远程ID，执行添加操作
                    weightApi.addWeightRecord(recordDTO).enqueue(new Callback<ApiResponse<WeightRecordDTO>>() {
                        @Override
                        public void onResponse(@NonNull Call<ApiResponse<WeightRecordDTO>> call, 
                                              @NonNull Response<ApiResponse<WeightRecordDTO>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                WeightRecordDTO remoteRecordDTO = response.body().getData();
                                if (remoteRecordDTO != null) {
                                    record.setRemoteId(remoteRecordDTO.getRemoteId());
                                    record.setSyncStatus(1); // 已同步
                                    executorService.execute(() -> weightRecordDao.update(record));
                                }
                                result.postValue(Resource.success(record));
                            } else {
                                String errorMsg = "服务器响应错误";
                                if (response.body() != null) {
                                    errorMsg = response.body().getMessage();
                                }
                                result.postValue(Resource.error(errorMsg, record));
                            }
                        }
                        
                        @Override
                        public void onFailure(@NonNull Call<ApiResponse<WeightRecordDTO>> call, @NonNull Throwable t) {
                            result.postValue(Resource.error("网络请求失败: " + t.getMessage(), record));
                        }
                    });
                }
            } else {
                // 无网络连接，仅保存本地
                result.postValue(Resource.success(record));
            }
        });
        
        return result;
    }
    
    /**
     * 删除体重记录
     */
    public LiveData<Resource<Boolean>> delete(WeightRecord record) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        
        if (record.getRemoteId() != null && NetworkUtils.isNetworkConnected(context)) {
            // 如果有远程ID且网络连接正常，从服务器删除
            weightApi.deleteWeightRecord(record.getRemoteId()).enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Void>> call, 
                                      @NonNull Response<ApiResponse<Void>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        // 从本地数据库删除
                        executorService.execute(() -> weightRecordDao.delete(record));
                        result.postValue(Resource.success(true));
                    } else {
                        String errorMsg = "服务器响应错误";
                        if (response.body() != null) {
                            errorMsg = response.body().getMessage();
                        }
                        result.postValue(Resource.error(errorMsg, false));
                    }
                }
                
                @Override
                public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                    // 标记为需要删除，稍后同步
                    record.setSyncStatus(3); // 需要删除
                    executorService.execute(() -> weightRecordDao.update(record));
                    result.postValue(Resource.error("网络请求失败: " + t.getMessage(), true));
                }
            });
        } else {
            // 无网络连接或无远程ID
            if (record.getRemoteId() == null) {
                // 如果没有远程ID，直接删除本地记录
                executorService.execute(() -> weightRecordDao.delete(record));
                result.postValue(Resource.success(true));
            } else {
                // 标记为需要删除，稍后同步
                record.setSyncStatus(3); // 需要删除
                executorService.execute(() -> weightRecordDao.update(record));
                result.postValue(Resource.success(true));
            }
        }
        
        return result;
    }
    
    /**
     * 同步数据
     */
    public LiveData<Resource<Boolean>> syncData(long userId) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        
        if (!NetworkUtils.isNetworkConnected(context)) {
            result.postValue(Resource.error("无网络连接", false));
            return result;
        }
        
        // 使用异步任务执行同步
        new SyncAsyncTask(weightRecordDao, weightApi, result, context).execute(userId);
        return result;
    }
    
    /**
     * 从服务器刷新用户体重记录
     */
    private void refreshWeightRecords(long userId) {
        if (NetworkUtils.isNetworkConnected(context)) {
            weightApi.getWeightRecordsByUserId(userId).enqueue(new Callback<ApiResponse<List<WeightRecordDTO>>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<List<WeightRecordDTO>>> call, 
                                      @NonNull Response<ApiResponse<List<WeightRecordDTO>>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<WeightRecordDTO> remoteRecordDTOs = response.body().getData();
                        if (remoteRecordDTOs != null && !remoteRecordDTOs.isEmpty()) {
                            executorService.execute(() -> {
                                // 将远程记录更新到本地数据库
                                for (WeightRecordDTO remoteRecordDTO : remoteRecordDTOs) {
                                    WeightRecord localRecord = weightRecordDao.getById(remoteRecordDTO.getId());
                                    
                                    // 转换为实体类
                                    WeightRecord remoteRecord = remoteRecordDTO.toWeightRecord();
                                    
                                    if (localRecord == null) {
                                        // 本地不存在，添加
                                        remoteRecord.setSyncStatus(1); // 已同步
                                        weightRecordDao.insert(remoteRecord);
                                    } else if (localRecord.getSyncStatus() != 2 && localRecord.getSyncStatus() != 3) {
                                        // 本地存在且未修改，更新
                                        remoteRecord.setId(localRecord.getId());
                                        remoteRecord.setSyncStatus(1); // 已同步
                                        weightRecordDao.update(remoteRecord);
                                    }
                                    // 如果本地已修改或标记删除，则不更新
                                }
                            });
                        }
                    } else {
                        Log.e(TAG, "刷新体重记录失败: " + response.message());
                    }
                }
                
                @Override
                public void onFailure(@NonNull Call<ApiResponse<List<WeightRecordDTO>>> call, @NonNull Throwable t) {
                    Log.e(TAG, "刷新体重记录失败: " + t.getMessage());
                }
            });
        }
    }
    
    /**
     * 获取用户的所有体重记录
     */
    public LiveData<List<WeightRecord>> getByUserId(long userId) {
        refreshWeightRecords(userId);
        return weightRecordDao.getByUserId(userId);
    }
    
    /**
     * 根据ID获取体重记录
     */
    public WeightRecord getById(long id) {
        return weightRecordDao.getById(id);
    }
    
    /**
     * 获取用户最新的体重记录
     */
    public WeightRecord getLatestByUserId(long userId) {
        return weightRecordDao.getLatestByUserId(userId);
    }
    
    /**
     * 获取用户指定日期的体重记录
     */
    public List<WeightRecord> getByUserIdAndDate(long userId, Date date) {
        long timestamp = date.getTime();
        return weightRecordDao.getByUserIdAndDate(userId, timestamp);
    }
    
    /**
     * 获取用户指定日期范围的体重记录
     */
    public LiveData<List<WeightRecord>> getByUserIdAndDateRange(long userId, Date startDate, Date endDate) {
        long startTime = startDate.getTime();
        long endTime = endDate.getTime();
        return weightRecordDao.getByUserIdAndDateRange(userId, startTime, endTime);
    }
    
    /**
     * 获取用户最近30天的体重记录
     */
    public LiveData<List<WeightRecord>> getLast30DaysByUserId(long userId) {
        long startTime = DateTimeUtils.getDateBefore(30).getTime();
        return weightRecordDao.getLast30DaysByUserId(userId, startTime);
    }
    
    /**
     * 检查用户当天是否已有体重记录
     */
    public boolean hasRecordForToday(long userId) {
        long today = DateTimeUtils.startOfDay(new Date()).getTime();
        return weightRecordDao.countByUserIdAndDate(userId, today) > 0;
    }
    
    /**
     * 检查用户指定日期是否已有体重记录 (内部使用)
     */
    private boolean hasRecordForDayInternal(long userId, Date date) {
        long dayStart = DateTimeUtils.startOfDay(date).getTime();
        return weightRecordDao.countByUserIdAndDate(userId, dayStart) > 0;
    }
    
    /**
     * 获取用户体重统计数据（最大值、最小值、平均值）
     */
    public float[] getWeightStats(long userId) {
        return weightRecordDao.getWeightStats(userId);
    }
    
    /**
     * 计算BMI
     * BMI = 体重(kg) / (身高(m) * 身高(m))
     */
    public static float calculateBMI(float weight, float heightInCm) {
        if (heightInCm <= 0 || weight <= 0) {
            return 0;
        }
        float heightInM = heightInCm / 100f;
        return weight / (heightInM * heightInM);
    }
    
    /**
     * 计算BMI (支持Double类型参数)
     * BMI = 体重(kg) / (身高(m) * 身高(m))
     */
    public static float calculateBMI(Double weight, Double heightInCm) {
        if (heightInCm == null || weight == null || heightInCm <= 0 || weight <= 0) {
            return 0;
        }
        float weightF = weight.floatValue();
        float heightInCmF = heightInCm.floatValue();
        return calculateBMI(weightF, heightInCmF);
    }
    
    /**
     * 获取BMI状态描述
     */
    public static String getBmiStatus(float bmi) {
        if (bmi <= 0) {
            return "未知";
        } else if (bmi < 18.5) {
            return "偏瘦";
        } else if (bmi < 24) {
            return "正常";
        } else if (bmi < 28) {
            return "超重";
        } else {
            return "肥胖";
        }
    }
    
    /**
     * 数据同步异步任务
     */
    private static class SyncAsyncTask extends AsyncTask<Long, Void, Boolean> {
        private final WeightRecordDao dao;
        private final WeightApi api;
        private final MutableLiveData<Resource<Boolean>> result;
        private final Context context;
        private String errorMessage;
        
        SyncAsyncTask(WeightRecordDao dao, WeightApi api, MutableLiveData<Resource<Boolean>> result, Context context) {
            this.dao = dao;
            this.api = api;
            this.result = result;
            this.context = context;
        }
        
        @Override
        protected Boolean doInBackground(Long... userIds) {
            if (userIds.length == 0) return false;
            long userId = userIds[0];
            boolean success = true;
            
            try {
                // 同步本地未同步的记录到服务器
                
                // 1. 获取需要添加的记录(syncStatus = 0)
                List<WeightRecord> recordsToAdd = dao.getBySyncStatus(0);
                for (WeightRecord record : recordsToAdd) {
                    try {
                        // 转换为DTO
                        WeightRecordDTO recordDTO = new WeightRecordDTO(record);
                        Response<ApiResponse<WeightRecordDTO>> response = api.addWeightRecord(recordDTO).execute();
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            WeightRecordDTO remoteRecordDTO = response.body().getData();
                            if (remoteRecordDTO != null) {
                                record.setRemoteId(remoteRecordDTO.getRemoteId());
                                record.setSyncStatus(1); // 已同步
                                dao.update(record);
                            }
                        } else {
                            success = false;
                        }
                    } catch (Exception e) {
                        Log.e("WeightRepository", "同步添加记录失败: " + e.getMessage());
                        success = false;
                    }
                }
                
                // 2. 获取需要更新的记录(syncStatus = 2)
                List<WeightRecord> recordsToUpdate = dao.getBySyncStatus(2);
                for (WeightRecord record : recordsToUpdate) {
                    if (record.getRemoteId() != null) {
                        try {
                            // 转换为DTO
                            WeightRecordDTO recordDTO = new WeightRecordDTO(record);
                            Response<ApiResponse<WeightRecordDTO>> response = api.updateWeightRecord(record.getRemoteId(), recordDTO).execute();
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                record.setSyncStatus(1); // 已同步
                                dao.update(record);
                            } else {
                                success = false;
                            }
                        } catch (Exception e) {
                            Log.e("WeightRepository", "同步更新记录失败: " + e.getMessage());
                            success = false;
                        }
                    } else {
                        try {
                            // 如果没有远程ID，尝试添加
                            WeightRecordDTO recordDTO = new WeightRecordDTO(record);
                            Response<ApiResponse<WeightRecordDTO>> response = api.addWeightRecord(recordDTO).execute();
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                WeightRecordDTO remoteRecordDTO = response.body().getData();
                                if (remoteRecordDTO != null) {
                                    record.setRemoteId(remoteRecordDTO.getRemoteId());
                                    record.setSyncStatus(1); // 已同步
                                    dao.update(record);
                                }
                            } else {
                                success = false;
                            }
                        } catch (Exception e) {
                            Log.e("WeightRepository", "同步未有远程ID的记录失败: " + e.getMessage());
                            success = false;
                        }
                    }
                }
                
                // 3. 处理需要删除的记录(syncStatus = 3)
                List<WeightRecord> recordsToDelete = dao.getBySyncStatus(3);
                for (WeightRecord record : recordsToDelete) {
                    if (record.getRemoteId() != null) {
                        try {
                            Response<ApiResponse<Void>> response = api.deleteWeightRecord(record.getRemoteId()).execute();
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                dao.delete(record);
                            } else {
                                success = false;
                            }
                        } catch (Exception e) {
                            Log.e("WeightRepository", "同步删除记录失败: " + e.getMessage());
                            success = false;
                        }
                    } else {
                        // 无远程ID，直接删除本地记录
                        dao.delete(record);
                    }
                }
                
                // 4. 从服务器拉取最新数据
                if (NetworkUtils.isNetworkConnected(context)) {
                    try {
                        Response<ApiResponse<List<WeightRecordDTO>>> response = api.getWeightRecordsByUserId(userId).execute();
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            List<WeightRecordDTO> remoteRecordDTOs = response.body().getData();
                            if (remoteRecordDTOs != null) {
                                for (WeightRecordDTO remoteRecordDTO : remoteRecordDTOs) {
                                    // 转换为实体类
                                    WeightRecord remoteRecord = remoteRecordDTO.toWeightRecord();
                                    
                                    WeightRecord localRecord = dao.getByRemoteId(remoteRecord.getRemoteId());
                                    if (localRecord == null) {
                                        // 本地不存在，添加
                                        remoteRecord.setSyncStatus(1); // 已同步
                                        dao.insert(remoteRecord);
                                    } else if (localRecord.getSyncStatus() != 2 && localRecord.getSyncStatus() != 3) {
                                        // 本地存在且未修改/删除，更新
                                        remoteRecord.setId(localRecord.getId());
                                        remoteRecord.setSyncStatus(1); // 已同步
                                        dao.update(remoteRecord);
                                    }
                                }
                            }
                        } else {
                            success = false;
                            errorMessage = "从服务器获取数据失败";
                        }
                    } catch (Exception e) {
                        Log.e("WeightRepository", "获取服务器数据失败: " + e.getMessage());
                        success = false;
                        errorMessage = "网络异常: " + e.getMessage();
                    }
                }
                
                return success;
                
            } catch (Exception e) {
                Log.e("WeightRepository", "数据同步过程发生异常: " + e.getMessage());
                errorMessage = "同步异常: " + e.getMessage();
                return false;
            }
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                result.postValue(Resource.success(true));
            } else {
                result.postValue(Resource.error(errorMessage != null ? errorMessage : "同步失败", false));
            }
        }
    }
} 