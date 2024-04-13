package com.example.weatherappkotlin


import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoriteCitiesAdapter(
    private val context: Context,
    private val citiesList: MutableList<String>,
    private val sharedPreferences: SharedPreferences
) : RecyclerView.Adapter<FavoriteCitiesAdapter.CityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_city, parent, false)
        return CityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val city = citiesList[position]
        holder.bind(city)
    }

    override fun getItemCount(): Int {
        return citiesList.size
    }

    inner class CityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cityNameTextView: TextView = itemView.findViewById(R.id.cityNameTextView)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(cityName: String) {
            cityNameTextView.text = cityName
            deleteButton.setOnClickListener {
                // Usuwamy miasto z listy ulubionych w SharedPreferences
                val editor = sharedPreferences.edit()
                val citySet = sharedPreferences.getStringSet("favoriteCities", HashSet())?.toMutableSet()
                citySet?.remove(cityName)
                editor.putStringSet("favoriteCities", citySet)
                editor.apply()

                // Usuwamy miasto z listy wyświetlanej w RecyclerView
                citiesList.remove(cityName)
                notifyDataSetChanged()
            }
        }
    }
}
