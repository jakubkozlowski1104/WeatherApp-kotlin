package com.example.weatherappkotlin

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.weatherappkotlin.databinding.ActivityCityWeatherBinding

class CityWeatherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCityWeatherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windSpeed = intent.extras?.getDouble("windSpeed")
        val windDirection = intent.extras?.getDouble("windDirection")
        val temperatureForecast = intent.extras?.getDouble("temperatureForecast")
        val presure = intent.extras?.getDouble("pressure")

        Log.d("CityWeatherActivityCheck", "Prędkość wiatru: $windSpeed")
        Log.d("CityWeatherActivityCheck", "Kierunek wiatru: $windDirection")
        Log.d("CityWeatherActivityCheck", "Temperatura na jutro: $temperatureForecast")
        Log.d("CityWeatherActivityCheck", "pressure: $presure")

        // Tworzenie instancji Wind fragmentu z przekazaniem danych o wietrze jako argumentów
        val windFragment = Wind.newInstance(windSpeed ?: 0.0, windDirection ?: 0.0)
        replaceFragment(windFragment)

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home -> {
                    val homeFragment = createHomeFragment()
                    replaceFragment(homeFragment)
                }
                R.id.wind -> {
                    val windFragment = createWindFragment()
                    replaceFragment(windFragment)
                }
                R.id.nextDays -> {
                    val nextWeekFragment = createNextWeekFragment()
                    replaceFragment(nextWeekFragment)
                }
                else -> {}
            }
            true
        }
    }

    private fun createHomeFragment(): Home {
        return Home().apply {
            arguments = Bundle().apply {
                putString("cityName", intent.extras?.getString("cityName"))
                putDouble("temperature", intent.extras?.getDouble("temperature") ?: 0.0)
                putDouble("pressure", intent.extras?.getDouble("pressure") ?: 0.0)
                putString("description", intent.extras?.getString("description"))
                putString("coordinates", intent.extras?.getString("coordinates"))
                putDouble("windSpeed", intent.extras?.getDouble("windSpeed") ?: 0.0)
                putDouble("windDirection", intent.extras?.getDouble("windDirection") ?: 0.0)
            }
        }
    }

    private fun createWindFragment(): Wind {
        return Wind().apply {
            arguments = Bundle().apply {
                putDouble("windSpeed", intent.extras?.getDouble("windSpeed") ?: 0.0)
                putDouble("windDirection", intent.extras?.getDouble("windDirection") ?: 0.0)
            }
        }
    }

    private fun createNextWeekFragment(): NextWeek {
        return NextWeek().apply {
            arguments = Bundle().apply {
                putDouble("temperatureForecast", intent.extras?.getDouble("temperatureForecast") ?: 0.0)
            }
        }
    }


    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}
