package com.example.weatherappkotlin

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherappkotlin.databinding.FragmentForecastBinding
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class Forecast : Fragment() {
    private lateinit var binding: FragmentForecastBinding
    private lateinit var networkConnection: NetworkConnection
    private lateinit var forecastAdapter: ForecastAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForecastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        forecastAdapter = ForecastAdapter(emptyList())

        binding.forecastRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = forecastAdapter
        }

        val sharedPreferences = activity?.getSharedPreferences("CityWeatherPrefs", Context.MODE_PRIVATE)
        val cityName = sharedPreferences?.getString("cityName", "") ?: ""

        networkConnection = NetworkConnection(requireContext())
        networkConnection.observe(viewLifecycleOwner, Observer { isConnected ->
            if (isConnected) {
                Log.d("ForecastFragmentNet", "Internet connected")
                if (cityName.isNotEmpty()) {
                    fetchWeatherData(cityName)
                }
            } else {
                Log.d("ForecastFragmentNet", "No internet connection")
                readWeatherDataFromSharedPreferences(cityName)
            }
        })
    }


    private fun fetchWeatherData(cityName: String) {
        val apiKey = "54115490ba2f3c3c704b01a9e52dad7a"
        val url = "https://api.openweathermap.org/data/2.5/forecast?q=$cityName&appid=$apiKey&lang=pl"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Log.d("ForecastCheck", "Error fetching data: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val gson = Gson()
                    val jsonObject = gson.fromJson(response.body?.string(), JsonObject::class.java)
                    val forecastList = jsonObject.getAsJsonArray("list")

                    val filteredForecastItems = mutableListOf<ForecastItem>()

                    for (i in 0 until forecastList.size()) {
                        val dailyForecast = forecastList.get(i).asJsonObject
                        val dtTxt = dailyForecast.get("dt_txt").asString

                        if (dtTxt.contains("12:00:00")) {
                            val sharedPreferencesUnits = activity?.getSharedPreferences("TemperatureUnitPrefs", Context.MODE_PRIVATE)
                            val isFahrenheit = sharedPreferencesUnits?.getBoolean("temperatureUnit", false) ?: false

                            var temperature = dailyForecast.getAsJsonObject("main").get("temp").asDouble

                            if (!isFahrenheit) {
                                temperature -= 273.15
                            }

                            val formattedTemperature = if (isFahrenheit) {
                                "${(temperature * 9 / 5) + 32}°F"
                            } else {
                                "${temperature.toInt()}°C"
                            }

                            val description = dailyForecast.getAsJsonArray("weather").get(0).asJsonObject.get("description").asString
                            val dateText = formatDate(dailyForecast.get("dt").asLong)

                            val forecastItem = ForecastItem(dateText, temperature, description)
                            filteredForecastItems.add(forecastItem)
                        }
                    }

                    requireActivity().runOnUiThread {
                        forecastAdapter.setData(filteredForecastItems)
                        Log.e("ForecastShared", "Zapisano dane do SharedPreferences")
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Log.d("ForecastCheck", "Error fetching data: ${response.message}")
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

        val sharedPreferencesUnits = activity?.getSharedPreferences("TemperatureUnitPrefs", Context.MODE_PRIVATE)
        val isFahrenheit = sharedPreferencesUnits?.getBoolean("temperatureUnit", false) ?: false
        Log.d("unit", "is faren: $isFahrenheit")
        var temperature = 0.0;

        if (isFahrenheit) {
            temperature = Math.round(main.get("temp").asDouble).toDouble()
        } else {
            temperature = Math.round(main.get("temp").asDouble).toDouble()
        }
        val description = weather.get("description").asString
        val dateText = formatDate(dailyForecast.get("dt").asLong)

        val sharedPreferences = activity?.getSharedPreferences("CityWeatherPrefs", Context.MODE_PRIVATE)
        val cityName = sharedPreferences?.getString("cityName", "") ?: ""
        saveWeatherDataToSharedPreferences(cityName, temperature, description, dateText)
    }

    private fun getDayOfWeekLabelFromDate(dateString: String): String {
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = format.parse(dateString)
        val calendar = java.util.Calendar.getInstance()
        if (date != null) {
            calendar.time = date
        }
        val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        val daysOfWeek = arrayOf("Niedziela", "Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota")
        return daysOfWeek[dayOfWeek - 1]
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

    private fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())
        val date = Date(timestamp * 1000)

        val calendar = Calendar.getInstance()
        calendar.time = date

        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayOfWeekLabel = when (dayOfWeek) {
            Calendar.MONDAY -> "poniedziałek"
            Calendar.TUESDAY -> "wtorek"
            Calendar.WEDNESDAY -> "środa"
            Calendar.THURSDAY -> "czwartek"
            Calendar.FRIDAY -> "piątek"
            Calendar.SATURDAY -> "sobota"
            Calendar.SUNDAY -> "niedziela"
            else -> ""
        }

        return "$dayOfWeekLabel\n${dateFormat.format(date)}"
    }

    private fun saveWeatherDataToSharedPreferences(
        cityName: String,
        temperature: Double,
        description: String,
        dateText: String
    ) {
        val sharedPreferences = activity?.getSharedPreferences("CityWeatherPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()


        val key = "forecast-$dateText-$description"
        val forecastData = JsonObject().apply {
            addProperty("temperature", temperature)
            addProperty("description", description)
            addProperty("date", dateText)
        }

        editor?.apply {
            putString(key, forecastData.toString())
            apply()
        }

        Log.e("ForecastShared", "Zapisano")

        readWeatherDataFromSharedPreferences(cityName)
    }

    private fun readWeatherDataFromSharedPreferences(cityName: String) {
        val sharedPreferences = activity?.getSharedPreferences("CityWeatherPrefs", Context.MODE_PRIVATE)
        val allKeys = sharedPreferences?.all?.keys ?: emptySet()

        val forecastKeys = allKeys.filter { it.startsWith("forecast-") }
        val sortedForecastKeys = forecastKeys.sortedBy { it.substringAfter("forecast-").substringBefore("-") }

        val filteredForecastItems = mutableListOf<ForecastItem>()

        sortedForecastKeys.forEach { key ->
            val forecastDataString = sharedPreferences?.getString(key, "") ?: ""
            if (forecastDataString.isNotEmpty()) {
                try {
                    val gson = Gson()
                    val forecastData = gson.fromJson(forecastDataString, JsonObject::class.java)

                    val temperature = forecastData.get("temperature")?.asDouble ?: 0.0
                    val description = forecastData.get("description")?.asString ?: ""
                    val dateText = forecastData.get("date")?.asString ?: ""

                    Log.e("ForecastShared", "Odczytano: $temperature, $description, $dateText")

                    val forecastItem = ForecastItem(dateText, temperature, description)
                    filteredForecastItems.add(forecastItem)
                } catch (e: JsonSyntaxException) {
                    Log.e("ForecastFragment", "Error parsing forecast data: ${e.message}")
                }
            }
        }

        forecastAdapter.setData(filteredForecastItems)
        Log.e("ForecastShared", "SET DATA")
    }

}