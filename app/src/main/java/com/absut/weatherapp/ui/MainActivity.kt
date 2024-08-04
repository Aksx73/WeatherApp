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
import com.absut.isro.archive.ui.common.ErrorView
import com.absut.isro.archive.ui.common.ProgressView
import com.absut.weatherapp.R
import com.absut.weatherapp.domain.model.WeatherData
import com.absut.weatherapp.domain.model.WeatherInfo
import com.absut.weatherapp.domain.util.WeatherType
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
            color = MaterialTheme.colorScheme.surface
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply padding to avoid overlap with FAB
                    .background(MaterialTheme.colorScheme.surface)

            ) {
                WeatherCard(data = weatherInfo.currentWeatherData, location = location)

                Text(
                    text = "Hourly forecast | Today | ${
                        weatherInfo.currentWeatherData?.time?.format(
                            DateTimeFormatter.ofPattern("d MMMM")
                        )
                    }",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.size(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        items(weatherInfo.weatherDataPerDay[0] ?: emptyList()) {
                            WeatherListItem(item = it)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun WeatherCard(modifier: Modifier = Modifier, data: WeatherData?, location: String?) {

        val cardBackgroundColor: Color = if (isSystemInDarkTheme()) {
            MaterialTheme.colorScheme.surfaceContainerHighest
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        }

        Card(
            onClick = { /*TODO*/ },
            modifier = modifier.padding(horizontal = 16.dp, vertical = 24.dp),
            colors = CardDefaults.cardColors().copy(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
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
                        text = location ?: "Unknown",
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
                    text = "${data.temperatureCelsius.roundToInt()}°",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    //color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(
                    modifier = Modifier.height(8.dp),
                )
                Text(
                    text = data.weatherType.weatherDesc ?: "Unknown",
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
                        text = "${data.pressure.roundToInt()} hpa",
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    TextWithStartImage(
                        icon = R.drawable.ic_water_drop_black_24dp,
                        text = "${data.humidity.roundToInt()} %",
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    TextWithStartImage(
                        icon = R.drawable.ic_air_black_24dp,
                        text = "${data.windSpeed.roundToInt()} km/h",
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
        Column(modifier = modifier.padding(16.dp)) {
            Text(
                text = "${item.temperatureCelsius.roundToInt()}°",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.size(6.dp))
            Image(
                painter = painterResource(id = item.weatherType.iconRes),
                contentDescription = "weather icon",
                Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = item.time.format(DateTimeFormatter.ofPattern("h a")),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    @Preview
    @Composable
    private fun WeatherCardPreview() {
        WeatherCard(data = null, location = null)
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

    @Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
    @Composable
    private fun HomeSContentPreview() {
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
