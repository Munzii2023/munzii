package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.NavigationHeaderBinding
import com.google.android.material.navigation.NavigationView
import org.w3c.dom.Text

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var binding: ActivityMainBinding
    lateinit var _binding : NavigationHeaderBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        _binding = NavigationHeaderBinding.bind(binding.mainDrawer.getHeaderView(0))

        setContentView(binding.root)
        binding.mainDrawer.setNavigationItemSelectedListener(this)

    }

    private fun updateNavigationHeader() {
        val headerView = binding.mainDrawer.getHeaderView(0)
        val headerBinding = NavigationHeaderBinding.bind(headerView)

        if (MyApplication.checkAuth()) {
            headerBinding.headerEmail.text = MyApplication.email
            headerBinding.headerName.text = MyApplication.nickname
        } else {
            headerBinding.headerEmail.text = "로그인이 필요합니다"
            headerBinding.headerName.text = ""
        }
    }

    override fun onResume() {
        super.onResume()
        updateNavigationHeader()
    }

    override fun onStart(){
        // Intent에서 finish() 돌아올 때 실행
        // onCreate -> onStart -> onCreateOptionsMenu
        super.onStart()
        if(_binding.headerEmail.text.equals("로그인이 필요합니다")){
            if(MyApplication.checkAuth()){
                _binding.headerEmail!!.text= MyApplication.email
                _binding.headerName!!.text=MyApplication.nickname //있어야 함
            }
            else{
                _binding.headerEmail!!.text = "로그인이 필요합니다"
                _binding.headerName!!.text= ""
            }
        }

    }

  /*  override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId === R.id.menu_auth){
            val intent = Intent(this, AuthActivity::class.java)
            if(_binding.headerName!!.text!!.equals("인증")){
                intent.putExtra("data", "logout")
            }
            else{ //이메일, 구글계정
                intent.putExtra("data", "login")
            }
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    } */

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.item1 -> {Log.d("mobileApp", "네비게이션 뷰 메뉴 1")}
            R.id.item2 -> {Log.d("mobileApp", "네비게이션 뷰 메뉴 2")}
            R.id.item3 -> {Log.d("mobileApp", "네비게이션 뷰 메뉴 3")}
            R.id.item4 -> {
                if(item.itemId === R.id.item4){
                    val intent = Intent(this, AuthActivity::class.java)
                    if(_binding.headerEmail!!.text!!.equals("로그인이 필요합니다")){
                        intent.putExtra("data", "logout")
                    }
                    else{ //이메일, 구글계정
                        intent.putExtra("data", "login")
                    }
                    startActivity(intent)
                }
            }
        }
        return true
    }

    }