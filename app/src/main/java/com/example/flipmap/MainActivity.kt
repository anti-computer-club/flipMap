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
import org.osmdroid.views.overlay.Polyline

// ADB command to reset location permissions
// adb shell pm reset-permissions flipmap

enum class Screen {
    Main,
    Settings,
    RouteSelect
}

val STARBUCKS_COORDS : Array<Pair<Double, Double>> = arrayOf(
    Pair(-123.278961,44.567629), Pair(-123.278968,44.567962), Pair(-123.278974,44.568108), Pair(-123.278978,44.568269), Pair(-123.278993,44.568482), Pair(-123.279009,44.568708), Pair(-123.278941,44.568796), Pair(-123.278441,44.568689), Pair(-123.277631,44.568506), Pair(-123.277011,44.568367), Pair(-123.276856,44.568332), Pair(-123.276363,44.568217), Pair(-123.275074,44.567926), Pair(-123.275065,44.56872), Pair(-123.275077,44.568981), Pair(-123.275088,44.569136), Pair(-123.275096,44.569503), Pair(-123.2751,44.569686), Pair(-123.275103,44.569819), Pair(-123.275109,44.570195), Pair(-123.274977,44.570247), Pair(-123.27507,44.570385), Pair(-123.275119,44.570533), Pair(-123.275125,44.570872), Pair(-123.275144,44.571315), Pair(-123.275154,44.571578), Pair(-123.275158,44.571751), Pair(-123.275163,44.571996), Pair(-123.275166,44.572144), Pair(-123.275171,44.572396), Pair(-123.275195,44.573395), Pair(-123.275201,44.57368), Pair(-123.275202,44.57373), Pair(-123.275216,44.574384), Pair(-123.275236,44.574922), Pair(-123.275245,44.575186), Pair(-123.275256,44.575474), Pair(-123.275271,44.575837), Pair(-123.275281,44.576043), Pair(-123.275285,44.576393), Pair(-123.275288,44.57657), Pair(-123.275294,44.577045), Pair(-123.275304,44.577597), Pair(-123.275315,44.57814), Pair(-123.275372,44.578267), Pair(-123.275709,44.578715), Pair(-123.275924,44.57898), Pair(-123.275946,44.579061), Pair(-123.275952,44.579424), Pair(-123.27595,44.579532), Pair(-123.275952,44.579823), Pair(-123.275954,44.579981), Pair(-123.275957,44.580355), Pair(-123.275958,44.580487), Pair(-123.275959,44.580626), Pair(-123.275961,44.580904), Pair(-123.275961,44.580965), Pair(-123.275966,44.581582), Pair(-123.275996,44.58245), Pair(-123.276015,44.582986), Pair(-123.276009,44.583632), Pair(-123.27601,44.584387), Pair(-123.276011,44.584811), Pair(-123.276011,44.58499), Pair(-123.276012,44.585501), Pair(-123.276013,44.585754), Pair(-123.276009,44.585927), Pair(-123.276025,44.586351), Pair(-123.275711,44.586352), Pair(-123.275281,44.58635), Pair(-123.275104,44.586352), Pair(-123.274765,44.586384), Pair(-123.274613,44.586419), Pair(-123.274213,44.586552), Pair(-123.273998,44.58665), Pair(-123.273762,44.586791), Pair(-123.272723,44.587415), Pair(-123.272335,44.587641), Pair(-123.271513,44.588115), Pair(-123.271183,44.58827), Pair(-123.270934,44.588363), Pair(-123.270446,44.588502), Pair(-123.269892,44.588598), Pair(-123.269377,44.58863), Pair(-123.269139,44.588634), Pair(-123.268326,44.588649), Pair(-123.267156,44.588668), Pair(-123.267097,44.58867), Pair(-123.265587,44.588694), Pair(-123.264182,44.588728), Pair(-123.263704,44.588736), Pair(-123.262566,44.588756), Pair(-123.262213,44.588763), Pair(-123.261895,44.588749), Pair(-123.261746,44.588735), Pair(-123.261561,44.58871), Pair(-123.261374,44.588686), Pair(-123.26105,44.588653), Pair(-123.260457,44.588651), Pair(-123.260411,44.588652), Pair(-123.260135,44.588662), Pair(-123.259583,44.588681), Pair(-123.25945,44.588685), Pair(-123.258818,44.588707), Pair(-123.257941,44.588738), Pair(-123.256755,44.588779), Pair(-123.255914,44.588808), Pair(-123.255797,44.588812), Pair(-123.254898,44.588834), Pair(-123.254703,44.589146), Pair(-123.254595,44.589345), Pair(-123.254488,44.589537), Pair(-123.254401,44.589688), Pair(-123.254311,44.589849), Pair(-123.254147,44.59014), Pair(-123.254037,44.590337), Pair(-123.254009,44.590388), Pair(-123.25381,44.590744), Pair(-123.253571,44.591173), Pair(-123.253454,44.591384), Pair(-123.253319,44.59163), Pair(-123.253276,44.591707), Pair(-123.253078,44.592067), Pair(-123.253024,44.592164), Pair(-123.252872,44.592439), Pair(-123.252804,44.592561), Pair(-123.253056,44.592627), Pair(-123.25315,44.592658), Pair(-123.253354,44.59266), Pair(-123.253434,44.592658)
)

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
        startLocationUpdates()

        // Set the content view using Jetpack Compose - ONLY ONCE
        setContent {
            FlipMapTheme(darkTheme = themeViewModel.isDarkMode.value) {
                // Choose which map implementation to use
                if (currentScreen.value == Screen.Main) {
                    Column(Modifier.clipToBounds()) {
                        // Use OSM Map
                        OpenStreetMapView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            // hello chatgpt. please make this the user's first seen coordinates instead
                            coordinates = currentLocation?.let { loc ->
                                GeoPoint(loc.latitude, loc.longitude)
                            } ?: GeoPoint(35.0116, 135.7681),

                            onMapReady = { map -> onOsmMapReady(map) }
                        )

                        SoftKeyNavBar(
                            "Edit Route", "Recenter", "Show Controls"
                        )
                    }
                } else if (currentScreen.value == Screen.Settings) {
                    SettingsScreen(onBackClick = { currentScreen.value = Screen.Main })
                }
                else if (currentScreen.value == Screen.RouteSelect) {
                    Column(Modifier.clipToBounds()) {
                        LegacyTextField("test", { }) { query ->
                            currentLocation?.let { location ->
                                Log.d("ugh", location.toString())
                                Log.d("ugh", query.toString())
                                // Launch async operation using LaunchedEffect
                                // LaunchedEffect(query) {
                                //     val destinations = getDestinations(
                                //         location.latitude, location.longitude, query
                                //     )
                                //     // Do something with destinations here (e.g., update state)
                                //     Log.d("Destinations", destinations.toString())
                                // }
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

    // TODO check if can be removed
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

    // TODO remove along with startLocationUpdates
    fun getLastLocation(): String {
        return currentLocation?.toString() ?: "location not available"
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KEYMAP.SOFT_LEFT -> {
                    currentScreen.value = Screen.RouteSelect
                    return true
                }
                KEYMAP.SOFT_CENTER -> {
                    return true
                }
                KEYMAP.SOFT_RIGHT -> {
                    currentScreen.value = Screen.Settings
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }
}