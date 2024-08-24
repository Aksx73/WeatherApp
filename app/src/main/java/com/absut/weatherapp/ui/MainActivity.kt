package com.absut.weatherapp.ui

import android.Manifest
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.absut.weatherapp.ui.components.ErrorView
import com.absut.weatherapp.ui.components.ProgressView
import com.absut.weatherapp.R
import com.absut.weatherapp.domain.model.WeatherData
import com.absut.weatherapp.domain.model.WeatherInfo
import com.absut.weatherapp.domain.util.WeatherType
import com.absut.weatherapp.ui.components.TextWithStartImage
import com.absut.weatherapp.ui.components.WaveView
import com.absut.weatherapp.ui.components.WeatherCard
import com.absut.weatherapp.ui.components.WeatherListItem
import com.example.compose.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<WeatherViewModel>()

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { viewModel.loadWeatherInfo() }
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        setContent {
            AppTheme {
                HomeScreen()
            }
        }

    }

    @Composable
    fun HomeScreen(modifier: Modifier = Modifier) {
        val uiState by viewModel.uiState.observeAsState(WeatherUIState.Loading)
        val locationName by viewModel.locality.observeAsState("Locating..")

        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
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
                        HomeContent(weatherInfo = it, location = locationName)
                    }
                }
            }
        }
    }

    @Composable
    fun HomeContent(modifier: Modifier = Modifier, weatherInfo: WeatherInfo, location: String?) {
        val cardBackgroundColor: Color = if (isSystemInDarkTheme()) {
            MaterialTheme.colorScheme.surfaceContainerHighest
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        }

        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    icon = { Icon(Icons.Outlined.Loop, null) },
                    text = { Text(text = "Refresh") },
                )
            },
            floatingActionButtonPosition = FabPosition.Center
        ) { paddingValues -> // paddingValues are provided by Scaffold to handle FAB placement
            Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues) // Apply padding to avoid overlap with FAB
                        .padding(24.dp)
                ) {
                    Text(
                        text = "${
                            weatherInfo.currentWeatherData?.time?.format(
                                DateTimeFormatter.ofPattern("d MMMM")
                            )
                        }",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = location ?: "Unknown",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${weatherInfo.currentWeatherData?.temperatureCelsius?.roundToInt()}°",
                            style = MaterialTheme.typography.displayLarge,
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                        Image(
                            painter = painterResource(id = weatherInfo.currentWeatherData?.weatherType?.iconRes!!),
                            contentDescription = null,
                            modifier = Modifier
                                .size(78.dp)
                        )
                    }
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = weatherInfo.currentWeatherData?.weatherType?.weatherDesc
                            ?: "Unknown",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.size(20.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column {
                            TextWithStartImage(
                                icon = R.drawable.ic_clock_24,
                                text = "Hourly forecast",
                                textStyle = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 10.dp)
                            ) {
                                items(weatherInfo.weatherDataPerDay[0] ?: emptyList()) {
                                    WeatherListItem(item = it)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                    Row {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(end = 8.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.surface,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                TextWithStartImage(
                                    icon = R.drawable.ic_water_black_24dp,
                                    text = "Pressure",
                                    textStyle = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .align(Alignment.Start)
                                        .padding(top = 16.dp, start = 16.dp)
                                )
                                Row(modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)) {
                                    Text(
                                        text = weatherInfo.currentWeatherData?.pressure?.roundToInt()
                                            .toString(),
                                        style = MaterialTheme.typography.displayMedium,
                                        modifier = Modifier.alignByBaseline(),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "hpa",
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier
                                            .alignByBaseline()
                                            .padding(start = 8.dp)
                                    )

                                }
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(start = 8.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.surface,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                TextWithStartImage(
                                    icon = R.drawable.ic_air_black_24dp,
                                    text = "Wind",
                                    textStyle = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .align(Alignment.Start)
                                        .padding(top = 16.dp, start = 16.dp)
                                )
                                Row(modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)) {
                                    Text(
                                        text = weatherInfo.currentWeatherData?.windSpeed?.roundToInt()
                                            .toString(),
                                        style = MaterialTheme.typography.displayMedium,
                                        modifier = Modifier.alignByBaseline(),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "km/h",
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier
                                            .alignByBaseline()
                                            .padding(start = 8.dp)
                                    )

                                }
                            }

                        }

                    }
                    Spacer(modifier = Modifier.size(16.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .padding(end = 8.dp)
                            .height(150.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            WaveView(
                                fillPercentage = weatherInfo.currentWeatherData?.humidity?.toFloat()?.div(100) ?: 0f,
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                Modifier.fillMaxSize()
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            TextWithStartImage(
                                icon = R.drawable.ic_water_drop_black_24dp,
                                text = "Humidity",
                                textStyle = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(top = 16.dp, start = 16.dp)
                            )
                            Row(modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)) {
                                Text(
                                    text = weatherInfo.currentWeatherData?.humidity?.roundToInt()
                                        .toString(),
                                    style = MaterialTheme.typography.displayMedium,
                                    modifier = Modifier.alignByBaseline(),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "%",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier
                                        .alignByBaseline()
                                        .padding(start = 8.dp)
                                )

                            }
                        }

                    }


                    /*WeatherCard(data = weatherInfo.currentWeatherData, location = location){
                        //todo on click
                    }
    
                    Text(
                        text = "Hourly forecast | Today | ${
                            weatherInfo.currentWeatherData?.time?.format(
                                DateTimeFormatter.ofPattern("d MMMM")
                            )
                        }",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
    
                    Spacer(modifier = Modifier.size(12.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = MaterialTheme.shapes.small,
                        color = cardBackgroundColor
                    ) {
                        LazyRow(
                            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                        ) {
                            items(weatherInfo.weatherDataPerDay[0] ?: emptyList()) {
                                WeatherListItem(item = it)
                            }
                        }
                    }*/
                }
            }
        }
    }


    @Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
    @Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
    @Composable
    private fun HomeScreenContentPreview() {
        AppTheme {
            HomeContent(
                weatherInfo =
                WeatherInfo(
                    weatherDataPerDay = mapOf(
                        0 to listOf(
                            WeatherData(
                                time = LocalDateTime.now(),
                                temperatureCelsius = 32.0,
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
                                weatherType = WeatherType.fromWeatherCode(77)
                            ),
                            WeatherData(
                                time = LocalDateTime.now(),
                                temperatureCelsius = 27.0,
                                pressure = 720.0,
                                windSpeed = 12.0,
                                humidity = 32.0,
                                weatherType = WeatherType.fromWeatherCode(57)
                            ),
                            WeatherData(
                                time = LocalDateTime.now(),
                                temperatureCelsius = 29.0,
                                pressure = 720.0,
                                windSpeed = 12.0,
                                humidity = 32.0,
                                weatherType = WeatherType.fromWeatherCode(65)
                            ),
                            WeatherData(
                                time = LocalDateTime.now(),
                                temperatureCelsius = 31.0,
                                pressure = 720.0,
                                windSpeed = 12.0,
                                humidity = 32.0,
                                weatherType = WeatherType.fromWeatherCode(99)
                            )
                        )
                    ),
                    currentWeatherData = WeatherData(
                        time = LocalDateTime.now(),
                        temperatureCelsius = 25.0,
                        pressure = 720.0,
                        windSpeed = 12.0,
                        humidity = 32.0,
                        weatherType = WeatherType.fromWeatherCode(99)
                    )
                ),
                location = "Manchar"
            )
        }
    }

}
