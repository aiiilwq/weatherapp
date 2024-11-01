package com.example.weatherappcompose

import WeatherModel
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.weatherappcompose.ui.theme.WeatherAppComposeTheme
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import screens.DialogSearch
import screens.MainCard
import screens.TabLayout
import java.io.IOException

const val API_KEY = "ff4af17811ae4f2680a152755241010"

data class WeatherHistory(
    val city: String,
    val date: String,
    val condition: String,
    val temperature: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAppComposeTheme {
                val daylist = remember { mutableStateOf(listOf<WeatherModel>()) }
                val currentDay = remember { mutableStateOf(WeatherModel("", "", "", "", "", "", "", "")) }

                getData("Moscow", this, daylist, currentDay)

                Image(
                    painter = painterResource(id = R.drawable.img),
                    contentDescription = "Background Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.5f),
                    contentScale = ContentScale.Crop
                )

                Column {
                    MainCard(currentDay, onClickSync = {
                        dialogState.value
                        getData("Moscow", this@MainActivity, daylist, currentDay)
                    }, onClickSearch = {
                        val daylist = remember { mutableStateOf(false) }
                    }
                        if(dialogState.value){
                            DialogSearch()
                        }
                        )
                    TabLayout(daylist, currentDay)
                    WeatherHistoryScreen()
                }
            }
        }
    }

    private fun getData(city: String, context: Context, daylist: MutableState<List<WeatherModel>>, currentDay: MutableState<WeatherModel>) {
        val url = "https://api.weatherapi.com/v1/forecast.json?key=$API_KEY&q=$city&days=3&aqi=no&alerts=no"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("MyLog", "OkHttpError: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    val list = getWeatherByDays(it)
                    daylist.value = list
                    if (list.isNotEmpty()) {
                        currentDay.value = list[0]

                        saveWeatherHistory(
                            WeatherHistory(
                                currentDay.value.city,
                                currentDay.value.date,
                                currentDay.value.condition,
                                currentDay.value.currentTemp
                            )
                        )
                    }
                }
            }
        })
    }

    private fun getWeatherByDays(response: String): List<WeatherModel> {
        if (response.isEmpty()) return listOf()
        val list = ArrayList<WeatherModel>()
        val mainObject = JSONObject(response)
        val city = mainObject.getJSONObject("location").getString("name")
        val days = mainObject.getJSONObject("forecast").getJSONArray("forecastday")

        for (i in 0 until days.length()) {
            val item = days[i] as JSONObject
            list.add(
                WeatherModel(
                    city,
                    item.getString("date"),
                    "",
                    item.getJSONObject("day").getJSONObject("condition").getString("text"),
                    item.getJSONObject("day").getJSONObject("condition").getString("icon"),
                    item.getJSONObject("day").getString("maxtemp_c"),
                    item.getJSONObject("day").getString("mintemp_c"),
                    item.getJSONArray("hour").toString()
                )
            )
        }
        list[0] = list[0].copy(
            time = mainObject.getJSONObject("current").getString("last_updated"),
            currentTemp = mainObject.getJSONObject("current").getString("temp_c")
        )
        return list
    }

    private fun saveWeatherHistory(weather: WeatherHistory) {
        val sharedPreferences = getSharedPreferences("WeatherHistory", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(weather)
        editor.putString(weather.city + "_" + weather.date, json)
        editor.apply()
    }

    private fun loadWeatherHistory(): List<WeatherHistory> {
        val sharedPreferences = getSharedPreferences("WeatherHistory", Context.MODE_PRIVATE)
        val historyList = mutableListOf<WeatherHistory>()
        val allEntries = sharedPreferences.all

        for (entry in allEntries) {
            val json = entry.value as String
            val weather = Gson().fromJson(json, WeatherHistory::class.java)
            historyList.add(weather)
        }

        return historyList
    }

    @Composable
    fun WeatherHistoryScreen() {
        val history = loadWeatherHistory()
        Column {
            history.forEach { weather ->
                Text("City: ${weather.city}, Date: ${weather.date}, Condition: ${weather.condition}, Temp: ${weather.temperature}")
            }
        }
    }
}
