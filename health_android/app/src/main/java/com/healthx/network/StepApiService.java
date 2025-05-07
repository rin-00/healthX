package com.healthx.network;

import com.healthx.model.dto.StepGoalDTO;
import com.healthx.model.dto.StepRecordDTO;
import com.healthx.model.dto.StepStatisticsDTO;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 步数相关API服务接口
 */
public interface StepApiService {
    
    // 步数记录相关API
    
    /**
     * 添加步数记录
     * @param stepRecordDTO 步数记录DTO
     * @return API响应
     */
    @POST("/api/steps")
    Call<ApiResponse<StepRecordDTO>> addStepRecord(@Body StepRecordDTO stepRecordDTO);
    
    /**
     * 获取步数记录
     * @param id 记录ID
     * @return API响应
     */
    @GET("/api/steps/{id}")
    Call<ApiResponse<StepRecordDTO>> getStepRecord(@Path("id") long id);
    
    /**
     * 获取用户所有步数记录
     * @param userId 用户ID
     * @return API响应
     */
    @GET("/api/steps/user/{userId}")
    Call<ApiResponse<List<StepRecordDTO>>> getUserStepRecords(@Path("userId") long userId);
    
    /**
     * 获取用户指定日期的步数记录
     * @param userId 用户ID
     * @param date 日期（格式：yyyy-MM-dd）
     * @return API响应
     */
    @GET("/api/steps/user/{userId}/date/{date}")
    Call<ApiResponse<StepRecordDTO>> getUserStepRecordByDate(
            @Path("userId") long userId,
            @Path("date") String date);
    
    /**
     * 获取用户指定日期范围的步数记录
     * @param userId 用户ID
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @return API响应
     */
    @GET("/api/steps/user/{userId}/range")
    Call<ApiResponse<List<StepRecordDTO>>> getUserStepRecordsByDateRange(
            @Path("userId") long userId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate);
    
    /**
     * 获取用户指定日期范围内消耗的卡路里
     * @param userId 用户ID
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @return API响应
     */
    @GET("/api/steps/user/{userId}/calories")
    Call<ApiResponse<Map<String, Object>>> getUserCaloriesBurnedByDateRange(
            @Path("userId") long userId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate);
    
    /**
     * 更新步数记录
     * @param id 记录ID
     * @param stepRecordDTO 步数记录DTO
     * @return API响应
     */
    @PUT("/api/steps/{id}")
    Call<ApiResponse<StepRecordDTO>> updateStepRecord(
            @Path("id") long id,
            @Body StepRecordDTO stepRecordDTO);
    
    /**
     * 删除步数记录
     * @param id 记录ID
     * @return API响应
     */
    @DELETE("/api/steps/{id}")
    Call<ApiResponse<Void>> deleteStepRecord(@Path("id") long id);
    
    // 步数目标相关API
    
    /**
     * 创建步数目标
     * @param stepGoalDTO 步数目标DTO
     * @return API响应
     */
    @POST("/api/step-goals")
    Call<ApiResponse<StepGoalDTO>> createStepGoal(@Body StepGoalDTO stepGoalDTO);
    
    /**
     * 获取步数目标
     * @param id 目标ID
     * @return API响应
     */
    @GET("/api/step-goals/{id}")
    Call<ApiResponse<StepGoalDTO>> getStepGoal(@Path("id") long id);
    
    /**
     * 获取用户所有步数目标
     * @param userId 用户ID
     * @return API响应
     */
    @GET("/api/step-goals/user/{userId}")
    Call<ApiResponse<List<StepGoalDTO>>> getUserStepGoals(@Path("userId") long userId);
    
    /**
     * 获取用户当前激活的步数目标
     * @param userId 用户ID
     * @return API响应
     */
    @GET("/api/step-goals/user/{userId}/active")
    Call<ApiResponse<List<StepGoalDTO>>> getUserActiveStepGoals(@Path("userId") long userId);
    
    /**
     * 更新步数目标
     * @param id 目标ID
     * @param stepGoalDTO 步数目标DTO
     * @return API响应
     */
    @PUT("/api/step-goals/{id}")
    Call<ApiResponse<StepGoalDTO>> updateStepGoal(
            @Path("id") long id,
            @Body StepGoalDTO stepGoalDTO);
    
    /**
     * 激活/停用步数目标
     * @param id 目标ID
     * @param isActive 是否激活
     * @return API响应
     */
    @PUT("/api/step-goals/{id}/toggle-active")
    Call<ApiResponse<StepGoalDTO>> toggleStepGoalActive(
            @Path("id") long id,
            @Query("isActive") boolean isActive);
    
    /**
     * 删除步数目标
     * @param id 目标ID
     * @return API响应
     */
    @DELETE("/api/step-goals/{id}")
    Call<ApiResponse<Void>> deleteStepGoal(@Path("id") long id);
    
    /**
     * 计算用户在指定日期的步数目标达成度
     * @param userId 用户ID
     * @param date 日期（格式：yyyy-MM-dd）
     * @return API响应
     */
    @GET("/api/step-goals/user/{userId}/achievement")
    Call<ApiResponse<Map<String, Double>>> calculateGoalAchievement(
            @Path("userId") long userId,
            @Query("date") String date);
    
    // 步数统计相关API
    
    /**
     * 获取用户步数统计
     * @param userId 用户ID
     * @param statisticType 统计类型（WEEKLY/MONTHLY）
     * @param year 年份
     * @return API响应
     */
    @GET("/api/steps/user/{userId}/statistics")
    Call<ApiResponse<List<StepStatisticsDTO>>> getUserStepStatistics(
            @Path("userId") long userId,
            @Query("statisticType") String statisticType,
            @Query("year") int year);
} 