package com.example.flipmap

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
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
import kotlinx.coroutines.selects.select

/**
[[-123.275228, 44.560975], [-123.275265, 44.561154], [-123.274889, 44.561194], [-123.274572, 44.561226], [-123.274262, 44.561256], [-123.27429, 44.561367], [-123.274307, 44.561465], [-123.274318, 44.561671], [-123.27431, 44.561944], [-123.274297, 44.562213], [-123.274247, 44.562331], [-123.274155, 44.562476], [-123.274123, 44.562597], [-123.273852, 44.562595], [-123.273834, 44.562595], [-123.273621, 44.562594], [-123.273606, 44.562594], [-123.27339, 44.562592], [-123.27336, 44.562592], [-123.273131, 44.562591], [-123.273103, 44.562591], [-123.27289, 44.562589], [-123.272637, 44.562588], [-123.272139, 44.562585], [-123.27189, 44.562584], [-123.271639, 44.562583], [-123.27134, 44.562582], [-123.271144, 44.562567], [-123.270962, 44.562524], [-123.27072, 44.562469], [-123.270313, 44.562375], [-123.269786, 44.562222], [-123.269271, 44.562072], [-123.26875, 44.561928], [-123.268228, 44.561797], [-123.267927, 44.561715], [-123.267682, 44.561648], [-123.267196, 44.561511], [-123.266725, 44.56138], [-123.266664, 44.561364], [-123.266314, 44.561274], [-123.266201, 44.561244], [-123.266073, 44.561213], [-123.265596, 44.561093], [-123.265078, 44.560965], [-123.264529, 44.560821], [-123.264352, 44.560775], [-123.263997, 44.560682], [-123.263468, 44.560549], [-123.262929, 44.560412], [-123.262711, 44.560846], [-123.262672, 44.560924], [-123.262427, 44.561397], [-123.262272, 44.561702], [-123.262211, 44.561823], [-123.262149, 44.561945], [-123.261921, 44.562394], [-123.261711, 44.562804], [-123.261418, 44.563376], [-123.261015, 44.564169], [-123.260913, 44.564371], [-123.260815, 44.564561], [-123.260749, 44.56469], [-123.260664, 44.564854], [-123.260586, 44.565017], [-123.260508, 44.565163], [-123.260408, 44.565349], [-123.260237, 44.565688], [-123.260169, 44.565822], [-123.260131, 44.565897], [-123.260067, 44.566023], [-123.259911, 44.566332], [-123.259825, 44.566507], [-123.25981, 44.566535], [-123.2597, 44.566751], [-123.259613, 44.566913], [-123.25953, 44.567071], [-123.259411, 44.567301], [-123.259316, 44.567492], [-123.259299, 44.567526], [-123.259253, 44.567617], [-123.259185, 44.567753], [-123.25911, 44.567904], [-123.258917, 44.568292], [-123.258716, 44.568684], [-123.258588, 44.568934], [-123.258413, 44.569274], [-123.258145, 44.569796], [-123.257878, 44.570318], [-123.257837, 44.570399], [-123.257794, 44.570483], [-123.257493, 44.57108], [-123.257138, 44.57187], [-123.256816, 44.572832], [-123.256674, 44.573488], [-123.256635, 44.573806], [-123.256617, 44.574454], [-123.256684, 44.57499], [-123.256762, 44.575314], [-123.2569, 44.575727], [-123.257043, 44.576043], [-123.257258, 44.576448], [-123.257488, 44.576833], [-123.257727, 44.577068], [-123.258186, 44.577845], [-123.258299, 44.578082], [-123.258413, 44.57842], [-123.258486, 44.578777], [-123.258507, 44.579034], [-123.25847, 44.579512], [-123.25843, 44.579752], [-123.258311, 44.58016], [-123.258073, 44.580656], [-123.257736, 44.581175], [-123.255782, 44.58383], [-123.255231, 44.58466], [-123.254787, 44.585445], [-123.254577, 44.585861], [-123.253965, 44.587219], [-123.253596, 44.588036], [-123.253234, 44.588835], [-123.252973, 44.589457], [-123.251509, 44.592651], [-123.251084, 44.593621], [-123.250065, 44.595867], [-123.249645, 44.596845], [-123.249258, 44.597744], [-123.248819, 44.598787], [-123.248308, 44.600168], [-123.247628, 44.60208], [-123.246773, 44.604926], [-123.246354, 44.606392], [-123.245941, 44.607993], [-123.24537, 44.61084], [-123.244705, 44.613905], [-123.244611, 44.614325], [-123.244046, 44.616914], [-123.243545, 44.619297], [-123.243404, 44.619882], [-123.243175, 44.620938], [-123.243022, 44.621644], [-123.24289, 44.622255], [-123.242853, 44.622423], [-123.242758, 44.622863], [-123.242747, 44.622916], [-123.242551, 44.623855], [-123.242278, 44.62513], [-123.241899, 44.626861], [-123.241688, 44.627801], [-123.241435, 44.628997], [-123.241368, 44.629284], [-123.24115, 44.630314], [-123.240844, 44.631742], [-123.24066, 44.632981], [-123.240455, 44.636674], [-123.240292, 44.637495], [-123.240228, 44.637699], [-123.240207, 44.637767], [-123.24016, 44.637882], [-123.239954, 44.638283], [-123.239654, 44.638759], [-123.239368, 44.63912], [-123.238998, 44.639518], [-123.23877, 44.63974], [-123.235106, 44.643052], [-123.233736, 44.644354], [-123.23285, 44.645176], [-123.229648, 44.648067], [-123.228183, 44.649365], [-123.224358, 44.652831], [-123.223923, 44.65327], [-123.223559, 44.653668], [-123.223175, 44.654127], [-123.222874, 44.654537], [-123.222514, 44.655094], [-123.222233, 44.655591], [-123.222036, 44.656014], [-123.221828, 44.656531], [-123.221641, 44.657093], [-123.221471, 44.657818], [-123.221419, 44.65813], [-123.221366, 44.658644], [-123.221348, 44.659314], [-123.221419, 44.660258], [-123.221538, 44.66107], [-123.221703, 44.661847], [-123.222197, 44.665289], [-123.222547, 44.667488], [-123.222779, 44.668745], [-123.22296, 44.669956], [-123.222998, 44.67039], [-123.22299, 44.671197], [-123.222889, 44.671886], [-123.222107, 44.671935], [-123.221632, 44.671968], [-123.220905, 44.672018], [-123.220728, 44.672029], [-123.218612, 44.672169], [-123.218424, 44.672181], [-123.218433, 44.672258]]
 **/

