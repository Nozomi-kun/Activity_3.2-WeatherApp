package com.example.weatherapp

data class WeatherResponse(
    val name: String,
    val weather: List<Weather>
)

data class Weather(
    val main: String,        // "Clouds"
    val description: String, // "broken clouds"
    val icon: String         // "04d"
)
