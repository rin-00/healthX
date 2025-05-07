package com.healthx.network;

import com.healthx.model.WeightRecord;
import com.healthx.model.dto.WeightRecordDTO;
import com.healthx.network.ApiResponse;

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
 * 体重管理相关API接口
 */
public interface WeightApi {
    
    /**
     * 获取用户的体重记录
     */
    @GET("api/weights/user/{userId}")
    Call<ApiResponse<List<WeightRecordDTO>>> getWeightRecordsByUserId(@Path("userId") long userId);
    
    /**
     * 添加体重记录
     */
    @POST("api/weights")
    Call<ApiResponse<WeightRecordDTO>> addWeightRecord(@Body WeightRecordDTO weightRecordDTO);
    
    /**
     * 更新体重记录
     */
    @PUT("api/weights/{id}")
    Call<ApiResponse<WeightRecordDTO>> updateWeightRecord(@Path("id") Long id, @Body WeightRecordDTO weightRecordDTO);
    
    /**
     * 删除体重记录
     */
    @DELETE("api/weights/{id}")
    Call<ApiResponse<Void>> deleteWeightRecord(@Path("id") Long id);
    
    /**
     * 获取指定ID的体重记录
     */
    @GET("api/weights/records/{id}")
    Call<ApiResponse<WeightRecordDTO>> getWeightRecord(@Path("id") long id);
    
    /**
     * 获取用户指定日期的体重记录
     */
    @GET("api/weights/records/user/{userId}/date/{date}")
    Call<ApiResponse<List<WeightRecordDTO>>> getWeightRecordsByDate(
            @Path("userId") long userId,
            @Path("date") String date);
    
    /**
     * 获取用户指定日期范围的体重记录
     */
    @GET("api/weights/records/user/{userId}/range")
    Call<ApiResponse<List<WeightRecordDTO>>> getWeightRecordsByDateRange(
            @Path("userId") long userId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate);
    
    /**
     * 获取用户最近30天的体重记录
     */
    @GET("api/weights/records/user/{userId}/last30days")
    Call<ApiResponse<List<WeightRecordDTO>>> getUserLast30DaysWeightRecords(
            @Path("userId") long userId);
    
    /**
     * 获取用户最新的体重记录
     */
    @GET("api/weights/records/user/{userId}/latest")
    Call<ApiResponse<WeightRecordDTO>> getUserLatestWeightRecord(
            @Path("userId") long userId);
    
    /**
     * 计算BMI
     */
    @GET("api/weights/calculate-bmi")
    Call<ApiResponse<Map<String, Object>>> calculateBmi(
            @Query("weight") float weight,
            @Query("height") float height);
} 