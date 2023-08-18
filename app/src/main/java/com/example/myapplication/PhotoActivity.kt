package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class PhotoActivity : AppCompatActivity() {
    private var photoImageView: ImageView? = null
    private var photoDescription: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        photoImageView = findViewById(R.id.photoImageView)
        photoDescription = findViewById(R.id.photoDescription)

        val takePhotoButton = findViewById<Button>(R.id.takePhotoButton)
        val galleryButton = findViewById<Button>(R.id.galleryButton)

        takePhotoButton.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(
                    takePictureIntent,
                    REQUEST_IMAGE_CAPTURE
                )
            }
        }

        galleryButton.setOnClickListener {
            val pickPhotoIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val extras = data?.extras
            val imageBitmap = extras?.get("data") as Bitmap?
            photoImageView?.setImageBitmap(imageBitmap)
            photoImageView?.visibility = View.VISIBLE
            photoDescription?.text = "사진을 찍었습니다."
            photoDescription?.visibility = View.VISIBLE
        } else if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            photoImageView?.setImageURI(data?.data)
            photoImageView?.visibility = View.VISIBLE
            photoDescription?.text = "갤러리에서 사진을 선택했습니다."
            photoDescription?.visibility = View.VISIBLE
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
    }
}
