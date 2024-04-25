package com.hitsproject.photoeditor

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity

class PhotoPicker(private val activity: AppCompatActivity) {

    private val REQUEST_IMAGE_PICK = 1
    private lateinit var bitmap: Bitmap

    fun pickPhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Bitmap? {
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            bitmap = MediaStore.Images.Media.getBitmap(activity.contentResolver, uri)
            return bitmap
        }
        return null
    }
}