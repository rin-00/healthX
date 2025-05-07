package com.healthx.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.healthx.model.StepRecord;

import java.time.LocalDate;
import java.util.List;

/**
 * 步数记录数据访问对象
 */
@Dao
public interface StepRecordDao {
    
    /**
     * 插入步数记录
     * @param stepRecord 步数记录
     * @return 插入的记录ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(StepRecord stepRecord);
    
    /**
     * 更新步数记录
     * @param stepRecord 步数记录
     * @return 更新的行数
     */
    @Update
    int update(StepRecord stepRecord);
    
    /**
     * 删除步数记录
     * @param stepRecord 步数记录
     * @return 删除的行数
     */
    @Delete
    int delete(StepRecord stepRecord);
    
    /**
     * 获取特定ID的步数记录
     * @param id 记录ID
     * @return 步数记录
     */
    @Query("SELECT * FROM step_records WHERE id = :id")
    StepRecord getStepRecordById(long id);
    
    /**
     * 获取用户所有步数记录
     * @param userId 用户ID
     * @return 步数记录列表LiveData
     */
    @Query("SELECT * FROM step_records WHERE user_id = :userId ORDER BY record_date DESC")
    LiveData<List<StepRecord>> getStepRecordsByUserId(long userId);
    
    /**
     * 获取用户特定日期的步数记录
     * @param userId 用户ID
     * @param date 日期
     * @return 步数记录
     */
    @Query("SELECT * FROM step_records WHERE user_id = :userId AND record_date = :date")
    StepRecord getStepRecordByDate(long userId, LocalDate date);
    
    /**
     * 获取用户特定日期范围的步数记录
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 步数记录列表
     */
    @Query("SELECT * FROM step_records WHERE user_id = :userId AND record_date BETWEEN :startDate AND :endDate ORDER BY record_date DESC")
    List<StepRecord> getStepRecordsByDateRange(long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取未同步的步数记录
     * @return 未同步的步数记录列表
     */
    @Query("SELECT * FROM step_records WHERE sync_status = 0")
    List<StepRecord> getUnsyncedStepRecords();
    
    /**
     * 获取用户特定日期范围的步数总和
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 步数总和
     */
    @Query("SELECT SUM(step_count) FROM step_records WHERE user_id = :userId AND record_date BETWEEN :startDate AND :endDate")
    int getTotalStepsByDateRange(long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户特定日期范围的平均步数
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 平均步数
     */
    @Query("SELECT AVG(step_count) FROM step_records WHERE user_id = :userId AND record_date BETWEEN :startDate AND :endDate")
    double getAverageStepsByDateRange(long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户特定日期范围内的最大步数
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 最大步数
     */
    @Query("SELECT MAX(step_count) FROM step_records WHERE user_id = :userId AND record_date BETWEEN :startDate AND :endDate")
    int getMaxStepsByDateRange(long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户特定日期范围内的最小步数
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 最小步数
     */
    @Query("SELECT MIN(step_count) FROM step_records WHERE user_id = :userId AND record_date BETWEEN :startDate AND :endDate")
    int getMinStepsByDateRange(long userId, LocalDate startDate, LocalDate endDate);
} 