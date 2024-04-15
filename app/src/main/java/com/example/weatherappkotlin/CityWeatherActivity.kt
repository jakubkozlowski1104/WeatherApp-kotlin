package com.example.weatherappkotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.weatherappkotlin.databinding.ActivityCityWeatherBinding

class CityWeatherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCityWeatherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val defaultFragment = Home()
        replaceFragment(defaultFragment)

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home -> {
                    val homeFragment = Home()
                    replaceFragment(homeFragment)
                }
                R.id.wind -> {
                    val windFragment = Wind()
                    replaceFragment(windFragment)
                }
                R.id.forecast-> {
                    val forecastFragment = Forecast()
                    replaceFragment(forecastFragment)
                }
                else -> {}
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}

