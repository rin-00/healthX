package com.healthx.network;

import com.healthx.model.Diet;

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

public interface DietApiService {
    
    @POST("api/diets")
    Call<Diet> addDiet(@Body Diet diet);
    
    @GET("api/diets/{id}")
    Call<Diet> getDiet(@Path("id") long id);
    
    @GET("api/diets/user/{userId}")
    Call<List<Diet>> getUserDiets(@Path("userId") long userId);
    
    @GET("api/diets/user/{userId}/date")
    Call<List<Diet>> getUserDietsForDate(@Path("userId") long userId, @Query("date") String date);
    
    @GET("api/diets/user/{userId}/range")
    Call<List<Diet>> getUserDietsByDateRange(
            @Path("userId") long userId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );
    
    @GET("api/diets/user/{userId}/meal-type")
    Call<List<Diet>> getUserDietsByMealType(
            @Path("userId") long userId,
            @Query("mealType") String mealType
    );
    
    @GET("api/diets/user/{userId}/calories")
    Call<Map<String, Double>> getDailyCalories(
            @Path("userId") long userId,
            @Query("date") String date
    );
    
    @PUT("api/diets/{id}")
    Call<Diet> updateDiet(@Path("id") long id, @Body Diet diet);
    
    @DELETE("api/diets/{id}")
    Call<Void> deleteDiet(@Path("id") long id);
} 