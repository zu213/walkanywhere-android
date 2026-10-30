package com.zachupstone.walkanywhere.data.api

import com.zachupstone.walkanywhere.data.api.DirectionsDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface MapDirectionsService {

    @GET("directions/json")
    suspend fun getDirections(
        @Query("origin") originLatLng: String,
        @Query("destination") destinationLatLang: String,
        @Query("key") apiKey: String,
        @Header("X-Android-Package") packageName: String,
        @Header("X-Android-Cert") certSha1: String,
    ): Response<DirectionsDto>

}