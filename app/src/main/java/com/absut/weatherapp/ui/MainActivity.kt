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
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

        viewModel.locality.observe(this) {
            binding.content.txtLocation.text = it
        }
    }

    private fun bind(weatherInfo: WeatherInfo?) {
        weatherInfo?.currentWeatherData?.let { data ->
            binding.content.apply {
                //  txtLocation.text = data.
                image.setImageResource(data.weatherType.iconRes)
                txtTime.text =
                    "Today, ${data.time.format(DateTimeFormatter.ofPattern("h a"))}"  //value -> Today, 2 pm
                txtTemp.text = "${data.temperatureCelsius.roundToInt()}°" // value -> 25°
                txtWeatherType.text = data.weatherType.weatherDesc
                txtHumidity.text = "${data.humidity.roundToInt()} %"
                txtPressure.text = "${data.pressure.roundToInt()} hpa"
                txtWind.text = "${data.windSpeed.roundToInt()} km/h"
                binding.content.txtToday.text =
                    "Today | ${data.time.format(DateTimeFormatter.ofPattern("d MMMM"))}"
            }
        }

        weatherInfo?.weatherDataPerDay?.get(0)?.let { data ->
            weatherAdapter.submitList(data)

        }
    }

    @Composable
    fun HomeScreen(modifier: Modifier = Modifier) {

    }

    @Composable
    fun WeatherCard(modifier: Modifier = Modifier) {
        Card(
            onClick = { /*TODO*/ },
            modifier = modifier.padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextWithStartImage(
                        icon = R.drawable.ic_place_black_24dp,
                        text = "Manchar",
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    Text(text = "Today 3pm", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_rainshower),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "25",
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    //color = colorResource(id = android.R.attr.textColorPrimary),
                )
                Spacer(
                    modifier = Modifier.height(8.dp),
                )
                Text(
                    text = "Clear Sky",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 20.sp,
                    //color = colorResource(id = android.R.attr.textColorPrimary),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    TextWithStartImage(
                        icon = R.drawable.ic_water_black_24dp,
                        text = "720hpa",
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    TextWithStartImage(
                        icon = R.drawable.ic_water_drop_black_24dp,
                        text = "32%",
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    TextWithStartImage(
                        icon = R.drawable.ic_air_black_24dp,
                        text = "12 km/h",
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

    }

    @Composable
    fun TextWithStartImage(
        @DrawableRes icon: Int,
        text: String,
        textStyle: TextStyle
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp)) // Add spacing if needed
            Text(text = text, style = textStyle)
        }
    }

    @Composable
    fun WeatherListItem(modifier: Modifier = Modifier) {
        Column(modifier = modifier.padding(8.dp)) {
            Text(
                text = "24",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.size(6.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_rainythunder),
                contentDescription = "weather icon",
                Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = "3 pm",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    @Preview
    @Composable
    private fun WeatherCardPreview() {
        WeatherCard()
    }

    @Preview
    @Composable
    private fun TextWithStartImagePreview() {
        TextWithStartImage(
            icon = R.drawable.ic_place_black_24dp,
            text = "Manchar",
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }

    @Preview
    @Composable
    private fun WeatherListItemPreview() {
        Surface{
            WeatherListItem()
        }
    }


}