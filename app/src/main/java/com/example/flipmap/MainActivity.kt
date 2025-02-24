package com.example.flipmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Surface
import android.view.View
import android.view.WindowManager
// import android.provider.ContactsContract.CommonDataKinds.Website.URL
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import java.net.URL
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.InputStream
import java.io.OutputStream
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import com.example.flipmap.ui.theme.ThemeViewModel
import kotlinx.coroutines.selects.select
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth

val STARBUCKS_ROUTE = floatArrayOf( -0.9967f, -1.0f, 0.0f, -1.0f, -0.98870f, 0.0f, -0.96651f, -0.98617f, 0.0f, -0.93829f, -0.98415f, 0.0f, -0.91068f, -0.98226f, 0.0f, -0.91318f, -0.97525f, 0.0f, -0.91469f, -0.96907f, 0.0f, -0.91567f, -0.95606f, 0.0f, -0.91496f, -0.93883f, 0.0f, -0.91380f, -0.92185f, 0.0f, -0.90935f, -0.91440f, 0.0f, -0.90116f, -0.90525f, 0.0f, -0.89831f, -0.89761f, 0.0f, -0.89768f, -0.85103f, 0.0f, -0.89715f, -0.80167f, 0.0f, -0.89679f, -0.77888f, 0.0f, -0.89332f, -0.77288f, 0.0f, -0.88887f, -0.76682f, 0.0f, -0.87240f, -0.75628f, 0.0f, -0.80855f, -0.70421f, 0.0f, -0.77463f, -0.67056f, 0.0f, -0.74542f, -0.63459f, 0.0f, -0.73331f, -0.61824f, 0.0f, -0.72556f, -0.60624f, 0.0f, -0.71016f, -0.58283f, 0.0f, -0.70651f, -0.57765f, 0.0f, -0.70419f, -0.57424f, 0.0f, -0.69814f, -0.56540f, 0.0f, -0.69716f, -0.56389f, 0.0f, -0.68879f, -0.55158f, 0.0f, -0.68799f, -0.55038f, 0.0f, -0.68514f, -0.54628f, 0.0f, -0.68042f, -0.53934f, 0.0f, -0.66457f, -0.51503f, 0.0f, -0.66119f, -0.51011f, 0.0f, -0.65611f, -0.50285f, 0.0f, -0.64756f, -0.49061f, 0.0f, -0.64062f, -0.48051f, 0.0f, -0.62147f, -0.48391f, 0.0f, -0.58461f, -0.49067f, 0.0f, -0.55567f, -0.49591f, 0.0f, -0.51569f, -0.50317f, 0.0f, -0.50732f, -0.50468f, 0.0f, -0.50304f, -0.50550f, 0.0f, -0.47517f, -0.51055f, 0.0f, -0.43217f, -0.51832f, 0.0f, -0.38943f, -0.52602f, 0.0f, -0.34526f, -0.53422f, 0.0f, -0.29887f, -0.54300f, 0.0f, -0.26744f, -0.54874f, 0.0f, -0.24678f, -0.55259f, 0.0f, -0.20181f, -0.56111f, 0.0f, -0.18837f, -0.54224f, 0.0f, -0.17492f, -0.52343f, 0.0f, -0.16887f, -0.51497f, 0.0f, -0.15738f, -0.49875f, 0.0f, -0.13636f, -0.46757f, 0.0f, -0.11526f, -0.43620f, 0.0f, -0.10475f, -0.42117f, 0.0f, -0.09362f, -0.40527f, 0.0f, -0.07849f, -0.38374f, 0.0f, -0.07564f, -0.37970f, 0.0f, -0.05507f, -0.35370f, 0.0f, -0.01242f, -0.29985f, 0.0f, -0.00645f, -0.29228f, 0.0f, 0.010818f, -0.27031f, 0.0f, 0.031387f, -0.24424f, 0.0f, 0.057833f, -0.21060f, 0.0f, 0.060326f, -0.20706f, 0.0f, 0.077512f, -0.18504f, 0.0f, 0.083745f, -0.17715f, 0.0f, 0.104492f, -0.15057f, 0.0f, 0.117314f, -0.13422f, 0.0f, 0.138328f, -0.10727f, 0.0f, 0.157383f, -0.08290f, 0.0f, 0.177596f, -0.05740f, 0.0f, 0.200837f, -0.02894f, 0.0f, 0.215262f, -0.00931f, 0.0f, 0.243488f, 0.026795f, 0.0f, 0.246516f, 0.030771f, 0.0f, 0.271626f, 0.062647f, 0.0f, 0.280263f, 0.073504f, 0.0f, 0.293441f, 0.090484f, 0.0f, 0.314099f, 0.116869f, 0.0f, 0.323360f, 0.128735f, 0.0f, 0.325408f, 0.131323f, 0.0f, 0.331908f, 0.139719f, 0.0f, 0.343217f, 0.154173f, 0.0f, 0.362272f, 0.178538f, 0.0f, 0.379546f, 0.200694f, 0.0f, 0.387115f, 0.210288f, 0.0f, 0.394060f, 0.219188f, 0.0f, 0.400828f, 0.227962f, 0.0f, 0.417390f, 0.249108f, 0.0f, 0.421397f, 0.254284f, 0.0f, 0.425493f, 0.259460f, 0.0f, 0.436000f, 0.273031f, 0.0f, 0.445438f, 0.285087f, 0.0f, 0.456658f, 0.299479f, 0.0f, 0.460219f, 0.304023f, 0.0f, 0.482302f, 0.332176f, 0.0f, 0.488001f, 0.339498f, 0.0f, 0.494412f, 0.347767f, 0.0f, 0.508926f, 0.366135f, 0.0f, 0.517563f, 0.377244f, 0.0f, 0.531899f, 0.394224f, 0.0f, 0.558078f, 0.431339f, 0.0f, 0.580250f, 0.460312f, 0.0f, 0.586483f, 0.468202f, 0.0f, 0.619696f, 0.511062f, 0.0f, 0.628422f, 0.521855f, 0.0f, 0.653799f, 0.553353f, 0.0f, 0.672766f, 0.578475f, 0.0f, 0.685143f, 0.594066f, 0.0f, 0.708472f, 0.624112f, 0.0f, 0.720315f, 0.639198f, 0.0f, 0.727082f, 0.647845f, 0.0f, 0.738123f, 0.662048f, 0.0f, 0.747117f, 0.673536f, 0.0f, 0.752548f, 0.680479f, 0.0f, 0.754329f, 0.682752f, 0.0f, 0.762343f, 0.692914f, 0.0f, 0.770802f, 0.703645f, 0.0f, 0.771960f, 0.705160f, 0.0f, 0.787988f, 0.725548f, 0.0f, 0.794666f, 0.734006f, 0.0f, 0.813543f, 0.758497f, 0.0f, 0.830906f, 0.778191f, 0.0f, 0.840523f, 0.790752f, 0.0f, 0.850051f, 0.802872f, 0.0f, 0.857797f, 0.812403f, 0.0f, 0.865811f, 0.822565f, 0.0f, 0.880414f, 0.840934f, 0.0f, 0.890209f, 0.853369f, 0.0f, 0.892702f, 0.856588f, 0.0f, 0.910422f, 0.879059f, 0.0f, 0.931703f, 0.906138f, 0.0f, 0.942121f, 0.919457f, 0.0f, 0.954142f, 0.934985f, 0.0f, 0.957971f, 0.939845f, 0.0f, 0.975602f, 0.962569f, 0.0f, 0.980410f, 0.968691f, 0.0f, 0.993945f, 0.986050f, 0.0f, 1.0f, 0.993750f, 0.0f, 0.977561f, 0.997916f, 0.0f, 0.969191f, 0.999873f, 0.0f, 0.951026f, 1.0f, 0.0f, 0.943902f, 0.999873f, 0.0f,)
val STARBUCKS_COORDS : Array<Pair<Double, Double>> = arrayOf(
    Pair(-123.275228, 44.560975), Pair(-123.275265, 44.561154), Pair(-123.274889, 44.561194), Pair(-123.274572, 44.561226), Pair(-123.274262, 44.561256), Pair(-123.27429, 44.561367), Pair(-123.274307, 44.561465), Pair(-123.274318, 44.561671), Pair(-123.27431, 44.561944), Pair(-123.274297, 44.562213), Pair(-123.274247, 44.562331), Pair(-123.274155, 44.562476), Pair(-123.274123, 44.562597), Pair(-123.274116, 44.563335), Pair(-123.27411, 44.564117), Pair(-123.274106, 44.564478), Pair(-123.274067, 44.564573), Pair(-123.274017, 44.564669), Pair(-123.273832, 44.564836), Pair(-123.273115, 44.565661), Pair(-123.272734, 44.566194), Pair(-123.272406, 44.566764), Pair(-123.27227, 44.567023), Pair(-123.272183, 44.567213), Pair(-123.27201, 44.567584), Pair(-123.271969, 44.567666), Pair(-123.271943, 44.56772), Pair(-123.271875, 44.56786), Pair(-123.271864, 44.567884), Pair(-123.27177, 44.568079), Pair(-123.271761, 44.568098), Pair(-123.271729, 44.568163), Pair(-123.271676, 44.568273), Pair(-123.271498, 44.568658), Pair(-123.27146, 44.568736), Pair(-123.271403, 44.568851), Pair(-123.271307, 44.569045), Pair(-123.271229, 44.569205), Pair(-123.271014, 44.569151), Pair(-123.2706, 44.569044), Pair(-123.270275, 44.568961), Pair(-123.269826, 44.568846), Pair(-123.269732, 44.568822), Pair(-123.269684, 44.568809), Pair(-123.269371, 44.568729), Pair(-123.268888, 44.568606), Pair(-123.268408, 44.568484), Pair(-123.267912, 44.568354), Pair(-123.267391, 44.568215), Pair(-123.267038, 44.568124), Pair(-123.266806, 44.568063), Pair(-123.266301, 44.567928), Pair(-123.26615, 44.568227), Pair(-123.265999, 44.568525), Pair(-123.265931, 44.568659), Pair(-123.265802, 44.568916), Pair(-123.265566, 44.56941), Pair(-123.265329, 44.569907), Pair(-123.265211, 44.570145), Pair(-123.265086, 44.570397), Pair(-123.264916, 44.570738), Pair(-123.264884, 44.570802), Pair(-123.264653, 44.571214), Pair(-123.264174, 44.572067), Pair(-123.264107, 44.572187), Pair(-123.263913, 44.572535), Pair(-123.263682, 44.572948), Pair(-123.263385, 44.573481), Pair(-123.263357, 44.573537), Pair(-123.263164, 44.573886), Pair(-123.263094, 44.574011), Pair(-123.262861, 44.574432), Pair(-123.262717, 44.574691), Pair(-123.262481, 44.575118), Pair(-123.262267, 44.575504), Pair(-123.26204, 44.575908), Pair(-123.261779, 44.576359), Pair(-123.261617, 44.57667), Pair(-123.2613, 44.577242), Pair(-123.261266, 44.577305), Pair(-123.260984, 44.57781), Pair(-123.260887, 44.577982), Pair(-123.260739, 44.578251), Pair(-123.260507, 44.578669), Pair(-123.260403, 44.578857), Pair(-123.26038, 44.578898), Pair(-123.260307, 44.579031), Pair(-123.26018, 44.57926), Pair(-123.259966, 44.579646), Pair(-123.259772, 44.579997), Pair(-123.259687, 44.580149), Pair(-123.259609, 44.58029), Pair(-123.259533, 44.580429), Pair(-123.259347, 44.580764), Pair(-123.259302, 44.580846), Pair(-123.259256, 44.580928), Pair(-123.259138, 44.581143), Pair(-123.259032, 44.581334), Pair(-123.258906, 44.581562), Pair(-123.258866, 44.581634), Pair(-123.258618, 44.58208), Pair(-123.258554, 44.582196), Pair(-123.258482, 44.582327), Pair(-123.258319, 44.582618), Pair(-123.258222, 44.582794), Pair(-123.258061, 44.583063), Pair(-123.257767, 44.583651), Pair(-123.257518, 44.58411), Pair(-123.257448, 44.584235), Pair(-123.257075, 44.584914), Pair(-123.256977, 44.585085), Pair(-123.256692, 44.585584), Pair(-123.256479, 44.585982), Pair(-123.25634, 44.586229), Pair(-123.256078, 44.586705), Pair(-123.255945, 44.586944), Pair(-123.255869, 44.587081), Pair(-123.255745, 44.587306), Pair(-123.255644, 44.587488), Pair(-123.255583, 44.587598), Pair(-123.255563, 44.587634), Pair(-123.255473, 44.587795), Pair(-123.255378, 44.587965), Pair(-123.255365, 44.587989), Pair(-123.255185, 44.588312), Pair(-123.25511, 44.588446), Pair(-123.254898, 44.588834), Pair(-123.254703, 44.589146), Pair(-123.254595, 44.589345), Pair(-123.254488, 44.589537), Pair(-123.254401, 44.589688), Pair(-123.254311, 44.589849), Pair(-123.254147, 44.59014), Pair(-123.254037, 44.590337), Pair(-123.254009, 44.590388), Pair(-123.25381, 44.590744), Pair(-123.253571, 44.591173), Pair(-123.253454, 44.591384), Pair(-123.253319, 44.59163), Pair(-123.253276, 44.591707), Pair(-123.253078, 44.592067), Pair(-123.253024, 44.592164), Pair(-123.252872, 44.592439), Pair(-123.252804, 44.592561), Pair(-123.253056, 44.592627), Pair(-123.25315, 44.592658), Pair(-123.253354, 44.59266), Pair(-123.253434, 44.592658)
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

val KEYMAP = CatMappings

class MainActivity : ComponentActivity() {
    var currentScreen = mutableStateOf(Screen.Main)
    private lateinit var locationManager: LocationManager
    private var currentLocation: Location? = null
    var mapView: MyGLSurfaceView? = null
    val themeViewModel = ThemeViewModel()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // make top bar transparent
        enableEdgeToEdge()
        // Hide bottom system navbar
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // location permissions
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        requestLocationPermission()
        // startLocationUpdates()
        // Request a string response from the provided URL.

        setContent {
            FlipMapTheme(darkTheme = themeViewModel.isDarkMode.value) {
                MapApp()
            }
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
                // you could show a rationale dialog here
            }
            else -> {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0x10ca710
                )
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
                val currentCoords = floatArrayOf(location.latitude.toFloat(), location.longitude.toFloat(), 0.0f)
                mapView?.updateLocation(currentCoords)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        locationManager.requestLocationUpdates(
            LocationManager.FUSED_PROVIDER, 5000L, 10f, locationListener
        )
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
fun LocationDisplay(modifier: Modifier = Modifier.wrapContentSize(Alignment.Center)) {
    val activity = LocalContext.current as MainActivity
    var locationText by remember { mutableStateOf(activity.getLastLocation()) }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("current location:")
        Text(locationText)
        Button(onClick = {
            locationText = activity.getLastLocation()
        }) {
            Text("refresh location")
        }
    }
}

@Composable
fun MapApp() {
    val activity = LocalContext.current as MainActivity
    val backgroundColor = MaterialTheme.colorScheme.background

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        when (activity.currentScreen.value) {
            Screen.Main -> {
                Column(Modifier.fillMaxSize()) {
                    MapView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        coordinates = STARBUCKS_COORDS
                    )
                    LocationDisplay()
                    SoftKeyNavBar(
                        onSettingsClick = { activity.currentScreen.value = Screen.Settings }
                    )
                }
            }
            Screen.Settings -> {
                SettingsScreen(onBackClick = { activity.currentScreen.value = Screen.Main })
            }
        }
    }
}


@Composable
fun SoftKeyNavBar(onSettingsClick: () -> Unit) {
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
                    KEYMAP.KEY_RIGHT, -> {
                        if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                            if (selectedIndex == -1) {
                                selectedIndex = 0
                            } else {
                                when (keyEvent.nativeKeyEvent.keyCode) {
                                    KEYMAP.KEY_LEFT -> selectedIndex = (selectedIndex - 1).coerceIn(0, 2)
                                    KEYMAP.KEY_RIGHT -> selectedIndex = (selectedIndex + 1).coerceIn(0, 2)
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
                        if (text == "Settings") {
                            onSettingsClick()
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