package com.example.weatherappkotlin

import Home
import NextWeek
import android.content.Context
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
                R.id.nextDays -> {
                    val nextWeekFragment = NextWeek()
                    replaceFragment(nextWeekFragment)
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

