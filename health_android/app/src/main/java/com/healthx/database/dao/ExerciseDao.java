package com.healthx.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.healthx.model.Exercise;

import org.threeten.bp.LocalDateTime;
import java.util.List;

@Dao
public interface ExerciseDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Exercise exercise);
    
    @Update
    void update(Exercise exercise);
    
    @Delete
    void delete(Exercise exercise);
    
    @Query("SELECT * FROM exercises WHERE id = :id")
    Exercise getExerciseById(long id);
    
    @Query("SELECT * FROM exercises WHERE remoteId = :remoteId")
    Exercise getExerciseByRemoteId(long remoteId);
    
    @Query("SELECT * FROM exercises WHERE userId = :userId ORDER BY exercisedAt DESC")
    LiveData<List<Exercise>> getExercisesByUserId(long userId);
    
    @Query("SELECT * FROM exercises WHERE userId = :userId AND exercisedAt BETWEEN :startTime AND :endTime ORDER BY exercisedAt DESC")
    LiveData<List<Exercise>> getExercisesByUserIdAndDateRange(long userId, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT * FROM exercises WHERE userId = :userId AND exerciseType = :exerciseType ORDER BY exercisedAt DESC")
    LiveData<List<Exercise>> getExercisesByUserIdAndType(long userId, String exerciseType);
    
    @Query("SELECT SUM(caloriesBurned) FROM exercises WHERE userId = :userId AND exercisedAt BETWEEN :startTime AND :endTime")
    LiveData<Double> getTotalCaloriesBurnedByDateRange(long userId, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT * FROM exercises WHERE syncStatus != 1")
    List<Exercise> getUnsyncedExercises();
    
    @Query("UPDATE exercises SET syncStatus = :status WHERE id = :id")
    void updateSyncStatus(long id, int status);
    
    @Query("UPDATE exercises SET remoteId = :remoteId, syncStatus = 1 WHERE id = :id")
    void updateRemoteId(long id, long remoteId);
} 