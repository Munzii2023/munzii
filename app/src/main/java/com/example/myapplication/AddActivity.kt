package com.example.myapplication

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myapplication.databinding.ActivityAddBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddBinding
//    lateinit var filePath: String
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding= ActivityAddBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val requestLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
//            if(it.resultCode === android.app.Activity.RESULT_OK){
//                // 이미지를 ImageView 보이기
//                Glide
//                    .with(applicationContext)
//                    .load(it.data?.data)
//                    .apply(RequestOptions().override(250, 200))
//                    .centerCrop()
//                    .into(binding.addImageView)
//                // 이미지의 주소 저장
//                val cursor = contentResolver.query(it.data?.data as Uri, arrayOf<String>(MediaStore.Images.Media.DATA), null, null, null)
//                cursor?.moveToFirst().let{
//                    filePath = cursor?.getString(0) as String
//                    Log.d("mobileApp", "${filePath}") //파일이름 확인
//                }
//            }
//        }
//        binding.btnGallery.setOnClickListener{
//            val intent = Intent(Intent.ACTION_PICK)
//            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "images/*")
//            requestLauncher.launch(intent)
//        }
//
//        binding.btnSave.setOnClickListener {
//            if(binding.addEditView.text.isNotEmpty() && binding.addImageView.drawable !== null){
//                //firestore 저장
//                saveStore()
//            }else{
//                Toast.makeText(this, "내용을 입력해주세요..", Toast.LENGTH_SHORT).show()
//            }
//            finish()
//        }
//    }
//
//    fun dateToString(date:Date): String{
//        val format = SimpleDateFormat("yyyy-mm-dd hh:mm")
//        return format.format(date)
//    }
//    fun saveStore(){
//        val data = mapOf(
//            "email" to MyApplication.email,
//            "content" to binding.addEditView.text.toString(),
//            "date" to dateToString(Date())
//        )
//
//        MyApplication.db.collection("news")
//            .add(data)
//            .addOnSuccessListener {
//                Log.d("mobileApp", "data firestore save ok")
//                //uploadImage(it.id)
//            }
//            .addOnFailureListener{
//                Log.d("mobileApp", "data firestore save error - ${it.toString()}")
//            }
//    }
//    fun uploadImage(docId:String){
//        val storage = MyApplication.storage
//        val storageRef = storage.reference
//        val imageRef = storageRef.child("images/${docId}.jpg")
//        val file = Uri.fromFile(File(filePath))
//        imageRef.putFile(file)
//            .addOnSuccessListener {
//                Log.d("mobileApp", "imageRef.putFile(file) - addOnSuccessListener")
//                finish()
//            }
//            .addOnFailureListener{
//                Log.d("mobileApp", "imageRef.putFile(file) - addOnFailureListener")
//            }
//    }
}