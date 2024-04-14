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

        networkConnection = NetworkConnection(requireContext())
        networkConnection.observe(viewLifecycleOwner, Observer { isConnected ->
            if (isConnected) {
                Log.d("HomeFragmentNet", "Internet connected")
                binding.networkStatusTextView.visibility = View.GONE
            } else {
                Log.d("HomeFragmentNet", "No internet connection")
                binding.networkStatusTextView.visibility = View.VISIBLE
            }
        })

        val sharedPreferences = activity?.getSharedPreferences("CityWeatherPrefs", Context.MODE_PRIVATE)
        val cityName = sharedPreferences?.getString("cityName", "") ?: ""
        Log.d("HomeFragmentCheck", "Odczytano nazwę miasta: $cityName")
        loadWeatherDataFromSharedPreferences(cityName);

        if (cityName.isNotEmpty()) {
            fetchWeatherData(cityName)
        }

        // Poprawna metoda do odnalezienia przycisku za pomocą View Binding
        binding.btnAddToFavourite.setOnClickListener {
            // Pobranie nazwy miasta z SharedPreferences lub z innego źródła
            val cityName = binding.cityNameTextView.text.toString()
            addCityToFavorites(cityName)
            Toast.makeText(context, "Dodano do ulubionych: $cityName", Toast.LENGTH_SHORT).show()
        }

        // Pobierz listę ulubionych miast
        val favoriteCitiesSet = sharedPreferences?.getStringSet("favoriteCities", HashSet()) ?: HashSet()
        Log.d("HomeFragmentCheck", "Ulubione miasta: $favoriteCitiesSet")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // Zwolnij zasoby, np. odłącz obserwację od networkConnection
        networkConnection.removeObservers(viewLifecycleOwner)
    }


    private fun addCityToFavorites(cityName: String) {
        val sharedPreferences = activity?.getSharedPreferences("FavoriteCitiesPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()

        // Pobranie aktualnej listy ulubionych miast
        val favoriteCities = sharedPreferences?.getStringSet("favoriteCities", HashSet())?.toMutableSet() ?: mutableSetOf()

        // Dodanie nowego miasta do listy
        favoriteCities.add(cityName)

        // Zapisanie zaktualizowanej listy do SharedPreferences
        editor?.putStringSet("favoriteCities", favoriteCities)?.apply()
        Log.d("FavoriteCitiesXD", "Ulubione miasta: $favoriteCities")
    }


    private fun loadWeatherDataFromSharedPreferences(cityName: String) {
        val sharedPreferences = activity?.getSharedPreferences(cityName, Context.MODE_PRIVATE)

        val temperature = sharedPreferences?.getString("temperature", "")?.toDoubleOrNull() ?: 0.0
        val pressure = sharedPreferences?.getString("pressure", "")?.toDoubleOrNull() ?: 0.0
        val description = sharedPreferences?.getString("description", "") ?: ""
        val lon = sharedPreferences?.getString("lon", "")?.toDoubleOrNull() ?: 0.0
        val lat = sharedPreferences?.getString("lat", "")?.toDoubleOrNull() ?: 0.0
        val windSpeed = sharedPreferences?.getString("windSpeed", "")?.toDoubleOrNull() ?: 0.0
        val windDirection = sharedPreferences?.getString("windDirection", "")?.toDoubleOrNull() ?: 0.0

        // Wyświetl lub wykorzystaj wczytane dane pogodowe
        // Możesz je np. przekazać do odpowiednich elementów interfejsu użytkownika

        // Przykład użycia danych:
        Log.d("checkShared", "Dla miasta $cityName - temperatura: $temperature °C, opis: $description $windSpeed, $windDirection")
    }

    private fun saveWeatherDataToSharedPreferences(
        cityName: String,
        temperature: Double,
        pressure: Double,
        description: String,
        lon: Double,
        lat: Double,
        windSpeed: Double,
        windDirection: Double
    ) {
        val sharedPreferences = activity?.getSharedPreferences(cityName, Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()

        editor?.apply {
            putString("temperature", temperature.toString())
            putString("pressure", pressure.toString())
            putString("description", description)
            putString("lon", lon.toString())
            putString("lat", lat.toString())
            putString("windSpeed", windSpeed.toString())
            putString("windDirection", windDirection.toString())
            apply()
        }


    }


    private fun fetchWeatherData(cityName: String) {
        val apiKey = "54115490ba2f3c3c704b01a9e52dad7a"
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$apiKey"

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

                    val temperature = jsonObject.getAsJsonObject("main").get("temp").asDouble
                    val temperatureCelsius = temperature - 273.15
                    val pressure = jsonObject.getAsJsonObject("main").get("pressure").asDouble
                    val description = jsonObject.getAsJsonArray("weather").get(0).asJsonObject.get("description").asString
                    val lon = jsonObject.get("coord").asJsonObject.get("lon").asDouble
                    val lat = jsonObject.get("coord").asJsonObject.get("lat").asDouble
                    val windSpeed = jsonObject.getAsJsonObject("wind").get("speed").asDouble
                    val windDirection = jsonObject.getAsJsonObject("wind").get("deg").asDouble

                    // Zapisz dane pogodowe do SharedPreferences dla danego miasta
                    saveWeatherDataToSharedPreferences(cityName, temperatureCelsius, pressure, description, lon, lat, windSpeed, windDirection)

                    requireActivity().runOnUiThread {
                        binding.cityNameTextView.text = cityName
                        binding.temperatureTextView.text = "$temperatureCelsius °C"
                        binding.coordinatesTextView.text = "Współrzędne: $lat, $lon"
                        binding.pressureTextView.text = "Ciśnienie: $pressure hPa"
                        binding.descriptionTextView.text = "Opis: $description"
                        binding.windSpeedTextView.text = "Prędkość wiatru: $windSpeed m/s"
                        binding.windDirectionTextView.text = "Kierunek wiatru: $windDirection°"
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Log.d("HomeFragment", "Błąd pobierania danych: ${response.message}")
                    }
                }
            }
        })
    }
}
