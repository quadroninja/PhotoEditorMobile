package com.hitsproject.photoeditor

import android.graphics.Bitmap
import java.lang.Math.pow
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.log
import kotlin.math.pow


class Processing {
    fun applyHDRToImage(bitmap: Bitmap): Bitmap {
        // Получение размеров изображения
        val width = bitmap.width
        val height = bitmap.height

        // Создание массива пикселей
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Применение алгоритма Reinhard
        val outputPixels = applyModifiedReinhardAlgorithm(pixels, width, height)

        // Создание нового Bitmap с преобразованными пикселями
        val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        outputBitmap.setPixels(outputPixels, 0, width, 0, 0, width, height)

        return outputBitmap
    }

    private fun applyModifiedReinhardAlgorithm(pixels: IntArray, width: Int, height: Int): IntArray {
        val outputPixels = IntArray(pixels.size)

        // Вычисление логарифмической яркости
        val logLuminance = computeLogLuminance(pixels, width, height)

        // Вычисление среднего логарифмического значения яркости
        val avgLogLuminance = logLuminance.average()

        // Вычисление максимальной и минимальной яркости
        val maxLuminance = logLuminance.maxOrNull() ?: 0.0
        val minLuminance = logLuminance.minOrNull() ?: 0.0

        // Применение модифицированного тонового отображения Reinhard
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val red = (pixel shr 16) and 0xFF
            val green = (pixel shr 8) and 0xFF
            val blue = pixel and 0xFF

            val luminance = (0.2126 * red + 0.7152 * green + 0.0722 * blue) / 255.0
            val newLuminance = applyModifiedReinhardToneMapping(luminance, avgLogLuminance, maxLuminance, minLuminance)
            val newRed = (newLuminance * red).toInt().coerceIn(0, 255)
            val newGreen = (newLuminance * green).toInt().coerceIn(0, 255)
            val newBlue = (newLuminance * blue).toInt().coerceIn(0, 255)

            val newPixel = (newRed shl 16) or (newGreen shl 8) or newBlue
            outputPixels[i] = newPixel
        }

        return outputPixels
    }

    private fun computeLogLuminance(pixels: IntArray, width: Int, height: Int): DoubleArray {
        val logLuminance = DoubleArray(pixels.size)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val red = (pixel shr 16) and 0xFF
            val green = (pixel shr 8) and 0xFF
            val blue = pixel and 0xFF

            val luminance = 0.2126 * red + 0.7152 * green + 0.0722 * blue
            logLuminance[i] = log(luminance / 255.0 + 1e-8, 2.71828)
        }

        return logLuminance
    }

    private fun applyModifiedReinhardToneMapping(luminance: Double, avgLogLuminance: Double, maxLuminance: Double, minLuminance: Double): Double {
        val lWhite = 1.0
        val luminanceScaled = (luminance - minLuminance) / (maxLuminance - minLuminance)
        return luminanceScaled / (1.0 + luminanceScaled) * (1.0 + luminanceScaled / (lWhite * lWhite))
    }
}