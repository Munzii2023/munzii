package com.example.myapplication

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class PhotoActivity : AppCompatActivity() {
    private var photoImageView: ImageView? = null
    private var photoDescription: TextView? = null
    private lateinit var tflite: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        photoImageView = findViewById(R.id.photoImageView)
        photoDescription = findViewById(R.id.photoDescription)
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
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    takePictureIntent.resolveActivity(packageManager)?.also {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                    }
                }
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
            photoDescription?.text = "갤러리에서 사진을 선택했습니다."
            photoDescription?.visibility = View.VISIBLE

            val classificationResult = classifyImage(imageBitmap)
            val resultTextView = findViewById<TextView>(R.id.resultTextView)
            resultTextView.text = "결과: $classificationResult"
            resultTextView.visibility = View.VISIBLE
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)

            photoImageView?.setImageBitmap(imageBitmap)
            photoImageView?.visibility = View.VISIBLE
            photoDescription?.text = "찍은 사진을 선택했습니다."
            photoDescription?.visibility = View.VISIBLE

            val classificationResult = classifyImage(imageBitmap)
            val resultTextView = findViewById<TextView>(R.id.resultTextView)
            resultTextView.text = "결과: $classificationResult"
        }
    }



    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
    }
}
