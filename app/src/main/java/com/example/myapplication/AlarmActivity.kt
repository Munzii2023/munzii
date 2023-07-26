package com.example.myapplication

import android.Manifest
import android.app.*
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
import com.gun0912.tedpermission.provider.TedPermissionProvider.context
import java.util.*

class AlarmActivity : AppCompatActivity() {

    private lateinit var notificationSwitch: Switch
    private lateinit var notificationSettingsLayout: LinearLayout
    private lateinit var locationEditText: EditText
    private lateinit var timePicker: TimePicker
    private lateinit var notificationStatusRadioGroup: RadioGroup
    private lateinit var notificationBadStatusRadioGroup: RadioGroup
    private lateinit var notificationGoodStatusRadioGroup: RadioGroup
    private lateinit var radioButtonBad: RadioButton
    private lateinit var radioButtonVeryBad: RadioButton
    private lateinit var radioButtonNomal: RadioButton
    private lateinit var radioButtonGood: RadioButton
    private lateinit var radioButtonVeryGood: RadioButton
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
        notificationBadStatusRadioGroup = findViewById(R.id.notificationBadStatusRadioGroup)
        notificationGoodStatusRadioGroup = findViewById(R.id.notificationGoodStatusRadioGroup)
        radioButtonBad = findViewById(R.id.radioButtonBad)
        radioButtonVeryBad = findViewById(R.id.radioButtonVeryBad)
        radioButtonNomal = findViewById(R.id.radioButtonNomal)
        radioButtonGood = findViewById(R.id.radioButtonGood)
        radioButtonVeryGood = findViewById(R.id.radioButtonVeryGood)
        saveButton = findViewById(R.id.saveButton)

        // SharedPreferences 객체 초기화
        sharedPreferences = getSharedPreferences("NotificationSettings", MODE_PRIVATE)

        // 저장된 설정을 가져와서 스위치와 설정값을 보여줍니다.
        val isNotificationEnabled = sharedPreferences.getBoolean("notification_enabled", false)
        notificationSwitch.isChecked = isNotificationEnabled

        if (isNotificationEnabled) {
            notificationSettingsLayout.visibility = View.VISIBLE
        } else {
            notificationSettingsLayout.visibility = View.GONE
        }


        val savedTime = sharedPreferences.getString("saved_time", "")
        val savedLocation = sharedPreferences.getString("saved_location", "")

        val savedBadStatus = sharedPreferences.getString("saved_bad_status", "")
        val savedGoodStatus = sharedPreferences.getString("saved_good_status", "")

        // 저장된 값이 있는지 확인하고 해당하는 라디오 버튼을 선택합니다
        if (savedBadStatus != null && savedBadStatus.isNotEmpty()) {
            when (savedBadStatus) {
                "나쁨" -> radioButtonBad.isChecked = true
                "매우 나쁨" -> radioButtonVeryBad.isChecked = true
            }
        }

        if (savedGoodStatus != null && savedGoodStatus.isNotEmpty()) {
            when (savedGoodStatus) {
                "보통" -> radioButtonNomal.isChecked = true
                "좋음" -> radioButtonGood.isChecked = true
                "매우 좋음" -> radioButtonVeryGood.isChecked = true
            }
        }

        // 가져온 값을 각각의 UI에 설정
        if (savedTime != null && savedTime.isNotEmpty()) {
            val timeParts = savedTime.split(":")
            timePicker.hour = timeParts[0].toInt()
            timePicker.minute = timeParts[1].toInt()
        }

        locationEditText.setText(savedLocation)

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

            // MainActivity로 화면 전환을 위한 코드
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }

    }

    private fun saveNotificationSettings() {
        // 사용자가 설정한 시간과 위치를 SharedPreferences에 저장
        val isNotificationEnabled = notificationSwitch.isChecked
        val time = "${timePicker.hour}:${timePicker.minute}"
        val location = locationEditText.text.toString()
        val selectedBadStatus =
            findViewById<RadioButton>(notificationBadStatusRadioGroup.checkedRadioButtonId)?.text?.toString() ?: ""
        val selectedGoodStatus =
            findViewById<RadioButton>(notificationGoodStatusRadioGroup.checkedRadioButtonId)?.text?.toString() ?: ""

        val editor = sharedPreferences.edit()
        editor.putBoolean("notification_enabled", isNotificationEnabled)
        editor.putString("saved_time", time)
        editor.putString("saved_location", location)
        editor.putString("saved_bad_status", selectedBadStatus)
        editor.putString("saved_good_status", selectedGoodStatus)

        editor.apply()

        // AlarmManager를 사용하여 알림 예약
        if (isNotificationEnabled) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val intent = Intent(this,AlarmActivity::class.java)
            intent.action = "ACTION_SHOW_NOTIFICATION"
            val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            // timePicker에서 선택한 시간을 가져와서 밀리초로 변환
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            calendar.set(Calendar.MINUTE, timePicker.minute)
            val selectedTimeInMillis = calendar.timeInMillis

            // 알림을 선택한 시간에 트리거하도록 알람 설정
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, selectedTimeInMillis, pendingIntent)
        } else {
            // 사용자가 알림을 해제한 경우 이전에 예약한 알림 취소
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, AlarmActivity::class.java)
            intent.action = "ACTION_SHOW_NOTIFICATION"
            val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
            pendingIntent?.let {
                alarmManager.cancel(it)
            }
        }
    }

    private fun displaySavedTimeAndLocation() {
        // SharedPreferences에서 저장된 시간과 위치를 가져옵니다
        val savedTime = sharedPreferences.getString("saved_time", "")
        val savedLocation = sharedPreferences.getString("saved_location", "")

        // 저장된 시간과 위치를 Log로 출력합니다
        Log.d("AlarmActivity", "저장된 시간: $savedTime, 저장된 위치: $savedLocation")
    }

    /*
    private fun showNotification() {
        // 알림 채널 생성 (Android 8.0 이상에서 필요)
        createNotificationChannel()

        // 사용자가 설정한 시간과 위치 정보 가져오기
        val time = "${timePicker.hour}:${timePicker.minute}"
        val location = locationEditText.text.toString()

        // 알림 내용 설정
        val contentText = "알림 내용: 시간 - $time, 위치 - $location"

        // 알림 생성
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("알림 제목")
            .setContentText(contentText) // 알림 내용 설정
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

        // 알림 생성
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())

    }
    */
    private fun createNotificationChannel() {
        // Android 8.0 이상에서 알림 채널을 생성해야 함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "NotificationChannelName"
            val descriptionText = "Notification Channel Description"
            val importance = NotificationManager.IMPORTANCE_HIGH // 중요도 설정을 IMPORTANCE_HIGH로 변경
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(ㅇtrue)
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