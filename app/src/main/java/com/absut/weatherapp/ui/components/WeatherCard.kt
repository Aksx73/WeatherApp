package com.absut.weatherapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.absut.weatherapp.R
import com.absut.weatherapp.domain.model.WeatherData
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun WeatherCard(
    modifier: Modifier = Modifier,
    data: WeatherData?,
    location: String?,
    onClick: () -> Unit
) {

    val cardBackgroundColor: Color = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.surfaceContainerHighest
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    Card(
        onClick = { onClick },
        modifier = modifier.padding(horizontal = 16.dp, vertical = 24.dp),
        colors = CardDefaults.cardColors().copy(
            containerColor = cardBackgroundColor
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

@Preview
@Composable
private fun WeatherCardPreview() {
    WeatherCard(data = null, location = null){
        //todo on click
    }
}