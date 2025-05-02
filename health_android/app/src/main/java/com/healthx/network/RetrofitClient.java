package com.healthx.network;

import com.healthx.util.Constants;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit客户端
 */
public class RetrofitClient {
    
    private static final String BASE_URL = Constants.API_BASE_URL;
    private static RetrofitClient instance;
    private final Retrofit retrofit;
    private final ApiService apiService;
    
    private RetrofitClient() {
        // 创建OkHttp客户端
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        
        // 添加日志拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(loggingInterceptor);
        
        // 添加认证拦截器
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                
                // 从本地获取token
                String token = TokenManager.getInstance().getToken();
                
                // 如果token存在，添加到请求头
                Request.Builder requestBuilder = original.newBuilder();
                if (token != null && !token.isEmpty()) {
                    requestBuilder.header("Authorization", "Bearer " + token);
                }
                
                requestBuilder.header("Content-Type", "application/json");
                requestBuilder.method(original.method(), original.body());
                
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });
        
        // 创建Retrofit实例
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        
        // 创建API Service
        apiService = retrofit.create(ApiService.class);
    }
    
    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }
    
    public ApiService getApiService() {
        return apiService;
    }
} 