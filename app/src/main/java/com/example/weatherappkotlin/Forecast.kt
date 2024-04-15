package com.example.weatherappkotlin

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.weatherappkotlin.databinding.FragmentForecastBinding
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class Forecast : Fragment() {
    private lateinit var binding: FragmentForecastBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForecastBinding.inflate(inflater, container, false)

        val sharedPreferences = activity?.getSharedPreferences("CityWeatherPrefs", Context.MODE_PRIVATE)
        val cityName = sharedPreferences?.getString("cityName", "") ?: ""
        Log.d("ForecastCheck", "Odczytano nazwę miasta: $cityName")

        if (cityName.isNotEmpty()) {
            fetchWeatherData(cityName)
        }
        return binding.root
    }

    private fun fetchWeatherData(cityName: String) {
        val apiKey = "54115490ba2f3c3c704b01a9e52dad7a"
        val url = "https://api.openweathermap.org/data/2.5/forecast?q=$cityName&appid=$apiKey"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Log.d("ForecastCheck", "Błąd pobierania danych: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val gson = Gson()
                    val jsonObject = gson.fromJson(response.body?.string(), JsonObject::class.java)

                    val forecastList = jsonObject.getAsJsonArray("list")

                    // Pobierz dane dla kolejnych dni co 8. element, zaczynając od indeksu 10 (prognoza na 12:00 kolejnego dnia)
                    var dayOffset = 0
                    for (i in 10 until forecastList.size() step 8) {
                        processForecast(forecastList, i, dayOffset)
                        dayOffset++
                    }

                    // Dodaj również ostatni dzień (indeks 39)
                    if (forecastList.size() > 39) {
                        processForecast(forecastList, 39, dayOffset)
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Log.d("ForecastCheck", "Błąd pobierania danych: ${response.message}")
                    }
                }
            }
        })
    }

    private fun processForecast(forecastList: JsonArray, index: Int, dayOffset: Int) {
        val dailyForecast = forecastList.get(index).asJsonObject
        val main = dailyForecast.getAsJsonObject("main")
        val weatherArray = dailyForecast.getAsJsonArray("weather")
        val weather = weatherArray.get(0).asJsonObject
        val wind = dailyForecast.getAsJsonObject("wind")

        val temperature = main.get("temp").asDouble - 273.15
        val description = weather.get("description").asString.capitalize()
        val windSpeed = wind.get("speed").asDouble

        val dateText = formatDate(dailyForecast.get("dt").asLong)

        requireActivity().runOnUiThread {
            val dayOfWeekLabel = getDayOfWeekLabel(dayOffset)
            val forecastText = "$dayOfWeekLabel ($dateText): Temperatura: ${temperature.format(1)}°C, Opis: $description, Prędkość wiatru: ${windSpeed.format(1)} m/s"
            binding.temperatureForecastTextView.append("\n$forecastText")
        }
    }

    // Funkcja pomocnicza do pobrania nazwy dnia tygodnia na podstawie przesunięcia od bieżącego dnia
    private fun getDayOfWeekLabel(offset: Int): String {
        val daysOfWeek = arrayOf("Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota", "Niedziela")
        val currentDayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) - 1
        val targetDayOfWeek = (currentDayOfWeek + offset) % 7
        return daysOfWeek[targetDayOfWeek]
    }

    // Rozszerzenie do formatowania liczby zmiennoprzecinkowej do określonej liczby miejsc po przecinku
    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

    // Funkcja pomocnicza do formatowania daty z timestampa
    private fun formatDate(timestamp: Long): String {
        val dateFormat = java.text.SimpleDateFormat("dd.MM")
        val date = java.util.Date(timestamp * 1000)
        return dateFormat.format(date)
    }

}
