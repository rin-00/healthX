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
import com.healthx.network.ApiClient;
import com.healthx.network.ApiResponse;
import com.healthx.network.WeightApi;
import com.healthx.util.DateTimeUtils;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 体重记录仓库类 - 重构后
 */
public class WeightRepository {
    private static final String TAG = "WeightRepository";
    
    private final WeightRecordDao weightRecordDao;
    private final WeightApi weightApi;
    private final Context context;
    
    public WeightRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        weightRecordDao = db.weightRecordDao();
        weightApi = ApiClient.getInstance().create(WeightApi.class);
        this.context = context;
    }
    
    // 本地数据操作 - 使用AsyncTask
    
    /**
     * 插入体重记录
     */
    public void insert(WeightRecord record) {
        record.setSyncStatus(0); // 未同步
        new InsertWeightRecordAsyncTask(weightRecordDao).execute(record);
    }
    
    /**
     * 更新体重记录
     */
    public void update(WeightRecord record) {
        record.setSyncStatus(2); // 需要更新
        new UpdateWeightRecordAsyncTask(weightRecordDao).execute(record);
    }
    
    /**
     * 删除体重记录
     */
    public void delete(WeightRecord record) {
        if (record.getRemoteId() != null) {
            record.setSyncStatus(3); // 需要删除
            new UpdateWeightRecordAsyncTask(weightRecordDao).execute(record);
        } else {
            new DeleteWeightRecordAsyncTask(weightRecordDao).execute(record);
        }
    }
    
    /**
     * 从服务器获取所有体重记录
     */
    public LiveData<List<WeightRecord>> fetchWeightRecordsFromServer(long userId) {
        MutableLiveData<List<WeightRecord>> recordsData = new MutableLiveData<>();
        
        weightApi.getWeightRecordsByUserId(userId).enqueue(new Callback<ApiResponse<List<WeightRecord>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<WeightRecord>>> call, 
                                @NonNull Response<ApiResponse<List<WeightRecord>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<WeightRecord> records = response.body().getData();
                    recordsData.setValue(records);
                    
                    // 保存到本地数据库
                    for (WeightRecord record : records) {
                        record.setSyncStatus(1); // 已同步
                        new InsertWeightRecordAsyncTask(weightRecordDao).execute(record);
                    }
                } else {
                    Log.e(TAG, "获取体重记录失败: " + response.message());
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<WeightRecord>>> call, @NonNull Throwable t) {
                Log.e(TAG, "网络请求失败: " + t.getMessage());
            }
        });
        
        return recordsData;
    }
    
    /**
     * 保存体重记录到服务器
     */
    public LiveData<WeightRecord> saveWeightRecordToServer(WeightRecord record) {
        MutableLiveData<WeightRecord> recordData = new MutableLiveData<>();
        
        try {
            Log.d(TAG, "正在保存体重记录到服务器: " + record.getWeight() + "kg");
            
            weightApi.addWeightRecord(record).enqueue(new Callback<ApiResponse<WeightRecord>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<WeightRecord>> call, 
                                    @NonNull Response<ApiResponse<WeightRecord>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        WeightRecord savedRecord = response.body().getData();
                        recordData.setValue(savedRecord);
                        
                        // 更新本地记录
                        record.setRemoteId(savedRecord.getId());
                        record.setSyncStatus(1); // 已同步
                        new UpdateWeightRecordAsyncTask(weightRecordDao).execute(record);
                        
                        Log.d(TAG, "体重记录保存成功，远程ID: " + savedRecord.getId());
                    } else {
                        Log.e(TAG, "保存体重记录失败: " + response.message());
                    }
                }
                
                @Override
                public void onFailure(@NonNull Call<ApiResponse<WeightRecord>> call, @NonNull Throwable t) {
                    Log.e(TAG, "网络请求失败: " + t.getMessage(), t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "发送网络请求时发生异常: ", e);
        }
        
        return recordData;
    }
    
    /**
     * 更新服务器上的体重记录
     */
    public LiveData<WeightRecord> updateWeightRecordOnServer(WeightRecord record) {
        MutableLiveData<WeightRecord> recordData = new MutableLiveData<>();
        
        if (record.getRemoteId() == null) {
            Log.e(TAG, "无法通过更新方式同步体重记录，缺少远程ID，尝试通过新增方式同步");
            return saveWeightRecordToServer(record);
        }
        
        try {
            Log.d(TAG, "正在更新服务器上的体重记录: " + record.getWeight() + "kg, ID: " + record.getRemoteId());
            
            weightApi.updateWeightRecord(record.getRemoteId(), record).enqueue(new Callback<ApiResponse<WeightRecord>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<WeightRecord>> call, 
                                    @NonNull Response<ApiResponse<WeightRecord>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        WeightRecord updatedRecord = response.body().getData();
                        recordData.setValue(updatedRecord);
                        
                        // 更新本地记录
                        record.setSyncStatus(1); // 已同步
                        new UpdateWeightRecordAsyncTask(weightRecordDao).execute(record);
                        
                        Log.d(TAG, "体重记录更新成功，远程ID: " + record.getRemoteId());
                    } else {
                        Log.e(TAG, "更新体重记录失败: " + response.message());
                    }
                }
                
                @Override
                public void onFailure(@NonNull Call<ApiResponse<WeightRecord>> call, @NonNull Throwable t) {
                    Log.e(TAG, "网络请求失败: " + t.getMessage(), t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "发送更新请求时发生异常: ", e);
        }
        
        return recordData;
    }
    
    /**
     * 从服务器删除体重记录
     */
    public LiveData<Boolean> deleteWeightRecordOnServer(WeightRecord record) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        if (record.getRemoteId() == null) {
            Log.e(TAG, "体重记录缺少远程ID，直接从本地数据库删除");
            // 如果缺少远程ID，则直接从本地数据库中删除
            new DeleteWeightRecordAsyncTask(weightRecordDao).execute(record);
            result.setValue(true);
            return result;
        }
        
        try {
            Log.d(TAG, "正在从服务器删除体重记录，ID: " + record.getRemoteId());
            
            weightApi.deleteWeightRecord(record.getRemoteId()).enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Void>> call, 
                                    @NonNull Response<ApiResponse<Void>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        result.setValue(true);
                        // 从本地数据库中删除记录
                        new DeleteWeightRecordAsyncTask(weightRecordDao).execute(record);
                        Log.d(TAG, "体重记录删除成功，远程ID: " + record.getRemoteId());
                    } else {
                        Log.e(TAG, "删除体重记录失败: " + response.message());
                        result.setValue(false);
                    }
                }
                
                @Override
                public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                    Log.e(TAG, "网络请求失败: " + t.getMessage(), t);
                    result.setValue(false);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "发送删除请求时发生异常: ", e);
            result.setValue(false);
        }
        
        return result;
    }
    
    /**
     * 同步未同步的数据到服务器
     */
    public void syncUnsyncedData() {
        Log.d(TAG, "开始同步未同步的体重数据到服务器...");
        new SyncUnsyncedDataAsyncTask(weightRecordDao, this).execute();
    }
    
    /**
     * 获取用户的所有体重记录
     */
    public LiveData<List<WeightRecord>> getByUserId(long userId) {
        // 尝试从服务器刷新数据
        fetchWeightRecordsFromServer(userId);
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
     * 检查用户当天是否已有体重记录
     */
    public boolean hasRecordForToday(long userId) {
        long today = DateTimeUtils.startOfDay(new Date()).getTime();
        return weightRecordDao.countByUserIdAndDate(userId, today) > 0;
    }
    
    // 其他方法保持不变...
    
    // 异步任务类
    private static class InsertWeightRecordAsyncTask extends AsyncTask<WeightRecord, Void, Void> {
        private WeightRecordDao weightRecordDao;
        
        InsertWeightRecordAsyncTask(WeightRecordDao weightRecordDao) {
            this.weightRecordDao = weightRecordDao;
        }
        
        @Override
        protected Void doInBackground(WeightRecord... records) {
            weightRecordDao.insert(records[0]);
            return null;
        }
    }
    
    private static class UpdateWeightRecordAsyncTask extends AsyncTask<WeightRecord, Void, Void> {
        private WeightRecordDao weightRecordDao;
        
        UpdateWeightRecordAsyncTask(WeightRecordDao weightRecordDao) {
            this.weightRecordDao = weightRecordDao;
        }
        
        @Override
        protected Void doInBackground(WeightRecord... records) {
            weightRecordDao.update(records[0]);
            return null;
        }
    }
    
    private static class DeleteWeightRecordAsyncTask extends AsyncTask<WeightRecord, Void, Void> {
        private WeightRecordDao weightRecordDao;
        
        DeleteWeightRecordAsyncTask(WeightRecordDao weightRecordDao) {
            this.weightRecordDao = weightRecordDao;
        }
        
        @Override
        protected Void doInBackground(WeightRecord... records) {
            weightRecordDao.delete(records[0]);
            return null;
        }
    }
    
    private static class SyncUnsyncedDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private WeightRecordDao weightRecordDao;
        private WeightRepository repository;
        
        SyncUnsyncedDataAsyncTask(WeightRecordDao weightRecordDao, WeightRepository repository) {
            this.weightRecordDao = weightRecordDao;
            this.repository = repository;
        }
        
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                List<WeightRecord> unsyncedRecords = weightRecordDao.getUnsyncedRecords();
                Log.d("WeightRepository", "找到 " + unsyncedRecords.size() + " 条未同步的体重记录");
                
                for (WeightRecord record : unsyncedRecords) {
                    try {
                        Log.d("WeightRepository", "正在同步体重记录: " + record.getWeight() + "kg, 同步状态: " + record.getSyncStatus());
                        
                        switch (record.getSyncStatus()) {
                            case 0: // 未同步
                                repository.saveWeightRecordToServer(record);
                                break;
                            case 2: // 需要更新
                                repository.updateWeightRecordOnServer(record);
                                break;
                            case 3: // 需要删除
                                repository.deleteWeightRecordOnServer(record);
                                break;
                        }
                        
                        // 添加延迟，避免请求过于频繁
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Log.e("WeightRepository", "同步延迟被中断: " + e.getMessage());
                        }
                    } catch (Exception e) {
                        Log.e("WeightRepository", "同步单条体重记录时发生异常: " + e.getMessage(), e);
                        // 继续处理下一条记录
                    }
                }
            } catch (Exception e) {
                Log.e("WeightRepository", "同步体重记录时发生异常: " + e.getMessage(), e);
            }
            return null;
        }
    }
} 