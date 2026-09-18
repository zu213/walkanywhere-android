package com.zachupstone.walkanywhere.map

import androidx.contentpager.content.Query
import com.android.volley.BuildConfig
import com.android.volley.Response

interface MapDirectionsService {

    @GET("directions/json")
    suspend fun getDirections(
        @Query("origin") originLatLng: String,
        @Query("destination") destinationLatLang: String,
        @Query("key") apiKey: String = BuildConfig.MAPS_API_KEY
    ): Response<DirectionsDto>

}