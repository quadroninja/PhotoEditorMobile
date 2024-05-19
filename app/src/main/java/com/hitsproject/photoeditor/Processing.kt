package com.hitsproject.photoeditor

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

class Processing {
    fun resize(bitmap: Bitmap, coefficient: Double): Bitmap {
        val width = (bitmap.width * coefficient).toInt()
        val height = (bitmap.height * coefficient).toInt()
        val newBitmap = Bitmap.createBitmap(width, height, bitmap.config)

        runBlocking {
            withContext(Dispatchers.Default) {
                (0 until width).chunked(4).forEach { xRange ->
                    xRange.forEach { x ->
                        (0 until height).forEach { y ->
                            val xCoefficient = x.toDouble() / width * (bitmap.width - 1)
                            val yCoefficient = y.toDouble() / height * (bitmap.height - 1)

                            val x1 = xCoefficient.toInt()
                            val y1 = yCoefficient.toInt()
                            val x2 = if (x1 == bitmap.width - 1) x1 else x1 + 1
                            val y2 = if (y1 == bitmap.height - 1) y1 else y1 + 1

                            val color1 = bitmap.getPixel(x1, y1)
                            val color2 = bitmap.getPixel(x2, y1)
                            val color3 = bitmap.getPixel(x1, y2)
                            val color4 = bitmap.getPixel(x2, y2)

                            val red = if (coefficient > 1.0) {
                                (
                                        (color1.red * (x2 - xCoefficient) * (y2 - yCoefficient) +
                                                color2.red * (xCoefficient - x1) * (y2 - yCoefficient) +
                                                color3.red * (x2 - xCoefficient) * (yCoefficient - y1) +
                                                color4.red * (xCoefficient - x1) * (yCoefficient - y1)) /
                                                ((x2 - x1) * (y2 - y1))
                                        ).toInt()
                            } else {
                                (
                                        (color1.red * (1.0 - (xCoefficient - x1)) * (1.0 - (yCoefficient - y1)) +
                                                color2.red * ((xCoefficient - x1)) * (1.0 - (yCoefficient - y1)) +
                                                color3.red * (1.0 - (xCoefficient - x1)) * ((yCoefficient - y1)) +
                                                color4.red * ((xCoefficient - x1)) * ((yCoefficient - y1))) /
                                                ((x2 - x1) * (y2 - y1))
                                        ).toInt()
                            }

                            val green = if (coefficient > 1.0) {
                                (
                                        (color1.green * (x2 - xCoefficient) * (y2 - yCoefficient) +
                                                color2.green * (xCoefficient - x1) * (y2 - yCoefficient) +
                                                color3.green * (x2 - xCoefficient) * (yCoefficient - y1) +
                                                color4.green * (xCoefficient - x1) * (yCoefficient - y1)) /
                                                ((x2 - x1) * (y2 - y1))
                                        ).toInt()
                            } else {
                                (
                                        (color1.green * (1.0 - (xCoefficient - x1)) * (1.0 - (yCoefficient - y1)) +
                                                color2.green * ((xCoefficient - x1)) * (1.0 - (yCoefficient - y1)) +
                                                color3.green * (1.0 - (xCoefficient - x1)) * ((yCoefficient - y1)) +
                                                color4.green * ((xCoefficient - x1)) * ((yCoefficient - y1))) /
                                                ((x2 - x1) * (y2 - y1))
                                        ).toInt()
                            }

                            val blue = if (coefficient > 1.0) {
                                (
                                        (color1.blue * (x2 - xCoefficient) * (y2 - yCoefficient) +
                                                color2.blue * (xCoefficient - x1) * (y2 - yCoefficient) +
                                                color3.blue * (x2 - xCoefficient) * (yCoefficient - y1) +
                                                color4.blue * (xCoefficient - x1) * (yCoefficient - y1)) /
                                                ((x2 - x1) * (y2 - y1))
                                        ).toInt()
                            } else {
                                (
                                        (color1.blue * (1.0 - (xCoefficient - x1)) * (1.0 - (yCoefficient - y1)) +
                                                color2.blue * ((xCoefficient - x1)) * (1.0 - (yCoefficient - y1)) +
                                                color3.blue * (1.0 - (xCoefficient - x1)) * ((yCoefficient - y1)) +
                                                color4.blue * ((xCoefficient - x1)) * ((yCoefficient - y1))) /
                                                ((x2 - x1) * (y2 - y1))
                                        ).toInt()
                            }

                            val alpha = if (coefficient > 1.0) {
                                (
                                        (color1.alpha * (x2 - xCoefficient) * (y2 - yCoefficient) +
                                                color2.alpha * (xCoefficient - x1) * (y2 - yCoefficient) +
                                                color3.alpha * (x2 - xCoefficient) * (yCoefficient - y1) +
                                                color4.alpha * (xCoefficient - x1) * (yCoefficient - y1)) /
                                                ((x2 - x1) * (y2 - y1))
                                        ).toInt()
                            } else {
                                (
                                        (color1.alpha * (1.0 - (xCoefficient - x1)) * (1.0 - (yCoefficient - y1)) +
                                                color2.alpha * ((xCoefficient - x1)) * (1.0 - (yCoefficient - y1)) +
                                                color3.alpha * (1.0 - (xCoefficient - x1)) * ((yCoefficient - y1)) +
                                                color4.alpha * ((xCoefficient - x1)) * ((yCoefficient - y1))) /
                                                ((x2 - x1) * (y2 - y1))
                                        ).toInt()
                            }

                            newBitmap.setPixel(x, y, Color.argb(alpha, red, green, blue))
                        }
                    }
                }
            }
        }

        return newBitmap
    }

