package com.example.flipmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import androidx.compose.ui.unit.IntSize
import kotlin.math.max

// Render a route
fun setRouteCoordinates(map: MapView, geoPoints: List<GeoPoint>) {
    val line = Polyline()
    line.setPoints(geoPoints)
    map.overlays.clear()
    map.overlays.add(line)
    map.invalidate()
}

@Deprecated("deprecated in favor of drawNumberedMapPoints")
fun drawMapPoints(map: MapView, geoPoints: List<GeoPoint>) {
    map.overlays.clear() // optional, remove previous markers

    geoPoints.forEachIndexed { index, point ->
        val marker = Marker(map).apply {
            position = point
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = ContextCompat.getDrawable(map.context, org.osmdroid.library.R.drawable.marker_default) // default marker
            title = "${index + 1}" // will show on tap
            snippet = "Point ${index + 1}"
            setOnMarkerClickListener { m, _ ->
                m.showInfoWindow()
                true
            }
        }
        map.overlays.add(marker)
    }

    map.invalidate()
    Log.d("paul", "drew ${geoPoints.size} points")
}

// Draw some numbered points
fun drawNumberedMapPoints(map: MapView, geoPoints: List<GeoPoint>) {
    map.overlays.clear()

    geoPoints.forEachIndexed { index, point ->
        val marker = Marker(map).apply {
            position = point
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = createNumberedIcon(map.context, index + 1) // custom drawable with number
            title = "Point ${index + 1}" // optional
        }
        map.overlays.add(marker)
    }
    map.invalidate()
}

// create bitmap with numbers inside
fun createNumberedIcon(context: Context, number: Int): Drawable {
    val size = 30
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // draw circle background
    val paintCircle = Paint().apply {
        color = Color.RED
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paintCircle)

    // draw number text
    val paintText = Paint().apply {
        color = Color.WHITE
        textSize = 32f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val textY = (size / 2f) - ((paintText.descent() + paintText.ascent()) / 2)
    canvas.drawText(number.toString(), size / 2f, textY, paintText)

    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Mostly just wraps zoomToBoundingBox
 * @param visibleSize The dimensions of the map space
 **/
fun zoomToBoundingBox(map: MapView, geoPoints: List<GeoPoint>, visibleSize: IntSize, paddingFraction: Float = 0.05f) {
    if (geoPoints.isEmpty()) return

    if (geoPoints.size == 1) {
        map.controller.setZoom(15.0) // zoomTo not working
        map.controller.setCenter(geoPoints[0])
        return
    }

    val boundingBox = BoundingBox.fromGeoPoints(geoPoints)

    val horizontalPadding = (visibleSize.width * paddingFraction).toInt()
    val verticalPadding = ((visibleSize.height) * paddingFraction).toInt()+58 // TODO couldn't get size to be read - adding manually

    // osmdroid wants uniform padding, so pick max
    val uniformPadding = max(horizontalPadding, verticalPadding)

    map.zoomToBoundingBox(boundingBox, true, uniformPadding)
    map.controller.setCenter(boundingBox.centerWithDateLine)
}