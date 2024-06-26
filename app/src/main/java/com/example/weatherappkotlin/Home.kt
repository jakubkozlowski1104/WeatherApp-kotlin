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
import com.example.weatherappkotlin.databinding.FragmentHomeBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class Home : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var networkConnection: NetworkConnection

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = activity?.getSharedPreferences("CityWeatherPrefs", Context.MODE_PRIVATE)
        val cityName = sharedPreferences?.getString("cityName", "") ?: ""

        networkConnection = NetworkConnection(requireContext())
        networkConnection.observe(viewLifecycleOwner, Observer { isConnected ->
            if (isConnected) {
                Log.d("HomeFragmentNet", "Internet connected")
                binding.networkStatusTextView.visibility = View.GONE
                binding.btnRefreshData.visibility = View.VISIBLE
                if (cityName.isNotEmpty()) {
                    fetchWeatherData(cityName)
                }
                binding.btnRefreshData.setOnClickListener {
                    if (cityName.isNotEmpty()) {
                        fetchWeatherData(cityName)
                        Toast.makeText(context, "Dane zostały odświeżone ", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.d("HomeFragmentNet", "No internet connection")
                binding.networkStatusTextView.visibility = View.VISIBLE
                binding.btnRefreshData.visibility = View.GONE

                // Read weather data from SharedPreferences when no internet connection
                readWeatherDataFromSharedPreferences(cityName)
            }
        })

        if (isCityInFavorites(cityName)) {
            binding.btnAddToFavourite.text = "Twoje Ulubione"
        } else {
            binding.btnAddToFavourite.text = "Dodaj do ulubionych"
        }

        binding.btnAddToFavourite.setOnClickListener {
            val cityName = binding.cityNameTextView.text.toString()
            if (isCityInFavorites(cityName)) {
                Toast.makeText(context, "Miasto $cityName już jest w ulubionych!", Toast.LENGTH_SHORT).show()
            } else {
                addCityToFavorites(cityName)
                Toast.makeText(context, "Dodano do ulubionych: $cityName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isCityInFavorites(cityName: String): Boolean {
        val sharedPreferences = activity?.getSharedPreferences("FavoriteCitiesPrefs", Context.MODE_PRIVATE)
        val favoriteCities = sharedPreferences?.getStringSet("favoriteCities", emptySet()) ?: emptySet()
        return favoriteCities.contains(cityName)
    }

    private fun fetchWeatherData(cityName: String) {
        val apiKey = "54115490ba2f3c3c704b01a9e52dad7a"
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$apiKey&lang=pl"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Log.d("HomeFragment", "Błąd pobierania danych: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val gson = Gson()
                    val jsonObject = gson.fromJson(response.body?.string(), JsonObject::class.java)

                    val mainObject = jsonObject.getAsJsonObject("main")
                    val temperature = mainObject.get("temp").asDouble
                    val tempMin = mainObject.get("temp_min").asDouble
                    val tempMax = mainObject.get("temp_max").asDouble

                    val weatherArray = jsonObject.getAsJsonArray("weather")
                    val weatherObject = weatherArray.get(0).asJsonObject
                    val description = weatherObject.get("description").asString

                    val lon = jsonObject.getAsJsonObject("coord").get("lon").asDouble
                    val lat = jsonObject.getAsJsonObject("coord").get("lat").asDouble

                    val timezoneOffset = jsonObject.get("timezone").asLong
                    val currentTimeMillis = System.currentTimeMillis()
                    val localTimeMillis = currentTimeMillis + (timezoneOffset * 1000)

                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = localTimeMillis
                    val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                    sdf.timeZone = TimeZone.getDefault()
                    val localDateTime = sdf.format(calendar.time)

                    saveWeatherDataToSharedPreferences(
                        cityName,
                        temperature,
                        tempMin,
                        tempMax,
                        description,
                        lon,
                        lat,
                        localDateTime
                    )

                    requireActivity().runOnUiThread {
                        displayWeatherData(cityName, temperature, tempMin, tempMax, description, lon, lat, localDateTime)
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Log.d("HomeFragment", "Błąd pobierania danych: ${response.message}")
                    }
                }
            }
        })
    }

    private fun displayWeatherData(
        cityName: String,
        temperature: Double,
        tempMin: Double,
        tempMax: Double,
        description: String,
        lon: Double,
        lat: Double,
        localDateTime: String,
    ) {
        binding.cityNameTextView.text = cityName
        binding.temperatureTextView.text = "${temperature.toInt() - 273}°C"
        binding.minTempTextView.text = "Min: ${tempMin.toInt() - 273}°C"
        binding.maxTempTextView.text = "Max: ${tempMax.toInt()- 273}°C"
        binding.coordinatesTextView.text = "$lat, $lon"
        binding.descriptionTextView.text = description

        val iconResource = weatherIconsMap[description] ?: R.drawable.sunny
        binding.imageView.setImageResource(iconResource)

        binding.timeTextView.text = localDateTime
    }

    private fun saveWeatherDataToSharedPreferences(
        cityName: String,
        temperature: Double,
        tempMin: Double,
        tempMax: Double,
        description: String,
        lon: Double,
        lat: Double,
        localDateTime: String,
    ) {
        val sharedPreferences = activity?.getSharedPreferences(cityName, Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()

        editor?.apply {
            putString("temperature", temperature.toString())
            putString("temp_min", tempMin.toString())
            putString("temp_max", tempMax.toString())
            putString("description", description)
            putString("lon", lon.toString())
            putString("lat", lat.toString())
            putString("localDateTime", localDateTime)
            apply()
        }
    }

    private fun readWeatherDataFromSharedPreferences(cityName: String) {
        val sharedPreferences = activity?.getSharedPreferences(cityName, Context.MODE_PRIVATE)
        val temperature = sharedPreferences?.getString("temperature", "0")?.toDoubleOrNull() ?: 0.0
        val tempMin = sharedPreferences?.getString("temp_min", "0")?.toDoubleOrNull() ?: 0.0
        val tempMax = sharedPreferences?.getString("temp_max", "0")?.toDoubleOrNull() ?: 0.0
        val description = sharedPreferences?.getString("description", "") ?: ""
        val lon = sharedPreferences?.getString("lon", "0")?.toDoubleOrNull() ?: 0.0
        val lat = sharedPreferences?.getString("lat", "0")?.toDoubleOrNull() ?: 0.0
        val localDateTime = sharedPreferences?.getString("localDateTime", "") ?: ""

        displayWeatherData(cityName, temperature, tempMin, tempMax, description, lon, lat, localDateTime)
    }

    private fun addCityToFavorites(cityName: String) {
        val sharedPreferences = activity?.getSharedPreferences("FavoriteCitiesPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()

        val favoriteCities = sharedPreferences?.getStringSet("favoriteCities", emptySet())?.toMutableSet() ?: mutableSetOf()
        favoriteCities.add(cityName)

        editor?.putStringSet("favoriteCities", favoriteCities)?.apply()
        Log.d("FavoriteCitiesXD", "Ulubione miasta: $favoriteCities")
    }

    private val weatherIconsMap = mapOf(
        "bezchmurnie" to R.drawable.clear_sun,
        "pochmurnie" to R.drawable.cloudy_sunny,
        "zachmurzenie" to R.drawable.cloudy,
        "zachmurzenie duże" to R.drawable.cloudy,
        "słabe opady deszczu" to R.drawable.rainy,
        "ulewy" to R.drawable.rainy,
        "słabe przelotne opady deszczu" to R.drawable.rainy,
        "burze z piorunami" to R.drawable.storm,
        "słabe opady śniegu" to R.drawable.snowy,
        "opady śniegu" to R.drawable.snowy
    )
}
