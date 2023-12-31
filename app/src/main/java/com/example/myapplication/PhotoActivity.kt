package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.myapplication.databinding.ActivityPhotoBinding
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel


class PhotoActivity : AppCompatActivity() {
    private var photoImageView: ImageView? = null
    private var photoviewline: View? = null
    private var resimageview: ImageView? = null
    private var restext: TextView? = null
    private lateinit var tflite: Interpreter
    private lateinit var imageCaptureUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        photoImageView = findViewById(R.id.photoImageView)
        photoviewline = findViewById(R.id.photoviewline)
        resimageview = findViewById(R.id.resimageview)
        restext = findViewById(R.id.restext)

        tflite = Interpreter(loadModelFile())

        val takePhotoButton = findViewById<Button>(R.id.takePhotoButton)
        val galleryButton = findViewById<Button>(R.id.galleryButton)


        takePhotoButton.setOnClickListener {
            val cameraPermissionCheck = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            )

            if (cameraPermissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CAMERA),
                    1000
                )
            } else {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val url = "tmp_" + System.currentTimeMillis().toString() + ".jpg"
                imageCaptureUri = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.fileprovider",
                    File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), url)
                )

                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageCaptureUri)
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            }
        }




        galleryButton.setOnClickListener {
            val pickPhotoIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK)
        }

        if (intent.getBooleanExtra("fromCamera", false)) {
            // fromCamera 값이 true일 경우 takePhotoButton을 자동으로 클릭
            takePhotoButton.performClick()
            // 다음에 takePhotoButton이 클릭되어도 이벤트를 처리하지 않도록 리스너 제거
            takePhotoButton.setOnClickListener(null)
        } else if (intent.getBooleanExtra("fromGallery", false)) {
            // fromGallery 값이 true일 경우 galleryButton을 자동으로 클릭
            galleryButton.performClick()
            // 다음에 galleryButton이 클릭되어도 이벤트를 처리하지 않도록 리스너 제거
            galleryButton.setOnClickListener(null)
        }
    }



    //모델 파일 로드하여 bytebuffer로 변환
    private fun loadModelFile(): ByteBuffer {
        val assetManager = assets
        val modelFileDescriptor = assetManager.openFd("ensemble_model.tflite")
        val inputStream = FileInputStream(modelFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = modelFileDescriptor.startOffset
        val declaredLength = modelFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength) //FileChannel의 map 메서드를 사용하여 ByteBuffer를 생성
    }

    //주어진 이미지를 모델의 입력으로 제공, 모델 출력 분석하여 최종 분류 결과 반환
    private fun classifyImage(bitmap: Bitmap): String {
        val inputShape = tflite.getInputTensor(0).shape()
        val inputSize = inputShape[1]
        val imgData = ByteBuffer.allocateDirect(inputSize * inputSize * 3 * 4)
        imgData.order(ByteOrder.nativeOrder())
        imgData.rewind()

        val output = Array(1) { FloatArray(1) }
        tflite.run(imgData, output)

        return if (output[0][0] > 0.5) {
            "good"
        } else {
            "bad"
        }
    }

    //결과 출력
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
            photoImageView?.setImageBitmap(imageBitmap)
            photoImageView?.visibility = View.VISIBLE

            val classificationResult = classifyImage(imageBitmap)
            val resultTextView = findViewById<TextView>(R.id.resultTextView)
            resultTextView.text = "결과: $classificationResult"
            resultTextView.visibility = View.VISIBLE
            photoviewline!!.visibility = View.VISIBLE
            resimageview!!.visibility = View.VISIBLE
            restext!!.visibility = View.VISIBLE
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = if (data?.hasExtra("data") == true) {
                data.extras?.get("data") as Bitmap
            } else {
                val imageUri = imageCaptureUri
                val imageStream = contentResolver.openInputStream(imageUri!!)
                BitmapFactory.decodeStream(imageStream)
            }

            photoImageView?.setImageBitmap(imageBitmap)
            photoImageView?.visibility = View.VISIBLE

            val classificationResult = classifyImage(imageBitmap)
            val resultTextView = findViewById<TextView>(R.id.resultTextView)
            resultTextView.text = "결과: $classificationResult"
            resultTextView.visibility = View.VISIBLE
            photoviewline!!.visibility = View.VISIBLE
            resimageview!!.visibility = View.VISIBLE
            restext!!.visibility = View.VISIBLE
        }

    }



    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
    }
}
