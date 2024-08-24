package com.absut.weatherapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun WaveView(fillPercentage: Float, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val waveHeight = height * (1 - fillPercentage)

        val path = Path().apply {
            // Move to the starting point
            moveTo(0f, waveHeight)

            val amplitude = 10f // controls the wave height
            val frequency = 8 // controls number of waves

            for (x in 0 until width.toInt()) {
                val y = waveHeight + amplitude * sin((x * frequency * Math.PI / width)).toFloat()
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        // Draw the filled shape
        drawPath(path, color)
    }
}

@Preview
@Composable
private fun WaveViewPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            WaveView(
                fillPercentage = 0.45f,
                color = Color.Blue,
                Modifier
                    .size(200.dp)
                    .padding(16.dp)
            )
        }
    }
}