package com.healthx.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.healthx.util.Constants;

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
            // 创建一个不验证证书的TrustManager
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
        
        // 创建自定义的Gson实例以处理LocalDateTime
        Gson gson = new GsonBuilder()
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
                        return LocalDateTime.parse(dateString, formatter);
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
    
    public ApiService getApiService() {
        return apiService;
    }
} 