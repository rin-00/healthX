package com.healthx.network;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Token管理器，用于保存和获取用户token
 */
public class TokenManager {
    
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
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * 保存用户Token
     */
    public void saveToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }
    
    /**
     * 获取用户Token
     */
    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }
    
    /**
     * 保存用户信息
     */
    public void saveUserInfo(Long userId, String username, String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }
    
    /**
     * 获取用户ID
     */
    public Long getUserId() {
        return sharedPreferences.getLong(KEY_USER_ID, -1);
    }
    
    /**
     * 获取用户名
     */
    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }
    
    /**
     * 获取用户邮箱
     */
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }
    
    /**
     * 清除用户信息
     */
    public void clearUserInfo() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_USER_EMAIL);
        editor.apply();
    }
    
    /**
     * 用户是否已登录
     */
    public boolean isLoggedIn() {
        return getToken() != null;
    }
} 