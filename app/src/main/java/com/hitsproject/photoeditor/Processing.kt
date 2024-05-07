package com.hitsproject.photoeditor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

class Processing {
    fun resize(image: Bitmap, scaleCoefX: Double, scaleCoefY: Double) : Bitmap
    {
        val pixelArray = IntArray(image.height * image.width)
        val out = IntArray(image.height * image.width) {0xFF000000.toInt()}
        image.getPixels(pixelArray, 0, image.width, 0, 0, image.width, image.height)

        for (i in 0 until image.height)
        {
            for (j in 0 until image.width)
            {
                val index = i * image.width + j
                val v: Double = i * scaleCoefY
                val u: Double = j * scaleCoefX
                val y: Int = floor(v).toInt()
                val x: Int = floor(u).toInt()
                val uRatio: Double = u - x
                val vRatio: Double = v - y
                val uOpposite: Double = 1.0 - uRatio
                val vOpposite: Double = 1.0 - vRatio

                val ind00 = y * image.width + x
                val ind01 = y * image.width + (x + 1)
                val ind10 = (y + 1) * image.width + x
                val ind11 = (y + 1) * image.width + (x + 1)

                if (y >= 0 && y + 1 < image.height && x >= 0 && x + 1 < image.width)
                {
                    val result = Color.rgb(
                        ((Color.red(pixelArray[ind00]) * uOpposite + Color.red(pixelArray[ind01]) * uRatio) * vOpposite +
                                (Color.red(pixelArray[ind10]) * uOpposite + Color.red(pixelArray[ind11]) * uRatio) * vRatio).toInt(),
                        ((Color.green(pixelArray[ind00]) * uOpposite + Color.green(pixelArray[ind01]) * uRatio) * vOpposite +
                                (Color.green(pixelArray[ind10]) * uOpposite + Color.green(pixelArray[ind11]) * uRatio) * vRatio).toInt(),
                        ((Color.blue(pixelArray[ind00]) * uOpposite + Color.blue(pixelArray[ind01]) * uRatio) * vOpposite +
                                (Color.blue(pixelArray[ind10]) * uOpposite + Color.blue(pixelArray[ind11]) * uRatio) * vRatio).toInt(),
                    )

                    out[index] = result
                }
            }
        }
        val resizedImage = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        resizedImage.setPixels(out, 0, image.width, 0, 0, image.width, image.height)
        return resizedImage
    }
    fun rotate(image: Bitmap, angle: Double) : Bitmap
    {
        val pixelArray = IntArray(image.height * image.width)
        val out = IntArray(image.height * image.width) {0xFF000000.toInt()}
        image.getPixels(pixelArray, 0, image.width, 0, 0, image.width, image.height)

        for (i in 0 until image.height)
        {
            for (j in 0 until image.width)
            {
                val index = i * image.width + j
                val v: Double = -j * sin(angle) + i * cos(angle)
                val u: Double = j * cos(angle) + i * sin(angle)
                val y: Int = floor(v).toInt()
                val x: Int = floor(u).toInt()
                val uRatio: Double = u - x
                val vRatio: Double = v - y
                val uOpposite: Double = 1.0 - uRatio
                val vOpposite: Double = 1.0 - vRatio

                val ind00 = y * image.width + x
                val ind01 = y * image.width + (x + 1)
                val ind10 = (y + 1) * image.width + x
                val ind11 = (y + 1) * image.width + (x + 1)

                if (y >= 0 && y + 1 < image.height && x >= 0 && x + 1 < image.width)
                {
                    val result = Color.rgb(
                        ((Color.red(pixelArray[ind00]) * uOpposite + Color.red(pixelArray[ind01]) * uRatio) * vOpposite +
                                (Color.red(pixelArray[ind10]) * uOpposite + Color.red(pixelArray[ind11]) * uRatio) * vRatio).toInt(),
                        ((Color.green(pixelArray[ind00]) * uOpposite + Color.green(pixelArray[ind01]) * uRatio) * vOpposite +
                                (Color.green(pixelArray[ind10]) * uOpposite + Color.green(pixelArray[ind11]) * uRatio) * vRatio).toInt(),
                        ((Color.blue(pixelArray[ind00]) * uOpposite + Color.blue(pixelArray[ind01]) * uRatio) * vOpposite +
                                (Color.blue(pixelArray[ind10]) * uOpposite + Color.blue(pixelArray[ind11]) * uRatio) * vRatio).toInt(),
                    )

                    out[index] = result
                }
            }
        }
        val rotatedImage = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        rotatedImage.setPixels(out, 0, image.width, 0, 0, image.width, image.height)
        return rotatedImage
    }
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