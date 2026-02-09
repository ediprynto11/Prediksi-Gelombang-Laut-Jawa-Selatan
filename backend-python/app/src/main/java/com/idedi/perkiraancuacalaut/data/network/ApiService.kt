package com.idedi.perkiraancuacalaut.data.network

import com.idedi.perkiraancuacalaut.data.model.ForecastResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("predict")
    suspend fun getForecast(@Body body: Map<String, String>): Response<ForecastResponse>
}
