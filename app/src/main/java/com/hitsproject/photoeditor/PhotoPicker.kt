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
    private val REQUEST_IMAGE_PICK = 123
    private val REQUEST_IMAGE_CAPTURE = 456
    private val REQUEST_CAMERA_PERMISSION = 789
    private val REQUEST_IMAGE_PERMISSION = 987
    private val REQUEST_MEDIA_PERMISSION = 765
    private val REQUEST_MANAGE_STORAGE_PERMISSION = 654
    private val REQUEST_WRITE_STORAGE_PERMISSION = 321

    private var bitmap: Bitmap? = null
    private var photoUri: Uri? = null

    fun showToast(message: String, context: Context) {
        val duration: Int = Toast.LENGTH_SHORT
        Toast.makeText(context, message, duration).show()
    }

    fun pickPhotoDialog(context: Context) {
        requestStoragePermission()

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Выберите источник фото")
        builder.setMessage("Откуда вы хотите загрузить фото?")
        builder.setPositiveButton("Галерея") { _, _ ->
            if ((ContextCompat.checkSelfPermission(
                    activity,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(
                    activity,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED)
            ) {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activity.startActivityForResult(intent, REQUEST_IMAGE_PICK)
            } else {
                this.showToast("Нет разрешения. Выдайте разрешение к фото в настройках.", context)
            }
        }
        builder.setNegativeButton("Камера") { _, _ ->
            if (ContextCompat.checkSelfPermission(
                    activity,
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val file = File(Environment.getExternalStorageDirectory(), "image.jpg")
                photoUri = FileProvider.getUriForFile(
                    activity,
                    "${activity.packageName}.fileprovider",
                    file
                )
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            } else {
                this.showToast("Нет разрешения. Выдайте разрешение к камере в настройках.", context)
            }
        }
        builder.show()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Bitmap? {
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            bitmap = MediaStore.Images.Media.getBitmap(activity.contentResolver, uri)
            return rotateBitmapIfNeeded(bitmap)
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageUri = photoUri
            return try {
                val bitmap = MediaStore.Images.Media.getBitmap(activity.contentResolver, imageUri)
                rotateBitmapIfNeeded(bitmap)
            } catch (e: IOException) {
                Log.e("PhotoPicker", "Error getting bitmap from captured image", e)
                null
            }
        }
        return null
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null || photoUri == null) return bitmap

        return try {
            val exif = ExifInterface(activity.contentResolver.openInputStream(photoUri!!)!!)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270)
                else -> bitmap
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
                activity.startActivityForResult(intent, REQUEST_MANAGE_STORAGE_PERMISSION)
            }
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_STORAGE_PERMISSION
            )
        }
    }
}