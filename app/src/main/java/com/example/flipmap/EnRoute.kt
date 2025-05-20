package com.example.flipmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
// import android.provider.ContactsContract.CommonDataKinds.Website.URL
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
// import com.android.volley.toolbox.Volley
import com.example.flipmap.ui.theme.FlipMapTheme
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.example.flipmap.ui.theme.ThemeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.mapsforge.map.android.view.MapView
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline


class EnRoute : ViewModel() {
    private val _routeData = mutableStateOf<List<GeoPoint>?>(null)
    val routeData: State<List<GeoPoint>?> = _routeData

    fun fetchRoute(query: String, origin: String) {
        viewModelScope.launch {
            try {
                val res = doHttpPost(query, origin) // do real http here
                _routeData.value = parseGeoPoints(res)
            } catch (e: Exception) {
                Log.e("network", "error: $e")
            }
        }
    }

    private suspend fun doHttpPost(query: String, origin: String): String {
        // use ktor, retrofit, or HttpURLConnection
        return """[[35.0116, 135.7681], [35.015, 135.77]]""" // fake response
    }

    private fun parseGeoPoints(json: String): List<GeoPoint> {
        // quick + dirty parsing, or gson/moshi/kotlinx.serialization
        return listOf(GeoPoint(35.0116, 135.7681), GeoPoint(35.015, 135.77))
    }
}

