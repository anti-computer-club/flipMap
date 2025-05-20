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

// ADB command to reset location permissions
// adb shell pm reset-permissions flipmap

sealed class Screen(val route: String) {
    object Main        : Screen("main")
    object RouteSelect : Screen("route_select")
    object Settings    : Screen("settings")
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
    private lateinit var locationManager: LocationManager
    private var currentLocation: Location? = null
    val themeViewModel = ThemeViewModel()
    private lateinit var currentScreen: MutableState<Screen>
    private lateinit var navController: NavHostController
    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)

        // Configure OSMDroid
        Configuration.getInstance().load(
            this,
            PreferenceManager.getDefaultSharedPreferences(this)
        )

        // Make top bar transparent
        // enableEdgeToEdge()

        // Hide bottom system navbar
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Location permissions
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        requestLocationPermission()

        // This shouldn't drive state, but is used for things that need to know what state we're in
        currentScreen = mutableStateOf(Screen.Main)
        setContent {
            navController = rememberNavController()
            FlipMapTheme(darkTheme = themeViewModel.isDarkMode.value) {
                NavHost(navController, startDestination = Screen.Main.route) {
                    composable(Screen.Main.route) {
                        LaunchedEffect(Unit) { currentScreen.value = Screen.Main }
                        Column(Modifier.clipToBounds()) {
                            // Use OSM Map
                            OpenStreetMapView(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                coordinates = currentLocation?.let { loc ->
                                    GeoPoint(loc.latitude, loc.longitude)
                                } ?: GeoPoint(35.0116, 135.7681),

                                onMapReady = { map -> onOsmMapReady(map) }
                            )

                            SoftKeyNavBar(
                                "Edit Route", "Recenter", "Show Controls"
                            )
                        }
                    }
                    composable(Screen.RouteSelect.route) {
                        val scope = rememberCoroutineScope()
                        LaunchedEffect(Unit) { currentScreen.value = Screen.RouteSelect }
                        Column(Modifier.clipToBounds()) {
                            LegacyTextField("") { query ->
                                scope.launch {
                                    // Log.d("ugh", location.toString())
                                    // Log.d("ugh", query)

                                    val destinations = getDestinations(45.5, -123.3, query)
                                    Log.d("ugh", destinations.toString())
                                    // or do whatever w/ destinations
                                }
                            }
                            // Use OSM Map
                            OpenStreetMapView(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                coordinates = GeoPoint(35.0116, 135.7681),
                                onMapReady = { map -> onOsmMapReady(map) }
                            )

                            SoftKeyNavBar(
                                "Edit Route", "Recenter", "Show Controls"
                            )
                        }
                    }
                    composable(Screen.Settings.route){
                        LaunchedEffect(Unit) { currentScreen.value = Screen.Settings }
                        SettingsScreen(onBackClick = {  } )
                    }
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
    fun setRouteCoordinates(map: org.osmdroid.views.MapView, geoPoints: List<GeoPoint>) {
        val line = Polyline()
        line.setPoints(geoPoints)
        map.overlays.clear() // optional, clears old lines
        map.overlays.add(line)
        map.invalidate()
        Log.d("paul", "did da stuff")
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // startLocationUpdates()
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

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KEYMAP.SOFT_LEFT -> {
                    when (currentScreen.value) {
                        Screen.Main -> navController.navigate(Screen.RouteSelect.route)
                        // Screen.RouteSelect -> /* maybe do something else */
                        else -> {}
                    }
                    return true
                }
                KEYMAP.SOFT_RIGHT -> {
                    when (currentScreen.value) {
                        Screen.Main, Screen.RouteSelect -> navController.navigate(Screen.Settings.route)
                        // etc
                        else -> {}
                    }
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }
}