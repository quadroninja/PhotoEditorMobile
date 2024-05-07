package com.hitsproject.photoeditor

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

lateinit var image: Bitmap

class MainActivity : AppCompatActivity() {

    private lateinit var photoPicker: PhotoPicker
    private lateinit var process: Processing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        photoPicker = PhotoPicker(this)
        process = Processing()

        val button = findViewById<FloatingActionButton>(R.id.AddPhoto)
        button.setOnClickListener {
            photoPicker.pickPhotoDialog(this)
        }

        val navigation = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        navigation.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.bw -> {
                    image = process.applyBlackAndWhiteFilter(image)
                    photoPicker.setBitmap(image)
                    val imageView = findViewById<ImageView>(R.id.ViewImage)
                    imageView.setImageBitmap(image)
                    true
                }

                else -> false
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val bitmap = photoPicker.onActivityResult(requestCode, resultCode, data)
        if (bitmap != null) {
            val imageView = findViewById<ImageView>(R.id.ViewImage)
            image = bitmap
            imageView.setImageBitmap(bitmap)
        }
    }
}