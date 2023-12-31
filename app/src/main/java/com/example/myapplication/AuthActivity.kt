package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Nickname
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.play.integrity.internal.l
import com.google.firebase.auth.GoogleAuthProvider
import java.util.*


class AuthActivity : AppCompatActivity() {
    lateinit var binding: ActivityAuthBinding
    lateinit var filePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        changeVisibility(intent.getStringExtra("data").toString())


        binding.goSignInBtn.setOnClickListener{
            // 회원가입
            changeVisibility("signin")
        }

        binding.signBtn.setOnClickListener {
            //이메일,비밀번호 회원가입........................
            val email:String = binding.authEmailEditView.text.toString()
            val password:String = binding.authPasswordEditView.text.toString()
            val Nickname:String = binding.authNickNameEditView.text.toString()
            val DeviceId:String = binding.authDeviceIdEditView.text.toString()

            MyApplication.auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this){task->
                    if(task.isSuccessful){
                        MyApplication.auth.currentUser?.sendEmailVerification()?.addOnCompleteListener{
                            sendTask ->
                                if(sendTask.isSuccessful){
                                    Toast.makeText(baseContext, "회원가입 성공...이메일 확인", Toast.LENGTH_LONG).show()
                                    changeVisibility("logout")
                                }
                                else{
                                    Toast.makeText(baseContext, "메일 전송 실패...", Toast.LENGTH_LONG).show()
                                    changeVisibility("logout")
                                }
                        }
                    }
                    else{
                        Toast.makeText(baseContext, "회원가입 실패...", Toast.LENGTH_LONG).show()
                        changeVisibility("logout")
                    }

                    //기기id, 닉네임 firebase에 들어가게
                    if(binding.authEmailEditView.text!!.isNotEmpty() && binding.authNickNameEditView.text.isNotEmpty()){
                        //firestore 저장
                        saveStore()
                    }else{
                        Toast.makeText(this, "내용을 입력해주세요..", Toast.LENGTH_SHORT).show()
                    }
                    finish()
                }
        }

        binding.loginBtn.setOnClickListener {
            //이메일, 비밀번호 로그인.......................
            val email:String = binding.authEmailEditView.text.toString()
            val password:String = binding.authPasswordEditView.text.toString()
            MyApplication.auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this){task ->
                    if(task.isSuccessful){
                        if(MyApplication.checkAuth()){
                            MyApplication.email = email
                            //changeVisibility("login")
                            finish()
                        }
                        else{
                            Toast.makeText(baseContext, "이메일 인증 실패...", Toast.LENGTH_LONG).show()
                        }
                    }
                    else{
                        Toast.makeText(baseContext, "로그인 실패...", Toast.LENGTH_LONG).show()
                        changeVisibility("logout")
                    }
                    binding.authEmailEditView.text?.clear()
                    binding.authPasswordEditView.text.clear()

                }
        }


        binding.logoutBtn.setOnClickListener {
            //로그아웃...........
            MyApplication.auth.signOut()
            MyApplication.email = null
            //changeVisibility("logout")
            finish()
        }


        val requestLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            // ApiException : Google Play 서비스 호출이 실패했을 때 테스크에서 반환할 예외
            try{
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                MyApplication.auth.signInWithCredential(credential)
                    .addOnCompleteListener(this){task ->
                        if(task.isSuccessful){
                            MyApplication.email = account.email
                            //changeVisibility("login")
                            Log.d("mobileApp","GoogleSignIn - Successful")
                            finish()
                        }
                        else{
                            changeVisibility("logout")
                            Log.d("mobileApp","GoogleSignIn - NOT Successful")
                        }
                    }
            }catch (e: ApiException){
                changeVisibility("logout")
                Log.d("mobileApp", "GoogleSignIn - ${e.message}")
            }
        }
        binding.googleLoginBtn.setOnClickListener {
            //구글 로그인....................
            val gso : GoogleSignInOptions = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val signInIntent : Intent = GoogleSignIn.getClient(this, gso).signInIntent
            requestLauncher.launch(signInIntent)
        }
    }

    fun saveStore(){
        val data = mapOf(
            "email" to binding.authEmailEditView.text.toString(),
            "nickname" to binding.authNickNameEditView.text.toString(),
            "deviceid" to binding.authDeviceIdEditView.text.toString()
        )
        val email: String = data["email"] as String

        MyApplication.db.collection("member").document(email)
            .set(data)
            .addOnSuccessListener {
                Log.d("mobileApp", "data firestore save ok")
            }
            .addOnFailureListener{
                Log.d("mobileApp", "data firestore save error - ${it.toString()}")
            }
    }
    fun changeVisibility(mode: String){
        if(mode.equals("signin")){
            binding.run {
                logoutBtn.visibility = View.GONE
                goSignInBtn.visibility = View.GONE
                googleLoginBtn.visibility = View.GONE
                naverLoginBtn.visibility= View.GONE
                kakaoLoginBtn.visibility= View.GONE
                authEmailEditView.visibility = View.VISIBLE
                authPasswordEditView.visibility = View.VISIBLE
                authNickNameEditView.visibility=View.VISIBLE
                authDeviceIdEditView.visibility=View.VISIBLE
                signBtn.visibility = View.VISIBLE
                loginBtn.visibility = View.GONE
                areyounotauth.visibility= View.GONE
                authview1.visibility= View.GONE
                authview3.visibility= View.GONE
                authview4.visibility= View.GONE
                authSNStext.visibility= View.GONE
            }
        }else if(mode.equals("login")){
            binding.run {
                authMainTextView.text = "${MyApplication.email} 님 반갑습니다."
                logoutBtn.visibility= View.VISIBLE
                goSignInBtn.visibility= View.GONE
                googleLoginBtn.visibility= View.GONE
                naverLoginBtn.visibility= View.GONE
                kakaoLoginBtn.visibility= View.GONE
                authEmailEditView.visibility= View.GONE
                authPasswordEditView.visibility= View.GONE
                authNickNameEditView.visibility=View.GONE
                authDeviceIdEditView.visibility=View.GONE
                signBtn.visibility= View.GONE
                loginBtn.visibility= View.GONE
                areyounotauth.visibility= View.GONE
                authview1.visibility= View.GONE
                authview3.visibility= View.GONE
                authview4.visibility= View.GONE
                authSNStext.visibility= View.GONE
            }

        }else if(mode.equals("logout")){
            binding.run {
                authMainTextView.text = "로그인 하거나 회원가입 해주세요."
                logoutBtn.visibility = View.GONE
                goSignInBtn.visibility = View.VISIBLE
                googleLoginBtn.visibility = View.VISIBLE
                naverLoginBtn.visibility= View.VISIBLE
                kakaoLoginBtn.visibility= View.VISIBLE
                authEmailEditView.visibility = View.VISIBLE
                authPasswordEditView.visibility = View.VISIBLE
                authNickNameEditView.visibility=View.GONE
                authDeviceIdEditView.visibility=View.GONE
                signBtn.visibility = View.GONE
                loginBtn.visibility = View.VISIBLE
                areyounotauth.visibility= View.VISIBLE
                authview1.visibility= View.VISIBLE
                authview2.visibility= View.VISIBLE
                authview3.visibility= View.VISIBLE
                authview4.visibility= View.VISIBLE
                authSNStext.visibility= View.VISIBLE
            }
        }
    }
}