// adb shell pm reset-permissions flipmap

enum class Screen {
    Main,
    Settings
}
const val KEY_APPSELECT = 580

class MainActivity : ComponentActivity() {
    var currentScreen = mutableStateOf(Screen.Main)
    private lateinit var locationManager: LocationManager
    private var currentLocation: Location? = null
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
            FlipMapTheme {
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
                println("location updated: $location")
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
fun MapView(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.clipToBounds(),
        factory = { context ->
            // Creates view
            MyGLSurfaceView(context).apply { }
        },
        update = { view ->
            // View's been inflated or state read in this block has been updated
            // Add logic here if necessary
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

    when (activity.currentScreen.value) {
        Screen.Main -> {
            Column(Modifier.fillMaxSize()) {
                MapView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
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

@Composable
fun SoftKeyNavBar(onSettingsClick: () -> Unit) {
    var selectedIndex by remember { mutableStateOf(-1) }

    Row(
        Modifier
            .fillMaxWidth()
            .focusable(true)
            .onKeyEvent { keyEvent ->
                when (keyEvent.nativeKeyEvent.keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP,
                    KeyEvent.KEYCODE_DPAD_DOWN,
                    KeyEvent.KEYCODE_DPAD_LEFT,
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                            if (selectedIndex == -1) {
                                selectedIndex = 0
                            } else {
                                when (keyEvent.nativeKeyEvent.keyCode) {
                                    KeyEvent.KEYCODE_DPAD_LEFT -> selectedIndex = (selectedIndex - 1).coerceIn(0, 2)
                                    KeyEvent.KEYCODE_DPAD_RIGHT -> selectedIndex = (selectedIndex + 1).coerceIn(0, 2)
                                }
                            }
                        }
                        true
                    }
                    KeyEvent.KEYCODE_ENTER -> {
                        if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                            if (selectedIndex == 2) {
                                onSettingsClick()
                            }
                        }
                        true
                    }
                    KeyEvent.KEYCODE_BACK -> {
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
}