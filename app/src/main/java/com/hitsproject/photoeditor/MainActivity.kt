package com.hitsproject.photoeditor

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.SeekBar
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

    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var photoPicker: PhotoPicker
    private lateinit var process: Processing
    private lateinit var save: ImageSaver

    private lateinit var seekBar: SeekBar
    private lateinit var applyButton: FloatingActionButton
    private lateinit var valueTextView: TextView

    private var currentEffect: ((Bitmap, Double) -> Bitmap)? = null
    private var currentEffectMinValue: Double = 0.0
    private var currentEffectMaxValue: Double = 0.0

    private var isAdded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        photoPicker = PhotoPicker(this)
        process = Processing()
        save = ImageSaver()

        val addPhotoButton = findViewById<FloatingActionButton>(R.id.AddPhoto)
        val navigation = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        seekBar = findViewById(R.id.getValue)
        applyButton = findViewById(R.id.apply)
        valueTextView = findViewById(R.id.showValue)

        addPhotoButton.setOnClickListener {
            photoPicker.pickPhoto(this)
        }

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
                            Toast.LENGTH_SHORT
                        ).show()
                        true
                    }
                }
                R.id.saving -> {
                    if (isAdded) save.saveImageToDevice(this, image, "newImage")
                    else Toast.makeText(
                        this,
                        getString(R.string.image_not_added),
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }
                else -> false
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updateSliderValue(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        applyButton.setOnClickListener {
            val value = (seekBar.progress / 10.0)
            applyEffect(value)
            setVisible()
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
            currentEffect = { bitmap, value -> process.rotate(bitmap, value) }
            val value = findViewById<TextView>(R.id.showValue)
            value.visibility = VISIBLE
            currentEffectMinValue = 0.0
            currentEffectMaxValue = 360.0
            getSliderValue(currentEffectMinValue, currentEffectMaxValue)
            setInvisible()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.findViewById<TextView>(R.id.resize_menu_item)?.setOnClickListener {
            currentEffect = { bitmap, value -> process.resize(bitmap, value) }
            currentEffectMinValue = 0.1
            currentEffectMaxValue = 2.0
            getSliderValue(currentEffectMinValue, currentEffectMaxValue)
            setInvisible()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.findViewById<TextView>(R.id.unsharp_mask_menu_item)?.setOnClickListener {
            currentEffect = { bitmap, value -> process.unsharpMask(bitmap, value.toFloat()) }
            currentEffectMinValue = 0.0
            currentEffectMaxValue = 2.0
            getSliderValue(currentEffectMinValue, currentEffectMaxValue)
            setInvisible()
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

    private fun setInvisible() {
        val navigation = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val addPhotoButton = findViewById<FloatingActionButton>(R.id.AddPhoto)
        val showValue = findViewById<TextView>(R.id.showValue)
        seekBar.visibility = VISIBLE
        applyButton.visibility = VISIBLE
        showValue.visibility = VISIBLE
        navigation.visibility = GONE
        addPhotoButton.visibility = GONE
    }

    private fun setVisible() {
        val navigation = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val addPhotoButton = findViewById<FloatingActionButton>(R.id.AddPhoto)
        val showValue = findViewById<TextView>(R.id.showValue)
        showValue.visibility = GONE
        seekBar.visibility = GONE
        applyButton.visibility = GONE
        navigation.visibility = VISIBLE
        addPhotoButton.visibility = VISIBLE
    }

    @SuppressLint("SetTextI18n")
    private fun getSliderValue(minValue: Double, maxValue: Double) {
        seekBar.min = (minValue * 10).toInt()
        seekBar.max = (maxValue * 10).toInt()

        val selectedValue = minValue + (maxValue - minValue) / 2
        updateSliderValue(((selectedValue * 10).toInt()))
    }

    @SuppressLint("SetTextI18n")
    private fun updateSliderValue(progress: Int) {
        val value = progress.toDouble() / 10
        valueTextView.text = "Значение: $value"
    }

    private fun applyEffect(value: Double) {
        if (currentEffect != null) {
            image = currentEffect!!.invoke(image, value)
            updateImageView()
        }
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