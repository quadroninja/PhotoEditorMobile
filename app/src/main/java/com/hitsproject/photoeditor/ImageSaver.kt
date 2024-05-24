package com.hitsproject.photoeditor

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

class ImageSaver {
    fun saveImageToDevice(context: Context, bitmap: Bitmap, imageName: String) {
        var toast = Toast.makeText(context,
            context.getString(R.string.save_in_process), Toast.LENGTH_SHORT)
        toast.show()
        val imageDirectory = File(Environment.getExternalStorageDirectory(), "MyImages")
        if (!imageDirectory.exists()) {
            imageDirectory.mkdirs()
        }
        val imageFile = File(imageDirectory, "$imageName.png")
        val fos = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.close()
        toast = Toast.makeText(context,
                          context.getString(R.string.saving_success) + imageFile.absolutePath,
                               Toast.LENGTH_LONG)
        toast.show()
    }
}