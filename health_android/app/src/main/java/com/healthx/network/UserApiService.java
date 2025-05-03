package com.healthx.network;

import com.healthx.model.User;
import com.healthx.network.model.ApiResponse;
import com.healthx.network.model.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * 用户API服务接口
 */
public interface UserApiService {
    
    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GET("api/users/{id}")
    Call<ApiResponse<UserResponse>> getUserById(@Path("id") long id);
    
    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @GET("api/users/by-username/{username}")
    Call<ApiResponse<UserResponse>> getUserByUsername(@Path("username") String username);
    
    /**
     * 更新用户信息
     *
     * @param id 用户ID
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    @PUT("api/users/{id}")
    Call<ApiResponse<UserResponse>> updateUser(@Path("id") long id, @Body User user);
    
    /**
     * 更新用户基本资料（邮箱和昵称）
     *
     * @param id 用户ID
     * @param user 包含新邮箱和昵称的用户对象
     * @return 更新后的用户信息
     */
    @PUT("api/users/{id}/profile")
    Call<ApiResponse<UserResponse>> updateUserProfile(@Path("id") long id, @Body User user);
    
    /**
     * 更新用户健康数据（性别、年龄、身高、体重）
     *
     * @param id 用户ID
     * @param user 包含新健康数据的用户对象
     * @return 更新后的用户信息
     */
    @PUT("api/users/{id}/health-data")
    Call<ApiResponse<UserResponse>> updateUserHealthData(@Path("id") long id, @Body User user);
} 