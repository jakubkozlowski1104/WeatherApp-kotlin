package com.example.weatherappkotlin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.weatherappkotlin.WeatherViewModel
import com.example.weatherappkotlin.databinding.FragmentHomeBinding

class Home : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        val cityName = arguments?.getString("cityName")
        val temperature = arguments?.getDouble("temperature")
        val coordinates = arguments?.getString("coordinates")
        val time = arguments?.getString("time")
        val pressure = arguments?.getDouble("pressure")
        val description = arguments?.getString("description")
        val windSpeed = arguments?.getDouble("windSpeed")
        val windDirection = arguments?.getDouble("windDirection")

        arguments?.let {
            // ...
            Log.d("HomeFragment", "Temperatura: ${it.getDouble("temperature")}")
            Log.d("HomeFragment", "Ciśnienie: ${it.getDouble("pressure")}")
            // Dodaj logi dla innych danych, jeśli potrzebujesz
        }

        cityName?.let {
            binding.cityNameTextView.text = it
        }
        temperature?.let {
            binding.temperatureTextView.text = "$it K"
        }
        coordinates?.let {
            binding.coordinatesTextView.text = "Współrzędne: $it"
        }
        time?.let {
            binding.timeTextView.text = "Czas: $it"
        }
        pressure?.let {
            binding.pressureTextView.text = "Ciśnienie: $it hPa"
        }
        description?.let {
            binding.descriptionTextView.text = "Opis: $it"
        }
        windSpeed?.let {
            binding.windSpeedTextView.text = "Prędkość wiatru: $it m/s"
        }
        windDirection?.let {
            binding.windDirectionTextView.text = "Kierunek wiatru: $it°"
        }

        return binding.root
    }
}
