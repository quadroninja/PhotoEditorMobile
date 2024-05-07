package com.hitsproject.photoeditor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

class PhotoPicker(private val activity: AppCompatActivity) {
    private enum class RequestCode {
        IMAGE_PICK,
        IMAGE_CAPTURE,
        CAMERA_PERMISSION,
        IMAGE_PERMISSION,
        MANAGE_STORAGE_PERMISSION,
        WRITE_STORAGE_PERMISSION
    }

    private var bitmap: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private var photoUri: Uri? = null

    fun getBitmap(): Bitmap {
        return bitmap
    }

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
    }

    fun pickPhotoDialog(context: Context) {
        requestStoragePermission()

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Выберите источник фото")
        builder.setMessage("Откуда вы хотите загрузить фото?")

        builder.setPositiveButton("Галерея") { _, _ ->
            if (hasImagePermission()) {
                startImagePick()
            } else {
                requestImagePermission()
            }
        }

        builder.setNegativeButton("Камера") { _, _ ->
            if (hasCameraPermission()) {
                startImageCapture()
            } else {
                requestCameraPermission()
            }
        }

        builder.show()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Bitmap {
        return when (requestCode) {
            RequestCode.IMAGE_PICK.ordinal -> handleImagePick(resultCode, data)
            RequestCode.IMAGE_CAPTURE.ordinal -> handleImageCapture(resultCode)
            else -> bitmap
        }
    }

    private fun handleImagePick(resultCode: Int, data: Intent?): Bitmap {
        return if (resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(activity.contentResolver, uri)
            rotateBitmapIfNeeded(bitmap)
        } else {
            bitmap
        }
    }

    private fun handleImageCapture(resultCode: Int): Bitmap {
        return if (resultCode == Activity.RESULT_OK && photoUri != null) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(activity.contentResolver, photoUri)
                rotateBitmapIfNeeded(bitmap)
            } catch (e: IOException) {
                Log.e("PhotoPicker", "Error getting bitmap from captured image", e)
                bitmap
            }
        } else {
            bitmap
        }
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap): Bitmap {
        return try {
            if (photoUri != null) {
                val exif = ExifInterface(activity.contentResolver.openInputStream(photoUri!!)!!)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270)
                    else -> bitmap
                }
            } else {
                bitmap
            }
        } catch (e: IOException) {
            Log.e("PhotoPicker", "Error rotating bitmap", e)
            bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${activity.packageName}")
                activity.startActivityForResult(intent, RequestCode.MANAGE_STORAGE_PERMISSION.ordinal)
            }
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                RequestCode.WRITE_STORAGE_PERMISSION.ordinal
            )
        }
    }

    private fun requestCameraPermission() {
        if (!hasCameraPermission()) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.CAMERA),
                RequestCode.CAMERA_PERMISSION.ordinal
            )
        } else {
            showToast("Нет разрешения. Выдайте разрешение к камере в настройках.", activity)
        }
    }

    private fun requestImagePermission() {
        if (!hasImagePermission()) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ),
                RequestCode.IMAGE_PERMISSION.ordinal
            )
        } else {
            showToast("Нет разрешения. Выдайте разрешение к фото в настройках.", activity)
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasImagePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
            activity,
            android.Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startImagePick() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(intent, RequestCode.IMAGE_PICK.ordinal)
    }

    private fun startImageCapture() {
        val file = File(Environment.getExternalStorageDirectory(), "image.jpg")
        photoUri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            file
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        activity.startActivityForResult(intent, RequestCode.IMAGE_CAPTURE.ordinal)
    }

    private fun showToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}