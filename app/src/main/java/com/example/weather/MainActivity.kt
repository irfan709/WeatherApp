package com.example.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.weather.api.ApiUtils
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.models.WeatherModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var currentLocation: Location
    private lateinit var fusedLocation: FusedLocationProviderClient

    private companion object {
        const val LOCATION_REQUEST_CODE = 101
        const val apiKey = "aef8211ba888cff50b9bcb485a0bb400"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocation = LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocation()
        checkPermissions()

        binding.searchCity.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                getWeatherForCity(binding.searchCity.text.toString())
                hideSoftKeyboard()
                return@setOnEditorActionListener true
            } else {
                return@setOnEditorActionListener false
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            if (hasPermissions()) {
                getCurrentLocation()
            } else {
                requestLocationPermissions()
            }
        }

        binding.mainLayout.post {
            binding.swipeRefresh.isRefreshing = true
            getCurrentLocation()
        }
    }

    private fun checkPermissions() {
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                LOCATION_REQUEST_CODE
            )
        }
    }

    private fun getWeatherForCity(city: String) {
        ApiUtils.getApiInterface()?.getCityWeather(city, apiKey)?.enqueue(
            object : Callback<WeatherModel> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(
                    call: Call<WeatherModel>,
                    response: Response<WeatherModel>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            showData(it)
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "No city found!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<WeatherModel>, t: Throwable) {

                }
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchCurrentLocationWeather(latitude: String, longitude: String) {
        ApiUtils.getApiInterface()?.getCurrentLocationWeather(latitude, longitude, apiKey)
            ?.enqueue(
                object : Callback<WeatherModel> {
                    override fun onResponse(
                        call: Call<WeatherModel>,
                        response: Response<WeatherModel>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.let {
                                showData(it)
                            }
                        }
                    }

                    override fun onFailure(call: Call<WeatherModel>, t: Throwable) {

                    }
                }
            )
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showData(weatherModel: WeatherModel) {
        binding.apply {
            val currentDate = SimpleDateFormat("dd/MM/yyyy hh:mm").format(Date())
            currentDateTime.text = currentDate.toString()
            minTemp.text = "Min " + k2c(weatherModel.main.temp_min) + "°"
            maxTemp.text = "Max " + k2c(weatherModel.main.temp_max) + "°"
            mainTemp.text = "" + k2c(weatherModel.main.temp) + "°"
            weatherTypeTv.text = weatherModel.weather[0].main
            feelsLikeTv.text = "Feels like" + k2c(weatherModel.main.feels_like) + "°"
            pressureValue.text = weatherModel.main.pressure.toString()
            humidityValue.text = weatherModel.main.humidity.toString()
            windSpeedValue.text = weatherModel.wind.speed.toString()
            sunriseValue.text = ts2d(weatherModel.sys.sunrise.toLong())
            sunsetValue.text = ts2d(weatherModel.sys.sunset.toLong())
            temperatureValue.text =
                "" + (k2c(weatherModel.main.temp).times(1.8)).plus(32).roundToInt() + "°"
            groundValue.text = weatherModel.main.grnd_level.toString()
            seaValue.text = weatherModel.main.sea_level.toString()
            countryValue.text = weatherModel.sys.country
        }

        updateUi(weatherModel.weather[0].id)
    }

    private fun updateUi(id: Int) {
        runOnUiThread {
            binding.apply {
                when (id) {
                    in 200..232 -> {
                        weatherImg.setImageResource(R.drawable.ic_storm_weather)
                        mainLayout.background = ContextCompat.getDrawable(
                            this@MainActivity, R.drawable.strom_bg
                        )
                    }

                    in 300..321 -> {
                        weatherImg.setImageResource(R.drawable.ic_few_clouds)
                        mainLayout.background = ContextCompat.getDrawable(
                            this@MainActivity, R.drawable.clouds_bg
                        )
                    }

                    in 500..531 -> {
                        weatherImg.setImageResource(R.drawable.ic_rainy_weather)
                        mainLayout.background = ContextCompat.getDrawable(
                            this@MainActivity, R.drawable.rainy_bg
                        )
                    }

                    in 600..622 -> {
                        weatherImg.setImageResource(R.drawable.ic_snow_weather)
                        mainLayout.background = ContextCompat.getDrawable(
                            this@MainActivity, R.drawable.snow_bg
                        )
                    }

                    in 701..781 -> {
                        weatherImg.setImageResource(R.drawable.ic_broken_clouds)
                        mainLayout.background = ContextCompat.getDrawable(
                            this@MainActivity, R.drawable.borken_clouds_bg
                        )
                    }

                    800 -> {
                        weatherImg.setImageResource(R.drawable.ic_clear_day)
                        mainLayout.background = ContextCompat.getDrawable(
                            this@MainActivity, R.drawable.clear_bg
                        )
                    }

                    in 801..804 -> {
                        weatherImg.setImageResource(R.drawable.ic_cloudy_weather)
                        mainLayout.background = ContextCompat.getDrawable(
                            this@MainActivity, R.drawable.cloudy_weather_bg
                        )
                    }

                    else -> {
                        weatherImg.setImageResource(R.drawable.ic_unknown)
                        mainLayout.background = ContextCompat.getDrawable(
                            this@MainActivity, R.drawable.unknown_bg
                        )
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun ts2d(local: Long): String {
        val localTime = local.let {
            Instant.ofEpochSecond(it)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
        }
        return localTime.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun k2c(tempMin: Double): Double {
        val temperature: Double = tempMin.minus(273)
        return temperature.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentLocation() {
        if (hasPermissions()) {
            if (hasLocationEnabled()) {
                binding.swipeRefresh.isRefreshing = true
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    showPermissionExplanationDialog()
                    return
                }
                fusedLocation.lastLocation.addOnCompleteListener(this) { task ->
                    binding.swipeRefresh.isRefreshing = false
                    val location: Location = task.result
                    currentLocation = location
                    fetchCurrentLocationWeather(
                        location.latitude.toString(),
                        location.longitude.toString()
                    )
                }
            } else {
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this, "Turn on Location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            binding.swipeRefresh.isRefreshing = false
            requestLocationPermissions()
        }
    }

    private fun showPermissionExplanationDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app requires location permission to function properly.")
            .setPositiveButton("Grant Permission") { _, _ ->
                requestLocationPermissions()
            }
            .setNegativeButton("Cancel") { _, _ ->
            }
            .setCancelable(false)

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
            LOCATION_REQUEST_CODE
        )
    }


    private fun hasLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun hasPermissions(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun hideSoftKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm: InputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            binding.searchCity.clearFocus()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}