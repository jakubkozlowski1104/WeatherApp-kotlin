package com.example.weatherappkotlin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


class NextWeek : Fragment() {
    private var temperatureForecast: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            temperatureForecast = it.getDouble("temperatureForecast")
            Log.d("NextWeekFragment", "Temperatura na jutro: ${it.getDouble("temperatureForecast")}")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_next_week, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        temperatureForecast?.let {
            view.findViewById<TextView>(R.id.temperatureForecastTextView).text = "temperatura na jutro: $it C"
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(temperatureForecast: Double) =
            NextWeek().apply {
                arguments = Bundle().apply {
                    putDouble("temperatureForecast", temperatureForecast)
                }
            }
    }
}
