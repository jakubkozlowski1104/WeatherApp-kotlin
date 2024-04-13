package com.example.weatherappkotlin



import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavouritesCitiesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavoriteCitiesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourite_cities)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Pobierz ulubione miasta z SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("FavoriteCitiesPrefs", Context.MODE_PRIVATE)
        val favoriteCitiesSet: Set<String>? = sharedPreferences.getStringSet("favoriteCities", HashSet())

        // Konwertuj Set<String> na List<String> dla adaptera RecyclerView
        val favoriteCitiesList: List<String> = favoriteCitiesSet?.toList() ?: listOf()

        // Utwórz i ustaw adapter dla RecyclerView
        adapter = FavoriteCitiesAdapter(this, favoriteCitiesList)
        recyclerView.adapter = adapter
    }
}
