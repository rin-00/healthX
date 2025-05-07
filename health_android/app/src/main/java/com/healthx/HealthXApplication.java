package com.healthx;

import android.app.Application;
import android.util.Log;

import com.healthx.database.AppDatabase;
import com.healthx.network.TokenManager;
import com.jakewharton.threetenabp.AndroidThreeTen;

/**
 * 自定义Application类，用于全局初始化
 */
public class HealthXApplication extends Application {
    
    private static final String TAG = "HealthXApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 临时代码：重置数据库（仅开发环境使用，发布前移除！）
        // AppDatabase.resetDatabase(this); // 注释此行以避免每次启动清除数据
        
        // 确保数据库实例已初始化
        AppDatabase.getInstance(this);
        Log.d(TAG, "数据库已初始化");
        
        // 初始化TokenManager
        try {
            TokenManager.getInstance().init(this);
            Log.d(TAG, "TokenManager initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing TokenManager: " + e.getMessage());
        }
        
        // 初始化ThreeTenABP，提供Java 8日期时间API的向后兼容性
        AndroidThreeTen.init(this);
        Log.d(TAG, "AndroidThreeTen initialized");
        
        // 其他全局初始化
        Log.d(TAG, "Application initialized");
    }
} 