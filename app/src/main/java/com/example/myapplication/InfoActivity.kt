package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Display
import android.view.Gravity
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityInfoBinding
import java.io.File
import kotlin.math.round


class InfoActivity : AppCompatActivity() {
    private var width: Int = 0
    private var height: Int = 0
    private lateinit var binding: ActivityInfoBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //info의 각 정보 변수와 main에서의 실질적인 값 연결
        val addressvalue = intent.getStringExtra("addressvalue")
        val pm10value = intent.getStringExtra("pm10value")
        val pm25value = intent.getStringExtra("pm25value")
        val stationvalue = intent.getStringExtra("stationvalue")

        //activity_info.xml과 연결
        binding.addressValue.text = addressvalue
        binding.pm10Value.text = pm10value
        binding.pm25Value.text = pm25value
        binding.stationValue.text = stationvalue

        val imageView = findViewById<ImageView>(R.id.imageview)

        // 이미지 리소스 ID를 가져오기 위해 pm10value에 따라 적절한 이미지 선택
        val imageResId = when {
            pm10value != null -> when {
                pm10value <= "15" -> R.drawable.marker_good
                pm10value <= "35" -> R.drawable.marker_soso
                pm10value <= "75" -> R.drawable.marker_bad
                else -> R.drawable.marker_terri
            }
            else -> R.drawable.smile // pm10value가 null일 경우 기본 이미지 사용
        }

        // 리소스 ID를 사용하여 이미지뷰의 이미지를 변경
        imageView.setImageResource(imageResId)



    }

    //다른 화면을 누르면 InfoActivity 종료
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        setResult(Activity.RESULT_CANCELED)
        finish()
        return true
    }

    override fun onResume() {
        super.onResume()
        initLayout()
    }

    private fun initLayout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API Level 30 버전
            val windowMetrics = windowManager.currentWindowMetrics
            width = windowMetrics.bounds.width()
            height = windowMetrics.bounds.height()
        } else { // API Level 30 이전 버전
            val display: Display = windowManager.defaultDisplay
            val displayMetrics = DisplayMetrics()
            display.getRealMetrics(displayMetrics)
            width = displayMetrics.widthPixels
            height = displayMetrics.heightPixels
        }
        window.setLayout(round(width * 0.9).toInt(), round(height * 0.2).toInt())
        window.setGravity(Gravity.BOTTOM)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}