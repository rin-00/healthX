package com.healthx.util;

/**
 * 应用常量
 */
public class Constants {
    
    // API基础URL
    // 模拟器使用 10.0.2.2 (Android模拟器中指向主机的特殊IP)
    // 雷电模拟器通常可以使用 10.0.2.2 或 host.docker.internal 或 计算机的实际IP
    // 或者尝试使用本地回环地址 127.0.0.1 (部分模拟器支持)
    // public static final String API_BASE_URL = "http://10.0.2.2:8080/";
    // public static final String API_BASE_URL = "http://127.0.0.1:8080/";
    public static final String API_BASE_URL = "http://100.78.121.133:8080/"; // WLAN适配器的IP地址 - 雷电模拟器使用主机实际IP
    // 如果连接超时，可以尝试以下地址
    // public static final String API_BASE_URL = "http://localhost:8080/";
    // public static final String API_BASE_URL = "http://26.138.99.222:8080/"; // Radmin VPN适配器的IP地址
    
    // SharedPreferences常量
    public static final String PREF_NAME = "HealthXPrefs";
    
    // Bundle键值
    public static final String KEY_USER = "user";
    
    // 正则表达式
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]{3,20}$";
    public static final String PASSWORD_PATTERN = "^[a-zA-Z0-9_-]{6,20}$";
} 