package com.aup.dailyweather

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("weather")
    fun getWeatherDate(
        @Query("q") city: String?,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): Call<DailyWeather>
}
