package com.absut.weatherapp.ui

import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.absut.weatherapp.domain.location.LocationTracker
import com.absut.weatherapp.domain.model.WeatherInfo
import com.absut.weatherapp.domain.repository.WeatherRepository
import com.absut.weatherapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val locationTracker: LocationTracker,
    private val geocoder: Geocoder
) : ViewModel() {

    var uiState = MutableLiveData<WeatherUIState>()
        private set

    var locality = MutableLiveData<String>()
        private set

    fun loadWeatherInfo() {
        viewModelScope.launch {
            uiState.value = WeatherUIState.Loading

            locationTracker.getCurrentLocation()?.let { location ->
                getLocationName(location.latitude, location.longitude)
                val result = repository.getWeatherData(location.latitude, location.longitude)
                when (result) {
                    is Resource.Success -> {
                        uiState.value = WeatherUIState.Success(result.data)
                    }
                    is Resource.Error -> {
                        uiState.value = WeatherUIState.Error(result.message)
                    }
                }
            } ?: kotlin.run {
                uiState.value =
                    WeatherUIState.Error("Couldn't retrieve location. Make sure to grant permission and enable GPS.")
            }

        }
    }

    fun getLocationName(lat: Double, long: Double) {
        try {
            val addresses = geocoder.getFromLocation(lat, long, 1)
            val obj: Address = addresses?.get(0)!!
            locality.value = obj.locality ?: "Unknown"
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("TAG", "getLocationName: ${e.message}")
            locality.value = "-"
        }
    }


}

sealed interface WeatherUIState {
    object Loading : WeatherUIState
    data class Success(val weatherInfo: WeatherInfo? = null) : WeatherUIState
    data class Error(val msg: String? = null) : WeatherUIState
}
