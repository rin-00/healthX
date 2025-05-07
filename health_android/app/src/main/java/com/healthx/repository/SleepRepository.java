package com.healthx.repository;

import android.content.Context;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.healthx.database.AppDatabase;
import com.healthx.database.dao.SleepRecordDao;
import com.healthx.model.SleepRecord;
import com.healthx.model.SleepRecordDTO;
import com.healthx.network.ApiResponse;
import com.healthx.network.RetrofitClient;
import com.healthx.network.SleepApiService;
import com.healthx.util.NetworkUtils;
import com.healthx.util.DateTimeUtils;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SleepRepository {
    
    private static SleepRepository instance;
    private final SleepRecordDao sleepRecordDao;
    private final SleepApiService sleepApiService;
    private final Context context;
    
    private SleepRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        sleepRecordDao = database.sleepRecordDao();
        sleepApiService = RetrofitClient.getInstance(context).createService(SleepApiService.class);
        this.context = context;
    }
    
    public static synchronized SleepRepository getInstance(Context context) {
        if (instance == null) {
            instance = new SleepRepository(context);
        }
        return instance;
    }
    
    // 添加睡眠记录
    public LiveData<Resource<SleepRecord>> addSleepRecord(SleepRecord sleepRecord) {
        MutableLiveData<Resource<SleepRecord>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));
        
        // 创建异步任务来检查是否存在重复记录并执行插入
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                // 先检查该日期是否已有记录（同步方式）
                if (sleepRecord.getStartTime() != null) {
                    String dateStr = sleepRecord.getStartTime().toLocalDate().toString();
                    List<SleepRecord> existingRecords = sleepRecordDao.getByUserIdAndDateSync(
                            sleepRecord.getUserId(), dateStr);
                    
                    if (existingRecords != null && !existingRecords.isEmpty()) {
                        // 如果已有记录，返回false表示有重复
                        return false;
                    }
                }
                return true; // 没有重复记录
            }
            
            @Override
            protected void onPostExecute(Boolean canInsert) {
                if (!canInsert) {
                    // 有重复记录，返回错误
                    result.setValue(Resource.error("该日期已有睡眠记录，不能重复添加", null));
                    return;
                }
                
                // 没有重复记录，继续插入操作
                // 先存储到本地数据库
                new InsertAsyncTask(sleepRecordDao, newId -> {
                    sleepRecord.setId(newId);
                    
                    // 检查网络连接
                    if (NetworkUtils.isNetworkConnected(context)) {
                        // 创建DTO对象
                        SleepRecordDTO dto = new SleepRecordDTO(sleepRecord);
                        
                        // 发送到服务器
                        sleepApiService.addSleepRecord(dto).enqueue(new Callback<SleepRecordDTO>() {
                            @Override
                            public void onResponse(Call<SleepRecordDTO> call, Response<SleepRecordDTO> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    SleepRecordDTO remoteDto = response.body();
                                    sleepRecord.setRemoteId(remoteDto.getId());
                                    sleepRecord.setSyncStatus(1); // 已同步
                                    new UpdateAsyncTask(sleepRecordDao).execute(sleepRecord);
                                    result.setValue(Resource.success(sleepRecord));
                                } else {
                                    result.setValue(Resource.error("服务器响应错误", sleepRecord));
                                }
                            }
                            
                            @Override
                            public void onFailure(Call<SleepRecordDTO> call, Throwable t) {
                                result.setValue(Resource.error("网络请求失败: " + t.getMessage(), sleepRecord));
                            }
                        });
                    } else {
                        // 无网络连接，仅保存本地
                        result.setValue(Resource.success(sleepRecord));
                    }
                }).execute(sleepRecord);
            }
        }.execute();
        
        return result;
    }
    
    // 获取睡眠记录（通过ID）
    public LiveData<SleepRecord> getSleepRecordById(long id) {
        return sleepRecordDao.getById(id);
    }
    
    // 获取用户所有睡眠记录
    public LiveData<List<SleepRecord>> getUserSleepRecords(long userId) {
        refreshSleepRecords(userId);
        return sleepRecordDao.getAllByUserId(userId);
    }
    
    // 获取用户指定日期的睡眠记录
    public LiveData<SleepRecord> getUserSleepRecordForDate(long userId, LocalDate date) {
        LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.MIDNIGHT);
        return sleepRecordDao.getByUserIdAndDate(userId, dateTime);
    }
    
    // 获取用户指定日期范围的睡眠记录
    public LiveData<List<SleepRecord>> getUserSleepRecordsByDateRange(long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.MIDNIGHT);
        LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.MAX);
        return sleepRecordDao.getByDateRange(userId, startDateTime, endDateTime);
    }
    
    // 获取用户最近7天的睡眠记录
    public LiveData<List<SleepRecord>> getUserLast7DaysSleepRecords(long userId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIDNIGHT);
        return sleepRecordDao.getLast7Days(userId, sevenDaysAgo);
    }
    
    // 更新睡眠记录
    public LiveData<Resource<SleepRecord>> updateSleepRecord(SleepRecord sleepRecord) {
        MutableLiveData<Resource<SleepRecord>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));
        
        // 创建异步任务来检查是否存在重复记录并执行更新
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                // 检查是否修改了日期，如果修改了需要确保新日期没有重复记录
                if (sleepRecord.getStartTime() != null && sleepRecord.getId() > 0) {
                    // 获取原记录
                    SleepRecord originalRecord = sleepRecordDao.getByIdSync(sleepRecord.getId());
                    
                    if (originalRecord != null && originalRecord.getStartTime() != null) {
                        LocalDate originalDate = originalRecord.getStartTime().toLocalDate();
                        LocalDate newDate = sleepRecord.getStartTime().toLocalDate();
                        
                        // 如果日期发生变化，检查新日期是否已有记录
                        if (!originalDate.equals(newDate)) {
                            String dateStr = newDate.toString();
                            List<SleepRecord> existingRecords = sleepRecordDao.getByUserIdAndDateSync(
                                    sleepRecord.getUserId(), dateStr);
                            
                            if (existingRecords != null && !existingRecords.isEmpty()) {
                                // 如果已有记录，表示不能更新
                                return false;
                            }
                        }
                    }
                }
                return true; // 允许更新
            }
            
            @Override
            protected void onPostExecute(Boolean canUpdate) {
                if (!canUpdate) {
                    // 有重复记录，返回错误
                    result.setValue(Resource.error("新日期已有睡眠记录，不能重复添加", null));
                    return;
                }
                
                // 先更新本地数据库
                sleepRecord.setSyncStatus(2); // 需要更新
                new UpdateAsyncTask(sleepRecordDao).execute(sleepRecord);
                
                // 检查网络连接
                if (NetworkUtils.isNetworkConnected(context)) {
                    // 创建DTO对象
                    SleepRecordDTO dto = new SleepRecordDTO(sleepRecord);
                    
                    // 发送到服务器
                    if (sleepRecord.getRemoteId() != null) {
                        sleepApiService.updateSleepRecord(sleepRecord.getRemoteId(), dto).enqueue(new Callback<SleepRecordDTO>() {
                            @Override
                            public void onResponse(Call<SleepRecordDTO> call, Response<SleepRecordDTO> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    SleepRecordDTO remoteDto = response.body();
                                    sleepRecord.setSyncStatus(1); // 已同步
                                    new UpdateAsyncTask(sleepRecordDao).execute(sleepRecord);
                                    result.setValue(Resource.success(sleepRecord));
                                } else {
                                    result.setValue(Resource.error("服务器响应错误", sleepRecord));
                                }
                            }
                            
                            @Override
                            public void onFailure(Call<SleepRecordDTO> call, Throwable t) {
                                result.setValue(Resource.error("网络请求失败: " + t.getMessage(), sleepRecord));
                            }
                        });
                    } else {
                        // 如果没有remoteId，则执行添加操作
                        sleepApiService.addSleepRecord(dto).enqueue(new Callback<SleepRecordDTO>() {
                            @Override
                            public void onResponse(Call<SleepRecordDTO> call, Response<SleepRecordDTO> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    SleepRecordDTO remoteDto = response.body();
                                    sleepRecord.setRemoteId(remoteDto.getId());
                                    sleepRecord.setSyncStatus(1); // 已同步
                                    new UpdateAsyncTask(sleepRecordDao).execute(sleepRecord);
                                    result.setValue(Resource.success(sleepRecord));
                                } else {
                                    result.setValue(Resource.error("服务器响应错误", sleepRecord));
                                }
                            }
                            
                            @Override
                            public void onFailure(Call<SleepRecordDTO> call, Throwable t) {
                                result.setValue(Resource.error("网络请求失败: " + t.getMessage(), sleepRecord));
                            }
                        });
                    }
                } else {
                    // 无网络连接，仅保存本地
                    result.setValue(Resource.success(sleepRecord));
                }
            }
        }.execute();
        
        return result;
    }
    
    // 删除睡眠记录
    public LiveData<Resource<Boolean>> deleteSleepRecord(SleepRecord sleepRecord) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));
        
        if (sleepRecord.getRemoteId() != null && NetworkUtils.isNetworkConnected(context)) {
            // 如果有远程ID且网络连接正常，从服务器删除
            sleepApiService.deleteSleepRecord(sleepRecord.getRemoteId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        // 从本地数据库删除
                        new DeleteAsyncTask(sleepRecordDao).execute(sleepRecord);
                        result.setValue(Resource.success(true));
                    } else {
                        result.setValue(Resource.error("服务器响应错误", false));
                    }
                }
                
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // 标记为需要删除，稍后同步
                    sleepRecord.setSyncStatus(3); // 需要删除
                    new UpdateAsyncTask(sleepRecordDao).execute(sleepRecord);
                    result.setValue(Resource.error("网络请求失败: " + t.getMessage(), true));
                }
            });
        } else {
            // 无网络连接或无远程ID
            if (sleepRecord.getRemoteId() == null) {
                // 如果没有远程ID，直接删除本地记录
                new DeleteAsyncTask(sleepRecordDao).execute(sleepRecord);
                result.setValue(Resource.success(true));
            } else {
                // 标记为需要删除，稍后同步
                sleepRecord.setSyncStatus(3); // 需要删除
                new UpdateAsyncTask(sleepRecordDao).execute(sleepRecord);
                result.setValue(Resource.success(true));
            }
        }
        
        return result;
    }
    
    // 从服务器刷新睡眠记录
    private void refreshSleepRecords(long userId) {
        if (NetworkUtils.isNetworkConnected(context)) {
            sleepApiService.getUserSleepRecords(userId).enqueue(new Callback<List<SleepRecordDTO>>() {
                @Override
                public void onResponse(Call<List<SleepRecordDTO>> call, Response<List<SleepRecordDTO>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<SleepRecord> records = new ArrayList<>();
                        for (SleepRecordDTO dto : response.body()) {
                            records.add(dto.toEntity());
                        }
                        new InsertAllAsyncTask(sleepRecordDao, userId).execute(records);
                    }
                }
                
                @Override
                public void onFailure(Call<List<SleepRecordDTO>> call, Throwable t) {
                    // 处理失败
                }
            });
        }
    }
    
    // 同步本地与远程数据
    public LiveData<Resource<Boolean>> syncData(long userId) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));
        
        if (!NetworkUtils.isNetworkConnected(context)) {
            result.setValue(Resource.error("无网络连接", false));
            return result;
        }
        
        new SyncAsyncTask(sleepRecordDao, sleepApiService, result).execute(userId);
        return result;
    }
    
    // 清理重复的睡眠记录（确保每天每个用户只有一条记录）
    public LiveData<Resource<Boolean>> cleanupDuplicateRecords(long userId) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));
        
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    // 获取用户所有记录
                    List<SleepRecord> allRecords = sleepRecordDao.getAllByUserIdSync(userId);
                    
                    // 按日期分组，保留每个日期最新的一条记录
                    Map<String, List<SleepRecord>> recordsByDate = new HashMap<>();
                    
                    // 按日期分组
                    for (SleepRecord record : allRecords) {
                        if (record.getStartTime() != null) {
                            String dateKey = record.getStartTime().toLocalDate().toString();
                            if (!recordsByDate.containsKey(dateKey)) {
                                recordsByDate.put(dateKey, new ArrayList<>());
                            }
                            recordsByDate.get(dateKey).add(record);
                        }
                    }
                    
                    // 对于每个日期，保留最新的一条记录（根据ID或远程ID）
                    int cleanedCount = 0;
                    for (String dateKey : recordsByDate.keySet()) {
                        List<SleepRecord> recordsForDate = recordsByDate.get(dateKey);
                        if (recordsForDate.size() > 1) {
                            // 按照ID或远程ID排序，找出最新的记录
                            SleepRecord newestRecord = recordsForDate.get(0);
                            for (SleepRecord record : recordsForDate) {
                                if (record.getRemoteId() != null) {
                                    if (newestRecord.getRemoteId() == null || 
                                        record.getRemoteId() > newestRecord.getRemoteId()) {
                                        newestRecord = record;
                                    }
                                } else if (newestRecord.getRemoteId() == null && 
                                           record.getId() > newestRecord.getId()) {
                                    newestRecord = record;
                                }
                            }
                            
                            // 删除其他记录
                            for (SleepRecord record : recordsForDate) {
                                if (!record.equals(newestRecord)) {
                                    sleepRecordDao.delete(record);
                                    cleanedCount++;
                                }
                            }
                        }
                    }
                    
                    return cleanedCount > 0;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            
            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    result.setValue(Resource.success(true));
                } else {
                    result.setValue(Resource.success(false));
                }
            }
        }.execute();
        
        return result;
    }
    
    // 异步任务类
    
    // 插入操作
    private static class InsertAsyncTask extends AsyncTask<SleepRecord, Void, Long> {
        private final SleepRecordDao dao;
        private final OnInsertComplete listener;
        
        public interface OnInsertComplete {
            void onInsertComplete(long id);
        }
        
        InsertAsyncTask(SleepRecordDao dao, OnInsertComplete listener) {
            this.dao = dao;
            this.listener = listener;
        }
        
        @Override
        protected Long doInBackground(SleepRecord... sleepRecords) {
            return dao.insert(sleepRecords[0]);
        }
        
        @Override
        protected void onPostExecute(Long id) {
            listener.onInsertComplete(id);
        }
    }
    
    // 批量插入操作
    private static class InsertAllAsyncTask extends AsyncTask<List<SleepRecord>, Void, Void> {
        private final SleepRecordDao dao;
        private final long userId;
        
        InsertAllAsyncTask(SleepRecordDao dao, long userId) {
            this.dao = dao;
            this.userId = userId;
        }
        
        @SafeVarargs
        @Override
        protected final Void doInBackground(List<SleepRecord>... lists) {
            if (lists.length > 0 && lists[0] != null) {
                // 获取现有记录
                List<SleepRecord> existingRecords = dao.getAllByUserIdSync(userId);
                
                // 新记录按日期分组，确保每天只有一条记录
                List<SleepRecord> uniqueRecords = new ArrayList<>();
                
                // 记录已处理的日期
                Set<String> processedDates = new HashSet<>();
                
                // 从远程获取的记录，按照ID倒序排序（确保最新的记录优先）
                List<SleepRecord> remoteRecords = new ArrayList<>(lists[0]);
                Collections.sort(remoteRecords, (a, b) -> Long.compare(b.getRemoteId() != null ? b.getRemoteId() : 0,
                                                                     a.getRemoteId() != null ? a.getRemoteId() : 0));
                
                // 仅保留每天最新的一条记录
                for (SleepRecord record : remoteRecords) {
                    if (record.getStartTime() != null) {
                        String dateKey = record.getStartTime().toLocalDate().toString();
                        if (!processedDates.contains(dateKey)) {
                            uniqueRecords.add(record);
                            processedDates.add(dateKey);
                        }
                    }
                }
                
                // 删除本地已有的与远程同步的相同日期的记录
                for (SleepRecord existingRecord : existingRecords) {
                    if (existingRecord.getStartTime() != null && 
                        existingRecord.getRemoteId() != null && 
                        processedDates.contains(existingRecord.getStartTime().toLocalDate().toString())) {
                        dao.delete(existingRecord);
                    }
                }
                
                // 插入唯一的记录
                for (SleepRecord record : uniqueRecords) {
                    dao.insert(record);
                }
            }
            return null;
        }
    }
    
    // 更新操作
    private static class UpdateAsyncTask extends AsyncTask<SleepRecord, Void, Void> {
        private final SleepRecordDao dao;
        
        UpdateAsyncTask(SleepRecordDao dao) {
            this.dao = dao;
        }
        
        @Override
        protected Void doInBackground(SleepRecord... sleepRecords) {
            dao.update(sleepRecords[0]);
            return null;
        }
    }
    
    // 删除操作
    private static class DeleteAsyncTask extends AsyncTask<SleepRecord, Void, Void> {
        private final SleepRecordDao dao;
        
        DeleteAsyncTask(SleepRecordDao dao) {
            this.dao = dao;
        }
        
        @Override
        protected Void doInBackground(SleepRecord... sleepRecords) {
            dao.delete(sleepRecords[0]);
            return null;
        }
    }
    
    // 同步操作
    private static class SyncAsyncTask extends AsyncTask<Long, Void, Boolean> {
        private final SleepRecordDao dao;
        private final SleepApiService apiService;
        private final MutableLiveData<Resource<Boolean>> result;
        private String errorMessage;
        
        SyncAsyncTask(SleepRecordDao dao, SleepApiService apiService, MutableLiveData<Resource<Boolean>> result) {
            this.dao = dao;
            this.apiService = apiService;
            this.result = result;
        }
        
        @Override
        protected Boolean doInBackground(Long... userIds) {
            try {
                long userId = userIds[0];
                // 获取未同步的记录
                List<SleepRecord> unsyncedRecords = dao.getBySyncStatus(0, userId);
                for (SleepRecord record : unsyncedRecords) {
                    try {
                        SleepRecordDTO dto = new SleepRecordDTO(record);
                        Response<SleepRecordDTO> response = apiService.addSleepRecord(dto).execute();
                        if (response.isSuccessful() && response.body() != null) {
                            record.setRemoteId(response.body().getId());
                            record.setSyncStatus(1); // 已同步
                            dao.update(record);
                        }
                    } catch (Exception e) {
                        errorMessage = "同步新记录失败: " + e.getMessage();
                        return false;
                    }
                }
                
                // 获取需要更新的记录
                List<SleepRecord> updateRecords = dao.getBySyncStatus(2, userId);
                for (SleepRecord record : updateRecords) {
                    try {
                        SleepRecordDTO dto = new SleepRecordDTO(record);
                        if (record.getRemoteId() != null) {
                            Response<SleepRecordDTO> response = apiService.updateSleepRecord(record.getRemoteId(), dto).execute();
                            if (response.isSuccessful() && response.body() != null) {
                                record.setSyncStatus(1); // 已同步
                                dao.update(record);
                            }
                        } else {
                            // 如果没有远程ID，执行添加操作
                            Response<SleepRecordDTO> response = apiService.addSleepRecord(dto).execute();
                            if (response.isSuccessful() && response.body() != null) {
                                record.setRemoteId(response.body().getId());
                                record.setSyncStatus(1); // 已同步
                                dao.update(record);
                            }
                        }
                    } catch (Exception e) {
                        errorMessage = "同步更新记录失败: " + e.getMessage();
                        return false;
                    }
                }
                
                // 获取需要删除的记录
                List<SleepRecord> deleteRecords = dao.getBySyncStatus(3, userId);
                for (SleepRecord record : deleteRecords) {
                    try {
                        if (record.getRemoteId() != null) {
                            Response<Void> response = apiService.deleteSleepRecord(record.getRemoteId()).execute();
                            if (response.isSuccessful()) {
                                dao.delete(record);
                            }
                        } else {
                            dao.delete(record);
                        }
                    } catch (Exception e) {
                        errorMessage = "同步删除记录失败: " + e.getMessage();
                        return false;
                    }
                }
                
                return true;
            } catch (Exception e) {
                errorMessage = "同步过程中发生错误: " + e.getMessage();
                return false;
            }
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                result.setValue(Resource.success(true));
            } else {
                result.setValue(Resource.error(errorMessage, false));
            }
        }
    }
} 