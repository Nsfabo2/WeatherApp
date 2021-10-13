package com.example.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import kotlinx.coroutines.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

/*
Create a weather application that displays the city, country, and time of last update at the top -Done

Users should be able to change the city by tapping it and entering a new zip code

The current temperature should be prominently displayed in the middle of the screen -Done

At the bottom, create six widgets to display: sunrise time, sunset time, wind, pressure, humidity, refresh data -Done

Make sure to implement the following in your application:

Use coroutines to asynchronously fetch weather data

Parse JSON data to populate the app with updated information

Use try blocks as safeguards against crashes

Create a custom background -Done

Add custom images to the app -Done

Use Android Manifest to limit app to portrait mode -Done

Allow users to retry fetching data if error occurs
 */

class MainActivity : AppCompatActivity() {

    //For the api
    private var CITY = "90210" //beverly hills
    private val APIKey = "03c4149fce888d2d95a0e63de460afda"
    private val Link = "https://api.openweathermap.org/data/2.5/weather?zip="//"https://api.openweathermap.org/data/2.5/weather?q="
    private val LinkUnit = "&units=metric&appid="

    //textviews and buttons
    private lateinit var CityCountryTV: TextView
    private lateinit var DateTimeTV: TextView
    private lateinit var WeatherTV: TextView
    private lateinit var TemperatureTV: TextView
    private lateinit var MinimumTempTV: TextView
    private lateinit var MaxTempTV: TextView
    private lateinit var SunriseTV: TextView
    private lateinit var SunsetTV: TextView
    private lateinit var WindTV: TextView
    private lateinit var PressureTV: TextView
    private lateinit var HumidityTV: TextView
    private lateinit var RefreshDataLLO: LinearLayout
    private lateinit var Errorbtn: Button
    private lateinit var ErrorTV: TextView
    private lateinit var ErrorLLO: LinearLayout
    private lateinit var PrograssBar: ProgressBar
    private lateinit var Gobtn: Button
    private lateinit var ZipCodeEV: EditText
    private lateinit var ZipRLO: RelativeLayout
    private lateinit var RLOM: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //initializing
        DateTimeTV = findViewById<TextView>(R.id.DateTimeTV)
        WeatherTV = findViewById<TextView>(R.id.WeatherTV)
        TemperatureTV = findViewById<TextView>(R.id.TemperatureTV)
        MinimumTempTV = findViewById<TextView>(R.id.MinimumTempTV)
        MaxTempTV = findViewById<TextView>(R.id.MaxTempTV)
        SunriseTV = findViewById<TextView>(R.id.SunriseTV)
        SunsetTV = findViewById<TextView>(R.id.SunsetTV)
        WindTV =  findViewById<TextView>(R.id.WindTV)
        PressureTV = findViewById<TextView>(R.id.PressureTV)
        HumidityTV = findViewById<TextView>(R.id.HumidityTV)
        RefreshDataLLO = findViewById<LinearLayout>(R.id.RefreshDataLLO)

