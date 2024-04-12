package com.example.weatherappkotlin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WeatherViewModel : ViewModel() {
    private val _cityName = MutableLiveData<String>().apply { value = "" }
    val cityName: LiveData<String> = _cityName

    private val _temperature = MutableLiveData<Double>().apply { value = 0.0 }
    val temperature: LiveData<Double> = _temperature

    fun updateWeatherData(cityName: String, temperature: Double) {
        Log.d("checkLog", "Updating weather data: $cityName, $temperature K")
        _cityName.postValue(cityName)
        _temperature.postValue(temperature)
    }
}

