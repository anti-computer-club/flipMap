package com.example.flipmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.example.flipmap.ui.theme.FlipMapTheme
import com.example.flipmap.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

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

sealed class Screen(val route: String) {
    object Main        : Screen("main")
    object RouteSelect : Screen("route_select")
    object Settings    : Screen("settings")
}

/**
 * Info for NavBar entry
 * @param onClick Item action callback
 **/
data class NavBarItem(
    val label: String,
    val onClick: () -> Unit
)
data class NavBar(
    val left: NavBarItem,
    val center: NavBarItem,
    val right: NavBarItem
)

/**
 * A map of keyMaps, capable of calling async callbacks
**/
class KeyHandler(private val scope: CoroutineScope) {
    var hotkeys by mutableStateOf<Map<Int, suspend () -> Unit>>(emptyMap())

    /**
     * Add a new keyCode to [hotkeys]
     * @param action Keypress callback
     */
    fun bind(keyCode: Int, action: suspend () -> Unit) {
        hotkeys = hotkeys + (keyCode to action)
    }

    /**
     * Clear all [hotkeys] binds
     */
    fun clear() {
        hotkeys = emptyMap()
    }

    fun onKeyPressed(keyCode: Int) {
        hotkeys[keyCode]?.let { action ->
            scope.launch { action() }
        }
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var locationManager: LocationManager
    private var currentLocation: GeoPoint? = null
    val themeViewModel = ThemeViewModel()
    // Instantiate empty navbar
    private var currentNavBar by mutableStateOf(NavBar(NavBarItem("", { }), NavBarItem("", { }), NavBarItem("", { })))
    private lateinit var navController: NavHostController
    val keyHandler = KeyHandler(lifecycleScope)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set up the navbar items that don't require complicated state
        // In general, keep them as high-scope as possible for reuse
        val editRouteButton = NavBarItem("Edit Route") {
            navController.navigate(Screen.RouteSelect.route) {
                // Refresh screen, don't add a bunch of duplicates to the navstack
                launchSingleTop = true
                restoreState = false
            } }
        val settingsRouteButton = NavBarItem("Settings") { navController.navigate(Screen.Settings.route) }

        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(
            this,
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val lat = prefs.getString("last_lat", null)?.toDoubleOrNull()
        val lon = prefs.getString("last_lon", null)?.toDoubleOrNull()
        lat?.let { latVal ->
            lon?.let { lonVal ->
                currentLocation = GeoPoint(latVal, lonVal)
                Log.d("prefs", "read prefs")
            }
        }

        // Hide bottom system navbar
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        requestLocationPermission()

        // All layouts defined here
        setContent {
            navController = rememberNavController()
            FlipMapTheme(darkTheme = themeViewModel.isDarkMode.value) {
                val context = LocalContext.current
                // Map defined up here to share between different composables
                val mapState = remember { mutableStateOf<MapView?>(null) }
                val overlayState = remember { mutableStateOf<MyLocationNewOverlay?>(null) }
                val visibleMapSize = remember { mutableStateOf(IntSize.Zero) }

                val recenterButton = NavBarItem("Recenter") {
                    overlayState.value?.enableFollowLocation()
                    if (mapState.value?.zoomLevelDouble!! < 12.0){
                        mapState.value?.controller?.setZoom(12.0)
                    }
                    mapState.value?.controller?.stopAnimation(true)
                    Log.d("button", "recentered")
                }
                val NavBarMain = NavBar(editRouteButton, recenterButton, settingsRouteButton)
                val NavBarRouteSelect = NavBar(editRouteButton, recenterButton, settingsRouteButton)
                val NavBarSettings = NavBar(editRouteButton, recenterButton, settingsRouteButton)

                // Future extensions should include an abstraction for this, but scoping bindings
                // could be a footgun, so leaving as-is until necessary
                keyHandler.bind(KeyEvent.KEYCODE_POUND) {
                    mapState.value?.controller?.stopAnimation(true)
                    if (mapState.value?.zoomLevelDouble!! < 22.0) {
                        mapState.value?.controller?.zoomIn()
                    }
                }
                keyHandler.bind(KeyEvent.KEYCODE_STAR) {
                    mapState.value?.controller?.stopAnimation(true)
                    if (mapState.value?.zoomLevelDouble!! > 5.0) {
                        mapState.value?.controller?.zoomOut()
                    }
                }
                keyHandler.bind(KeyEvent.KEYCODE_DPAD_RIGHT) {
                    overlayState.value?.disableFollowLocation()
                    mapState.value?.controller?.stopAnimation(false)
                    mapState.value?.controller?.scrollBy(25, 0)
                }
                keyHandler.bind(KeyEvent.KEYCODE_DPAD_LEFT) {
                    overlayState.value?.disableFollowLocation()
                    mapState.value?.controller?.stopAnimation(false)
                    mapState.value?.controller?.scrollBy(-25, 0)
                }
                keyHandler.bind(KeyEvent.KEYCODE_DPAD_UP) {
                    overlayState.value?.disableFollowLocation()
                    mapState.value?.controller?.stopAnimation(false)
                    mapState.value?.controller?.scrollBy(0, -25)
                }
                keyHandler.bind(KeyEvent.KEYCODE_DPAD_DOWN) {
                    overlayState.value?.disableFollowLocation()
                    mapState.value?.controller?.stopAnimation(false)
                    mapState.value?.controller?.scrollBy(0, 25)
                }

                // Map rendering. only one instance throughout
                // All composables include a spacer to show the map behind the UI
                OpenStreetMapView(
                    modifier = Modifier.fillMaxSize(),
                    coordinates = currentLocation ?: GeoPoint(44.5, -123.7681),
                    onMapReady = { map ->
                        mapState.value = map
                        val overlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map).apply {
                            enableMyLocation()
                            enableFollowLocation()
                        }
                        map.overlays.add(overlay)
                        overlayState.value = overlay
                        onOsmMapReady(map)
                        overlay.run {
                            runOnFirstFix {
                                val loc = myLocation
                                currentLocation = loc
                                saveLocationToPrefs(loc)
                            }
                        }

                    },
                    onSizeChanged = { size -> visibleMapSize.value = size }
                )

                // handles all the different screen transitions
                // composables include a spacer to show the map, which is rendered behind them
                NavHost(navController, startDestination = Screen.Main.route, modifier = Modifier.fillMaxSize()) {
                    // default screen - just the map
                    composable(Screen.Main.route) {
                        LaunchedEffect(Unit) {
                            currentNavBar = NavBarMain
                            mapState.value?.overlays?.clear()
                            keyHandler.clear()
                        }
                        Column(Modifier
                            .fillMaxSize()
                            .clipToBounds()) {
                            Spacer(Modifier.weight(1f))
                            SoftKeyNavBar(currentNavBar.left.label, currentNavBar.center.label, currentNavBar.right.label)
                        }
                    }

                    // search for and select destinations
                    composable(Screen.RouteSelect.route) {
                        val scope = rememberCoroutineScope()
                        LaunchedEffect(Unit) {  currentNavBar = NavBarRouteSelect }
                        Column(Modifier
                            .fillMaxSize()
                            .clipToBounds()) {
                            // Text entry field
                            LegacyTextField("") { query ->
                                // Given callback to fetch top 3 results
                                // render them, map to 1/2/3, and render route on keypress
                                // this has grown unwieldy - future work should include abstractions
                                scope.launch {
                                    val cl = currentLocation ?: return@launch
                                    val destinations = getDestinations(cl.latitude, cl.longitude, query)
                                    Log.d("destinations", destinations.toString())
                                    mapState.value?.let { it ->
                                        if (destinations != null) {
                                            overlayState.value?.disableFollowLocation()
                                            drawNumberedMapPoints(it, destinations)
                                            zoomToBoundingBox(it, destinations, visibleMapSize.value)

                                            // Map destinations to hardware keys
                                            keyHandler.clear()
                                            destinations.take(3).forEachIndexed { idx, destination ->
                                                keyHandler.bind(KeyEvent.KEYCODE_1 + idx) {
                                                    overlayState.value?.disableFollowLocation()
                                                    mapState.value?.controller?.setCenter(destination)
                                                    if (mapState.value?.zoomLevelDouble!! < 12.0){
                                                        mapState.value?.controller?.setZoom(12.0)
                                                    }
                                                    val route = getRouteFromApi(cl.latitude, cl.longitude, destination.latitude, destination.longitude)
                                                    if (route != null) {
                                                        setRouteCoordinates(it, route)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            SoftKeyNavBar(currentNavBar.left.label, currentNavBar.center.label, currentNavBar.right.label)
                        }
                    }

                    composable(Screen.Settings.route) {
                        LaunchedEffect(Unit) { currentNavBar = NavBarSettings }
                        SettingsScreen()
                        SoftKeyNavBar(currentNavBar.left.label, currentNavBar.center.label, currentNavBar.right.label)
                    }
                }
            }
        }
    }
    fun onOsmMapReady(map: MapView) {
        currentLocation?.let { location ->
            val geoPoint = GeoPoint(location.latitude, location.longitude)
            map.controller.setCenter(geoPoint)
        }

    }

    fun saveLocationToPrefs(location: GeoPoint) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit()
            .putString("last_lat", location.latitude.toString())
            .putString("last_lon", location.longitude.toString())
            .apply()
        Log.d("pref", "wrote prefs")
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
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

    // This catches key events
    // It was *too* good at catching them, so we release key events triggered
    // while a text field is being edited
    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        Log.d("FlipMapKeyEvent", event.toString())
        if (currentFocus !is EditText) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (event.keyCode) {
                    KEYMAP.SOFT_LEFT -> {
                        currentNavBar.left.onClick()
                        return true
                    }
                    KEYMAP.SOFT_CENTER -> {
                        currentNavBar.center.onClick()
                        return true
                    }
                    KEYMAP.SOFT_RIGHT -> {
                        currentNavBar.right.onClick()
                        return true
                    }

                }
                keyHandler.onKeyPressed(event.keyCode)
            }
        }
        return super.dispatchKeyEvent(event)
    }
}