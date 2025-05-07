package com.healthx.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.healthx.model.User;
import com.healthx.network.ApiService;
import com.healthx.network.RetrofitClient;
import com.healthx.network.TokenManager;
import com.healthx.network.model.ApiResponse;
import com.healthx.network.model.JwtResponse;
import com.healthx.network.model.LoginRequest;
import com.healthx.network.model.RegisterRequest;
import com.healthx.network.model.UserResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 用户仓库类，处理用户相关的网络请求和数据处理
 */
public class UserRepository {
    
    private static final String TAG = "UserRepository";
    private final ApiService apiService;
    private final TokenManager tokenManager;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    
    // 本地缓存的用户数据
    private final Map<Long, User> userCache = new HashMap<>();
    private static final String PREF_USER_CACHE = "user_cache";
    
    public UserRepository(Context context) {
        apiService = RetrofitClient.getInstance().getApiService();
        tokenManager = TokenManager.getInstance();
        sharedPreferences = context.getSharedPreferences("health_prefs", Context.MODE_PRIVATE);
        gson = new Gson();
        
        // 从SharedPreferences加载缓存
        loadCache();
    }
    
    private void loadCache() {
        String userCacheJson = sharedPreferences.getString(PREF_USER_CACHE, null);
        if (userCacheJson != null) {
            Type type = new TypeToken<Map<Long, User>>(){}.getType();
            Map<Long, User> loadedCache = gson.fromJson(userCacheJson, type);
            if (loadedCache != null) {
                userCache.putAll(loadedCache);
            }
        }
    }
    
    private void saveCache() {
        String userCacheJson = gson.toJson(userCache);
        sharedPreferences.edit().putString(PREF_USER_CACHE, userCacheJson).apply();
    }
    
    /**
     * 从本地缓存获取用户
     */
    public User getUserById(long userId) {
        return userCache.get(userId);
    }
    
    /**
     * 保存用户到本地缓存
     */
    public void saveUser(User user) {
        if (user != null && user.getId() != null) {
            userCache.put(user.getId(), user);
            saveCache();
        }
    }
    
