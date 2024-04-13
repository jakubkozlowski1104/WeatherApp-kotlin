import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.weatherappkotlin.databinding.FragmentNextWeekBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class NextWeek : Fragment() {
    private lateinit var binding: FragmentNextWeekBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNextWeekBinding.inflate(inflater, container, false)

        val sharedPreferences = activity?.getSharedPreferences("CityWeatherPrefs", Context.MODE_PRIVATE)
        val cityName = sharedPreferences?.getString("cityName", "") ?: ""
        Log.d("NextWeekFragmentCheck", "Odczytano nazwę miasta: $cityName")

        if (cityName.isNotEmpty()) {
            fetchWeatherData(cityName)
        }

        return binding.root
    }

    private fun fetchWeatherData(cityName: String) {
        val apiKey = "54115490ba2f3c3c704b01a9e52dad7a"
        val url = "https://api.openweathermap.org/data/2.5/forecast?q=$cityName&appid=$apiKey"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Log.d("NextWeekFragment", "Błąd pobierania danych: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val gson = Gson()
                    val jsonObject = gson.fromJson(response.body?.string(), JsonObject::class.java)

                    val forecastList = jsonObject.getAsJsonArray("list")
                    val tomorrowForecast = forecastList.get(8).asJsonObject
                    val temperature = tomorrowForecast.getAsJsonObject("main").get("temp").asDouble
                    val temperatureCelsius = temperature - 273.15

                    requireActivity().runOnUiThread {
                        binding.temperatureForecastTextView.text = "Temperatura na jutro: $temperatureCelsius C"
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Log.d("NextWeekFragment", "Błąd pobierania danych: ${response.message}")
                    }
                }
            }
        })
    }
}
