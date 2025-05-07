package com.healthx.network;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.healthx.util.Constants;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * API客户端单例
 */
public class ApiClient {
    
    private static final String TAG = "ApiClient";
    
    // 为各种模拟器和设备提供不同的BASE_URL选项
    // 雷电模拟器特殊地址（10.0.2.2 - Android模拟器标准地址）
    private static final String EMULATOR_URL = "http://10.0.2.2:8080/";
    // 雷电模拟器备用地址
    private static final String LDPLAYER_URL = "http://127.0.0.1:8080/";
    // 局域网地址示例
    private static final String LOCAL_NETWORK_URL = "http://192.168.1.100:8080/";
    // 本地地址
    private static final String LOCALHOST_URL = "http://localhost:8080/";
    
    // 设置当前使用的BASE_URL - 修改这里指向Constants类的值，与RetrofitClient保持一致
    private static final String BASE_URL = Constants.API_BASE_URL;
    
    private static final int CONNECT_TIMEOUT = 30; // 增加到30秒
    private static final int READ_TIMEOUT = 30;    // 增加到30秒
    private static final int WRITE_TIMEOUT = 30;   // 增加到30秒
    
    private static ApiClient instance;
    private static boolean isInitialized = false;
    protected Retrofit retrofit; // 修改为protected，允许子类访问
    
    private ApiClient() {
        // 创建OkHttpClient，配置超时和日志
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
        
        // 添加日志拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> 
            Log.d(TAG, "API日志: " + message)
        );
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder.addInterceptor(loggingInterceptor);
        
        // 创建自定义的Gson实例以处理LocalDateTime
        Gson gson = createGson();
        
        // 创建Retrofit实例
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        
        isInitialized = true;
        Log.d(TAG, "ApiClient初始化完成，使用服务器地址: " + BASE_URL);
    }
    
    /**
     * 创建自定义的Gson实例，处理LocalDateTime的序列化和反序列化
     */
    private Gson createGson() {
        return new GsonBuilder()
                // LocalDateTime处理
                .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    
                    @Override
                    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(formatter.format(src));
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    
                    @Override
                    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                        String dateString = json.getAsString();
                        LocalDateTime result = com.healthx.util.DateTimeUtils.parseFlexibleDateTime(dateString);
                        if (result != null) {
                            return result;
                        } else {
                            // 如果灵活解析失败，回退到简单格式
                            try {
                                return LocalDateTime.parse(dateString, formatter);
                            } catch (Exception e) {
                                Log.e("ApiClient", "无法解析日期时间: " + dateString, e);
                                throw new RuntimeException("无法解析日期时间: " + dateString, e);
                            }
                        }
                    }
                })
                // LocalDate处理
                .registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
                    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
                    
                    @Override
                    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(formatter.format(src));
                    }
                })
                .registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
                    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
                    
                    @Override
                    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                        String dateString = json.getAsString();
                        return LocalDate.parse(dateString, formatter);
                    }
                })
                .create();
    }
    
    /**
     * 获取Retrofit实例
     *
     * @return Retrofit实例
     */
    public static synchronized Retrofit getClient() {
        if (instance == null) {
            getInstance();
        }
        return instance.retrofit;
    }
    
    /**
     * 获取ApiClient单例
     *
     * @return ApiClient实例
     */
    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }
    
    /**
     * 重置ApiClient实例（在连接失败时调用）
     * 
     * @param newBaseUrl 新的基础URL
     * @return 重新创建的ApiClient实例
     */
    public static synchronized ApiClient resetInstance(String newBaseUrl) {
        if (instance != null) {
            Log.d(TAG, "正在重置ApiClient，新地址: " + newBaseUrl);
            instance = null;
        }
        
        // 重新创建OkHttpClient和Retrofit
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new HttpLoggingInterceptor(message -> 
                    Log.d(TAG, "API日志: " + message)
                ).setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
        
        // 创建自定义的Gson实例
        Gson gson = new GsonBuilder()
                // LocalDateTime处理
                .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    
                    @Override
                    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(formatter.format(src));
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    
                    @Override
                    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                        String dateString = json.getAsString();
                        LocalDateTime result = com.healthx.util.DateTimeUtils.parseFlexibleDateTime(dateString);
                        if (result != null) {
                            return result;
                        } else {
                            // 如果灵活解析失败，回退到简单格式
                            try {
                                return LocalDateTime.parse(dateString, formatter);
                            } catch (Exception e) {
                                Log.e("ApiClient", "无法解析日期时间: " + dateString, e);
                                throw new RuntimeException("无法解析日期时间: " + dateString, e);
                            }
                        }
                    }
                })
                // LocalDate处理
                .registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
                    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
                    
                    @Override
                    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(formatter.format(src));
                    }
                })
                .registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
                    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
                    
                    @Override
                    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                        String dateString = json.getAsString();
                        return LocalDate.parse(dateString, formatter);
                    }
                })
                .create();
        
        // 创建新实例
        instance = new ApiClient();
        
        // 覆盖retrofit实例
        instance.retrofit = new Retrofit.Builder()
                .baseUrl(newBaseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        
        Log.d(TAG, "ApiClient重置完成，新地址: " + newBaseUrl);
        return instance;
    }
    
    /**
     * 判断ApiClient是否已初始化
     * 
     * @return 是否已初始化
     */
    public static boolean isInitialized() {
        return isInitialized;
    }
    
    /**
     * 创建API服务接口
     *
     * @param serviceClass API服务接口类
     * @param <T> API服务接口类型
     * @return API服务接口实例
     */
    public <T> T create(Class<T> serviceClass) {
        return retrofit.create(serviceClass);
    }
} 