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

val STARBUCKS_ROUTE = floatArrayOf( -0.9967f, -1.0f, 0.0f, -1.0f, -0.98870f, 0.0f, -0.96651f, -0.98617f, 0.0f, -0.93829f, -0.98415f, 0.0f, -0.91068f, -0.98226f, 0.0f, -0.91318f, -0.97525f, 0.0f, -0.91469f, -0.96907f, 0.0f, -0.91567f, -0.95606f, 0.0f, -0.91496f, -0.93883f, 0.0f, -0.91380f, -0.92185f, 0.0f, -0.90935f, -0.91440f, 0.0f, -0.90116f, -0.90525f, 0.0f, -0.89831f, -0.89761f, 0.0f, -0.89768f, -0.85103f, 0.0f, -0.89715f, -0.80167f, 0.0f, -0.89679f, -0.77888f, 0.0f, -0.89332f, -0.77288f, 0.0f, -0.88887f, -0.76682f, 0.0f, -0.87240f, -0.75628f, 0.0f, -0.80855f, -0.70421f, 0.0f, -0.77463f, -0.67056f, 0.0f, -0.74542f, -0.63459f, 0.0f, -0.73331f, -0.61824f, 0.0f, -0.72556f, -0.60624f, 0.0f, -0.71016f, -0.58283f, 0.0f, -0.70651f, -0.57765f, 0.0f, -0.70419f, -0.57424f, 0.0f, -0.69814f, -0.56540f, 0.0f, -0.69716f, -0.56389f, 0.0f, -0.68879f, -0.55158f, 0.0f, -0.68799f, -0.55038f, 0.0f, -0.68514f, -0.54628f, 0.0f, -0.68042f, -0.53934f, 0.0f, -0.66457f, -0.51503f, 0.0f, -0.66119f, -0.51011f, 0.0f, -0.65611f, -0.50285f, 0.0f, -0.64756f, -0.49061f, 0.0f, -0.64062f, -0.48051f, 0.0f, -0.62147f, -0.48391f, 0.0f, -0.58461f, -0.49067f, 0.0f, -0.55567f, -0.49591f, 0.0f, -0.51569f, -0.50317f, 0.0f, -0.50732f, -0.50468f, 0.0f, -0.50304f, -0.50550f, 0.0f, -0.47517f, -0.51055f, 0.0f, -0.43217f, -0.51832f, 0.0f, -0.38943f, -0.52602f, 0.0f, -0.34526f, -0.53422f, 0.0f, -0.29887f, -0.54300f, 0.0f, -0.26744f, -0.54874f, 0.0f, -0.24678f, -0.55259f, 0.0f, -0.20181f, -0.56111f, 0.0f, -0.18837f, -0.54224f, 0.0f, -0.17492f, -0.52343f, 0.0f, -0.16887f, -0.51497f, 0.0f, -0.15738f, -0.49875f, 0.0f, -0.13636f, -0.46757f, 0.0f, -0.11526f, -0.43620f, 0.0f, -0.10475f, -0.42117f, 0.0f, -0.09362f, -0.40527f, 0.0f, -0.07849f, -0.38374f, 0.0f, -0.07564f, -0.37970f, 0.0f, -0.05507f, -0.35370f, 0.0f, -0.01242f, -0.29985f, 0.0f, -0.00645f, -0.29228f, 0.0f, 0.010818f, -0.27031f, 0.0f, 0.031387f, -0.24424f, 0.0f, 0.057833f, -0.21060f, 0.0f, 0.060326f, -0.20706f, 0.0f, 0.077512f, -0.18504f, 0.0f, 0.083745f, -0.17715f, 0.0f, 0.104492f, -0.15057f, 0.0f, 0.117314f, -0.13422f, 0.0f, 0.138328f, -0.10727f, 0.0f, 0.157383f, -0.08290f, 0.0f, 0.177596f, -0.05740f, 0.0f, 0.200837f, -0.02894f, 0.0f, 0.215262f, -0.00931f, 0.0f, 0.243488f, 0.026795f, 0.0f, 0.246516f, 0.030771f, 0.0f, 0.271626f, 0.062647f, 0.0f, 0.280263f, 0.073504f, 0.0f, 0.293441f, 0.090484f, 0.0f, 0.314099f, 0.116869f, 0.0f, 0.323360f, 0.128735f, 0.0f, 0.325408f, 0.131323f, 0.0f, 0.331908f, 0.139719f, 0.0f, 0.343217f, 0.154173f, 0.0f, 0.362272f, 0.178538f, 0.0f, 0.379546f, 0.200694f, 0.0f, 0.387115f, 0.210288f, 0.0f, 0.394060f, 0.219188f, 0.0f, 0.400828f, 0.227962f, 0.0f, 0.417390f, 0.249108f, 0.0f, 0.421397f, 0.254284f, 0.0f, 0.425493f, 0.259460f, 0.0f, 0.436000f, 0.273031f, 0.0f, 0.445438f, 0.285087f, 0.0f, 0.456658f, 0.299479f, 0.0f, 0.460219f, 0.304023f, 0.0f, 0.482302f, 0.332176f, 0.0f, 0.488001f, 0.339498f, 0.0f, 0.494412f, 0.347767f, 0.0f, 0.508926f, 0.366135f, 0.0f, 0.517563f, 0.377244f, 0.0f, 0.531899f, 0.394224f, 0.0f, 0.558078f, 0.431339f, 0.0f, 0.580250f, 0.460312f, 0.0f, 0.586483f, 0.468202f, 0.0f, 0.619696f, 0.511062f, 0.0f, 0.628422f, 0.521855f, 0.0f, 0.653799f, 0.553353f, 0.0f, 0.672766f, 0.578475f, 0.0f, 0.685143f, 0.594066f, 0.0f, 0.708472f, 0.624112f, 0.0f, 0.720315f, 0.639198f, 0.0f, 0.727082f, 0.647845f, 0.0f, 0.738123f, 0.662048f, 0.0f, 0.747117f, 0.673536f, 0.0f, 0.752548f, 0.680479f, 0.0f, 0.754329f, 0.682752f, 0.0f, 0.762343f, 0.692914f, 0.0f, 0.770802f, 0.703645f, 0.0f, 0.771960f, 0.705160f, 0.0f, 0.787988f, 0.725548f, 0.0f, 0.794666f, 0.734006f, 0.0f, 0.813543f, 0.758497f, 0.0f, 0.830906f, 0.778191f, 0.0f, 0.840523f, 0.790752f, 0.0f, 0.850051f, 0.802872f, 0.0f, 0.857797f, 0.812403f, 0.0f, 0.865811f, 0.822565f, 0.0f, 0.880414f, 0.840934f, 0.0f, 0.890209f, 0.853369f, 0.0f, 0.892702f, 0.856588f, 0.0f, 0.910422f, 0.879059f, 0.0f, 0.931703f, 0.906138f, 0.0f, 0.942121f, 0.919457f, 0.0f, 0.954142f, 0.934985f, 0.0f, 0.957971f, 0.939845f, 0.0f, 0.975602f, 0.962569f, 0.0f, 0.980410f, 0.968691f, 0.0f, 0.993945f, 0.986050f, 0.0f, 1.0f, 0.993750f, 0.0f, 0.977561f, 0.997916f, 0.0f, 0.969191f, 0.999873f, 0.0f, 0.951026f, 1.0f, 0.0f, 0.943902f, 0.999873f, 0.0f,)
val STARBUCKS_COORDS : Array<Pair<Double, Double>> = arrayOf(
    Pair(-123.278961,44.567629), Pair(-123.278968,44.567962), Pair(-123.278974,44.568108), Pair(-123.278978,44.568269), Pair(-123.278993,44.568482), Pair(-123.279009,44.568708), Pair(-123.278941,44.568796), Pair(-123.278441,44.568689), Pair(-123.277631,44.568506), Pair(-123.277011,44.568367), Pair(-123.276856,44.568332), Pair(-123.276363,44.568217), Pair(-123.275074,44.567926), Pair(-123.275065,44.56872), Pair(-123.275077,44.568981), Pair(-123.275088,44.569136), Pair(-123.275096,44.569503), Pair(-123.2751,44.569686), Pair(-123.275103,44.569819), Pair(-123.275109,44.570195), Pair(-123.274977,44.570247), Pair(-123.27507,44.570385), Pair(-123.275119,44.570533), Pair(-123.275125,44.570872), Pair(-123.275144,44.571315), Pair(-123.275154,44.571578), Pair(-123.275158,44.571751), Pair(-123.275163,44.571996), Pair(-123.275166,44.572144), Pair(-123.275171,44.572396), Pair(-123.275195,44.573395), Pair(-123.275201,44.57368), Pair(-123.275202,44.57373), Pair(-123.275216,44.574384), Pair(-123.275236,44.574922), Pair(-123.275245,44.575186), Pair(-123.275256,44.575474), Pair(-123.275271,44.575837), Pair(-123.275281,44.576043), Pair(-123.275285,44.576393), Pair(-123.275288,44.57657), Pair(-123.275294,44.577045), Pair(-123.275304,44.577597), Pair(-123.275315,44.57814), Pair(-123.275372,44.578267), Pair(-123.275709,44.578715), Pair(-123.275924,44.57898), Pair(-123.275946,44.579061), Pair(-123.275952,44.579424), Pair(-123.27595,44.579532), Pair(-123.275952,44.579823), Pair(-123.275954,44.579981), Pair(-123.275957,44.580355), Pair(-123.275958,44.580487), Pair(-123.275959,44.580626), Pair(-123.275961,44.580904), Pair(-123.275961,44.580965), Pair(-123.275966,44.581582), Pair(-123.275996,44.58245), Pair(-123.276015,44.582986), Pair(-123.276009,44.583632), Pair(-123.27601,44.584387), Pair(-123.276011,44.584811), Pair(-123.276011,44.58499), Pair(-123.276012,44.585501), Pair(-123.276013,44.585754), Pair(-123.276009,44.585927), Pair(-123.276025,44.586351), Pair(-123.275711,44.586352), Pair(-123.275281,44.58635), Pair(-123.275104,44.586352), Pair(-123.274765,44.586384), Pair(-123.274613,44.586419), Pair(-123.274213,44.586552), Pair(-123.273998,44.58665), Pair(-123.273762,44.586791), Pair(-123.272723,44.587415), Pair(-123.272335,44.587641), Pair(-123.271513,44.588115), Pair(-123.271183,44.58827), Pair(-123.270934,44.588363), Pair(-123.270446,44.588502), Pair(-123.269892,44.588598), Pair(-123.269377,44.58863), Pair(-123.269139,44.588634), Pair(-123.268326,44.588649), Pair(-123.267156,44.588668), Pair(-123.267097,44.58867), Pair(-123.265587,44.588694), Pair(-123.264182,44.588728), Pair(-123.263704,44.588736), Pair(-123.262566,44.588756), Pair(-123.262213,44.588763), Pair(-123.261895,44.588749), Pair(-123.261746,44.588735), Pair(-123.261561,44.58871), Pair(-123.261374,44.588686), Pair(-123.26105,44.588653), Pair(-123.260457,44.588651), Pair(-123.260411,44.588652), Pair(-123.260135,44.588662), Pair(-123.259583,44.588681), Pair(-123.25945,44.588685), Pair(-123.258818,44.588707), Pair(-123.257941,44.588738), Pair(-123.256755,44.588779), Pair(-123.255914,44.588808), Pair(-123.255797,44.588812), Pair(-123.254898,44.588834), Pair(-123.254703,44.589146), Pair(-123.254595,44.589345), Pair(-123.254488,44.589537), Pair(-123.254401,44.589688), Pair(-123.254311,44.589849), Pair(-123.254147,44.59014), Pair(-123.254037,44.590337), Pair(-123.254009,44.590388), Pair(-123.25381,44.590744), Pair(-123.253571,44.591173), Pair(-123.253454,44.591384), Pair(-123.253319,44.59163), Pair(-123.253276,44.591707), Pair(-123.253078,44.592067), Pair(-123.253024,44.592164), Pair(-123.252872,44.592439), Pair(-123.252804,44.592561), Pair(-123.253056,44.592627), Pair(-123.25315,44.592658), Pair(-123.253354,44.59266), Pair(-123.253434,44.592658)
)

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
    var mapView: MyGLSurfaceView? = null
    val themeViewModel = ThemeViewModel()
    private var osmMapView: MapView? = null
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

                        // Or use your custom GL map
                        // MapView(
                        //     modifier = Modifier
                        //         .fillMaxWidth()
                        //         .weight(1f),
                        //     coordinates = STARBUCKS_COORDS
                        // )

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
        // osmMapView = map
        currentLocation?.let { location ->
            val geoPoint = GeoPoint(location.latitude, location.longitude)
            //map.controller.setCenter(geoPoint)

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
                val currentCoords = floatArrayOf(
                    location.latitude.toFloat(),
                    location.longitude.toFloat(),
                    0.0f
                )
                mapView?.updateLocation(currentCoords)

                // Update OSM map if available
                osmMapView?.let { mapView ->
                    val geoPoint = GeoPoint(location.latitude, location.longitude)
                }
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

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
fun <T: Number> MapView(modifier: Modifier = Modifier, coordinates: Array<Pair<T, T>>) {
    AndroidView(
        modifier = modifier.clipToBounds(),
        factory = { context ->
            // Creates view
            MyGLSurfaceView(context).apply {
                updateRoute(coordinates)
                (context as MainActivity).mapView = this
            }
        },
        update = { view ->
            (view as MyGLSurfaceView).updateRoute(coordinates) // TODO is this necessary?
        }
    )
}

@Composable
fun MapApp() {
    val activity = LocalContext.current as MainActivity
    var showLocationDialog by remember { mutableStateOf(false) }
    var coordinates by remember { mutableStateOf(STARBUCKS_COORDS) }
    val scope = rememberCoroutineScope()
    val backgroundColor = MaterialTheme.colorScheme.background

    when (activity.currentScreen.value) {
        Screen.Main -> {
            Column(Modifier.fillMaxSize()) {
                MapView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    coordinates = coordinates
                )
                SoftKeyNavBar(
                    onSettingsClick = { activity.currentScreen.value = Screen.Settings }
                ) { showLocationDialog = true }
            }

            LocationInputDialog(
                showDialog = showLocationDialog,
                onDismiss = { showLocationDialog = false },
                onConfirm = { query ->
                    scope.launch {
                        activity.getRouteFromApi(query)?.let { newCoords ->
                            coordinates = newCoords
                        }
                    }
                }
            )
        }
        Screen.Settings -> {
            SettingsScreen(onBackClick = { activity.currentScreen.value = Screen.Main })
        }
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
                println("Key pressed: ${keyEvent.nativeKeyEvent.keyCode}")
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




// https://developer.android.com/develop/ui/views/graphics/opengl/environment
class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: MyGLRenderer
    init {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)
        renderer = MyGLRenderer()
        // Render the view only when there is a change in the drawing data

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)
        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }
    // currently takes coords in range 0-1
    fun <T: Number> updateRoute(coords: Array<Pair<T, T>>) {
        renderer.setRouteCoordinates(coords)
    }
    // takes coords in lat/lon
    fun updateLocation(coords: FloatArray) {
        renderer.setLocation(coords)
    }
}