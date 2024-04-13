package com.example.weatherappkotlin


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavouritesCitiesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourite_cities)

        recyclerView = findViewById(R.id.recyclerView)
        val sharedPreferences = getSharedPreferences("FavoriteCitiesPrefs", Context.MODE_PRIVATE)
        val favoriteCitiesSet: Set<String>? = sharedPreferences.getStringSet("favoriteCities", HashSet())

        // Konwertujemy Set<String> na List<String> dla adaptera RecyclerView
        val favoriteCitiesList: List<String> = favoriteCitiesSet?.toList() ?: listOf()

        // Utworzenie i ustawienie adaptera dla RecyclerView
        val adapter = FavoriteCitiesAdapter(this, favoriteCitiesList, sharedPreferences)
        recyclerView.adapter = adapter

        recyclerView.layoutManager = LinearLayoutManager(this)
    }
}
