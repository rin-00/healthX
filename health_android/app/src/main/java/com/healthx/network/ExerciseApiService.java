package com.healthx.network;

import com.healthx.model.Exercise;

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

public interface ExerciseApiService {
    
    @POST("api/exercises")
    Call<Exercise> addExercise(@Body Exercise exercise);
    
    @GET("api/exercises/{id}")
    Call<Exercise> getExercise(@Path("id") long id);
    
    @GET("api/exercises/user/{userId}")
    Call<List<Exercise>> getUserExercises(@Path("userId") long userId);
    
    @GET("api/exercises/user/{userId}/date")
    Call<List<Exercise>> getUserExercisesForDate(@Path("userId") long userId, @Query("date") String date);
    
    @GET("api/exercises/user/{userId}/range")
    Call<List<Exercise>> getUserExercisesByDateRange(
            @Path("userId") long userId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );
    
    @GET("api/exercises/user/{userId}/type")
    Call<List<Exercise>> getUserExercisesByType(
            @Path("userId") long userId,
            @Query("exerciseType") String exerciseType
    );
    
    @GET("api/exercises/user/{userId}/calories")
    Call<Map<String, Double>> getDailyCaloriesBurned(
            @Path("userId") long userId,
            @Query("date") String date
    );
    
    @PUT("api/exercises/{id}")
    Call<Exercise> updateExercise(@Path("id") long id, @Body Exercise exercise);
    
    @DELETE("api/exercises/{id}")
    Call<Void> deleteExercise(@Path("id") long id);
} 