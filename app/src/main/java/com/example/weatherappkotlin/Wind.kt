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
                // Pobierz dane z API, gdy jest dostęp do Internetu
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
                // Odczytaj dane z SharedPreferences, gdy brak dostępu do Internetu
                val sharedPreferences = activity?.getSharedPreferences(cityName, Context.MODE_PRIVATE)
                val windSpeed = sharedPreferences?.getString("windSpeed", "")?.toDoubleOrNull() ?: 0.0
                val windDirection = sharedPreferences?.getString("windDirection", "")?.toDoubleOrNull() ?: 0.0

                Log.d("WindFragmentNet", "Shared")
                // Wyświetl dane z SharedPreferences
                displayWeatherData(cityName, windSpeed, windDirection)
            }
        })
    }

    private fun fetchWeatherData(cityName: String) {
        Log.d("WindFragmentNet", "Fetch")
        val apiKey = "54115490ba2f3c3c704b01a9e52dad7a"
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$apiKey"

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

                    val windSpeed = jsonObject.getAsJsonObject("wind").get("speed").asDouble
                    val windDirection = jsonObject.getAsJsonObject("wind").get("deg").asDouble

                    // Zapisz dane pogodowe do SharedPreferences dla danego miasta
                    saveWeatherDataToSharedPreferences(cityName, windSpeed, windDirection)

                    requireActivity().runOnUiThread {
                        displayWeatherData(cityName, windSpeed, windDirection)
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
        windSpeed: Double,
        windDirection: Double
    ) {
        binding.windSpeedTextView.text = "Prędkość wiatru: $windSpeed m/s"
        binding.windDirectionTextView.text = "Kierunek wiatru: $windDirection°"
    }

    private fun saveWeatherDataToSharedPreferences(
        cityName: String,
        windSpeed: Double,
        windDirection: Double
    ) {
        val sharedPreferences = activity?.getSharedPreferences(cityName, Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()

        editor?.apply {
            putString("windSpeed", windSpeed.toString())
            putString("windDirection", windDirection.toString())
            apply()
        }
    }
}
