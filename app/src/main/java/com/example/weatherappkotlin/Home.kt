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
                // Pobierz dane z API, gdy jest dostęp do Internetu
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
                // Odczytaj dane z SharedPreferences, gdy brak dostępu do Internetu
                val sharedPreferences = activity?.getSharedPreferences(cityName, Context.MODE_PRIVATE)
                val temperature = sharedPreferences?.getString("temperature", "")?.toDoubleOrNull() ?: 0.0
                val pressure = sharedPreferences?.getString("pressure", "")?.toDoubleOrNull() ?: 0.0
                val description = sharedPreferences?.getString("description", "") ?: ""
                val lon = sharedPreferences?.getString("lon", "")?.toDoubleOrNull() ?: 0.0
                val lat = sharedPreferences?.getString("lat", "")?.toDoubleOrNull() ?: 0.0
                val localDateTime = sharedPreferences?.getString("localDateTime", "") ?: ""
                Log.d("HomeFragmentNet", "Shared")
                // Wyświetl dane z SharedPreferences
                displayWeatherData(cityName, temperature, pressure, description, lon, lat, localDateTime)
            }
        })

        if (isCityInFavorites(cityName)) {
            binding.btnAddToFavourite.text = "Twoje Ulubione"
        } else {
            binding.btnAddToFavourite.text = "Dodaj do ulubionych"
        }

        // Dodaj obsługę kliknięcia przycisku
        binding.btnAddToFavourite.setOnClickListener {
            val cityName = binding.cityNameTextView.text.toString()
            if (isCityInFavorites(cityName)) {
                Toast.makeText(context, "Miasto $cityName juz jest w ulubionych! ", Toast.LENGTH_SHORT).show()

                binding.btnAddToFavourite.text = "Twoje Ulubione"
            } else {
                addCityToFavorites(cityName)
                binding.btnAddToFavourite.text = "Twoje Ulubione"
                Toast.makeText(context, "Dodano do ulubionych: $cityName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isCityInFavorites(cityName: String): Boolean {
        val sharedPreferences = activity?.getSharedPreferences("FavoriteCitiesPrefs", Context.MODE_PRIVATE)
        val favoriteCities = sharedPreferences?.getStringSet("favoriteCities", HashSet()) ?: emptySet()
        return favoriteCities.contains(cityName)
    }

    private fun fetchWeatherData(cityName: String) {
        Log.d("HomeFragmentNet", "Fetch")
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

                    val temperature = jsonObject.getAsJsonObject("main").get("temp").asDouble
                    val temperatureCelsius = temperature - 273.15
                    val pressure = jsonObject.getAsJsonObject("main").get("pressure").asDouble
                    val description = jsonObject.getAsJsonArray("weather").get(0).asJsonObject.get("description").asString
                    val lon = jsonObject.get("coord").asJsonObject.get("lon").asDouble
                    val lat = jsonObject.get("coord").asJsonObject.get("lat").asDouble


                    val timezoneOffset = jsonObject.get("timezone").asLong // Pobierz przesunięcie czasowe (timezone)
                    val currentTimeMillis = System.currentTimeMillis() // Pobierz aktualny czas w milisekundach
                    val localTimeMillis = currentTimeMillis + (timezoneOffset * 1000) // Przelicz na czas lokalny (z uwzględnieniem przesunięcia czasowego)
                    // Ustawienie strefy czasowej na obiekcie Calendar
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = localTimeMillis
                    // Formatowanie daty i czasu do czytelnej postaci
                    val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()) // Format daty i godziny
                    sdf.timeZone = TimeZone.getDefault() // Ustawienie strefy czasowej na lokalną

                    val localDateTime = sdf.format(calendar.time) // Sformatuj datę i czas do postaci tekstowej

                    // Zapisz dane pogodowe do SharedPreferences dla danego miasta
                    saveWeatherDataToSharedPreferences(cityName, temperatureCelsius, pressure, description, lon, lat, localDateTime)

                    requireActivity().runOnUiThread {
                        displayWeatherData(cityName, temperatureCelsius, pressure, description, lon, lat, localDateTime)
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
        pressure: Double,
        description: String,
        lon: Double,
        lat: Double,
        localDateTime: String,
    ) {
        // Wyświetl dane pogodowe w interfejsie użytkownika
        binding.cityNameTextView.text = cityName
        val roundedTemperature = temperature.toInt()
        binding.temperatureTextView.text = "$roundedTemperature°C"
        binding.coordinatesTextView.text = "$lat, $lon"
        binding.descriptionTextView.text = "$description"

        // Wybierz odpowiedni obraz na podstawie opisu
        val iconResource = weatherIconsMap[description] ?: R.drawable.sunny // Użyj domyślnego obrazu, jeśli nie ma mapowania
        binding.imageView.setImageResource(iconResource)

        // Wyświetl aktualny czas w interfejsie użytkownika
        binding.timeTextView.text = "$localDateTime"
    }

    private fun saveWeatherDataToSharedPreferences(
        cityName: String,
        temperature: Double,
        pressure: Double,
        description: String,
        lon: Double,
        lat: Double,
        localDateTime: String,
    ) {
        val sharedPreferences = activity?.getSharedPreferences(cityName, Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()

        editor?.apply {
            putString("temperature", temperature.toString())
            putString("pressure", pressure.toString())
            putString("description", description)
            putString("lon", lon.toString())
            putString("lat", lat.toString())
            putString("localDateTime", localDateTime)
            apply()
        }
    }

    private fun addCityToFavorites(cityName: String) {
        val sharedPreferences = activity?.getSharedPreferences("FavoriteCitiesPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()

        val favoriteCities = sharedPreferences?.getStringSet("favoriteCities", HashSet())?.toMutableSet() ?: mutableSetOf()
        favoriteCities.add(cityName)

        editor?.putStringSet("favoriteCities", favoriteCities)?.apply()
        Log.d("FavoriteCitiesXD", "Ulubione miasta: $favoriteCities")
    }

    val weatherIconsMap = mapOf(
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
