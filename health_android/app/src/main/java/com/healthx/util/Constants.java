package com.healthx.util;

/**
 * 应用常量
 */
public class Constants {
    
    // API基础URL
    public static final String API_BASE_URL = "http://10.0.2.2:8080/";
    
    // SharedPreferences常量
    public static final String PREF_NAME = "HealthXPrefs";
    
    // Bundle键值
    public static final String KEY_USER = "user";
    
    // 正则表达式
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]{3,20}$";
    public static final String PASSWORD_PATTERN = "^[a-zA-Z0-9_-]{6,20}$";
} 