package com.example.weatherappkotlin
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.weatherappkotlin.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var networkConnection: NetworkConnection
    private lateinit var networkConnectionObserver: Observer<Boolean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        networkConnection = NetworkConnection(this)
        networkConnectionObserver = Observer { isConnected ->
            if (isConnected) {
                updateInternetStatus("Connected")
            } else {
                updateInternetStatus("No Connection")
            }
        }

        networkConnection.observe(this, networkConnectionObserver)

        val sharedPreferences = getSharedPreferences("FavoriteCitiesPrefs", Context.MODE_PRIVATE)
        val favoriteCitiesSet = sharedPreferences.getStringSet("favoriteCities", HashSet()) ?: HashSet()
        Log.d("MainActivity", "Ulubione miasta przy uruchomieniu: $favoriteCitiesSet")

        val sharedPreferencesToggle = getSharedPreferences("TemperatureUnitPrefs", Context.MODE_PRIVATE)
        val isFahrenheitSelected = sharedPreferencesToggle.getBoolean("temperatureUnit", false)

        // Ustawienie stanu przycisku toggle zgodnie z zapisanymi preferencjami
        binding.toggleTemperatureUnit.isChecked = isFahrenheitSelected

        // Obsługa zmiany stanu przycisku toggle
        binding.toggleTemperatureUnit.setOnCheckedChangeListener { buttonView, isChecked ->
            // Zapisanie wyboru jednostki temperatury do SharedPreferences
            sharedPreferencesToggle.edit().putBoolean("temperatureUnit", isChecked).apply()
        }

        binding.btnFind.setOnClickListener {
            val cityName = binding.inpCityName.text.toString()
            if (cityName.isNotEmpty()) {
                fetchWeatherData(cityName)
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnFavourites.setOnClickListener {
            val intent = Intent(this@MainActivity, FavouritesCitiesActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        networkConnection.observe(this, networkConnectionObserver)
    }

    private fun updateInternetStatus(status: String) {
        binding.textInternetStatus.text = "Internet: $status"
    }

    private fun fetchWeatherData(cityName: String) {
        val apiKey = "54115490ba2f3c3c704b01a9e52dad7a"
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$apiKey"

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
                    if (jsonObject.has("name")) {
                        val cityName = jsonObject.get("name").asString
                        val sharedPreferences = getSharedPreferences("CityWeatherPrefs", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("cityName", cityName)
                        editor.apply()

                        val cityNameShared = sharedPreferences.getString("cityName", "Nie znaleziono")

                        Log.d("MainActivityCheck", "Odczytano nazwę miasta: $cityNameShared")

                        val intent = Intent(this@MainActivity, CityWeatherActivity::class.java)
                        val bundle = Bundle()
                        bundle.putString("cityName", cityName)
                        intent.putExtras(bundle)
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

    override fun onStop() {
        super.onStop()
        networkConnection.removeObserver(networkConnectionObserver)
    }
}

