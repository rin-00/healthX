package com.healthx.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences工具类，用于管理应用的偏好设置
 */
public class PreferenceManager {
    
    private static final String PREF_NAME = "health_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * 保存用户ID
     */
    public static void saveUserId(Context context, long userId) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putLong(KEY_USER_ID, userId);
        editor.apply();
    }
    
    /**
     * 获取用户ID
     */
    public static long getUserId(Context context) {
        return getSharedPreferences(context).getLong(KEY_USER_ID, 1); // 默认返回1，表示默认用户
    }
    
    /**
     * 保存用户名
     */
    public static void saveUserName(Context context, String userName) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }
    
    /**
     * 获取用户名
     */
    public static String getUserName(Context context) {
        return getSharedPreferences(context).getString(KEY_USER_NAME, "");
    }
    
    /**
     * 保存用户邮箱
     */
    public static void saveUserEmail(Context context, String email) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }
    
    /**
     * 获取用户邮箱
     */
    public static String getUserEmail(Context context) {
        return getSharedPreferences(context).getString(KEY_USER_EMAIL, "");
    }
    
    /**
     * 保存登录状态
     */
    public static void setLoggedIn(Context context, boolean isLoggedIn) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }
    
    /**
     * 获取登录状态
     */
    public static boolean isLoggedIn(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * 保存键值对
     */
    public static void saveString(Context context, String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(key, value);
        editor.apply();
    }
    
    /**
     * 获取字符串值
     */
    public static String getString(Context context, String key, String defaultValue) {
        return getSharedPreferences(context).getString(key, defaultValue);
    }
    
    /**
     * 保存整数值
     */
    public static void saveInt(Context context, String key, int value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(key, value);
        editor.apply();
    }
    
    /**
     * 获取整数值
     */
    public static int getInt(Context context, String key, int defaultValue) {
        return getSharedPreferences(context).getInt(key, defaultValue);
    }
    
    /**
     * 保存长整数值
     */
    public static void saveLong(Context context, String key, long value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putLong(key, value);
        editor.apply();
    }
    
    /**
     * 获取长整数值
     */
    public static long getLong(Context context, String key, long defaultValue) {
        return getSharedPreferences(context).getLong(key, defaultValue);
    }
    
    /**
     * 保存布尔值
     */
    public static void saveBoolean(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
    
    /**
     * 获取布尔值
     */
    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return getSharedPreferences(context).getBoolean(key, defaultValue);
    }
    
    /**
     * 清除所有偏好设置
     */
    public static void clearPreferences(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.clear();
        editor.apply();
    }
    
    /**
     * 删除指定键的偏好设置
     */
    public static void removePreference(Context context, String key) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(key);
        editor.apply();
    }
} 