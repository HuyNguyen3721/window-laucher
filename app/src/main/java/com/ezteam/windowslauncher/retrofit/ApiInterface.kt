package com.ezteam.windowslauncher.retrofit

import com.ezteam.windowslauncher.model.weather.WeatherInfoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("weather")
    fun callApiForWeatherInfoByCityId(@Query("id") cityId: Int): Call<WeatherInfoResponse>

    @GET("weather")
    fun callApiForWeatherInfoByCityName(@Query("q") cityName: String): Call<WeatherInfoResponse>

    @GET("weather")
    fun callApiForWeatherInfoByLatLon(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Call<WeatherInfoResponse>
}