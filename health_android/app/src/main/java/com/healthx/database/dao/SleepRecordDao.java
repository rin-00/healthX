package com.healthx.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.healthx.model.SleepRecord;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalDate;

import java.util.List;

@Dao
public interface SleepRecordDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SleepRecord sleepRecord);
    
    @Update
    void update(SleepRecord sleepRecord);
    
    @Delete
    void delete(SleepRecord sleepRecord);
    
    @Query("DELETE FROM sleep_records WHERE id = :id")
    void deleteById(long id);
    
    @Query("SELECT * FROM sleep_records WHERE id = :id")
    LiveData<SleepRecord> getById(long id);
    
    @Query("SELECT * FROM sleep_records WHERE id = :id")
    SleepRecord getByIdSync(long id);
    
    @Query("SELECT * FROM sleep_records WHERE userId = :userId ORDER BY startTime DESC")
    LiveData<List<SleepRecord>> getAllByUserId(long userId);
    
    @Query("SELECT * FROM sleep_records WHERE userId = :userId ORDER BY startTime DESC")
    List<SleepRecord> getAllByUserIdSync(long userId);
    
    @Query("SELECT * FROM sleep_records WHERE userId = :userId AND date(startTime) = date(:date) LIMIT 1")
    LiveData<SleepRecord> getByUserIdAndDate(long userId, LocalDateTime date);
    
    @Query("SELECT * FROM sleep_records WHERE userId = :userId AND date(startTime) = :date")
    List<SleepRecord> getByUserIdAndDateSync(long userId, String date);
    
    @Query("DELETE FROM sleep_records WHERE userId = :userId AND date(startTime) = :date AND id != :exceptId")
    void deleteByDateExcept(long userId, String date, long exceptId);
    
    @Query("SELECT * FROM sleep_records WHERE userId = :userId AND startTime >= :startDate AND startTime <= :endDate ORDER BY startTime DESC")
    LiveData<List<SleepRecord>> getByDateRange(long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT * FROM sleep_records WHERE userId = :userId AND startTime >= :sevenDaysAgo ORDER BY startTime DESC")
    LiveData<List<SleepRecord>> getLast7Days(long userId, LocalDateTime sevenDaysAgo);
    
    // 同步相关方法
    @Query("SELECT * FROM sleep_records WHERE syncStatus = :syncStatus AND userId = :userId")
    List<SleepRecord> getBySyncStatus(int syncStatus, long userId);
    
    @Query("UPDATE sleep_records SET remoteId = :remoteId, syncStatus = :syncStatus WHERE id = :id")
    void updateSyncStatus(long id, Long remoteId, int syncStatus);
} 