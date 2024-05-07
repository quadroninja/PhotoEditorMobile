package com.hitsproject.photoeditor

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

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

    fun applySepiaFilter(image: Bitmap): Bitmap {
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

            // Вычисляем новые значения RGB для эффекта сепии
            val redSepiaValue = (red * 0.393 + green * 0.769 + blue * 0.189).toInt()
            val greenSepiaValue = (red * 0.349 + green * 0.686 + blue * 0.168).toInt()
            val blueSepiaValue = (red * 0.272 + green * 0.534 + blue * 0.131).toInt()

            // Ограничиваем значения RGB в диапазоне 0-255
            pixels[i] = Color.rgb(
                min(redSepiaValue, 255),
                min(greenSepiaValue, 255),
                min(blueSepiaValue, 255)
            )
        }

        // Создаем новое отфильтрованное изображение
        val filteredBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        filteredBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        return filteredBitmap
    }

    private fun min(value: Int, max: Int): Int {
        return if (value < 0) 0 else if (value > max) max else value
    }
}