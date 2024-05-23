package com.example.weatherappkotlin


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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
        private val seeMoreButton: Button = itemView.findViewById(R.id.btnSeeMore)
        fun bind(cityName: String) {
            cityNameTextView.text = cityName
            deleteButton.setOnClickListener {
                val editor = sharedPreferences.edit()
                val citySet = sharedPreferences.getStringSet("favoriteCities", HashSet())?.toMutableSet()
                citySet?.remove(cityName)
                editor.putStringSet("favoriteCities", citySet)
                editor.apply()

                citiesList.remove(cityName)
                notifyDataSetChanged()
            }

            seeMoreButton.setOnClickListener {
                val sharedPreferences = itemView.context.getSharedPreferences("CityWeatherPrefs", Context.MODE_PRIVATE)

                val editor = sharedPreferences.edit()
                editor.putString("cityName", cityName)
                editor.apply()

                val intent = Intent(itemView.context, CityWeatherActivity::class.java)
                intent.putExtra("cityName", cityName)
                itemView.context.startActivity(intent)
            }

        }


    }
}
