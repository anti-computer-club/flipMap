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
import org.osmdroid.util.GeoPoint

suspend fun getRouteFromApi(src_lat: Double, src_lon: Double, dest_lat: Double, dest_lon: Double)
    : List<GeoPoint>? {

    val json = JSONObject().apply {
        put("src_lat", src_lat)
        put("src_lon", src_lon)
        put("dst_lat", dest_lat)
        put("dst_lon", dest_lon)
    }

    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val requestBody = json.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://api.anticomputer.club/route")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonResponse = response.body?.string()?.let { JSONObject(it) }
                Log.d("API_RESPONSE", "Response: $jsonResponse")

                val routeArray = jsonResponse?.getJSONArray("route")
                routeArray?.let { parseRouteJson(it) }
            } else {
                Log.e("API_ERROR", "Response not successful: ${response.code}")
                Log.e("API_ERROR", "Response not successful: ${response.body}")
                Log.e("API_ERROR", "Sent: $json")
                null
            }
        } catch (e: Exception) {
            Log.e("API_ERROR", "Exception during API call", e)
            e.printStackTrace()
            null
        }
    }
}

suspend fun getDestinations(src_lat: Double, src_lon: Double, query: String): List<GeoPoint>? {
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

            Log.d("API_DEBUG", "Sending JSON: $json")
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonResponse = response.body?.string()?.let { JSONObject(it) }
                Log.d("API_RESPONSE", "Response: $jsonResponse")

                val routeArray = jsonResponse?.getJSONArray("results")
                parseGeoPoints(routeArray.toString())
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
// maybe turn this into something real at some point
fun parseGeoPoints(json: String): List<GeoPoint> {
    val arr = JSONArray(json)
    val result = mutableListOf<GeoPoint>()
    for (i in 0 until arr.length()) {
        val obj = arr.getJSONObject(i)
        val lat = obj.getDouble("lat")
        val lon = obj.getDouble("lon")
        result.add(GeoPoint(lat, lon))
    }
    return result
}
fun parseRouteJson(json: JSONArray): List<GeoPoint> {
    val result = mutableListOf<GeoPoint>()
    for (i in 0 until json.length() step 2) {
        val lon = json.getDouble(i)
        val lat = json.getDouble(i + 1)
        result.add(GeoPoint(lat, lon))
    }
    return result
}