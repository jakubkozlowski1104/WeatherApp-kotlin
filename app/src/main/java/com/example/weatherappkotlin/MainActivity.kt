package com.example.weatherappkotlin

import android.content.Context
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

    val networkConnection = NetworkConnection(applicationContext)
        networkConnection.observe(this){
            if(it) {
                Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show()
            } else  {
                Toast.makeText(this, "not connected", Toast.LENGTH_SHORT).show()

            }
        }

        val sharedPreferences = getSharedPreferences("FavoriteCitiesPrefs", Context.MODE_PRIVATE)
        val favoriteCitiesSet = sharedPreferences.getStringSet("favoriteCities", HashSet()) ?: HashSet()
        Log.d("MainActivity", "Ulubione miasta przy uruchomieniu: $favoriteCitiesSet")

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
            startActivity(intent) // Uruchomienie FavouritesCitiesActivity
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

                        // Wyświetlenie nazwy miasta w logach
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
}

