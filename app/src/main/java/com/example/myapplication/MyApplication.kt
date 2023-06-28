package com.example.myapplication

import android.app.Application
import androidx.multidex.MultiDexApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


// 전역 응용 프로그램 상태를 유지하기 위한 기본 클래스
// 첫 번째 액티비티(MainActivity)가 표시되기 전에 전역 상태를 초기화하는 데 사용
class MyApplication : MultiDexApplication() {
    companion object{
        lateinit var db : FirebaseFirestore
//        lateinit var storage : FirebaseStorage

        lateinit var auth : FirebaseAuth
        var email:String? = null
        fun checkAuth(): Boolean{
            var currentuser = auth.currentUser
            return currentuser?.let{
                email = currentuser.email
                if(currentuser.isEmailVerified) true
                else false
            } ?:false
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