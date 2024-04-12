package com.example.weatherappkotlin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment


class Wind : Fragment() {
    private var windSpeed: Double? = null
    private var windDirection: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            windSpeed = it.getDouble("windSpeed")
            windDirection = it.getDouble("windDirection")
                // ...
            Log.d("WindFragment", "Prędkość wiatru: ${it.getDouble("windSpeed")}")
            Log.d("WindFragment", "Kierunek wiatru: ${it.getDouble("windDirection")}")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wind, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        windSpeed?.let {
            view.findViewById<TextView>(R.id.windSpeedTextView).text = "Prędkość wiatru: $it m/s"
        }
        windDirection?.let {
            view.findViewById<TextView>(R.id.windDirectionTextView).text = "Kierunek wiatru: $it°"
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(windSpeed: Double, windDirection: Double) =
            Wind().apply {
                arguments = Bundle().apply {
                    putDouble("windSpeed", windSpeed)
                    putDouble("windDirection", windDirection)
                }
            }
    }
}