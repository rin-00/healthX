package com.healthx.network;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.healthx.util.Constants;
import com.healthx.util.DateTimeUtils;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
        
        // 添加超时设置 - 增加超时时间
        httpClient.connectTimeout(60, TimeUnit.SECONDS);
        httpClient.readTimeout(60, TimeUnit.SECONDS);
        httpClient.writeTimeout(60, TimeUnit.SECONDS);
        
        // 添加日志拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(loggingInterceptor);
        
        // 配置不安全的SSL（仅用于开发测试，生产环境不要使用）
        try {
            // 创建一个不验证证书链的TrustManager
            final TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }
                    
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }
                    
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
            };
            
            // 安装TrustManager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            
            // 创建SSLSocketFactory
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            
            // 设置OkHttp使用不安全的SSL
            httpClient.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            httpClient.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
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
        
        // 创建自定义的Gson实例以处理LocalDateTime
        Gson gson = new GsonBuilder()
                // LocalDateTime处理
                .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                    @Override
                    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(DateTimeUtils.formatDateTimeForApi(src));
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                    @Override
                    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                        try {
                            String dateString = json.getAsString();
                            // 使用增强的灵活解析方法
                            LocalDateTime result = DateTimeUtils.parseFlexibleDateTime(dateString);
                            if (result == null) {
                                throw new RuntimeException("无法解析日期时间: " + dateString);
                            }
                            return result;
                        } catch (Exception e) {
                            throw new RuntimeException("日期时间反序列化失败: " + e.getMessage(), e);
                        }
                    }
                })
                // LocalDate处理
                .registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
                    @Override
                    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(DateTimeUtils.formatDateForApi(src));
                    }
                })
                .registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
                    @Override
                    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                        try {
                            String dateString = json.getAsString();
                            LocalDate result = DateTimeUtils.parseDate(dateString);
                            if (result == null) {
                                throw new RuntimeException("无法解析日期: " + dateString);
                            }
                            return result;
                        } catch (Exception e) {
                            throw new RuntimeException("日期反序列化失败: " + e.getMessage(), e);
                        }
                    }
                })
                .create();
        
        // 创建Retrofit实例
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
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
    
    /**
     * 获取RetrofitClient实例（接受Context参数的重载方法）
     * 
     * @param context 应用上下文
     * @return RetrofitClient实例
     */
    public static synchronized RetrofitClient getInstance(Context context) {
        // 这里忽略context参数，只是为了兼容接口
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }
    
    public ApiService getApiService() {
        return apiService;
    }

    /**
     * 创建API服务接口实例
     * 
     * @param serviceClass API服务接口类
     * @return API服务接口实例
     */
    public <T> T createService(Class<T> serviceClass) {
        return retrofit.create(serviceClass);
    }
} 