    /**
     * 用户注册
     */
    public LiveData<Resource<User>> register(String username, String password, String email, String nickname) {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));
        
        Log.d(TAG, "注册请求: " + username + ", " + email + ", " + nickname);
        
        RegisterRequest request = new RegisterRequest(username, password, email, nickname);
        
        apiService.register(request).enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserResponse>> call, @NonNull Response<ApiResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserResponse userResponse = response.body().getData();
                    User user = new User(
                            userResponse.getId(),
                            userResponse.getUsername(),
                            userResponse.getEmail(),
                            userResponse.getNickname(),
                            null
                    );
                    // 设置健康数据
                    user.setGender(userResponse.getGender());
                    user.setAge(userResponse.getAge());
                    user.setHeight(userResponse.getHeight());
                    user.setWeight(userResponse.getWeight());
                    
                    // 保存到缓存
                    saveUser(user);
                    
                    Log.d(TAG, "注册成功: " + username);
                    result.setValue(Resource.success(user));
                } else {
                    String errorMsg = "注册失败";
                    
                    if (response.body() != null) {
                        errorMsg = response.body().getMessage();
                        Log.e(TAG, "服务器返回错误: " + errorMsg);
                    } else {
                        try {
                            errorMsg = "服务器错误: " + response.code() + " " + response.message();
                            if (response.errorBody() != null) {
                                errorMsg += " - " + response.errorBody().string();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "解析错误响应失败", e);
                        }
                        Log.e(TAG, "HTTP错误: " + errorMsg);
                    }
                    
                    result.setValue(Resource.error(errorMsg, null));
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserResponse>> call, @NonNull Throwable t) {
                String errorMsg;
                
                if (t instanceof ConnectException) {
                    errorMsg = "无法连接到服务器，请检查网络连接";
                    Log.e(TAG, "连接错误", t);
                } else if (t instanceof SocketTimeoutException) {
                    errorMsg = "连接服务器超时，请稍后重试";
                    Log.e(TAG, "连接超时", t);
                } else if (t instanceof UnknownHostException) {
                    errorMsg = "找不到服务器，请检查API地址配置";
                    Log.e(TAG, "未知主机", t);
                } else {
                    errorMsg = "网络错误: " + t.getMessage();
                    Log.e(TAG, "注册失败", t);
                }
                
                result.setValue(Resource.error(errorMsg, null));
            }
        });
        
        return result;
    }
    
    /**
     * 用户登录
     */
    public LiveData<Resource<User>> login(String username, String password) {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));
        
        LoginRequest request = new LoginRequest(username, password);
        
        apiService.login(request).enqueue(new Callback<ApiResponse<JwtResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<JwtResponse>> call, @NonNull Response<ApiResponse<JwtResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    JwtResponse jwtResponse = response.body().getData();
                    
                    // 保存Token和用户信息
                    tokenManager.saveToken(jwtResponse.getToken());
                    tokenManager.saveUserInfo(
                            jwtResponse.getId(),
                            jwtResponse.getUsername(),
                            jwtResponse.getEmail()
                    );
                    
                    // 保存用户ID到SharedPreferences
                    sharedPreferences.edit().putLong("user_id", jwtResponse.getId()).apply();
                    
                    User user = new User(
                            jwtResponse.getId(),
                            jwtResponse.getUsername(),
                            jwtResponse.getEmail(),
                            jwtResponse.getNickname() != null ? jwtResponse.getNickname() : "",
                            jwtResponse.getToken()
                    );
                    
                    // 设置健康数据
                    if (jwtResponse.getGender() != null) {
                        user.setGender(jwtResponse.getGender());
                    }
                    if (jwtResponse.getAge() != null) {
                        user.setAge(jwtResponse.getAge());
                    }
                    if (jwtResponse.getHeight() != null) {
                        user.setHeight(jwtResponse.getHeight());
                    }
                    if (jwtResponse.getWeight() != null) {
                        user.setWeight(jwtResponse.getWeight());
                    }
                    
                    // 保存到缓存
                    saveUser(user);
                    
                    result.setValue(Resource.success(user));
                } else {
                    String errorMsg = "登录失败";
                    if (response.body() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    result.setValue(Resource.error(errorMsg, null));
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ApiResponse<JwtResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "登录失败: " + t.getMessage());
                result.setValue(Resource.error("网络错误: " + t.getMessage(), null));
            }
        });
        
        return result;
    }
    
    /**
     * 检查用户名是否存在
     */
    public LiveData<Resource<Boolean>> checkUsername(String username) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));
        
        apiService.checkUsername(username).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Boolean exists = response.body().getData();
                    result.setValue(Resource.success(exists));
                } else {
                    result.setValue(Resource.error("检查用户名失败", null));
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Throwable t) {
                Log.e(TAG, "检查用户名失败: " + t.getMessage());
                result.setValue(Resource.error("网络错误: " + t.getMessage(), null));
            }
        });
        
        return result;
    }
    
    /**
     * 检查邮箱是否存在
     */
    public LiveData<Resource<Boolean>> checkEmail(String email) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));
        
        apiService.checkEmail(email).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Boolean exists = response.body().getData();
                    result.setValue(Resource.success(exists));
                } else {
                    result.setValue(Resource.error("检查邮箱失败", null));
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Throwable t) {
                Log.e(TAG, "检查邮箱失败: " + t.getMessage());
                result.setValue(Resource.error("网络错误: " + t.getMessage(), null));
            }
        });
        
        return result;
    }
    
    /**
     * 获取当前登录的用户信息
     */
    public LiveData<Resource<User>> getCurrentUser() {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));
        
        long userId = sharedPreferences.getLong("user_id", -1);
        if (userId == -1) {
            result.setValue(Resource.error("未登录", null));
            return result;
        }
        
        // 先尝试从缓存获取
        User cachedUser = getUserById(userId);
        if (cachedUser != null) {
            result.setValue(Resource.success(cachedUser));
        }
        
        // 再从网络获取最新数据
        apiService.getUserById(userId).enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserResponse>> call, @NonNull Response<ApiResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserResponse userResponse = response.body().getData();
                    User user = new User(
                            userResponse.getId(),
                            userResponse.getUsername(),
                            userResponse.getEmail(),
                            userResponse.getNickname(),
                            tokenManager.getToken()
                    );
                    
                    // 设置健康数据
                    user.setGender(userResponse.getGender());
                    user.setAge(userResponse.getAge());
                    user.setHeight(userResponse.getHeight());
                    user.setWeight(userResponse.getWeight());
                    
                    // 保存到缓存
                    saveUser(user);
                    
                    result.setValue(Resource.success(user));
                } else {
                    String errorMsg = "获取用户信息失败";
                    if (response.body() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    result.setValue(Resource.error(errorMsg, null));
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "获取用户信息失败: " + t.getMessage());
                result.setValue(Resource.error("网络错误: " + t.getMessage(), null));
            }
        });
        
        return result;
    }
    
    /**
     * 退出登录
     */
    public void logout() {
        // 清除Token
        tokenManager.clearUserInfo();
        
        // 清除SharedPreferences中的用户ID
        sharedPreferences.edit().remove("user_id").apply();
        
        // 清除缓存
        userCache.clear();
        saveCache();
    }
    
    /**
     * 获取默认用户信息
     * 实际应用中应该从SharedPreferences或数据库获取当前登录用户
     * 这里为了演示，返回一个默认用户
     */
    public User getDefaultUser() {
        // 尝试从SharedPreferences获取当前用户ID
        long userId = sharedPreferences.getLong("user_id", 1); // 默认用户ID为1
        
        // 尝试从缓存获取用户
        User user = getUserById(userId);
        if (user != null) {
            return user;
        }
        
        // 如果缓存中没有，创建一个默认用户
        User defaultUser = new User();
        defaultUser.setId(userId);
        defaultUser.setUsername("default_user");
        defaultUser.setEmail("default@example.com");
        defaultUser.setNickname("默认用户");
        defaultUser.setGender("男");
        defaultUser.setAge(25);
        defaultUser.setHeight(175.0);
        defaultUser.setWeight(70.0);
        
        // 保存到缓存
        saveUser(defaultUser);
        
        return defaultUser;
    }
} 