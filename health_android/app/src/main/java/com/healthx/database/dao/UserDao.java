package com.healthx.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.healthx.model.User;

import java.util.List;

@Dao
public interface UserDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUser(User user);
    
    @Update
    void updateUser(User user);
    
    @Delete
    void deleteUser(User user);
    
    @Query("SELECT * FROM users WHERE id = :userId")
    User getUserById(long userId);
    
    @Query("SELECT * FROM users")
    LiveData<List<User>> getAllUsers();
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);
    
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);
    
    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();
} 