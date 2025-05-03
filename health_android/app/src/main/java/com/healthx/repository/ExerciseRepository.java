package com.healthx.repository;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.healthx.database.AppDatabase;
import com.healthx.database.dao.ExerciseDao;
import com.healthx.model.Exercise;
import com.healthx.network.ApiClient;
import com.healthx.network.ExerciseApiService;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExerciseRepository {
    private static final String TAG = "ExerciseRepository";
    
    private ExerciseDao exerciseDao;
    private ExerciseApiService exerciseApiService;
    
    public ExerciseRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        exerciseDao = db.exerciseDao();
        exerciseApiService = ApiClient.getClient().create(ExerciseApiService.class);
    }
    
    // 本地数据操作
    public void insert(Exercise exercise) {
        new InsertExerciseAsyncTask(exerciseDao).execute(exercise);
    }
    
    public void update(Exercise exercise) {
        new UpdateExerciseAsyncTask(exerciseDao).execute(exercise);
    }
    
    public void delete(Exercise exercise) {
        new DeleteExerciseAsyncTask(exerciseDao).execute(exercise);
    }
    
    public LiveData<List<Exercise>> getExercisesByUserId(long userId) {
        return exerciseDao.getExercisesByUserId(userId);
    }
    
    public LiveData<List<Exercise>> getExercisesByUserIdAndDateRange(long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return exerciseDao.getExercisesByUserIdAndDateRange(userId, startTime, endTime);
    }
    
    public LiveData<List<Exercise>> getExercisesByUserIdAndType(long userId, String exerciseType) {
        return exerciseDao.getExercisesByUserIdAndType(userId, exerciseType);
    }
    
    public LiveData<Double> getTotalCaloriesBurnedByDateRange(long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return exerciseDao.getTotalCaloriesBurnedByDateRange(userId, startTime, endTime);
    }
    
    public LiveData<Double> getTotalCaloriesBurnedForToday(long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        return getTotalCaloriesBurnedByDateRange(userId, startOfDay, endOfDay);
    }
    
    // 网络数据操作
    public LiveData<List<Exercise>> fetchUserExercisesFromServer(long userId) {
        MutableLiveData<List<Exercise>> exercisesData = new MutableLiveData<>();
        
        exerciseApiService.getUserExercises(userId).enqueue(new Callback<List<Exercise>>() {
            @Override
            public void onResponse(Call<List<Exercise>> call, Response<List<Exercise>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    exercisesData.setValue(response.body());
                    // 将数据保存到本地数据库
                    for (Exercise exercise : response.body()) {
                        exercise.setSyncStatus(1); // 标记为已同步
                        new InsertExerciseAsyncTask(exerciseDao).execute(exercise);
                    }
                } else {
                    Log.e(TAG, "获取运动记录失败: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<List<Exercise>> call, Throwable t) {
                Log.e(TAG, "网络请求失败: " + t.getMessage());
            }
        });
        
        return exercisesData;
    }
    
    public LiveData<Exercise> saveExerciseToServer(Exercise exercise) {
        MutableLiveData<Exercise> exerciseData = new MutableLiveData<>();
        
        try {
            Log.d(TAG, "正在保存运动记录到服务器: " + exercise.getExerciseName() + ", 时间: " + exercise.getExercisedAt());
            
            exerciseApiService.addExercise(exercise).enqueue(new Callback<Exercise>() {
                @Override
                public void onResponse(Call<Exercise> call, Response<Exercise> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Exercise savedExercise = response.body();
                        exerciseData.setValue(savedExercise);
                        
                        // 更新本地数据库中的记录，添加远程ID和同步状态
                        exercise.setRemoteId(savedExercise.getId());
                        exercise.setSyncStatus(1); // 已同步
                        new UpdateExerciseAsyncTask(exerciseDao).execute(exercise);
                        
                        Log.d(TAG, "运动记录保存成功，远程ID: " + savedExercise.getId());
                    } else {
                        Log.e(TAG, "保存运动记录失败: " + response.message() + 
                             ", 错误码: " + response.code() + 
                             (response.errorBody() != null ? 
                             ", 错误内容: " + response.errorBody().toString() : ""));
                    }
                }
                
                @Override
                public void onFailure(Call<Exercise> call, Throwable t) {
                    Log.e(TAG, "网络请求失败: " + t.getMessage(), t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "发送网络请求时发生异常: ", e);
        }
        
        return exerciseData;
    }
    
    public LiveData<Exercise> updateExerciseOnServer(Exercise exercise) {
        MutableLiveData<Exercise> exerciseData = new MutableLiveData<>();
        
        if (exercise.getRemoteId() == null) {
            Log.e(TAG, "无法通过更新方式同步运动记录，缺少远程ID，尝试通过新增方式同步");
            // 如果缺少远程ID，则执行保存操作而不是更新
            return saveExerciseToServer(exercise);
        }
        
        try {
            Log.d(TAG, "正在更新服务器上的运动记录: " + exercise.getExerciseName() + ", ID: " + exercise.getRemoteId());
            
            exerciseApiService.updateExercise(exercise.getRemoteId(), exercise).enqueue(new Callback<Exercise>() {
                @Override
                public void onResponse(Call<Exercise> call, Response<Exercise> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Exercise updatedExercise = response.body();
                        exerciseData.setValue(updatedExercise);
                        
                        // 更新本地数据库中的记录
                        exercise.setSyncStatus(1); // 已同步
                        new UpdateExerciseAsyncTask(exerciseDao).execute(exercise);
                        
                        Log.d(TAG, "运动记录更新成功，远程ID: " + exercise.getRemoteId());
                    } else {
                        Log.e(TAG, "更新运动记录失败: " + response.message() + 
                              ", 错误码: " + response.code() + 
                              (response.errorBody() != null ? 
                              ", 错误内容: " + response.errorBody().toString() : ""));
                    }
                }
                
                @Override
                public void onFailure(Call<Exercise> call, Throwable t) {
                    Log.e(TAG, "网络请求失败: " + t.getMessage(), t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "发送更新请求时发生异常: ", e);
        }
        
        return exerciseData;
    }
    
    public LiveData<Boolean> deleteExerciseOnServer(Exercise exercise) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        if (exercise.getRemoteId() == null) {
            Log.e(TAG, "运动记录缺少远程ID，直接从本地数据库删除");
            // 如果缺少远程ID，则直接从本地数据库中删除
            new DeleteExerciseAsyncTask(exerciseDao).execute(exercise);
            result.setValue(true);
            return result;
        }
        
        try {
            Log.d(TAG, "正在从服务器删除运动记录，ID: " + exercise.getRemoteId());
            
            exerciseApiService.deleteExercise(exercise.getRemoteId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        result.setValue(true);
                        // 从本地数据库中删除记录
                        new DeleteExerciseAsyncTask(exerciseDao).execute(exercise);
                        Log.d(TAG, "运动记录删除成功，远程ID: " + exercise.getRemoteId());
                    } else {
                        Log.e(TAG, "删除运动记录失败: " + response.message() + 
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
        Log.d(TAG, "开始同步未同步的运动数据到服务器...");
        new SyncUnsyncedDataAsyncTask(exerciseDao, this).execute();
    }
    
    // 异步任务类
    private static class InsertExerciseAsyncTask extends AsyncTask<Exercise, Void, Void> {
        private ExerciseDao exerciseDao;
        
        InsertExerciseAsyncTask(ExerciseDao exerciseDao) {
            this.exerciseDao = exerciseDao;
        }
        
        @Override
        protected Void doInBackground(Exercise... exercises) {
            exerciseDao.insert(exercises[0]);
            return null;
        }
    }
    
    private static class UpdateExerciseAsyncTask extends AsyncTask<Exercise, Void, Void> {
        private ExerciseDao exerciseDao;
        
        UpdateExerciseAsyncTask(ExerciseDao exerciseDao) {
            this.exerciseDao = exerciseDao;
        }
        
        @Override
        protected Void doInBackground(Exercise... exercises) {
            exerciseDao.update(exercises[0]);
            return null;
        }
    }
    
    private static class DeleteExerciseAsyncTask extends AsyncTask<Exercise, Void, Void> {
        private ExerciseDao exerciseDao;
        
        DeleteExerciseAsyncTask(ExerciseDao exerciseDao) {
            this.exerciseDao = exerciseDao;
        }
        
        @Override
        protected Void doInBackground(Exercise... exercises) {
            exerciseDao.delete(exercises[0]);
            return null;
        }
    }
    
    private static class SyncUnsyncedDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private ExerciseDao exerciseDao;
        private ExerciseRepository repository;
        
        SyncUnsyncedDataAsyncTask(ExerciseDao exerciseDao, ExerciseRepository repository) {
            this.exerciseDao = exerciseDao;
            this.repository = repository;
        }
        
        @Override
        protected Void doInBackground(Void... voids) {
            List<Exercise> unsyncedExercises = exerciseDao.getUnsyncedExercises();
            for (Exercise exercise : unsyncedExercises) {
                try {
                    switch (exercise.getSyncStatus()) {
                        case 0: // 未同步，需要添加到服务器
                            repository.saveExerciseToServer(exercise);
                            break;
                        case 2: // 需要更新
                            repository.updateExerciseOnServer(exercise);
                            break;
                        case 3: // 需要删除
                            repository.deleteExerciseOnServer(exercise);
                            break;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "同步运动记录时发生异常: " + e.getMessage(), e);
                }
            }
            return null;
        }
    }
} 