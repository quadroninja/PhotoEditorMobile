package com.hitsproject.photoeditor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

class PhotoPicker(private val activity: AppCompatActivity) {
    private val imageOrientationHelper = ImageOrientationHelper(activity)
    private var photoUri: Uri? = null
    private var bitmap: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

    init {
        requestAllRequiredPermissions()
    }

    fun getBitmap(): Bitmap {
        return bitmap
    }

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
    }

    fun pickPhoto(context: Context) {
        showPhotoPickerDialog(context)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Bitmap {
        return when (requestCode) {
            RequestCode.IMAGE_PICK.ordinal -> handleImagePick(resultCode, data)
            RequestCode.IMAGE_CAPTURE.ordinal -> handleImageCapture(resultCode)
            else -> bitmap
        }
    }

    private fun showPhotoPickerDialog(context: Context) {
        if (hasAllRequiredPermissions()) {
            showPhotoPickerOptions(context)
        } else {
            requestAllRequiredPermissions()
            showToast("Необходимы разрешения для доступа к фото и камере.", context)
        }
    }

    private fun showPhotoPickerOptions(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Выберите источник фото")
        builder.setMessage("Откуда вы хотите загрузить фото?")

        builder.setPositiveButton("Галерея") { _, _ ->
            startImagePick()
        }

        builder.setNegativeButton("Камера") { _, _ ->
            startImageCapture()
        }

        builder.show()
    }

    private fun requestAllRequiredPermissions() {
        requestStoragePermission()
        requestCameraPermission()
        requestImagePermission()
    }

    private fun hasAllRequiredPermissions(): Boolean {
        return hasStoragePermission() && hasCameraPermission() && hasImagePermission()
    }

    private fun handleImagePick(resultCode: Int, data: Intent?): Bitmap {
        return if (resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(activity.contentResolver, uri)
            imageOrientationHelper.getOrientedBitmap(bitmap, uri)
        } else {
            bitmap
        }
    }

    private fun handleImageCapture(resultCode: Int): Bitmap {
        return if (resultCode == Activity.RESULT_OK && photoUri != null) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(activity.contentResolver, photoUri)
                imageOrientationHelper.getOrientedBitmap(bitmap, photoUri)
            } catch (e: IOException) {
                Log.e("PhotoPicker", "Error getting bitmap from captured image", e)
                bitmap
            }
        } else {
            bitmap
        }
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
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

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestManageStoragePermission()
        } else {
            requestWriteStoragePermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestManageStoragePermission() {
        if (!Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:${activity.packageName}")
            activity.startActivityForResult(intent, RequestCode.MANAGE_STORAGE_PERMISSION.ordinal)
        }
    }

    private fun requestWriteStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                RequestCode.WRITE_STORAGE_PERMISSION.ordinal
            )
        }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.CAMERA),
                RequestCode.CAMERA_PERMISSION.ordinal
            )
        }
    }

    private fun requestImagePermission() {
        if (ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ),
                RequestCode.IMAGE_PERMISSION.ordinal
            )
        }
    }

    private fun startImagePick() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(intent, RequestCode.IMAGE_PICK.ordinal)
    }

    private fun startImageCapture() {
        photoUri = createPhotoUri()
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        activity.startActivityForResult(intent, RequestCode.IMAGE_CAPTURE.ordinal)
    }

    private fun createPhotoUri(): Uri {
        val file = File(Environment.getExternalStorageDirectory(), "image.jpg")
        return FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            file
        )
    }

    private fun showToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}