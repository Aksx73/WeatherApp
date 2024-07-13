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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import com.absut.isro.archive.ui.common.ErrorView
import com.absut.isro.archive.ui.common.ProgressView
import com.absut.weatherapp.R
import com.absut.weatherapp.databinding.ActivityMainBinding
import com.absut.weatherapp.domain.model.WeatherData
import com.absut.weatherapp.domain.model.WeatherInfo
import com.absut.weatherapp.domain.util.WeatherType
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
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
        Surface(modifier = modifier.fillMaxSize()) {
            Column {
                WeatherCard()

                Text(
                    text = "Today | 6 July",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )

                /*LazyRow {
                    items() {
                        WeatherListItem()
                    }
                }*/
            }
        }
    }

    @Composable
    fun HomeScreen2(modifier: Modifier = Modifier) {

        val uiState by viewModel.uiState.observeAsState(WeatherUIState.Loading)

        when (uiState) {
            is WeatherUIState.Error -> {
                ErrorView(text = (uiState as WeatherUIState.Error).msg) {
                    viewModel.loadWeatherInfo()
                }
            }

            WeatherUIState.Loading -> {
                ProgressView()
            }

            is WeatherUIState.Success -> {
                (uiState as WeatherUIState.Success).weatherInfo?.let {
                    HomeContent(weatherInfo = it)
                }
            }
        }
    }
}

    @Composable
    fun HomeContent(modifier: Modifier = Modifier, weatherInfo: WeatherInfo) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        /*todo refresh*/
                    },
                    icon = { Icon(Icons.Filled.Air, null) },
                    text = { Text(text = "Refresh") },
                )
            },
            floatingActionButtonPosition = FabPosition.Center
        ) { paddingValues -> // paddingValues are provided by Scaffold to handle FAB placement
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply padding to avoid overlap with FAB
            ) {
                WeatherCard(data = weatherInfo.currentWeatherData)

                Text(
                    text = "Today | ${
                        weatherInfo.currentWeatherData?.time?.format(
                            DateTimeFormatter.ofPattern(
                                "d MMMM"
                            )
                        )
                    }",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )

                LazyRow(
                     contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp)
                ) {
                    items(weatherInfo.weatherDataPerDay[0] ?: emptyList()) {
                        WeatherListItem(item = it)
                    }
                }
            }
        }
    }

    @Composable
    fun WeatherCard(modifier: Modifier = Modifier, data: WeatherData?) {
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
                    Text(
                        text = "Today, ${data?.time?.format(DateTimeFormatter.ofPattern("h a"))}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = painterResource(id = data?.weatherType?.iconRes!!),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${data?.temperatureCelsius?.roundToInt()}°",
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    //color = colorResource(id = android.R.attr.textColorPrimary),
                )
                Spacer(
                    modifier = Modifier.height(8.dp),
                )
                Text(
                    text = data?.weatherType?.weatherDesc ?: "Unknown",
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
                        text = "${data?.pressure?.roundToInt()} hpa",
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    TextWithStartImage(
                        icon = R.drawable.ic_water_drop_black_24dp,
                        text = "${data?.humidity?.roundToInt()} %",
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    TextWithStartImage(
                        icon = R.drawable.ic_air_black_24dp,
                        text = "${data?.windSpeed?.roundToInt()} km/h",
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
fun WeatherListItem(modifier: Modifier = Modifier, item: WeatherData) {
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
    Surface {
        TextWithStartImage(
            icon = R.drawable.ic_place_black_24dp,
            text = "Manchar",
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview
@Composable
private fun HomeSContentPreview() {
    HomeContent(
        weatherInfo =
        WeatherInfo(
            weatherDataPerDay = mapOf(
                1 to listOf(
                    WeatherData(
                        time = LocalDateTime.now(),
                        temperatureCelsius = 25.0,
                        pressure = 720.0,
                        windSpeed = 12.0,
                        humidity = 32.0,
                        weatherType = WeatherType.fromWeatherCode(0)
                    ),
                    WeatherData(
                        time = LocalDateTime.now(),
                        temperatureCelsius = 25.0,
                        pressure = 720.0,
                        windSpeed = 12.0,
                        humidity = 32.0,
                        weatherType = WeatherType.fromWeatherCode(2)
                    )
                ),
                2 to listOf(
                    WeatherData(
                        time = LocalDateTime.now(),
                        temperatureCelsius = 25.0,
                        pressure = 720.0,
                        windSpeed = 12.0,
                        humidity = 32.0,
                        weatherType = WeatherType.fromWeatherCode(0)
                    ),
                    WeatherData(
                        time = LocalDateTime.now(),
                        temperatureCelsius = 25.0,
                        pressure = 720.0,
                        windSpeed = 12.0,
                        humidity = 32.0,
                        weatherType = WeatherType.fromWeatherCode(2)
                    )
                )
            ),
            currentWeatherData =  WeatherData(
                time = LocalDateTime.now(),
                temperatureCelsius = 25.0,
                pressure = 720.0,
                windSpeed = 12.0,
                humidity = 32.0,
                weatherType = WeatherType.fromWeatherCode(0)
            )
        )
    )
}

@Preview
@Composable
private fun WeatherListItemPreview() {
    Surface {
        WeatherListItem(
            item =
            WeatherData(
                time = LocalDateTime.now(),
                temperatureCelsius = 25.0,
                pressure = 720.0,
                windSpeed = 12.0,
                humidity = 32.0,
                weatherType = WeatherType.fromWeatherCode(0)
            )
        )
    }
}
