package com.healthx.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 网络工具类，提供网络连接相关的工具方法
 */
public class NetworkUtils {
    
    /**
     * 检查设备是否连接到网络
     *
     * @param context 应用程序上下文
     * @return 如果设备连接到网络，则返回true；否则返回false
     */
    public static boolean isNetworkConnected(Context context) {
        if (context == null) {
            return false;
        }
        
        ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        
        return false;
    }
    
    /**
     * 检查是否连接到Wi-Fi网络
     *
     * @param context 应用程序上下文
     * @return 如果设备连接到Wi-Fi，则返回true；否则返回false
     */
    public static boolean isWifiConnected(Context context) {
        if (context == null) {
            return false;
        }
        
        ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo wifiNetworkInfo = 
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifiNetworkInfo != null && wifiNetworkInfo.isConnected();
        }
        
        return false;
    }
    
    /**
     * 检查是否连接到移动数据网络
     *
     * @param context 应用程序上下文
     * @return 如果设备连接到移动数据网络，则返回true；否则返回false
     */
    public static boolean isMobileDataConnected(Context context) {
        if (context == null) {
            return false;
        }
        
        ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo mobileNetworkInfo = 
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            return mobileNetworkInfo != null && mobileNetworkInfo.isConnected();
        }
        
        return false;
    }
} 