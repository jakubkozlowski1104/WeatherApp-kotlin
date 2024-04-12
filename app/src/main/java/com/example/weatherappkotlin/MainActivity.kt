package com.example.weatherappkotlin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.weatherappkotlin.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.log

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnFind.setOnClickListener {
            val cityName = binding.inpCityName.text.toString()
            if (cityName.isNotEmpty()) {
                fetchWeatherData(cityName)
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchWeatherData(cityName: String) {
        val apiKey = "54115490ba2f3c3c704b01a9e52dad7a"
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$apiKey"
        val urlForecast = "https://api.openweathermap.org/data/2.5/forecast?q=$cityName&appid=$apiKey"

        val client = OkHttpClient()

        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(applicationContext, "Location not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val gson = Gson()
                    val jsonObject = gson.fromJson(response.body?.string(), JsonObject::class.java)
                    if (jsonObject.has("main")) {
                        val temp = jsonObject.getAsJsonObject("main").get("temp").asDouble
                        val pressure = jsonObject.getAsJsonObject("main").get("pressure").asDouble
                        val description = jsonObject.getAsJsonArray("weather").get(0).asJsonObject.get("description").asString
                        val lon = jsonObject.get("coord").asJsonObject.get("lon").asDouble
                        val lat = jsonObject.get("coord").asJsonObject.get("lat").asDouble
                        val windSpeed = jsonObject.getAsJsonObject("wind").get("speed").asDouble
                        val windDirection = jsonObject.getAsJsonObject("wind").get("deg").asDouble

                        // Przygotowanie danych do przekazania do CityWeatherActivity
                        val intent = Intent(this@MainActivity, CityWeatherActivity::class.java)
                        val bundle = Bundle()
                        bundle.putString("cityName", cityName)
                        bundle.putDouble("temperature", temp)
                        bundle.putDouble("pressure", pressure)
                        bundle.putString("description", description)
                        bundle.putString("coordinates", "$lat, $lon")
                        bundle.putDouble("windSpeed", windSpeed)
                        bundle.putDouble("windDirection", windDirection)
                        intent.putExtras(bundle)

                        Log.d("MainActivityCheck", "Ciśnienie: $pressure")


                        startActivity(intent)
                    } else {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Location not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Location not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        val requestForecast = Request.Builder().url(urlForecast).build()
        client.newCall(requestForecast).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(applicationContext, "Location not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val gson = Gson()
                    val jsonObject = gson.fromJson(response.body?.string(), JsonObject::class.java)
                    // Przetwórz odpowiedź, aby uzyskać dane na kolejny dzień
                    val forecastList = jsonObject.getAsJsonArray("list")
                    val tomorrowForecast = forecastList.firstOrNull { forecast ->
                        val forecastTime = forecast.asJsonObject.get("dt").asLong
                        val forecastDate = Instant.ofEpochSecond(forecastTime).atZone(ZoneId.systemDefault()).toLocalDate()
                        forecastDate.isEqual(LocalDate.now().plusDays(1))
                    }
                    if (tomorrowForecast != null) {
                        val temp = tomorrowForecast.asJsonObject.getAsJsonObject("main").get("temp").asDouble
                        val intent = Intent(this@MainActivity, CityWeatherActivity::class.java)
                        val bundle = Bundle()
                        bundle.putString("cityName", cityName)
                        bundle.putDouble("temperatureForecast", temp)
                        intent.putExtras(bundle)
                        Log.d("MainActivityCheck", "Temperatura na jutro: $temp")
                        startActivity(intent)
                    } else {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Location not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Location not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}

