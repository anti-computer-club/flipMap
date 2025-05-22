package com.example.flipmap

import android.preference.PreferenceManager
import android.preference.PreferenceManager.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.onSizeChanged

@Composable
fun OpenStreetMapView(
    modifier: Modifier = Modifier,
    coordinates: GeoPoint,
    onMapReady: (MapView) -> Unit = {},
    onSizeChanged: (IntSize) -> Unit = {}
) {
    val context = LocalContext.current

    // configure osmdroid
    Configuration.getInstance()
        .load(context, getDefaultSharedPreferences(context))

    // create + remember MapView
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK) // TODO this can be added to settings
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(coordinates)
        }
    }

    // setup lifecycle observer for mapView
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Most of the interesting stuff happens in the callback (from MainActivity)
    // because we don't want to pass a bunch of state here
    LaunchedEffect(Unit) {
        onMapReady(mapView)
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { size -> onSizeChanged(size) }
    )
}