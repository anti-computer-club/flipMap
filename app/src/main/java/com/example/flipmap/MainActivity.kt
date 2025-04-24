package com.example.flipmap

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
// import android.provider.ContactsContract.CommonDataKinds.Website.URL
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
import androidx.compose.runtime.rememberCoroutineScope
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

// ADB command to reset location permissions
// adb shell pm reset-permissions flipmap

enum class Screen {
    Main,
    Settings
}

// perhaps better to move this somewhere
// TODO we can maybe figure out which mapping to use based on model information?
object KyoceraMappings {
    const val KEY_LEFT = KeyEvent.KEYCODE_DPAD_LEFT
    const val KEY_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT
    const val KEY_UP = KeyEvent.KEYCODE_DPAD_UP
    const val KEY_DOWN = KeyEvent.KEYCODE_DPAD_DOWN
    const val KEY_ENTER = KeyEvent.KEYCODE_DPAD_CENTER
    const val KEY_SPACE = KeyEvent.KEYCODE_SPACE
    const val KEY_BACK = KeyEvent.KEYCODE_BACK
    const val SOFT_LEFT = KeyEvent.KEYCODE_SOFT_LEFT
    const val SOFT_RIGHT = KeyEvent.KEYCODE_SOFT_RIGHT
    const val SOFT_CENTER = KeyEvent.KEYCODE_DPAD_CENTER
}
object CatMappings {
    const val KEY_LEFT = KeyEvent.KEYCODE_DPAD_LEFT
    const val KEY_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT
    const val KEY_UP = KeyEvent.KEYCODE_DPAD_UP
    const val KEY_DOWN = KeyEvent.KEYCODE_DPAD_DOWN
    const val KEY_ENTER = 66
    const val KEY_BACK = KeyEvent.KEYCODE_BACK
    const val SOFT_LEFT = 4
    const val SOFT_CENTER = 5
    const val SOFT_RIGHT = 6
}

val KEYMAP = KyoceraMappings

class MainActivity : ComponentActivity() {
    var currentScreen = mutableStateOf(Screen.Main)
    private lateinit var locationManager: LocationManager
    private var currentLocation: Location? = null
    val themeViewModel = ThemeViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure OSMDroid
        Configuration.getInstance().load(
            this,
            PreferenceManager.getDefaultSharedPreferences(this)
        )

        // Make top bar transparent
        enableEdgeToEdge()

        // Hide bottom system navbar
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Location permissions
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        requestLocationPermission()
        startLocationUpdates()

        // Set the content view using Jetpack Compose - ONLY ONCE
        setContent {
            FlipMapTheme(darkTheme = themeViewModel.isDarkMode.value) {
                // Choose which map implementation to use
                if (currentScreen.value == Screen.Main) {
                    Column(Modifier.fillMaxSize()) {
                        // Use OSM Map
                        OpenStreetMapView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            coordinates = GeoPoint(35.0116, 135.7681),
                            onMapReady = { map -> onOsmMapReady(map) }
                        )

                        SoftKeyNavBar(
                            onSettingsClick = { currentScreen.value = Screen.Settings }
                        ) { /* Show location dialog */ }
                    }
                } else if (currentScreen.value == Screen.Settings) {
                    SettingsScreen(onBackClick = { currentScreen.value = Screen.Main })
                }
            }
        }
    }
    fun onOsmMapReady(map: org.osmdroid.views.MapView) {
        currentLocation?.let { location ->
            val geoPoint = GeoPoint(location.latitude, location.longitude)
            map.controller.setCenter(geoPoint)

        }
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                startLocationUpdates()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                // You could show a rationale dialog here
            }
            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    0x10ca710
                )
            }
        }
    }

    // HTTP POST request to API endpoint (https://api.anti-computer.club/route)
    // returns coordinate pair array
    suspend fun getRouteFromApi(query: String): Array<Pair<Double, Double>>? {
        val currentLoc = currentLocation ?: return null

        // create a JSON object
        val json = JSONObject().apply {
            put("lat", currentLoc.latitude)
            put("lon", currentLoc.longitude)
            put("query", query)
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
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val locationListener = object : android.location.LocationListener {
            override fun onLocationChanged(location: Location) {
                currentLocation = location
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        // TODO maybe remove this later?
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000L, 10f, locationListener
            )
        } catch (e: Exception) {
            Log.e("Location", "Error requesting GPS location updates: ${e.message}")
            // Fallback to network provider
            try {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 5000L, 10f, locationListener
                )
            } catch (e: Exception) {
                Log.e("Location", "Error requesting network location updates: ${e.message}")
            }
        }
    }

    fun getLastLocation(): String {
        return currentLocation?.toString() ?: "location not available"
    }
}

@Composable
fun SoftKeyNavBar(onSettingsClick: () -> Unit, onEditRouteClick: () -> Unit) {
    var selectedIndex by remember { mutableStateOf(-1) }

    Row(
        Modifier
            .fillMaxWidth()
            .focusable(true)
            .onKeyEvent { keyEvent ->
                // TODO make this a str lol
                // Log.d("Key pressed", keyEvent.nativeKeyEvent.keyCode)
                when (keyEvent.nativeKeyEvent.keyCode) {
                    KEYMAP.KEY_UP,
                    KEYMAP.KEY_DOWN,
                    KEYMAP.KEY_LEFT,
                    KEYMAP.KEY_RIGHT,
                    -> {
                        if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                            if (selectedIndex == -1) {
                                selectedIndex = 0
                            } else {
                                when (keyEvent.nativeKeyEvent.keyCode) {
                                    KEYMAP.KEY_LEFT -> selectedIndex =
                                        (selectedIndex - 1).coerceIn(0, 2)

                                    KEYMAP.KEY_RIGHT -> selectedIndex =
                                        (selectedIndex + 1).coerceIn(0, 2)
                                }
                            }
                        }
                        true
                    }

                    KEYMAP.SOFT_LEFT -> {
                        if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                            selectedIndex = 0
                        }
                        true
                    }

                    KEYMAP.SOFT_CENTER -> {
                        if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                            selectedIndex = 1
                        }
                        true
                    }

                    KEYMAP.SOFT_RIGHT -> {
                        if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                            selectedIndex = 2
                        }
                        true
                    }

                    KEYMAP.KEY_ENTER -> {
                        if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                            if (selectedIndex == 2) {
                                onSettingsClick()
                            }
                        }
                        true
                    }

                    KEYMAP.KEY_BACK -> {
                        if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                            selectedIndex = -1
                        }
                        true
                    }

                    else -> false
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val items = listOf("Edit Route", "Go", "Settings")

        items.forEachIndexed { index, text ->
            Text(
                text = text,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        when (text) {
                            "Edit Route" -> onEditRouteClick()
                            "Settings" -> onSettingsClick()
                        }
                    },
                color = if (selectedIndex == index) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (selectedIndex == index) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                }
            )
        }
    }
}