package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.SharedPreferences
import android.graphics.Color
import android.util.Log
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class AlarmActivity : AppCompatActivity() {

    private lateinit var notificationSwitch: Switch
    private lateinit var notificationSettingsLayout: LinearLayout
    private lateinit var locationEditText: EditText
    private lateinit var timePicker: TimePicker
    private lateinit var notificationStatusRadioGroup: RadioGroup
    private lateinit var saveButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        notificationSwitch = findViewById(R.id.notificationSwitch)
        notificationSettingsLayout = findViewById(R.id.notificationSettingsLayout)
        locationEditText = findViewById(R.id.locationEditText)
        timePicker = findViewById(R.id.timePicker)
        notificationStatusRadioGroup = findViewById(R.id.notificationStatusRadioGroup)
        saveButton = findViewById(R.id.saveButton)

        // SharedPreferences 객체 초기화
        sharedPreferences = getSharedPreferences("NotificationSettings", MODE_PRIVATE)

        // Switch 상태 변경 이벤트 처리
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Switch 버튼이 Off일 때는 다른 기능들을 숨기고, On일 때는 보이도록 처리
            if (isChecked) {
                notificationSettingsLayout.visibility = View.VISIBLE
            } else {
                notificationSettingsLayout.visibility = View.GONE
            }
        }

        // 저장 버튼 클릭 이벤트 처리
        saveButton.setOnClickListener {
            saveNotificationSettings()

            // SharedPreferences에서 저장된 시간과 위치를 출력합니다 (저장확인용)
            displaySavedTimeAndLocation()

            Toast.makeText(this, "저장되었습니다", Toast.LENGTH_SHORT).show()

            // SharedPreferences에 저장된 값을 불러와서 UI에 설정
            val savedTime = sharedPreferences.getString("saved_time", "")

            val savedLocation = sharedPreferences.getString("saved_location", "")

            // 가져온 값을 각각의 UI에 설정
            if (savedTime != null) {
                if (savedTime.isNotEmpty()) {
                    val timeParts = savedTime.split(":")
                    timePicker.hour = timeParts[0].toInt()
                    timePicker.minute = timeParts[1].toInt()
                }
            }

            locationEditText.setText(savedLocation)

            // 알림 설정 정보가 저장되면 알림을 생성하고 보여줌
            showNotification()

            // MainActivity로 화면 전환을 위한 코드
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }
    }

    private fun saveNotificationSettings() {
        // 사용자가 설정한 시간과 위치를 SharedPreferences에 저장
        val time = "${timePicker.hour}:${timePicker.minute}"
        val location = locationEditText.text.toString()

        val editor = sharedPreferences.edit()
        editor.putString("saved_time", time)
        editor.putString("saved_location", location)
        editor.apply()
    }

    private fun showNotification() {
        // 알림 채널 생성 (Android 8.0 이상에서 필요)
        createNotificationChannel()

        // 사용자가 설정한 시간과 위치 정보 가져오기
        val time = "${timePicker.hour}:${timePicker.minute}"
        val location = locationEditText.text.toString()

        // 알림 생성
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("알림 제목")
            .setContentText("알림 내용: 시간 - $time, 위치 - $location")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)

        // 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.FOREGROUND_SERVICE
            )

            // 권한이 허용되었는지 확인
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                // 알림 보여주기
                val notificationManager = NotificationManagerCompat.from(this)
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
            } else {
                // 권한이 허용되지 않았다면 권한 요청
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.FOREGROUND_SERVICE),
                    PERMISSION_REQUEST_CODE
                )
            }
        }

        // 알림을 누르면 알림 설정 화면인 MainActivity로 이동하도록 PendingIntent 설정
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        notificationBuilder.setContentIntent(pendingIntent)

    }

    private fun displaySavedTimeAndLocation() {
        // SharedPreferences에서 저장된 시간과 위치를 가져옵니다
        val savedTime = sharedPreferences.getString("saved_time", "")
        val savedLocation = sharedPreferences.getString("saved_location", "")

        // 저장된 시간과 위치를 Log로 출력합니다
        Log.d("AlarmActivity", "저장된 시간: $savedTime, 저장된 위치: $savedLocation")
    }

    private fun createNotificationChannel() {
        // Android 8.0 이상에서 알림 채널을 생성해야 함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "NotificationChannelName"
            val descriptionText = "Notification Channel Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }

            // 알림 채널을 NotificationManager에 등록
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "MyChannelId"
        const val NOTIFICATION_ID = 1
        private const val PERMISSION_REQUEST_CODE = 100
    }
}