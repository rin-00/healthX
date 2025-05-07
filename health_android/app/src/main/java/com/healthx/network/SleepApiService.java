package com.healthx.network;

import com.healthx.model.SleepRecord;
import com.healthx.model.SleepRecordDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 睡眠记录API服务接口
 */
public interface SleepApiService {
    
    /**
     * 添加睡眠记录
     */
    @POST("api/sleep")
    Call<SleepRecordDTO> addSleepRecord(@Body SleepRecordDTO sleepRecordDTO);
    
    /**
     * 获取指定ID的睡眠记录
     */
    @GET("api/sleep/{id}")
    Call<SleepRecordDTO> getSleepRecord(@Path("id") long id);
    
    /**
     * 获取用户的所有睡眠记录
     */
    @GET("api/sleep/user/{userId}")
    Call<List<SleepRecordDTO>> getUserSleepRecords(@Path("userId") long userId);
    
    /**
     * 获取用户指定日期的睡眠记录
     */
    @GET("api/sleep/user/{userId}/date")
    Call<SleepRecordDTO> getUserSleepRecordForDate(
            @Path("userId") long userId,
            @Query("date") String date);
    
    /**
     * 获取用户指定日期范围的睡眠记录
     */
    @GET("api/sleep/user/{userId}/range")
    Call<List<SleepRecordDTO>> getUserSleepRecordsByDateRange(
            @Path("userId") long userId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate);
    
    /**
     * 获取用户最近7天的睡眠记录
     */
    @GET("api/sleep/user/{userId}/last7days")
    Call<List<SleepRecordDTO>> getUserLast7DaysSleepRecords(@Path("userId") long userId);
    
    /**
     * 更新睡眠记录
     */
    @PUT("api/sleep/{id}")
    Call<SleepRecordDTO> updateSleepRecord(@Path("id") long id, @Body SleepRecordDTO sleepRecordDTO);
    
    /**
     * 删除睡眠记录
     */
    @DELETE("api/sleep/{id}")
    Call<Void> deleteSleepRecord(@Path("id") long id);
} 