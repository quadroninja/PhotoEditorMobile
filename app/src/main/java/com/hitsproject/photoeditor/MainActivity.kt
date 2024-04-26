package com.hitsproject.photoeditor

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    private lateinit var photoPicker: PhotoPicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        photoPicker = PhotoPicker(this)

        val button = findViewById<FloatingActionButton>(R.id.AddPhoto)
        button.setOnClickListener() {
            photoPicker.pickPhotoDialog(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val bitmap = photoPicker.onActivityResult(requestCode, resultCode, data)
        if (bitmap != null) {
            val imageView = findViewById<ImageView>(R.id.ViewImage)
            imageView.setImageBitmap(bitmap)
        }
    }
}