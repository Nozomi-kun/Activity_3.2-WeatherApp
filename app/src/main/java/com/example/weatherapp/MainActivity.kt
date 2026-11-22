package com.example.weatherapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {

    private val apiKey = "565c1e2e5ff3c78e26f08c3577c8ed86"

    // Step 7: Retrofit instance with Moshi converter
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val weatherService = retrofit.create(WeatherService::class.java)

    private lateinit var cityText: TextView
    private lateinit var shortDescText: TextView
    private lateinit var longDescText: TextView
    private lateinit var weatherIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Keep the toolbar below the status bar and content above the nav bar
        val toolbar = findViewById<View>(R.id.toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            toolbar.setPadding(0, bars.top, 0, 0)
            v.setPadding(bars.left, 0, bars.right, bars.bottom)
            insets
        }

        cityText = findViewById(R.id.cityText)
        shortDescText = findViewById(R.id.shortDescText)
        longDescText = findViewById(R.id.longDescText)
        weatherIcon = findViewById(R.id.weatherIcon)

        fetchWeather("New York")
    }

    // Step 8: call the API
    private fun fetchWeather(city: String) {
        weatherService.getWeather(city, apiKey)
            .enqueue(object : Callback<WeatherResponse> {

                // Step 9: success path
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        val weather = body?.weather?.firstOrNull()
                        if (body != null && weather != null) {
                            showWeather(body.name, weather)
                        } else {
                            showError("Empty response from server")
                        }
                    } else {
                        // Step 10a: server replied with an error code
                        when (response.code()) {
                            401 -> showError("Invalid API key")
                            404 -> showError("City not found")
                            429 -> showError("Too many requests")
                            else -> showError("Server error: ${response.code()}")
                        }
                    }
                }

                // Step 10b: no network, timeout, or JSON parsing failure
                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Log.e("WeatherApp", "Request failed", t)
                    showError("Network error: ${t.localizedMessage}")
                }
            })
    }

    private fun showWeather(city: String, weather: Weather) {
        cityText.text = city
        shortDescText.text = weather.main
        longDescText.text = weather.description

        val iconUrl = "https://openweathermap.org/img/wn/${weather.icon}@2x.png"
        Glide.with(this)
            .load(iconUrl)
            .into(weatherIcon)
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
