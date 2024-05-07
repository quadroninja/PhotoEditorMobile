package com.hitsproject.photoeditor

import android.graphics.Bitmap
import android.graphics.Color

class Processing {
    fun applyBlackAndWhiteFilter(image: Bitmap): Bitmap {
        val width = image.width
        val height = image.height
        val pixels = IntArray(width * height)

        image.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)

            val average = (red + green + blue) / 3
            pixels[i] = Color.rgb(average, average, average)
        }

        val filteredBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        filteredBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        return filteredBitmap
    }
}