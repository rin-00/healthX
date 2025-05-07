package com.healthx.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.healthx.model.WeightRecord;

import java.util.List;

@Dao
public interface WeightRecordDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(WeightRecord weightRecord);
    
    @Update
    void update(WeightRecord weightRecord);
    
    @Delete
    void delete(WeightRecord weightRecord);
    
    @Query("DELETE FROM weight_records WHERE id = :id")
    void deleteById(long id);
    
    @Query("SELECT * FROM weight_records WHERE id = :id")
    WeightRecord getById(long id);
    
    @Query("SELECT * FROM weight_records WHERE userId = :userId ORDER BY measurementTime DESC")
    LiveData<List<WeightRecord>> getByUserId(long userId);
    
    @Query("SELECT * FROM weight_records WHERE userId = :userId ORDER BY measurementTime DESC LIMIT 1")
    WeightRecord getLatestByUserId(long userId);
    
    @Query("SELECT * FROM weight_records WHERE userId = :userId AND DATE(measurementTime/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')")
    List<WeightRecord> getByUserIdAndDate(long userId, long date);
    
    @Query("SELECT * FROM weight_records WHERE userId = :userId AND measurementTime BETWEEN :startTime AND :endTime ORDER BY measurementTime DESC")
    LiveData<List<WeightRecord>> getByUserIdAndDateRange(long userId, long startTime, long endTime);
    
    @Query("SELECT * FROM weight_records WHERE userId = :userId AND measurementTime >= :startTime ORDER BY measurementTime DESC")
    LiveData<List<WeightRecord>> getLast30DaysByUserId(long userId, long startTime);
    
    @Query("SELECT COUNT(*) FROM weight_records WHERE userId = :userId AND DATE(measurementTime/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')")
    int countByUserIdAndDate(long userId, long date);
    
    @Query("SELECT MAX(weight), MIN(weight), AVG(weight) FROM weight_records WHERE userId = :userId")
    float[] getWeightStats(long userId);
    
    @Query("SELECT * FROM weight_records WHERE syncStatus = :syncStatus")
    List<WeightRecord> getBySyncStatus(int syncStatus);
    
    @Query("SELECT * FROM weight_records WHERE userId = :userId AND syncStatus = :syncStatus")
    List<WeightRecord> getByUserIdAndSyncStatus(long userId, int syncStatus);
    
    /* 以下是根据重构计划新增的方法 */
    
    @Query("SELECT * FROM weight_records WHERE syncStatus != 1")
    List<WeightRecord> getUnsyncedRecords();
    
    @Query("UPDATE weight_records SET syncStatus = :status WHERE id = :id")
    void updateSyncStatus(long id, int status);
    
    @Query("UPDATE weight_records SET remoteId = :remoteId, syncStatus = 1 WHERE id = :id")
    void updateRemoteId(long id, long remoteId);
} 