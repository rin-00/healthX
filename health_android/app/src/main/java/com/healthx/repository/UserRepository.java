package com.healthx.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

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
    
    public UserRepository() {
        apiService = RetrofitClient.getInstance().getApiService();
        tokenManager = TokenManager.getInstance();
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
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserResponse userResponse = response.body().getData();
                    User user = new User(
                            userResponse.getId(),
                            userResponse.getUsername(),
                            userResponse.getEmail(),
                            userResponse.getNickname(),
                            null
                    );
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
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
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
            public void onResponse(Call<ApiResponse<JwtResponse>> call, Response<ApiResponse<JwtResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    JwtResponse jwtResponse = response.body().getData();
                    
                    // 保存Token和用户信息
                    tokenManager.saveToken(jwtResponse.getToken());
                    tokenManager.saveUserInfo(
                            jwtResponse.getId(),
                            jwtResponse.getUsername(),
                            jwtResponse.getEmail()
                    );
                    
                    User user = new User(
                            jwtResponse.getId(),
                            jwtResponse.getUsername(),
                            jwtResponse.getEmail(),
                            null,
                            jwtResponse.getToken()
                    );
                    
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
            public void onFailure(Call<ApiResponse<JwtResponse>> call, Throwable t) {
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
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Boolean exists = response.body().getData();
                    result.setValue(Resource.success(exists));
                } else {
                    result.setValue(Resource.error("检查用户名失败", null));
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
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
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Boolean exists = response.body().getData();
                    result.setValue(Resource.success(exists));
                } else {
                    result.setValue(Resource.error("检查邮箱失败", null));
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                Log.e(TAG, "检查邮箱失败: " + t.getMessage());
                result.setValue(Resource.error("网络错误: " + t.getMessage(), null));
            }
        });
        
        return result;
    }
    
    /**
     * 获取当前登录用户信息
     */
    public LiveData<Resource<User>> getCurrentUser() {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();
        
        if (!tokenManager.isLoggedIn()) {
            result.setValue(Resource.error("用户未登录", null));
            return result;
        }
        
        result.setValue(Resource.loading(null));
        
        Long userId = tokenManager.getUserId();
        apiService.getUserById(userId).enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserResponse userResponse = response.body().getData();
                    User user = new User(
                            userResponse.getId(),
                            userResponse.getUsername(),
                            userResponse.getEmail(),
                            userResponse.getNickname(),
                            tokenManager.getToken()
                    );
                    user.setGender(userResponse.getGender());
                    user.setAge(userResponse.getAge());
                    user.setHeight(userResponse.getHeight());
                    user.setWeight(userResponse.getWeight());
                    
                    result.setValue(Resource.success(user));
                } else {
                    result.setValue(Resource.error("获取用户信息失败", null));
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
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
        tokenManager.clearUserInfo();
    }
} 