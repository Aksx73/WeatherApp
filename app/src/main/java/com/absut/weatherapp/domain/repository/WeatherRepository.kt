package com.absut.weatherapp.domain.repository

import com.absut.weatherapp.domain.model.WeatherInfo
import com.absut.weatherapp.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun getWeatherData(lat: Double, lon: Double): Resource<WeatherInfo>
  //  fun getWeatherData(lat: Double, lon: Double): Flow<Resource<WeatherInfo>>

}