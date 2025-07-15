package com.example.flipmap

import android.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView


@Composable
fun OpenStreetMapView(
    modifier: Modifier = Modifier,
    coordinates: GeoPoint,
    onMapReady: (MapView) -> Unit = {},
    onSizeChanged: (IntSize) -> Unit = {}
) {
    val TONER: OnlineTileSourceBase = XYTileSource(
        "Stamen Toner",
        0, 20, 256, ".png", arrayOf(
            "https://tile.anticomputer.club/tiles/stamen_toner/",
        ), "© OpenStreetMap contributors",
        TileSourcePolicy(
            2,
            (TileSourcePolicy.FLAG_NO_BULK
                    or TileSourcePolicy.FLAG_NO_PREVENTIVE
                    or TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
                    or TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED)
        )
    )
    val context = LocalContext.current

    // configure osmdroid
    Configuration.getInstance()
        .load(context, getDefaultSharedPreferences(context))

    // create + remember MapView
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TONER) // TODO this can be added to settings
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