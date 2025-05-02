package com.healthx.network;

import com.healthx.network.model.ApiResponse;
import com.healthx.network.model.JwtResponse;
import com.healthx.network.model.LoginRequest;
import com.healthx.network.model.RegisterRequest;
import com.healthx.network.model.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Retrofit API接口
 */
public interface ApiService {

    /**
     * 用户注册
     */
    @POST("api/auth/register")
    Call<ApiResponse<UserResponse>> register(@Body RegisterRequest request);

    /**
     * 用户登录
     */
    @POST("api/auth/login")
    Call<ApiResponse<JwtResponse>> login(@Body LoginRequest request);

    /**
     * 检查用户名是否存在
     */
    @GET("api/auth/check-username/{username}")
    Call<ApiResponse<Boolean>> checkUsername(@Path("username") String username);

    /**
     * 检查邮箱是否存在
     */
    @GET("api/auth/check-email/{email}")
    Call<ApiResponse<Boolean>> checkEmail(@Path("email") String email);

    /**
     * 获取用户信息
     */
    @GET("api/users/{id}")
    Call<ApiResponse<UserResponse>> getUserById(@Path("id") Long id);

    /**
     * 根据用户名获取用户信息
     */
    @GET("api/users/by-username/{username}")
    Call<ApiResponse<UserResponse>> getUserByUsername(@Path("username") String username);
} 