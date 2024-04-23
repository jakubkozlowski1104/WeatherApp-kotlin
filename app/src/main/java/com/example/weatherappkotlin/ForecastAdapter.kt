package com.example.weatherappkotlin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class ForecastAdapter(private var forecastList: List<ForecastItem>) :
    RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val temperatureTextView: TextView = itemView.findViewById(R.id.temperatureTextView)
        val weatherIconImageView: ImageView = itemView.findViewById(R.id.weatherIconImageViewForecast)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val forecastItem = forecastList[position]

        holder.dateTextView.text = forecastItem.date

        // Pobierz preferencje jednostki temperatury
        val sharedPreferences = holder.itemView.context.getSharedPreferences("TemperatureUnitPrefs", Context.MODE_PRIVATE)
        val isFahrenheit = sharedPreferences?.getBoolean("temperatureUnit", false) ?: false

        // Formatuj temperaturę zgodnie z preferencją
        val formattedTemperature = if (isFahrenheit) {
            "${(forecastItem.temperature.toInt())}K"
        } else {
            "${forecastItem.temperature.toInt()}°C"
        }
        holder.temperatureTextView.text = formattedTemperature

        // Ustaw ikonę pogody na podstawie opisu
        val iconResource = getWeatherIconResource(forecastItem.description)
        holder.weatherIconImageView.setImageResource(iconResource)
    }

    override fun getItemCount(): Int {
        return forecastList.size
    }

    // Metoda do ustawiania nowych danych w adapterze
    fun setData(newForecastList: List<ForecastItem>) {
        forecastList = newForecastList
        notifyDataSetChanged() // Powiadom RecyclerView o zmianach w danych
    }

    // Metoda do mapowania opisu pogody na odpowiednią ikonę
    private fun getWeatherIconResource(description: String): Int {
        val lowercaseDescription = description.toLowerCase(Locale.getDefault())
        return weatherIconsMap[lowercaseDescription] ?: R.drawable.sunny
    }

    companion object {
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
}
