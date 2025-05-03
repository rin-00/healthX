package com.healthx.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.healthx.model.Diet;

import org.threeten.bp.LocalDateTime;
import java.util.List;

@Dao
public interface DietDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Diet diet);
    
    @Update
    void update(Diet diet);
    
    @Delete
    void delete(Diet diet);
    
    @Query("SELECT * FROM diets WHERE id = :id")
    Diet getDietById(long id);
    
    @Query("SELECT * FROM diets WHERE remoteId = :remoteId")
    Diet getDietByRemoteId(long remoteId);
    
    @Query("SELECT * FROM diets WHERE userId = :userId ORDER BY eatenAt DESC")
    LiveData<List<Diet>> getDietsByUserId(long userId);
    
    @Query("SELECT * FROM diets WHERE userId = :userId AND eatenAt BETWEEN :startTime AND :endTime ORDER BY eatenAt DESC")
    LiveData<List<Diet>> getDietsByUserIdAndDateRange(long userId, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT * FROM diets WHERE userId = :userId AND mealType = :mealType ORDER BY eatenAt DESC")
    LiveData<List<Diet>> getDietsByUserIdAndMealType(long userId, String mealType);
    
    @Query("SELECT SUM(calories) FROM diets WHERE userId = :userId AND eatenAt BETWEEN :startTime AND :endTime")
    LiveData<Double> getTotalCaloriesByDateRange(long userId, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT * FROM diets WHERE syncStatus != 1")
    List<Diet> getUnsyncedDiets();
    
    @Query("UPDATE diets SET syncStatus = :status WHERE id = :id")
    void updateSyncStatus(long id, int status);
    
    @Query("UPDATE diets SET remoteId = :remoteId, syncStatus = 1 WHERE id = :id")
    void updateRemoteId(long id, long remoteId);
} 