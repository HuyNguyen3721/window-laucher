package com.ezteam.windowslauncher.model.weather

import com.ezteam.windowslauncher.requestlistener.RequestCompleteListener

interface WeatherInfoShowModel {
    fun getWeatherInfoByCityId(cityId: Int, callback: RequestCompleteListener<WeatherInfoResponse>)
    fun getWeatherInfoByCityName(
        cityName: String,
        callback: RequestCompleteListener<WeatherInfoResponse>
    )

    fun getWeatherInfoByLatLon(
        lat: Double, log: Double,
        callback: RequestCompleteListener<WeatherInfoResponse>
    )
}