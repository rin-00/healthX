package com.healthx.repository;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.healthx.database.AppDatabase;
import com.healthx.database.dao.DietDao;
import com.healthx.model.Diet;
import com.healthx.network.ApiClient;
import com.healthx.network.DietApiService;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DietRepository {
    private static final String TAG = "DietRepository";
    
    private DietDao dietDao;
    private DietApiService dietApiService;
    
    public DietRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        dietDao = db.dietDao();
        dietApiService = ApiClient.getClient().create(DietApiService.class);
    }
    
    // 本地数据操作
    public void insert(Diet diet) {
        new InsertDietAsyncTask(dietDao).execute(diet);
    }
    
    public void update(Diet diet) {
        new UpdateDietAsyncTask(dietDao).execute(diet);
    }
    
    public void delete(Diet diet) {
        new DeleteDietAsyncTask(dietDao).execute(diet);
    }
    
    public LiveData<List<Diet>> getDietsByUserId(long userId) {
        return dietDao.getDietsByUserId(userId);
    }
    
    public LiveData<List<Diet>> getDietsByUserIdAndDateRange(long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return dietDao.getDietsByUserIdAndDateRange(userId, startTime, endTime);
    }
    
    public LiveData<List<Diet>> getDietsByUserIdAndMealType(long userId, String mealType) {
        return dietDao.getDietsByUserIdAndMealType(userId, mealType);
    }
    
    public LiveData<Double> getTotalCaloriesByDateRange(long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return dietDao.getTotalCaloriesByDateRange(userId, startTime, endTime);
    }
    
    public LiveData<Double> getTotalCaloriesForToday(long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        return getTotalCaloriesByDateRange(userId, startOfDay, endOfDay);
    }
    
    // 网络数据操作
    public LiveData<List<Diet>> fetchUserDietsFromServer(long userId) {
        MutableLiveData<List<Diet>> dietsData = new MutableLiveData<>();
        
        dietApiService.getUserDiets(userId).enqueue(new Callback<List<Diet>>() {
            @Override
            public void onResponse(Call<List<Diet>> call, Response<List<Diet>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dietsData.setValue(response.body());
                    // 将数据保存到本地数据库
                    for (Diet diet : response.body()) {
                        diet.setSyncStatus(1); // 标记为已同步
                        new InsertDietAsyncTask(dietDao).execute(diet);
                    }
                } else {
                    Log.e(TAG, "获取饮食记录失败: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<List<Diet>> call, Throwable t) {
                Log.e(TAG, "网络请求失败: " + t.getMessage());
            }
        });
        
        return dietsData;
    }
    
    public LiveData<Diet> saveDietToServer(Diet diet) {
        MutableLiveData<Diet> dietData = new MutableLiveData<>();
        
        try {
            Log.d(TAG, "正在保存饮食记录到服务器: " + diet.getFoodName() + ", 餐次: " + diet.getMealType() + 
                  ", 进食时间: " + diet.getEatenAt());
            
            dietApiService.addDiet(diet).enqueue(new Callback<Diet>() {
                @Override
                public void onResponse(Call<Diet> call, Response<Diet> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Diet savedDiet = response.body();
                        dietData.setValue(savedDiet);
                        
                        // 更新本地数据库中的记录，添加远程ID和同步状态
                        diet.setRemoteId(savedDiet.getId());
                        diet.setSyncStatus(1); // 已同步
                        new UpdateDietAsyncTask(dietDao).execute(diet);
                        
                        Log.d(TAG, "饮食记录保存成功，远程ID: " + savedDiet.getId());
                    } else {
                        Log.e(TAG, "保存饮食记录失败: " + response.message() + 
                             ", 错误码: " + response.code() + 
                             (response.errorBody() != null ? 
                             ", 错误内容: " + response.errorBody().toString() : ""));
                    }
                }
                
                @Override
                public void onFailure(Call<Diet> call, Throwable t) {
                    Log.e(TAG, "网络请求失败: " + t.getMessage(), t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "发送网络请求时发生异常: ", e);
        }
        
        return dietData;
    }
    
    public LiveData<Diet> updateDietOnServer(Diet diet) {
        MutableLiveData<Diet> dietData = new MutableLiveData<>();
        
        if (diet.getRemoteId() == null) {
            Log.e(TAG, "无法通过更新方式同步饮食记录，缺少远程ID，尝试通过新增方式同步");
            // 如果缺少远程ID，则执行保存操作而不是更新
            return saveDietToServer(diet);
        }
        
        try {
            Log.d(TAG, "正在更新服务器上的饮食记录: " + diet.getFoodName() + ", ID: " + diet.getRemoteId());
            
            dietApiService.updateDiet(diet.getRemoteId(), diet).enqueue(new Callback<Diet>() {
                @Override
                public void onResponse(Call<Diet> call, Response<Diet> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Diet updatedDiet = response.body();
                        dietData.setValue(updatedDiet);
                        
                        // 更新本地数据库中的记录
                        diet.setSyncStatus(1); // 已同步
                        new UpdateDietAsyncTask(dietDao).execute(diet);
                        
                        Log.d(TAG, "饮食记录更新成功，远程ID: " + diet.getRemoteId());
                    } else {
                        Log.e(TAG, "更新饮食记录失败: " + response.message() + 
                              ", 错误码: " + response.code() + 
                              (response.errorBody() != null ? 
                              ", 错误内容: " + response.errorBody().toString() : ""));
                    }
                }
                
                @Override
                public void onFailure(Call<Diet> call, Throwable t) {
                    Log.e(TAG, "网络请求失败: " + t.getMessage(), t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "发送更新请求时发生异常: ", e);
        }
        
        return dietData;
    }
    
    public LiveData<Boolean> deleteDietOnServer(Diet diet) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        if (diet.getRemoteId() == null) {
            Log.e(TAG, "饮食记录缺少远程ID，直接从本地数据库删除");
            // 如果缺少远程ID，则直接从本地数据库中删除
            new DeleteDietAsyncTask(dietDao).execute(diet);
            result.setValue(true);
            return result;
        }
        
        try {
            Log.d(TAG, "正在从服务器删除饮食记录，ID: " + diet.getRemoteId());
            
            dietApiService.deleteDiet(diet.getRemoteId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        result.setValue(true);
                        // 从本地数据库中删除记录
                        new DeleteDietAsyncTask(dietDao).execute(diet);
                        Log.d(TAG, "饮食记录删除成功，远程ID: " + diet.getRemoteId());
                    } else {
                        Log.e(TAG, "删除饮食记录失败: " + response.message() + 
                              ", 错误码: " + response.code());
                        result.setValue(false);
                    }
                }
                
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
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
    
    // 同步未同步的本地数据到服务器
    public void syncUnsyncedData() {
        Log.d(TAG, "开始同步未同步的数据到服务器...");
        new SyncUnsyncedDataAsyncTask(dietDao, this).execute();
    }
    
    // 异步任务类
    private static class InsertDietAsyncTask extends AsyncTask<Diet, Void, Void> {
        private DietDao dietDao;
        
        InsertDietAsyncTask(DietDao dietDao) {
            this.dietDao = dietDao;
        }
        
        @Override
        protected Void doInBackground(Diet... diets) {
            dietDao.insert(diets[0]);
            return null;
        }
    }
    
    private static class UpdateDietAsyncTask extends AsyncTask<Diet, Void, Void> {
        private DietDao dietDao;
        
        UpdateDietAsyncTask(DietDao dietDao) {
            this.dietDao = dietDao;
        }
        
        @Override
        protected Void doInBackground(Diet... diets) {
            dietDao.update(diets[0]);
            return null;
        }
    }
    
    private static class DeleteDietAsyncTask extends AsyncTask<Diet, Void, Void> {
        private DietDao dietDao;
        
        DeleteDietAsyncTask(DietDao dietDao) {
            this.dietDao = dietDao;
        }
        
        @Override
        protected Void doInBackground(Diet... diets) {
            dietDao.delete(diets[0]);
            return null;
        }
    }
    
    private static class SyncUnsyncedDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private DietDao dietDao;
        private DietRepository repository;
        
        SyncUnsyncedDataAsyncTask(DietDao dietDao, DietRepository repository) {
            this.dietDao = dietDao;
            this.repository = repository;
        }
        
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                List<Diet> unsyncedDiets = dietDao.getUnsyncedDiets();
                Log.d("DietRepository", "找到 " + unsyncedDiets.size() + " 条未同步的饮食记录");
                
                for (Diet diet : unsyncedDiets) {
                    try {
                        Log.d("DietRepository", "正在同步饮食记录: " + diet.getFoodName() + ", 同步状态: " + diet.getSyncStatus());
                        
                        switch (diet.getSyncStatus()) {
                            case 0: // 未同步
                                repository.saveDietToServer(diet);
                                break;
                            case 2: // 需要更新
                                repository.updateDietOnServer(diet);
                                break;
                            case 3: // 需要删除
                                repository.deleteDietOnServer(diet);
                                break;
                        }
                        
                        // 添加延迟，避免请求过于频繁
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Log.e("DietRepository", "同步延迟被中断: " + e.getMessage());
                        }
                    } catch (Exception e) {
                        Log.e("DietRepository", "同步单条饮食记录时发生异常: " + e.getMessage(), e);
                        // 继续处理下一条记录
                    }
                }
            } catch (Exception e) {
                Log.e("DietRepository", "同步饮食记录时发生异常: " + e.getMessage(), e);
            }
            return null;
        }
    }
} 