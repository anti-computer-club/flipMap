package com.example.flipmap

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
// import android.provider.ContactsContract.CommonDataKinds.Website.URL
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
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

val STARBUCKS_ROUTE = floatArrayOf( -0.9967f, -1.0f, 0.0f, -1.0f, -0.98870f, 0.0f, -0.96651f, -0.98617f, 0.0f, -0.93829f, -0.98415f, 0.0f, -0.91068f, -0.98226f, 0.0f, -0.91318f, -0.97525f, 0.0f, -0.91469f, -0.96907f, 0.0f, -0.91567f, -0.95606f, 0.0f, -0.91496f, -0.93883f, 0.0f, -0.91380f, -0.92185f, 0.0f, -0.90935f, -0.91440f, 0.0f, -0.90116f, -0.90525f, 0.0f, -0.89831f, -0.89761f, 0.0f, -0.89768f, -0.85103f, 0.0f, -0.89715f, -0.80167f, 0.0f, -0.89679f, -0.77888f, 0.0f, -0.89332f, -0.77288f, 0.0f, -0.88887f, -0.76682f, 0.0f, -0.87240f, -0.75628f, 0.0f, -0.80855f, -0.70421f, 0.0f, -0.77463f, -0.67056f, 0.0f, -0.74542f, -0.63459f, 0.0f, -0.73331f, -0.61824f, 0.0f, -0.72556f, -0.60624f, 0.0f, -0.71016f, -0.58283f, 0.0f, -0.70651f, -0.57765f, 0.0f, -0.70419f, -0.57424f, 0.0f, -0.69814f, -0.56540f, 0.0f, -0.69716f, -0.56389f, 0.0f, -0.68879f, -0.55158f, 0.0f, -0.68799f, -0.55038f, 0.0f, -0.68514f, -0.54628f, 0.0f, -0.68042f, -0.53934f, 0.0f, -0.66457f, -0.51503f, 0.0f, -0.66119f, -0.51011f, 0.0f, -0.65611f, -0.50285f, 0.0f, -0.64756f, -0.49061f, 0.0f, -0.64062f, -0.48051f, 0.0f, -0.62147f, -0.48391f, 0.0f, -0.58461f, -0.49067f, 0.0f, -0.55567f, -0.49591f, 0.0f, -0.51569f, -0.50317f, 0.0f, -0.50732f, -0.50468f, 0.0f, -0.50304f, -0.50550f, 0.0f, -0.47517f, -0.51055f, 0.0f, -0.43217f, -0.51832f, 0.0f, -0.38943f, -0.52602f, 0.0f, -0.34526f, -0.53422f, 0.0f, -0.29887f, -0.54300f, 0.0f, -0.26744f, -0.54874f, 0.0f, -0.24678f, -0.55259f, 0.0f, -0.20181f, -0.56111f, 0.0f, -0.18837f, -0.54224f, 0.0f, -0.17492f, -0.52343f, 0.0f, -0.16887f, -0.51497f, 0.0f, -0.15738f, -0.49875f, 0.0f, -0.13636f, -0.46757f, 0.0f, -0.11526f, -0.43620f, 0.0f, -0.10475f, -0.42117f, 0.0f, -0.09362f, -0.40527f, 0.0f, -0.07849f, -0.38374f, 0.0f, -0.07564f, -0.37970f, 0.0f, -0.05507f, -0.35370f, 0.0f, -0.01242f, -0.29985f, 0.0f, -0.00645f, -0.29228f, 0.0f, 0.010818f, -0.27031f, 0.0f, 0.031387f, -0.24424f, 0.0f, 0.057833f, -0.21060f, 0.0f, 0.060326f, -0.20706f, 0.0f, 0.077512f, -0.18504f, 0.0f, 0.083745f, -0.17715f, 0.0f, 0.104492f, -0.15057f, 0.0f, 0.117314f, -0.13422f, 0.0f, 0.138328f, -0.10727f, 0.0f, 0.157383f, -0.08290f, 0.0f, 0.177596f, -0.05740f, 0.0f, 0.200837f, -0.02894f, 0.0f, 0.215262f, -0.00931f, 0.0f, 0.243488f, 0.026795f, 0.0f, 0.246516f, 0.030771f, 0.0f, 0.271626f, 0.062647f, 0.0f, 0.280263f, 0.073504f, 0.0f, 0.293441f, 0.090484f, 0.0f, 0.314099f, 0.116869f, 0.0f, 0.323360f, 0.128735f, 0.0f, 0.325408f, 0.131323f, 0.0f, 0.331908f, 0.139719f, 0.0f, 0.343217f, 0.154173f, 0.0f, 0.362272f, 0.178538f, 0.0f, 0.379546f, 0.200694f, 0.0f, 0.387115f, 0.210288f, 0.0f, 0.394060f, 0.219188f, 0.0f, 0.400828f, 0.227962f, 0.0f, 0.417390f, 0.249108f, 0.0f, 0.421397f, 0.254284f, 0.0f, 0.425493f, 0.259460f, 0.0f, 0.436000f, 0.273031f, 0.0f, 0.445438f, 0.285087f, 0.0f, 0.456658f, 0.299479f, 0.0f, 0.460219f, 0.304023f, 0.0f, 0.482302f, 0.332176f, 0.0f, 0.488001f, 0.339498f, 0.0f, 0.494412f, 0.347767f, 0.0f, 0.508926f, 0.366135f, 0.0f, 0.517563f, 0.377244f, 0.0f, 0.531899f, 0.394224f, 0.0f, 0.558078f, 0.431339f, 0.0f, 0.580250f, 0.460312f, 0.0f, 0.586483f, 0.468202f, 0.0f, 0.619696f, 0.511062f, 0.0f, 0.628422f, 0.521855f, 0.0f, 0.653799f, 0.553353f, 0.0f, 0.672766f, 0.578475f, 0.0f, 0.685143f, 0.594066f, 0.0f, 0.708472f, 0.624112f, 0.0f, 0.720315f, 0.639198f, 0.0f, 0.727082f, 0.647845f, 0.0f, 0.738123f, 0.662048f, 0.0f, 0.747117f, 0.673536f, 0.0f, 0.752548f, 0.680479f, 0.0f, 0.754329f, 0.682752f, 0.0f, 0.762343f, 0.692914f, 0.0f, 0.770802f, 0.703645f, 0.0f, 0.771960f, 0.705160f, 0.0f, 0.787988f, 0.725548f, 0.0f, 0.794666f, 0.734006f, 0.0f, 0.813543f, 0.758497f, 0.0f, 0.830906f, 0.778191f, 0.0f, 0.840523f, 0.790752f, 0.0f, 0.850051f, 0.802872f, 0.0f, 0.857797f, 0.812403f, 0.0f, 0.865811f, 0.822565f, 0.0f, 0.880414f, 0.840934f, 0.0f, 0.890209f, 0.853369f, 0.0f, 0.892702f, 0.856588f, 0.0f, 0.910422f, 0.879059f, 0.0f, 0.931703f, 0.906138f, 0.0f, 0.942121f, 0.919457f, 0.0f, 0.954142f, 0.934985f, 0.0f, 0.957971f, 0.939845f, 0.0f, 0.975602f, 0.962569f, 0.0f, 0.980410f, 0.968691f, 0.0f, 0.993945f, 0.986050f, 0.0f, 1.0f, 0.993750f, 0.0f, 0.977561f, 0.997916f, 0.0f, 0.969191f, 0.999873f, 0.0f, 0.951026f, 1.0f, 0.0f, 0.943902f, 0.999873f, 0.0f,)

// adb shell pm reset-permissions flipmap
class MainActivity : ComponentActivity() {
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
fun MapView(modifier: Modifier = Modifier, coordinates: FloatArray) {
    AndroidView(
        modifier = modifier.clipToBounds(),
        factory = { context ->
            // Creates view
            MyGLSurfaceView(context).apply {
                updateCoordinates(coordinates)
            }
        },
        update = { view ->
            (view as MyGLSurfaceView).updateCoordinates(coordinates)
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
    Column(Modifier.fillMaxSize()) {
        MapView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
                coordinates = STARBUCKS_ROUTE
        )
        LocationDisplay()
        SoftKeyNavBar()
    }
}
@Composable
fun SoftKeyNavBar() {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        // State<List<T>>
        var menu by remember {
            mutableStateOf(2)
        }
        Text("Edit Route")
        Text("Go")
        Text("Settings")
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
    fun updateCoordinates(coords: FloatArray) {
        renderer.setRouteCoordinates(coords)
    }
}