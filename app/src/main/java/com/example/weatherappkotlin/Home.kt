import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.weatherappkotlin.databinding.FragmentHomeBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import java.io.IOException

class Home : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        val sharedPreferences = activity?.getSharedPreferences("CityWeatherPrefs", Context.MODE_PRIVATE)
        val cityName = sharedPreferences?.getString("cityName", "") ?: ""
        Log.d("HomeFragmentCheck", "Odczytano nazwę miasta: $cityName")

        if (cityName.isNotEmpty()) {
            fetchWeatherData(cityName)
        }

        return binding.root
    }

    private fun fetchWeatherData(cityName: String) {
        val apiKey = "54115490ba2f3c3c704b01a9e52dad7a"
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$apiKey"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Log.d("HomeFragment", "Błąd pobierania danych: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val gson = Gson()
                    val jsonObject = gson.fromJson(response.body?.string(), JsonObject::class.java)

                    val temperature = jsonObject.getAsJsonObject("main").get("temp").asDouble
                    val temperatureCelsius = temperature - 273.15
                    val pressure = jsonObject.getAsJsonObject("main").get("pressure").asDouble
                    val description = jsonObject.getAsJsonArray("weather").get(0).asJsonObject.get("description").asString
                    val lon = jsonObject.get("coord").asJsonObject.get("lon").asDouble
                    val lat = jsonObject.get("coord").asJsonObject.get("lat").asDouble
                    val windSpeed = jsonObject.getAsJsonObject("wind").get("speed").asDouble
                    val windDirection = jsonObject.getAsJsonObject("wind").get("deg").asDouble

                    requireActivity().runOnUiThread {
                        binding.cityNameTextView.text = cityName
                        binding.temperatureTextView.text = "$temperatureCelsius °C"
                        binding.coordinatesTextView.text = "Współrzędne: $lat, $lon"
                        binding.pressureTextView.text = "Ciśnienie: $pressure hPa"
                        binding.descriptionTextView.text = "Opis: $description"
                        binding.windSpeedTextView.text = "Prędkość wiatru: $windSpeed m/s"
                        binding.windDirectionTextView.text = "Kierunek wiatru: $windDirection°"
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Log.d("HomeFragment", "Błąd pobierania danych: ${response.message}")
                    }
                }
            }
        })
    }
}
