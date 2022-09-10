package com.absut.weatherapp.data.repository

import com.absut.weatherapp.data.mapper.toWeatherInfo
import com.absut.weatherapp.data.remote.WeatherApi
import com.absut.weatherapp.domain.model.WeatherInfo
import com.absut.weatherapp.domain.repository.WeatherRepository
import com.absut.weatherapp.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val api:WeatherApi
):WeatherRepository {
    override suspend fun getWeatherData(lat: Double, lon: Double): Resource<WeatherInfo> {
      return try {
          val data = api.getWeatherData(lat,lon).toWeatherInfo()
          Resource.Success(data)
      }catch (e:Exception){
          e.printStackTrace()
          Resource.Error(e.message ?: "An unknown error occurred")
      }
    }

  /*  override fun getWeatherData(lat: Double, lon: Double): Flow<Resource<WeatherInfo>> = flow {
        try {
            val data = api.getWeatherData(lat,lon).toWeatherInfo()
            emit(Resource.Success(data))
        }catch (e:Exception){
            e.printStackTrace()
           emit(Resource.Error(e.message ?: "An unknown error occurred"))
        }
    }*/
}