        //Go Button clicked
        ZipRLO = findViewById(R.id.ZipRLO)
        ZipCodeEV = findViewById(R.id.ZipCodeEV)
        Gobtn = findViewById(R.id.Gobtn)
        Gobtn.setOnClickListener{
            CITY = ZipCodeEV.text.toString()
            GetAPI()
            ZipCodeEV.text.clear()
            ZipRLO.isVisible = false
        }
        //error button clicked
        ErrorTV = findViewById(R.id.ErrorTV)
        Errorbtn = findViewById(R.id.Errorbtn)
        Errorbtn.setOnClickListener {
                CITY = "90210"
                GetAPI()
            }
        //else
        GetAPI()
    }//end oncreate

    fun GetAPI(){
    println("CITY: $CITY")
    CoroutineScope(IO).launch {
        updateStatus(-1)
        val data = async {
            GetWeatherData()
        }.await()
        if(data.isNotEmpty()){
            DisplayWeatherData(data)
            updateStatus(0)
        }else{
            updateStatus(1)
        }
    }
}//end getapi
    private fun GetWeatherData(): String {
    var response = ""//: String?
    val url = "$Link$CITY$LinkUnit$APIKey"
    try {
        response = URL(url).readText(Charsets.UTF_8)
    }catch (E: Exception){
        println("Error: $E")
    }
    return response
}//end GetWeatherData

    private suspend  fun DisplayWeatherData(data: String){
    withContext(Main){
        //jsson
        val jsonObj = JSONObject(data)
        val main = jsonObj.getJSONObject("main")
        val sys = jsonObj.getJSONObject("sys")
        val wind = jsonObj.getJSONObject("wind")
        val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

        val lastUpdate:Long = jsonObj.getLong("dt")
        val lastUpdateText = "Updated at: " + SimpleDateFormat(
            "dd/MM/yyyy hh:mm a",
            Locale.ENGLISH).format(Date(lastUpdate*1000))
        val currentTemperature = main.getString("temp")
        val temp = try{
            currentTemperature.substring(0, currentTemperature.indexOf(".")) + "°C"
        }catch(e: Exception){
            currentTemperature + "°C"
        }
        val minTemperature = main.getString("temp_min")
        val tempMin = "Low: " + minTemperature.substring(0, minTemperature.indexOf("."))+"°C"
        val maxTemperature = main.getString("temp_max")
        val tempMax = "High: " + maxTemperature.substring(0, maxTemperature.indexOf("."))+"°C"
        val pressure = main.getString("pressure")
        val humidity = main.getString("humidity")

        val sunrise:Long = sys.getLong("sunrise")
        val sunset:Long = sys.getLong("sunset")
        val windSpeed = wind.getString("speed")
        val weatherDescription = weather.getString("description")

        val address = jsonObj.getString("name")+", "+sys.getString("country")

        CityCountryTV = findViewById<TextView>(R.id.CityCountryTV)
        CityCountryTV.text = address
        CityCountryTV.setOnClickListener {
            ZipRLO.isVisible = true
        }

        DateTimeTV.text =  lastUpdateText
        WeatherTV.text = weatherDescription.capitalize(Locale.getDefault())
        TemperatureTV.text = temp
        MinimumTempTV.text = tempMin
        MaxTempTV.text = tempMax
        SunriseTV.text = SimpleDateFormat("hh:mm a",
            Locale.ENGLISH).format(Date(sunrise*1000))
        SunsetTV.text = SimpleDateFormat("hh:mm a",
            Locale.ENGLISH).format(Date(sunset*1000))
        WindTV.text = windSpeed
        PressureTV.text = pressure
        HumidityTV.text = humidity
        RefreshDataLLO.setOnClickListener {
            GetAPI()
        }
    }
}//end DisplayWeatherData

    private suspend fun updateStatus(state: Int){
//        states: -1 = loading, 0 = loaded, 1 = error
        withContext(Main){
            when{
                state < 0 -> {
                    PrograssBar =  findViewById<ProgressBar>(R.id.Pb)
                    PrograssBar.visibility = View.VISIBLE
                    RLOM = findViewById<RelativeLayout>(R.id.RLOM)
                    RLOM.visibility = View.GONE
                    ErrorLLO = findViewById<LinearLayout>(R.id.ErrorLLO)
                    ErrorLLO.visibility = View.GONE
                }
                state == 0 -> {
                    PrograssBar = findViewById<ProgressBar>(R.id.Pb)
                    PrograssBar.visibility = View.GONE
                    RLOM = findViewById<RelativeLayout>(R.id.RLOM)
                    RLOM.visibility = View.VISIBLE
                }
                state > 0 -> {
                    PrograssBar = findViewById<ProgressBar>(R.id.Pb)
                    PrograssBar.visibility = View.GONE
                    ErrorLLO = findViewById<LinearLayout>(R.id.ErrorLLO)
                    ErrorLLO.visibility = View.VISIBLE
                }
            }
        }
    }
}//end class