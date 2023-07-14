package com.example.myapplication

import android.app.Application
import android.util.Log
import androidx.multidex.MultiDexApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// 전역 응용 프로그램 상태를 유지하기 위한 기본 클래스
// 첫 번째 액티비티(MainActivity)가 표시되기 전에 전역 상태를 초기화하는 데 사용
class MyApplication : MultiDexApplication() {
    companion object{
        lateinit var db : FirebaseFirestore
//        lateinit var storage : FirebaseStorage

        lateinit var auth : FirebaseAuth
        var email:String? = null
        var nickname:String? = null

        var retroInterface : RetroInterface
        val retrofit : Retrofit
            get() = Retrofit.Builder()
                .baseUrl("https://apis.data.go.kr/B552584/ArpltnInforInqireSvc/")
                .addConverterFactory(GsonConverterFactory.create()) // Json데이터를 사용자가 정의한 Java 객채로 변환해주는 라이브러리
                .build()
        init {
            retroInterface = retrofit.create(RetroInterface::class.java)
        }

        fun checkAuth(): Boolean {
            val currentUser = auth.currentUser
            return currentUser?.let {
                email = currentUser.email
                val collectionPath = "member"
                val documentPath = email!!
                Log.d("mobile", email!!)

                db.collection(collectionPath).document(documentPath).get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            nickname = document.getString("nickname")
                            Log.d("mobile", nickname!!)
                            // 가져온 텍스트를 사용합니다.
                            // ...
                        } else {
                            // 도큐먼트가 존재하지 않음
                        }
                    }
                    .addOnFailureListener { exception ->
                        // 가져오기 실패 처리
                    }
                currentUser.isEmailVerified
            } ?: false
        }


//        var networkService : NetworkService
//        val retrofit: Retrofit
//            get() = Retrofit.Builder()
//                .baseUrl("http://apis.data.go.kr/B553748/CertImgListService/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//        init{
//            networkService = retrofit.create(NetworkService::class.java) // 초기화
//        }
    }

    override fun onCreate() {
        super.onCreate()
        auth = Firebase.auth

        db = FirebaseFirestore.getInstance()
        //storage = Firebase.storage // 앱 전체의 전역변수
    }
}