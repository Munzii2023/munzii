package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    var authMenuItem : MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        authMenuItem = menu!!.findItem(R.id.menu_auth)
        if(MyApplication.checkAuth()){
            authMenuItem!!.title = "${MyApplication.email}님"
        }
        else{
            authMenuItem!!.title = "인증"
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onStart(){
        // Intent에서 finish() 돌아올 때 실행
        // onCreate -> onStart -> onCreateOptionsMenu
        super.onStart()
        if(authMenuItem != null){
            if(MyApplication.checkAuth()){
                authMenuItem!!.title = "${MyApplication.email}님"
            }
            else{
                authMenuItem!!.title = "인증"
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId === R.id.menu_auth){
            val intent = Intent(this, AuthActivity::class.java)
            if(authMenuItem!!.title!!.equals("인증")){
                intent.putExtra("data", "logout")
            }
            else{ //이메일, 구글계정
                intent.putExtra("data", "login")
            }
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}