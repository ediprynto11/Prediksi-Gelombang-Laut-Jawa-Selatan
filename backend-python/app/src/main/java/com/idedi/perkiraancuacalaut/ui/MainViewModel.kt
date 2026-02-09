//package com.idedi.perkiraancuacalaut.ui
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.idedi.perkiraancuacalaut.data.ForecastRepository
//import com.idedi.perkiraancuacalaut.data.model.*
//import kotlinx.coroutines.launch
//import org.json.JSONObject
//
//class MainViewModel(private val repo: ForecastRepository) : ViewModel() {
//
//    private val _forecast = MutableLiveData<ForecastResponse?>()
//    val forecast: LiveData<ForecastResponse?> = _forecast
//
//    private val _loading = MutableLiveData(false)
//    val loading: LiveData<Boolean> = _loading
//
//    private val _errorMessage = MutableLiveData<String?>()
//    val errorMessage: LiveData<String?> = _errorMessage
//
//    fun loadForecast(region: String) {
//        viewModelScope.launch {
//            try {
//                _loading.value = true
//                _errorMessage.value = null
//
//                val json = repo.getForecast(region)
//                if (json != null) {
//                    _forecast.value = parseForecast(json)
//                } else {
//                    _errorMessage.value = "Gagal mengambil data dari server"
//                }
//            } catch (e: Exception) {
//                _errorMessage.value = e.message
//            } finally {
//                _loading.value = false
//            }
//        }
//    }
//
//    /** 🔍 Konversi JSON hasil API ke ForecastResponse **/
//    private fun parseForecast(json: JSONObject): ForecastResponse {
//        val region = json.optString("region", "-")
//
//        val coordsObj = json.optJSONObject("coords")
//        val coords = Coords(
//            lat = coordsObj?.optDouble("lat", 0.0) ?: 0.0,
//            lon = coordsObj?.optDouble("lon", 0.0) ?: 0.0
//        )
//
//        val safetyObj = json.optJSONObject("safety")
//        val safety = Safety(
//            today_model = safetyObj?.optString("today_model", "-") ?: "-",
//            `72h_model` = safetyObj?.optString("72h_model", "-") ?: "-"
//        )
//
//        val hourlyArray = json.optJSONArray("predictions_hourly_72h")
//        val hourlyList = mutableListOf<HourlyPrediction>()
//        if (hourlyArray != null) {
//            for (i in 0 until hourlyArray.length()) {
//                val item = hourlyArray.getJSONObject(i)
//                hourlyList.add(
//                    HourlyPrediction(
//                        timestamp = item.optString("timestamp", ""),
//                        wave_height_m = item.optDouble("wave_height_m", 0.0).toFloat(),
//                        wind_speed_mps = item.optDouble("wind_speed_mps", 0.0).toFloat(),
//                        precip_mm = item.optDouble("precip_mm", 0.0).toFloat()
//                    )
//                )
//            }
//        }
//
//        val dailyArray = json.optJSONArray("daily_worst_3days")
//        val dailyList = mutableListOf<DailyWorst>()
//        if (dailyArray != null) {
//            for (i in 0 until dailyArray.length()) {
//                val item = dailyArray.getJSONObject(i)
//                dailyList.add(
//                    DailyWorst(
//                        date = item.optString("date", ""),
//                        text = item.optString("text", ""),
//                        worst_label = item.optInt("worst_label", 0)
//                    )
//                )
//            }
//        }
//
//        val lastObsObj = json.optJSONObject("last_observation")
//        val lastObsMap = mutableMapOf<String, Any>()
//        if (lastObsObj != null) {
//            val keys = lastObsObj.keys()
//            while (keys.hasNext()) {
//                val key = keys.next()
//                lastObsMap[key] = lastObsObj.get(key)
//            }
//        }
//
//        return ForecastResponse(
//            region = region,
//            coords = coords,
//            last_observation = lastObsMap,
//            predictions_hourly_72h = hourlyList,
//            daily_worst_3days = dailyList,
//            safety = safety
//        )
//    }
//}


