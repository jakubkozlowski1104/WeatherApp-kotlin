package com.example.weatherappkotlin

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.weatherappkotlin.databinding.FragmentWindBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class Wind : Fragment() {
    private lateinit var binding: FragmentWindBinding
    private lateinit var networkConnection: NetworkConnection

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWindBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = activity?.getSharedPreferences("CityWeatherPrefs", Context.MODE_PRIVATE)
        val cityName = sharedPreferences?.getString("cityName", "") ?: ""

        networkConnection = NetworkConnection(requireContext())
        networkConnection.observe(viewLifecycleOwner, Observer { isConnected ->
            if (isConnected) {
                Log.d("WindFragmentNet", "Internet connected")
                binding.networkStatusTextView.visibility = View.GONE
                binding.refreshData.visibility = View.VISIBLE
                if (cityName.isNotEmpty()) {
                    fetchWeatherData(cityName)
                }
                binding.refreshData.setOnClickListener {
                    if (cityName.isNotEmpty()) {
                        fetchWeatherData(cityName)
                        Toast.makeText(context, "Dane zostały odświeżone", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.d("WindFragmentNet", "No internet connection")
                binding.networkStatusTextView.visibility = View.VISIBLE
                binding.refreshData.visibility = View.GONE

                // Read weather data from SharedPreferences when no internet connection
                readWeatherDataFromSharedPreferences(cityName)
            }
        })
    }

    private fun fetchWeatherData(cityName: String) {
        Log.d("WindFragmentNet", "Fetch")
        val apiKey = "54115490ba2f3c3c704b01a9e52dad7a"
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$apiKey&units=metric"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Log.d("WindFragment", "Błąd pobierania danych: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val gson = Gson()
                    val jsonObject = gson.fromJson(response.body?.string(), JsonObject::class.java)

                    val mainObject = jsonObject.getAsJsonObject("main")
                    val windObject = jsonObject.getAsJsonObject("wind")
                    val sysObject = jsonObject.getAsJsonObject("sys")

                    val pressure = mainObject.get("pressure").asDouble
                    val humidity = mainObject.get("humidity").asDouble
                    val sunriseTimestamp = sysObject.get("sunrise").asLong
                    val sunsetTimestamp = sysObject.get("sunset").asLong

                    val windSpeed = windObject.get("speed").asDouble
                    val windDirection = windObject.get("deg").asDouble

                    saveWeatherDataToSharedPreferences(cityName, pressure, humidity, sunriseTimestamp, sunsetTimestamp, windSpeed, windDirection)

                    requireActivity().runOnUiThread {
                        displayWeatherData(cityName, pressure, humidity, sunriseTimestamp, sunsetTimestamp, windSpeed, windDirection)
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Log.d("WindFragment", "Błąd pobierania danych: ${response.message}")
                    }
                }
            }
        })
    }

    private fun displayWeatherData(
        cityName: String,
        pressure: Double,
        humidity: Double,
        sunriseTimestamp: Long,
        sunsetTimestamp: Long,
        windSpeed: Double,
        windDirection: Double
    ) {
        binding.pressureTextView.text = "Ciśnienie: $pressure hPa"
        binding.humidityTextView.text = "Wilgotność: $humidity %"

        val sunriseTime = formatTimestamp(sunriseTimestamp)
        val sunsetTime = formatTimestamp(sunsetTimestamp)

        binding.sunriseTextView.text = "Wschód słońca: $sunriseTime"
        binding.sunsetTextView.text = "Zachód słońca: $sunsetTime"

        binding.windSpeedTextView.text = "Prędkość wiatru: $windSpeed m/s"
        binding.windDirectionTextView.text = "Kierunek wiatru: $windDirection°"
    }

    private fun formatTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = Date(timestamp * 1000)
        return dateFormat.format(date)
    }

    private fun saveWeatherDataToSharedPreferences(
        cityName: String,
        pressure: Double,
        humidity: Double,
        sunriseTimestamp: Long,
        sunsetTimestamp: Long,
        windSpeed: Double,
        windDirection: Double
    ) {
        val sharedPreferences = activity?.getSharedPreferences(cityName, Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()

        editor?.apply {
            putString("pressure", pressure.toString())
            putString("humidity", humidity.toString())
            putString("sunrise", sunriseTimestamp.toString())
            putString("sunset", sunsetTimestamp.toString())
            putString("windSpeed", windSpeed.toString())
            putString("windDirection", windDirection.toString())
            apply()
        }
    }

    private fun readWeatherDataFromSharedPreferences(cityName: String) {
        val sharedPreferences = activity?.getSharedPreferences(cityName, Context.MODE_PRIVATE)
        val pressure = sharedPreferences?.getString("pressure", "0")?.toDoubleOrNull() ?: 0.0
        val humidity = sharedPreferences?.getString("humidity", "0")?.toDoubleOrNull() ?: 0.0
        val sunriseTimestamp = sharedPreferences?.getString("sunrise", "0")?.toLongOrNull() ?: 0L
        val sunsetTimestamp = sharedPreferences?.getString("sunset", "0")?.toLongOrNull() ?: 0L
        val windSpeed = sharedPreferences?.getString("windSpeed", "0")?.toDoubleOrNull() ?: 0.0
        val windDirection = sharedPreferences?.getString("windDirection", "0")?.toDoubleOrNull() ?: 0.0

        displayWeatherData(cityName, pressure, humidity, sunriseTimestamp, sunsetTimestamp, windSpeed, windDirection)
    }
}