    fun rotate(image: Bitmap, angle: Double): Bitmap {
        val width = image.width
        val height = image.height
        val centerX = width / 2
        val centerY = height / 2

        val radians = Math.toRadians(angle)

        val rotatedWidth = ceil(width * abs(cos(radians)) + height * abs(sin(radians))).toInt()
        val rotatedHeight = ceil(width * abs(sin(radians)) + height * abs(cos(radians))).toInt()

        val rotatedImage = Bitmap.createBitmap(rotatedWidth, rotatedHeight, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        image.getPixels(pixels, 0, width, 0, 0, width, height)

        val cosAngle = cos(radians)
        val sinAngle = sin(radians)

        runBlocking {
            coroutineScope {
                for (y in 0 until rotatedHeight) {
                    launch {
                        for (x in 0 until rotatedWidth) {
                            val dx = x - rotatedWidth / 2
                            val dy = y - rotatedHeight / 2

                            val srcX = (dx * cosAngle + dy * sinAngle + centerX).toInt()
                            val srcY = (-dx * sinAngle + dy * cosAngle + centerY).toInt()

                            if (srcX in 0 until width && srcY in 0 until height) {
                                val index = srcY * width + srcX
                                rotatedImage.setPixel(x, y, pixels[index])
                            } else {
                                rotatedImage.setPixel(x, y, 0)
                            }
                        }
                    }
                }
            }
        }
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
        val width = bitmap.width
        val height = bitmap.height
        val negativeImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = bitmap.getPixel(x, y)
                val red = 255 - Color.red(color)
                val green = 255 - Color.green(color)
                val blue = 255 - Color.blue(color)
                val newColor = Color.rgb(red, green, blue)
                negativeImage.setPixel(x, y, newColor)
            }
        }

        return negativeImage
    }

    fun unsharpMask(bitmap: Bitmap, amount: Float = 0.6f): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val blurredPixels = IntArray(width * height)
        val tempPixels = IntArray(width * height)

        runBlocking {
            (0 until height).chunked(4).map { rows ->
                async(Dispatchers.Default) {
                    for (y in rows) {
                        for (x in 0 until width) {
                            var r = 0
                            var g = 0
                            var b = 0
                            var count = 0
                            for (dy in -1..1) {
                                for (dx in -1..1) {
                                    val xx = x + dx
                                    val yy = y + dy
                                    if (xx in 0..<width && yy >= 0 && yy < height) {
                                        val pixel = pixels[yy * width + xx]
                                        r += Color.red(pixel)
                                        g += Color.green(pixel)
                                        b += Color.blue(pixel)
                                        count++
                                    }
                                }
                            }
                            blurredPixels[y * width + x] =
                                Color.rgb(r / count, g / count, b / count)
                        }
                    }
                }
            }.awaitAll()
        }

        runBlocking {
            (0 until height).chunked(4).map { rows ->
                async(Dispatchers.Default) {
                    for (y in rows) {
                        for (x in 0 until width) {
                            val originalPixel = pixels[y * width + x]
                            val blurredPixel = blurredPixels[y * width + x]
                            val r =
                                (Color.red(originalPixel) + amount * (Color.red(originalPixel) - Color.red(
                                    blurredPixel
                                ))).toInt()
                            val g =
                                (Color.green(originalPixel) + amount * (Color.green(originalPixel) - Color.green(
                                    blurredPixel
                                ))).toInt()
                            val b =
                                (Color.blue(originalPixel) + amount * (Color.blue(originalPixel) - Color.blue(
                                    blurredPixel
                                ))).toInt()
                            tempPixels[y * width + x] = Color.rgb(
                                abs(r.coerceIn(0, 255)),
                                abs(g.coerceIn(0, 255)),
                                abs(b.coerceIn(0, 255))
                            )
                        }
                    }
                }
            }.awaitAll()
        }
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(tempPixels, 0, width, 0, 0, width, height)
        return result
    }
}