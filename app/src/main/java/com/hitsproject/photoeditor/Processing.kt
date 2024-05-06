package com.hitsproject.photoeditor

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Processing {
    suspend fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap = withContext(Dispatchers.Default) {
        val width = bitmap.width
        val height = bitmap.height
        val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val matrix = FloatArray(9)
        val sinDegrees = sin(degrees * Math.PI / 180.0)
        val cosDegrees = cos(degrees * Math.PI / 180.0)
        val scaleX = 1f / cosDegrees
        val scaleY = 1f / cosDegrees
        val xCenter = width / 2f
        val yCenter = height / 2f
        for (x in 0 until width) {
            for (y in 0 until height) {
                val xNew = (x - xCenter) * scaleX * cosDegrees - (y - yCenter) * scaleY * sinDegrees + xCenter
                val yNew = (x - xCenter) * scaleX * sinDegrees + (y - yCenter) * scaleY * cosDegrees + yCenter
                if (xNew >= 0 && xNew < width && yNew >= 0 && yNew < height) {
                    val color = bitmap.getPixel(x.toInt(), y.toInt())
                    newBitmap.setPixel(xNew.toInt(), yNew.toInt(), color)
                }
            }
        }
        newBitmap
    }
}