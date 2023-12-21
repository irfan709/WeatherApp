package com.example.weather.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiUtils {

    private var retrofit: Retrofit? = null

    private var BASE_URL = "https://api.openweathermap.org/data/2.5/"

    fun getApiInterface(): ApiiInterface? {
        if (retrofit == null) {
            retrofit = Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build()
        }
        return retrofit?.create(ApiiInterface::class.java)
    }
}