//Baru
package com.idedi.perkiraancuacalaut.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idedi.perkiraancuacalaut.data.ForecastRepository
import com.idedi.perkiraancuacalaut.data.model.*
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainViewModel(private val repo: ForecastRepository) : ViewModel() {

    private val _forecast = MutableLiveData<ForecastResponse?>()
    val forecast: LiveData<ForecastResponse?> = _forecast

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadForecast(region: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _errorMessage.value = null

                val json = repo.getForecast(region)
                if (json != null) {
                    _forecast.value = parseForecast(json)
                } else {
                    _errorMessage.value = "Gagal mengambil data dari server"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    /** 🔍 Konversi JSON hasil API ke ForecastResponse, gabungkan today_3h + predictions_3h_3days **/
    private fun parseForecast(json: JSONObject): ForecastResponse {
        val region = json.optString("region", "-")

        val coordsObj = json.optJSONObject("coords")
        val coords = Coords(
            lat = coordsObj?.optDouble("lat", 0.0) ?: 0.0,
            lon = coordsObj?.optDouble("lon", 0.0) ?: 0.0
        )

        val safetyObj = json.optJSONObject("safety")
        val safety = Safety(
            today_model = safetyObj?.optString("today_model", "-") ?: "-",
            `72h_model` = safetyObj?.optString("72h_model", "-") ?: "-"
        )

        // --- Parsing today_3h ---
        val todayArray = json.optJSONArray("today_3h")
        val today3hList = mutableListOf<HourlyPrediction>()
        if (todayArray != null) {
            for (i in 0 until todayArray.length()) {
                val item = todayArray.getJSONObject(i)
                today3hList.add(
                    HourlyPrediction(
                        timestamp = item.optString("timestamp", ""),
                        wave_height_m = item.optDouble("wave_height_m", 0.0).toFloat(),
                        wind_speed_mps = item.optDouble("wind_speed_mps", 0.0).toFloat(),
                        precip_mm = item.optDouble("precip_mm", 0.0).toFloat()
                    )
                )
            }
        }

        // --- Parsing predictions_3h_3days ---
        val predArray = json.optJSONArray("predictions_3h_3days")
        val predictions3h3DaysList = mutableListOf<HourlyPrediction>()
        if (predArray != null) {
            for (i in 0 until predArray.length()) {
                val item = predArray.getJSONObject(i)
                predictions3h3DaysList.add(
                    HourlyPrediction(
                        timestamp = item.optString("timestamp", ""),
                        wave_height_m = item.optDouble("wave_height_m", 0.0).toFloat(),
                        wind_speed_mps = item.optDouble("wind_speed_mps", 0.0).toFloat(),
                        precip_mm = item.optDouble("precip_mm", 0.0).toFloat()
                    )
                )
            }
        }

        // --- Parsing daily worst ---
        val dailyArray = json.optJSONArray("daily_worst_3days")
        val dailyList = mutableListOf<DailyWorst>()
        if (dailyArray != null) {
            for (i in 0 until dailyArray.length()) {
                val item = dailyArray.getJSONObject(i)
                dailyList.add(
                    DailyWorst(
                        date = item.optString("date", ""),
                        text = item.optString("text", ""),
                        worst_label = item.optInt("worst_label", 0)
                    )
                )
            }
        }

        val lastObsObj = json.optJSONObject("last_observation")
        val lastObsMap = mutableMapOf<String, Any>()
        if (lastObsObj != null) {
            val keys = lastObsObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                lastObsMap[key] = lastObsObj.get(key)
            }
        }

        println("✅ today_3h count = ${today3hList.size}")
        println("✅ predictions_3h_3days count = ${predictions3h3DaysList.size}")
        if (predictions3h3DaysList.isNotEmpty()) {
            println("⏱ First = ${predictions3h3DaysList.first().timestamp}")
            println("⏱ Last  = ${predictions3h3DaysList.last().timestamp}")
        }

        // --- Return ForecastResponse yang sudah diperbarui ---
        return ForecastResponse(
            region = region,
            coords = coords,
            last_observation = lastObsMap,
            today_3h = today3hList,
            predictions_3h_3days = predictions3h3DaysList,
            daily_worst_3days = dailyList,
            safety = safety
        )

//        // --- hourly: gabungkan today_3h + predictions_3h_3days ---
//        val hourlyList = mutableListOf<HourlyPrediction>()
//
//        val today3hArray = json.optJSONArray("today_3h")
//        if (today3hArray != null) {
//            for (i in 0 until today3hArray.length()) {
//                val item = today3hArray.getJSONObject(i)
//                hourlyList.add(
//                    HourlyPrediction(
//                        timestamp = item.optString("timestamp", ""),
//                        wave_height_m = item.optDouble("wave_height_m", 0.0).toFloat(),
//                        wind_speed_mps = item.optDouble("wind_speed_mps", 0.0).toFloat(),
//                        precip_mm = item.optDouble("precip_mm", 0.0).toFloat()
//                    )
//                )
//            }
//        }
//
//        val predictions3hArray = json.optJSONArray("predictions_3h_3days")
//        if (predictions3hArray != null) {
//            for (i in 0 until predictions3hArray.length()) {
//                val item = predictions3hArray.getJSONObject(i)
//                hourlyList.add(
//                    HourlyPrediction(
//                        timestamp = item.optString("timestamp", ""),
//                        wave_height_m = item.optDouble("wave_height_m", 0.0).toFloat(),
//                        wind_speed_mps = item.optDouble("wind_speed_mps", 0.0).toFloat(),
//                        precip_mm = item.optDouble("precip_mm", 0.0).toFloat()
//                    )
//                )
//            }
//        }
//
//        // --- daily worst ---
//        val dailyArray = json.optJSONArray("daily_worst_3days")
//        val dailyList = mutableListOf<DailyWorst>()
//        if (dailyArray != null) {
//            for (i in 0 until dailyArray.length()) {
//                val item = dailyArray.getJSONObject(i)
//                dailyList.add(
//                    DailyWorst(
//                        date = item.optString("date", ""),
//                        text = item.optString("text", ""),
//                        worst_label = item.optInt("worst_label", 0)
//                    )
//                )
//            }
//        }
//
//        // --- last observation ---
//        val lastObsObj = json.optJSONObject("last_observation")
//        val lastObsMap = mutableMapOf<String, Any>()
//        if (lastObsObj != null) {
//            val keys = lastObsObj.keys()
//            while (keys.hasNext()) {
//                val key = keys.next()
//                lastObsMap[key] = lastObsObj.get(key)
//            }
//        }
//
//        return ForecastResponse(
//            region = region,
//            coords = coords,
//            last_observation = lastObsMap,
//            predictions_hourly_72h = hourlyList,
//            daily_worst_3days = dailyList,
//            safety = safety
//        )
    }
}
