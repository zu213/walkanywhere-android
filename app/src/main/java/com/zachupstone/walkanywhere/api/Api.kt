package com.zachupstone.walkanywhere.api

import com.zachupstone.walkanywhere.map.MapDirectionsService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // This parses the JSON response into your DirectionsDto class
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val mapDirectionsService: MapDirectionsService by lazy {
        retrofit.create(MapDirectionsService::class.java)
    }
}
