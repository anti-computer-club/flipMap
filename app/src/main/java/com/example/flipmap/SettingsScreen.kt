package com.example.flipmap

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import java.util.logging.Logger

@Composable
fun SettingsScreen(mapView: MutableState<MapView?>) {
    val activity = LocalContext.current as MainActivity

    Column(Modifier
        // .fillMaxSize()
        .background(Color.White),
        ) {

        Text(
            text = "Settings",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontSize = 22.sp,
            modifier = Modifier.fillMaxWidth(1f),
        )

        Text(
            text = "Mapnik Style",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clickable {
                    Log.d("pref", "checking")
                    mapView.value?.let {
                        Log.d("pref", "set to mapnik")
                        it.setTileSource(TileSourceFactory.MAPNIK)
                        it.invalidate()
                     }
                 }
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .fillMaxWidth(1f),

            )
        Text(
            text = "Toner Style",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clickable {
                    Log.d("pref", "checking")
                    mapView.value?.let {
                        Log.d("pref", "set to toner")
                        it.setTileSource(CustomTileSources.TONER)
                        it.invalidate()
                    }
                }
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .fillMaxWidth(1f),
        )
    }
}