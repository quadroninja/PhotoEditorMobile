package com.hitsproject.photoeditor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class Processing {
    fun applyBlackAndWhiteFilter(image: Bitmap): Bitmap {
        val pixels = IntArray(image.width * image.height)
        image.getPixels(pixels, 0, image.width, 0, 0, image.width, image.height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val average = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
            pixels[i] = Color.rgb(average, average, average)
        }

        return Bitmap.createBitmap(pixels, image.width, image.height, Bitmap.Config.ARGB_8888)
    }

    fun applySepiaFilter(image: Bitmap): Bitmap {
        val pixels = IntArray(image.width * image.height)
        image.getPixels(pixels, 0, image.width, 0, 0, image.width, image.height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)

            val redSepiaValue = (red * 0.393 + green * 0.769 + blue * 0.189).toInt()
            val greenSepiaValue = (red * 0.349 + green * 0.686 + blue * 0.168).toInt()
            val blueSepiaValue = (red * 0.272 + green * 0.534 + blue * 0.131).toInt()

            pixels[i] = Color.rgb(
                minOf(redSepiaValue, 255),
                minOf(greenSepiaValue, 255),
                minOf(blueSepiaValue, 255)
            )
        }

        return Bitmap.createBitmap(pixels, image.width, image.height, Bitmap.Config.ARGB_8888)
    }

    fun applyNegativeFilter(bitmap: Bitmap): Bitmap {
        val editedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)

        val colorMatrix = ColorMatrix(
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            )
        )

        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)

        val canvas = Canvas(editedBitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return editedBitmap
    }
}