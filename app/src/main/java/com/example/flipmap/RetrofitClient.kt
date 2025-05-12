package com.example.flipmap.network

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    val api: NominatimApi = Retrofit.Builder()
        .baseUrl("https://nominatim.openstreetmap.org/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(NominatimApi::class.java)
}
