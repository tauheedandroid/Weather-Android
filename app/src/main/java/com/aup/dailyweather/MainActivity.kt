package com.aup.dailyweather


import android.widget.SearchView
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aup.dailyweather.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Default city weather show
        fetchWeatherData("Peshawar")

        // SearchView setup
        SearchCity()


    }

    // ✅ SearchView Function
    private fun SearchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    fetchWeatherData(query)   // fetch data for entered city
                } else {
                    Toast.makeText(this@MainActivity, "Please enter a city name", Toast.LENGTH_SHORT).show()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }


    private fun fetchWeatherData(cityName: String?) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()
            .create(ApiInterface::class.java)

        val response = retrofit.getWeatherDate(
            cityName,
            "3a5effaaa6ee313931df6f8daf63c864", // your API key
            "metric"
        )

        response.enqueue(object : Callback<DailyWeather> {
            override fun onResponse(
                call: Call<DailyWeather?>,
                response: Response<DailyWeather?>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val temperature = responseBody.main.temp.toString()
                        val humidity = responseBody.main.humidity
                        val windSpeed = responseBody.wind.speed
                        val sunRise = responseBody.sys.sunrise.toLong()
                        val sunSet = responseBody.sys.sunset.toLong()
                        val seaLevel = responseBody.main.pressure
                        val condition = responseBody.weather.firstOrNull()?.main ?: "Unknown"
                        val maxTemp = responseBody.main.temp_max
                        val minTemp = responseBody.main.temp_min

                        binding.temp.text = "$temperature °C"
                        binding.weather.text = condition
                        binding.maxTemp.text = "Max Temp: $maxTemp °C"
                        binding.minTemp.text = "Min Temp: $minTemp °C"
                        binding.humidity.text = "$humidity %"
                        binding.windSpeed.text = "$windSpeed m/s"
                        binding.sunRise.text = time(sunRise)
                        binding.sunset.text = time(sunSet)
                        binding.sea.text = "$seaLevel hPa"
                        binding.condition.text = condition
                        binding.day.text = dayname(System.currentTimeMillis())
                        binding.date.text = date()
                        binding.cityName.text = cityName
//                        Log.d("TAG", "Response: $temperataure")

                        changeImageAccordingToWeatherCondition(condition)
                    }
                }
            }

            private fun changeImageAccordingToWeatherCondition(conditions: String) {
                val condition = conditions.lowercase() // make it case-insensitive

                when {
                    // ☀️ Clear / Sunny
                    condition.contains("clear") || condition.contains("sun") -> {
                        binding.root.setBackgroundResource(R.drawable.sunny_background)
                        binding.lottieAnimationView.setAnimation(R.raw.sun)
                    }

                    // ☁️ Clouds / Mist / Fog
                    condition.contains("cloud") || condition.contains("overcast") ||
                            condition.contains("mist") || condition.contains("fog") -> {
                        binding.root.setBackgroundResource(R.drawable.colud_background)
                        binding.lottieAnimationView.setAnimation(R.raw.cloud)
                    }

                    // 🌧 Rain / Showers / Drizzle
                    condition.contains("rain") || condition.contains("shower") ||
                            condition.contains("drizzle") -> {
                        binding.root.setBackgroundResource(R.drawable.rain_background)
                        binding.lottieAnimationView.setAnimation(R.raw.rain)
                    }

                    // ❄️ Snow / Blizzard
                    condition.contains("snow") || condition.contains("blizzard") -> {
                        binding.root.setBackgroundResource(R.drawable.snow_background)
                        binding.lottieAnimationView.setAnimation(R.raw.snow)
                    }

                    // 🌤 Default fallback
                    else -> {
                        binding.root.setBackgroundResource(R.drawable.sunny_background)
                        binding.lottieAnimationView.setAnimation(R.raw.sun)
                    }
                }

                binding.lottieAnimationView.playAnimation()
            }


            override fun onFailure(call: Call<DailyWeather?>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun time(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }



    private fun dayname(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
