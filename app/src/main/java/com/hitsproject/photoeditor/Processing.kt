package com.hitsproject.photoeditor

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.exp
import kotlin.math.pow

class Processing {
    fun applyBlackAndWhiteFilter(image: Bitmap): Bitmap {
        val width = image.width
        val height = image.height
        val pixels = IntArray(width * height)

        // Получаем пиксели изображения в виде массива
        image.getPixels(pixels, 0, width, 0, 0, width, height)

        // Применяем фильтр к пикселям
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)

            // Вычисляем среднее значение RGB для получения оттенка серого
            val average = (red + green + blue) / 3
            pixels[i] = Color.rgb(average, average, average)
        }

        // Создаем новое отфильтрованное изображение
        val filteredBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        filteredBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        return filteredBitmap
    }
}