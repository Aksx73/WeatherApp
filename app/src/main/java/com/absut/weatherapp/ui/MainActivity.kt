package com.absut.weatherapp.ui

import android.Manifest
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.absut.weatherapp.R
import com.absut.weatherapp.databinding.ActivityMainBinding
import com.absut.weatherapp.domain.model.WeatherInfo
import dagger.hilt.android.AndroidEntryPoint
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<WeatherViewModel>()
    private lateinit var weatherAdapter: WeatherAdapter
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                viewModel.loadWeatherInfo()
            }

        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        weatherAdapter = WeatherAdapter()
        binding.content.recyclerView.adapter = weatherAdapter
        binding.content.recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.content.recyclerView.setHasFixedSize(true)

        viewModel.uiState.observe(this) { state ->
            binding.errorState.isVisible = state == WeatherUIState.Error()
            binding.progressCircular.isVisible = state == WeatherUIState.Loading
            binding.content.content.isVisible =
                state != WeatherUIState.Error() || state != WeatherUIState.Loading
            when (state) {
                is WeatherUIState.Error -> {
                    binding.content.content.visibility = View.GONE
                    binding.errorState.visibility = View.VISIBLE
                    binding.errorMsg.text = state.msg
                    binding.btRetry.setOnClickListener {
                        viewModel.loadWeatherInfo()
                    }
                }
                WeatherUIState.Loading -> {
                    binding.progressCircular.visibility = View.VISIBLE
                    binding.content.content.visibility = View.GONE
                }
                is WeatherUIState.Success -> {
                    bind(state.weatherInfo)
                }
            }
        }

        viewModel.locality.observe(this){
            binding.content.txtLocation.text = it
        }
    }

    private fun bind(weatherInfo: WeatherInfo?) {
        weatherInfo?.currentWeatherData?.let { data ->
            binding.content.apply {
                //  txtLocation.text = data.
                image.setImageResource(data.weatherType.iconRes)
                txtTime.text = "Today ${data.time.format(DateTimeFormatter.ofPattern("h a"))}"  //value -> Today 2 pm
                txtTemp.text = "${data.temperatureCelsius.roundToInt()}°" // value -> 25°
                txtWeatherType.text = data.weatherType.weatherDesc
                txtHumidity.text = "${data.humidity.roundToInt()} %"
                txtPressure.text = "${data.pressure.roundToInt()} hpa"
                txtWind.text = "${data.windSpeed.roundToInt()} km/h"

            }
        }

        weatherInfo?.weatherDataPerDay?.get(0)?.let { data ->
            weatherAdapter.submitList(data)
        }
    }


}