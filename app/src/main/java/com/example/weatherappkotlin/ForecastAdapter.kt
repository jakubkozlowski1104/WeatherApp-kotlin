package com.example.weatherappkotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ForecastAdapter(private var forecastList: List<ForecastItem>) :
    RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var dateTextView: TextView
        lateinit var temperatureTextView: TextView
        lateinit var weatherIconImageView: ImageView

        init {
            dateTextView = itemView.findViewById(R.id.dateTextView)
            temperatureTextView = itemView.findViewById(R.id.temperatureTextView)
            weatherIconImageView = itemView.findViewById(R.id.weatherIconImageViewForecast)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(view)
    }


    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val forecastItem = forecastList[position]

        holder.dateTextView.text = forecastItem.date
        holder.temperatureTextView.text = "${forecastItem.temperature.toInt()}°C"

        val iconResource = weatherIconsMap[forecastItem.description.toLowerCase()] ?: R.drawable.sunny
        holder.weatherIconImageView.setImageResource(iconResource)
    }

    override fun getItemCount(): Int {
        return forecastList.size
    }

    // Metoda do aktualizacji danych w adapterze
    fun setData(newForecastList: List<ForecastItem>) {
        forecastList = newForecastList
        notifyDataSetChanged() // Powiadom RecyclerView o zmianach w danych
    }

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
