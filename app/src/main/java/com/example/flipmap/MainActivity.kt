package com.example.flipmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
// import android.provider.ContactsContract.CommonDataKinds.Website.URL
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
// import com.android.volley.toolbox.Volley
import com.example.flipmap.ui.theme.FlipMapTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.example.flipmap.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch
// import org.mapsforge.map.android.view.MapView
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import androidx.compose.ui.unit.IntSize

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
    private var currentLocation: GeoPoint? = null
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

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        requestLocationPermission()

        // This doesn't & shouldn't drive state, but is used for things that need to know what state we're in
        currentScreen = mutableStateOf(Screen.Main)
        setContent {
            navController = rememberNavController()
            FlipMapTheme(darkTheme = themeViewModel.isDarkMode.value) {
                val mapState = remember { mutableStateOf<MapView?>(null) }
                val overlayState = remember { mutableStateOf<MyLocationNewOverlay?>(null) }
                val context = LocalContext.current

                // Define map up here to share between different screens
                val visibleMapSize = remember { mutableStateOf(IntSize.Zero) }

                OpenStreetMapView(
                    modifier = Modifier.fillMaxSize(),
                    coordinates = GeoPoint(44.5, -123.7681),
                    onMapReady = { map ->
                        mapState.value = map
                        val overlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map).apply {
                            enableMyLocation()
                            enableFollowLocation()
                        }
                        map.overlays.add(overlay)
                        overlayState.value = overlay
                        onOsmMapReady(map)
                    },
                    onSizeChanged = { size -> visibleMapSize.value = size }
                )

                // handles all the different screen transitions
                // composables skip defining the map and just include a spacer to show what's behind
                NavHost(navController, startDestination = Screen.Main.route, modifier = Modifier.fillMaxSize()) {
                    // default screen - just map
                    composable(Screen.Main.route) {
                        LaunchedEffect(Unit) { currentScreen.value = Screen.Main }
                        Column(Modifier.fillMaxSize().clipToBounds()) {
                            Spacer(Modifier.weight(1f)) // map is behind
                            SoftKeyNavBar("Edit Route", "Recenter", "Show Controls")
                        }
                    }

                    // search for destinations
                    // and maybe select them too
                    composable(Screen.RouteSelect.route) {
                        val scope = rememberCoroutineScope()
                        LaunchedEffect(Unit) { currentScreen.value = Screen.RouteSelect }
                        Column(Modifier.fillMaxSize().clipToBounds()) {
                            // Route input form
                            // After this maybe need to transition to another state where it's like
                            // Routeselect instead?
                            LegacyTextField("") { query ->
                                scope.launch {
                                    val destinations = currentLocation?.let { it ->
                                        getDestinations(it.latitude, it.longitude, query)
                                    }
                                    mapState.value?.let { it ->
                                        if (destinations != null) {
                                            overlayState.value?.disableFollowLocation()
                                            drawNumberedMapPoints(it, destinations)
                                            zoomToBoundingBox(it, destinations, visibleMapSize.value)
                                        }
                                    }
                                    Log.d("ugh", destinations.toString())
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            SoftKeyNavBar("Edit Route", "Recenter", "Show Controls")
                        }
                    }

                    composable(Screen.Settings.route) {
                        LaunchedEffect(Unit) { currentScreen.value = Screen.Settings }
                        SettingsScreen(onBackClick = {})
                    }
                }
            }
        }
    }
    fun onOsmMapReady(map: MapView) {
        currentLocation?.let { location ->
            val geoPoint = GeoPoint(location.latitude, location.longitude)
            map.controller.setCenter(geoPoint)
            // these don't matter bc the map is ready before the location appears
            // We will need to save most recent coordinates + route to make this work
            // probably
            // map.controller.stopAnimation(true)
            // map.controller.stopPanning()
            geoPoint
        }
        // TODO make real
        currentLocation = GeoPoint(44.56, -123.3)
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