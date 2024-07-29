package com.example.flipmap

import android.Manifest
import android.R
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.flipmap.ui.theme.FlipMapTheme


// adb shell pm reset-permissions flipmap
class MainActivity : ComponentActivity() {
    private lateinit var locationManager: LocationManager
    private var currentLocation: Location? = null
    // Public bc I wanna use it from compose??
    // private lateinit var gLView: GLSurfaceView

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // gLView =
        // setContentView(gLView)

        // Hide bottom system navbar
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                locationManager =
                    applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_FINE_LOCATION) -> {
            }
            else -> {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    0x10ca710)
            }
        }
        startLocationUpdates()

        setContent {
            FlipMapTheme {
                MapApp()
            }
        }
    }
    fun getLastLocation() : String {
        val locationManager =
            applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        val location = if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return "not available?"
        }
        else {
            locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
        }
        return location.toString()
    }
    private fun updateLocation(l : Location?) {
        println("Updated Location")
        currentLocation = l
    }
    fun getCurrentLocation(): Location? { return currentLocation}
    @RequiresApi(Build.VERSION_CODES.S)
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        // I'm on API 29 so we gotta do this
        locationManager.requestLocationUpdates(
            LocationManager.FUSED_PROVIDER,
            10000L,
            10f,
            ::updateLocation
        )
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
fun LocationDisplay(modifier: Modifier = Modifier
    .fillMaxSize()
    .wrapContentSize(Alignment.Center)) {
    val activity = LocalContext.current as MainActivity
    var location : Location? by remember { mutableStateOf(activity.getCurrentLocation()) }
    var address : String?  by remember {
        mutableStateOf(location?.toString())
}
    Column (
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "yummy"
        )
        Text(
            "test"
        )
        Button(onClick = {
                location = activity.getCurrentLocation()
                address = location.toString()
                println(location)
            }) {
            Text("Get last location")
        }
    }
}

@Composable
fun MapApp() {
    Column(Modifier.fillMaxSize()) {
        MapView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        Button(onClick = { }) {
            Text("Test button")
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
        // renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        // Render the view only when there is a change in the drawing data

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)
        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }
}