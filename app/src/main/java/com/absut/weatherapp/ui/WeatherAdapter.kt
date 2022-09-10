package com.absut.weatherapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.absut.weatherapp.databinding.WeatherListItemBinding
import com.absut.weatherapp.domain.model.WeatherData
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class WeatherAdapter : ListAdapter<WeatherData, WeatherAdapter.WeatherViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val binding = WeatherListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WeatherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class WeatherViewHolder(private val binding: WeatherListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(weatherData: WeatherData) {
            binding.apply {
                image.setImageResource(weatherData.weatherType.iconRes)
                txtTemp.text = "${weatherData.temperatureCelsius.roundToInt()}°"
                txtTime.text = weatherData.time.format(DateTimeFormatter.ofPattern("h a"))  //value -> 2 pm
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<WeatherData>() {
        override fun areItemsTheSame(oldItem: WeatherData, newItem: WeatherData) =
            oldItem.time == newItem.time

        override fun areContentsTheSame(oldItem: WeatherData, newItem: WeatherData) =
            oldItem == newItem
    }
}