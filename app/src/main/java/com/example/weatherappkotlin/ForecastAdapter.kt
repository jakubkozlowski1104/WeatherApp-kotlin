package com.example.weatherappkotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ForecastAdapter(private var forecastList: List<ForecastItem>) :
    RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val temperatureTextView: TextView = itemView.findViewById(R.id.temperatureTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(view)
    }


    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val forecastItem = forecastList[position]

        holder.dateTextView.text = forecastItem.date
        holder.temperatureTextView.text = "Temperatura: ${forecastItem.temperature}°C"
        holder.descriptionTextView.text = forecastItem.description
    }

    override fun getItemCount(): Int {
        return forecastList.size
    }

    // Metoda do aktualizacji danych w adapterze
    fun setData(newForecastList: List<ForecastItem>) {
        forecastList = newForecastList
        notifyDataSetChanged() // Powiadom RecyclerView o zmianach w danych
    }
}
