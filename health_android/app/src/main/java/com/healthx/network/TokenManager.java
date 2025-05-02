package com.healthx.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Token管理器，用于保存和获取用户token
 */
public class TokenManager {
    
    private static final String TAG = "TokenManager";
    private static final String PREF_NAME = "HealthXPrefs";
    private static final String KEY_TOKEN = "user_token";
    private static final String KEY_USERNAME = "user_name";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    
    private static TokenManager instance;
    private SharedPreferences sharedPreferences;
    
    private TokenManager() {
        // 私有构造函数
    }
    
    public static synchronized TokenManager getInstance() {
        if (instance == null) {
            instance = new TokenManager();
        }
        return instance;
    }
    
    public void init(Context context) {
        try {
            if (context != null) {
                sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                Log.d(TAG, "TokenManager initialized successfully");
            } else {
                Log.e(TAG, "Failed to initialize TokenManager: Context is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing TokenManager: " + e.getMessage());
        }
    }
    
    /**
     * 检查TokenManager是否已初始化
     */
    public boolean isInitialized() {
        return sharedPreferences != null;
    }
    
    /**
     * 获取SharedPreferences对象
     */
    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }
    
    /**
     * 保存用户Token
     */
    public void saveToken(String token) {
        if (sharedPreferences == null) {
            Log.e(TAG, "Cannot save token: SharedPreferences not initialized");
            return;
        }
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_TOKEN, token);
            editor.apply();
            Log.d(TAG, "Token saved successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error saving token: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户Token
     */
    public String getToken() {
        if (sharedPreferences == null) {
            Log.e(TAG, "Cannot get token: SharedPreferences not initialized");
            return null;
        }
        try {
            return sharedPreferences.getString(KEY_TOKEN, null);
        } catch (Exception e) {
            Log.e(TAG, "Error getting token: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 保存用户信息
     */
    public void saveUserInfo(Long userId, String username, String email) {
        if (sharedPreferences == null) {
            Log.e(TAG, "Cannot save user info: SharedPreferences not initialized");
            return;
        }
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(KEY_USER_ID, userId);
            editor.putString(KEY_USERNAME, username);
            editor.putString(KEY_USER_EMAIL, email);
            editor.apply();
            Log.d(TAG, "User info saved successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error saving user info: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户ID
     */
    public Long getUserId() {
        if (sharedPreferences == null) {
            Log.e(TAG, "Cannot get user ID: SharedPreferences not initialized");
            return -1L;
        }
        try {
            return sharedPreferences.getLong(KEY_USER_ID, -1);
        } catch (Exception e) {
            Log.e(TAG, "Error getting user ID: " + e.getMessage());
            return -1L;
        }
    }
    
    /**
     * 获取用户名
     */
    public String getUsername() {
        if (sharedPreferences == null) {
            Log.e(TAG, "Cannot get username: SharedPreferences not initialized");
            return null;
        }
        try {
            return sharedPreferences.getString(KEY_USERNAME, null);
        } catch (Exception e) {
            Log.e(TAG, "Error getting username: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取用户邮箱
     */
    public String getUserEmail() {
        if (sharedPreferences == null) {
            Log.e(TAG, "Cannot get user email: SharedPreferences not initialized");
            return null;
        }
        try {
            return sharedPreferences.getString(KEY_USER_EMAIL, null);
        } catch (Exception e) {
            Log.e(TAG, "Error getting user email: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 清除用户信息
     */
    public void clearUserInfo() {
        if (sharedPreferences == null) {
            Log.e(TAG, "Cannot clear user info: SharedPreferences not initialized");
            return;
        }
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(KEY_TOKEN);
            editor.remove(KEY_USER_ID);
            editor.remove(KEY_USERNAME);
            editor.remove(KEY_USER_EMAIL);
            editor.apply();
            Log.d(TAG, "User info cleared successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing user info: " + e.getMessage());
        }
    }
    
    /**
     * 用户是否已登录
     */
    public boolean isLoggedIn() {
        return getToken() != null;
    }
} 