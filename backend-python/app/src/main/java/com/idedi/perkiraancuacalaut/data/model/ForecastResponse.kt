package com.idedi.perkiraancuacalaut.data.model

data class ForecastResponse(
    val region: String,
    val coords: Coords,
    val last_observation: Map<String, Any>,
//    val predictions_hourly_72h: List<HourlyPrediction>,
    val today_3h: List<HourlyPrediction>,           // data hari ini
    val predictions_3h_3days: List<HourlyPrediction>, // prediksi 3 hari ke depan
    val daily_worst_3days: List<DailyWorst>,
    val safety: Safety
)

data class Coords(
    val lat: Double,
    val lon: Double
)

data class HourlyPrediction(
    val timestamp: String,
    val wave_height_m: Float,
    val wind_speed_mps: Float,
    val precip_mm: Float
)

data class DailyWorst(
    val date: String,
    val text: String,
    val worst_label: Int
)

data class Safety(
    val today_model: String,
    val `72h_model`: String
)
