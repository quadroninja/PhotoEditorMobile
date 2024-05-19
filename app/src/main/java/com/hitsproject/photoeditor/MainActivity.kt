package com.hitsproject.photoeditor

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.opencv.android.OpenCVLoader

lateinit var image: Bitmap
var isAdded: Boolean = false

class MainActivity : AppCompatActivity() {

    private lateinit var image: Bitmap
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var photoPicker: PhotoPicker
    private lateinit var process: Processing
    private lateinit var save: ImageSaver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        photoPicker = PhotoPicker(this)
        process = Processing()
        save = ImageSaver()

        val addPhotoButton = findViewById<FloatingActionButton>(R.id.AddPhoto)
        addPhotoButton.setOnClickListener {
            photoPicker.pickPhoto(this)
        }

        val navigation = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        navigation.menu.findItem(R.id.show_bottom_sheet).isVisible = true
        navigation.menu.findItem(R.id.show_bottom_sheet).isChecked = true
        navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.show_bottom_sheet -> {
                    if (isAdded) {
                        showBottomSheetMenu()
                        true
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.image_not_added),
                            Toast.LENGTH_SHORT)
                            .show()
                        true
                    }
                }
                R.id.saving -> {
                    if (isAdded) save.saveImageToDevice(this, image, "newImage")
                    else Toast.makeText(
                        this,
                        getString(R.string.image_not_added),
                        Toast.LENGTH_SHORT)
                        .show()
                    true
                }
                else -> false
            }
        }
    }

    private fun showBottomSheetMenu() {
        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_menu)

        bottomSheetDialog.findViewById<TextView>(R.id.bw_menu_item)?.setOnClickListener {
            image = process.applyBlackAndWhiteFilter(image)
            updateImageView()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.findViewById<TextView>(R.id.sepia_menu_item)?.setOnClickListener {
            image = process.applySepiaFilter(image)
            updateImageView()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.findViewById<TextView>(R.id.rotate_menu_item)?.setOnClickListener {
            image = process.rotate(image, 70.0)
            updateImageView()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.findViewById<TextView>(R.id.resize_menu_item)?.setOnClickListener {
            image = process.resize(image, 0.1)
            updateImageView()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.findViewById<TextView>(R.id.unsharp_mask_menu_item)?.setOnClickListener {
            image = process.unsharpMask(image)
            updateImageView()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.findViewById<TextView>(R.id.negative_filter_item)?.setOnClickListener {
            image = process.applyNegativeFilter(image)
            updateImageView()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.findViewById<TextView>(R.id.detect_faces_item)?.setOnClickListener {
            if (!OpenCVLoader.initDebug()) {
                Log.e("OpenCV", "OpenCV initialization failed.")
            } else {
                Log.d("OpenCV", "OpenCV initialization successful.")
            }
            image = process.detectFaces(image, this)
            updateImageView()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun updateImageView() {
        val imageView = findViewById<ImageView>(R.id.ViewImage)
        imageView.setImageBitmap(image)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val bitmap = photoPicker.onActivityResult(requestCode, resultCode, data)
        image = bitmap
        isAdded = true
        updateImageView()
    }
}