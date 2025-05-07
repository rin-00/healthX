package com.healthx.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.healthx.model.StepGoal;

import java.time.LocalDate;
import java.util.List;

/**
 * 步数目标数据访问对象
 */
@Dao
public interface StepGoalDao {
    
    /**
     * 插入步数目标
     * @param stepGoal 步数目标
     * @return 插入的记录ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(StepGoal stepGoal);
    
    /**
     * 更新步数目标
     * @param stepGoal 步数目标
     * @return 更新的行数
     */
    @Update
    int update(StepGoal stepGoal);
    
    /**
     * 删除步数目标
     * @param stepGoal 步数目标
     * @return 删除的行数
     */
    @Delete
    int delete(StepGoal stepGoal);
    
    /**
     * At 获取特定ID的步数目标
     * @param id 目标ID
     * @return 步数目标
     */
    @Query("SELECT * FROM step_goals WHERE id = :id")
    StepGoal getStepGoalById(long id);
    
    /**
     * 获取用户所有步数目标
     * @param userId 用户ID
     * @return 步数目标列表LiveData
     */
    @Query("SELECT * FROM step_goals WHERE user_id = :userId ORDER BY start_date DESC")
    LiveData<List<StepGoal>> getStepGoalsByUserId(long userId);
    
    /**
     * 获取用户当前激活的步数目标
     * @param userId 用户ID
     * @return 激活的步数目标列表
     */
    @Query("SELECT * FROM step_goals WHERE user_id = :userId AND is_active = 1 " +
           "AND (end_date IS NULL OR end_date >= :currentDate) " +
           "ORDER BY start_date DESC")
    List<StepGoal> getActiveStepGoals(long userId, LocalDate currentDate);
    
    /**
     * 获取用户在特定日期有效的步数目标
     * @param userId 用户ID
     * @param date 日期
     * @return 有效的步数目标
     */
    @Query("SELECT * FROM step_goals WHERE user_id = :userId " +
           "AND is_active = 1 " +
           "AND start_date <= :date " +
           "AND (end_date IS NULL OR end_date >= :date) " +
           "ORDER BY start_date DESC LIMIT 1")
    StepGoal getActiveStepGoalByDate(long userId, LocalDate date);
    
    /**
     * 停用所有用户目标
     * @param userId 用户ID
     * @return 更新的行数
     */
    @Query("UPDATE step_goals SET is_active = 0 WHERE user_id = :userId")
    int deactivateAllUserGoals(long userId);
    
    /**
     * 获取未同步的步数目标
     * @return 未同步的步数目标列表
     */
    @Query("SELECT * FROM step_goals WHERE sync_status = 0")
    List<StepGoal> getUnsyncedStepGoals();
} 