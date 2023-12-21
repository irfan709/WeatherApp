package com.example.weather.api

import com.example.weather.models.WeatherModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiiInterface {

    @GET("weather")
    fun getCurrentLocationWeather(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("APPID") appId: String
    ): Call<WeatherModel>

    @GET("weather")
    fun getCityWeather(
        @Query("q") q: String,
        @Query("APPID") appId: String
    ): Call<WeatherModel>
}