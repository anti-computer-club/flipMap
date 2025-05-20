package com.example.flipmap

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

suspend fun getRouteFromApi(src_lat: Double, src_lon: Double, dest_lat: Double, dest_lon: Double)
    : Array<Pair<Double, Double>>? {

    // create a JSON object
    val json = JSONObject().apply {
        put("lat", src_lat)
        put("lon", src_lon)
        put("query", "$dest_lat,$dest_lon")
    }

    // use withContext for long-running tasks
    return withContext(Dispatchers.IO) {
        try {
            // create HTTP client instance
            val client = OkHttpClient()

            // convert into req body
            val requestBody = json.toString().toRequestBody("application/json".toMediaType())

            // build POST request with API URL and JSON req body
            val request = Request.Builder()
                .url("https://api.anticomputer.club/route")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonResponse = JSONObject(response.body?.string())
                Log.d("API_RESPONSE", "Response: $jsonResponse")

                val routeArray = jsonResponse.getJSONArray("route")
                Array(routeArray.length()/2) { i ->
                    try {
                        val lat = routeArray.getDouble(i*2)
                        val lon = routeArray.getDouble(i*2+1)
                        Pair(lat, lon)
                    } catch (e: Exception) {
                        Log.e("API_ERROR", "Error parsing coordinate at index ${i*2}", e)
                        Pair(0.0, 0.0) // Fallback
                    }
                }
            } else {
                Log.e("API_ERROR", "Response not successful: ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.e("API_ERROR", "Exception during API call", e)
            e.printStackTrace()
            null
        }
    }
}

suspend fun getDestinations(src_lat: Double, src_lon: Double, query: String): JSONArray? {
    val json = JSONObject().apply {
        put("lat", src_lat)
        put("lon", src_lon)
        put("query", query)
        put("amount", 3)
    }

    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val requestBody = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://api.anticomputer.club/get_locations")
                .post(requestBody)
                .build()

            Log.d("API_DEBUG", "Sending JSON: ${json.toString()}")
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonResponse = JSONObject(response.body?.string())
                Log.d("API_RESPONSE", "Response: $jsonResponse")

                val routeArray = jsonResponse.getJSONArray("results")
                routeArray
            } else {
                Log.e("API_ERROR", "Response not successful: ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.e("API_ERROR", "Exception during API call", e)
            null
        }
    }
}