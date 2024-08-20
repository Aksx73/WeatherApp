package com.absut.weatherapp.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.absut.weatherapp.R
import kotlin.math.roundToInt

@Composable
fun TextWithStartImage(
    @DrawableRes icon: Int,
    text: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(6.dp)) // Add spacing if needed
        Text(text = text, style = textStyle)
    }
}

@Preview
@Composable
private fun Preview() {
    Surface {
        TextWithStartImage(
            icon = R.drawable.ic_water_black_24dp,
            text = "Compose",
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}