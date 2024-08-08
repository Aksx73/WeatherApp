package com.absut.weatherapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.absut.weatherapp.domain.model.WeatherData
import com.absut.weatherapp.domain.util.WeatherType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

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
