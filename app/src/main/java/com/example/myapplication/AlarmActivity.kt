package com.example.myapplication

import MYModel
import MyAModel
import MySModel
import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.SharedPreferences
import android.graphics.Color
import android.location.Geocoder
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
import com.naver.maps.geometry.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*
import kotlin.properties.Delegates

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
    // 사용자가 설정한 위치
    private var userSettingLocation: String = "기본 위치"
    // 사용자가 설정한 위치의 TM 좌표
    private var tmX by Delegates.notNull<Double>()
    private var tmY by Delegates.notNull<Double>()
    // 미세먼지와 초미세먼지 정보를 저장할 변수
    private var pm10Value: Double = 0.0
    private var pm25Value: Double = 0.0
    // 알림 내용
    private var contentText: String = "알림 내용:"


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
        val savedLocation = sharedPreferences.getString("saved_location", "") ?: ""

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
        userSettingLocation = savedLocation

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

            saveFineDustInfoSettings()

            // MainActivity로 화면 전환을 위한 코드
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }

        // 액티비티가 알람에 의해 트리거되었는지 확인합니다.
        if (intent?.action == "ACTION_SHOW_NOTIFICATION") {
            // 알림 작업을 여기서 처리
            showNotification()
            // 액티비티를 즉시 종료하여 표시되지 않도록 합니다
            finish()
        }
    }

    private fun saveNotificationSettings() {
        // 사용자가 설정한 시간과 위치를 SharedPreferences에 저장
        val isNotificationEnabled = notificationSwitch.isChecked
        val time = "${timePicker.hour}:${timePicker.minute}"
        val location = locationEditText.text.toString()
        Log.d("AlarmActivity", "SharedPreferences에 저장된 값은 시간은 $time 입니다.")

        val selectedBadStatus =
            findViewById<RadioButton>(notificationBadStatusRadioGroup.checkedRadioButtonId)?.text?.toString()
                ?: ""
        val selectedGoodStatus =
            findViewById<RadioButton>(notificationGoodStatusRadioGroup.checkedRadioButtonId)?.text?.toString()
                ?: ""

        val editor = sharedPreferences.edit()
        editor.putBoolean("notification_enabled", isNotificationEnabled)
        editor.putString("saved_time", time)
        editor.putString("saved_location", location)
        editor.putString("saved_bad_status", selectedBadStatus)
        editor.putString("saved_good_status", selectedGoodStatus)

        editor.apply()
    }

    // 사용자가 설정한 위치에 따른 미세먼지 정보 저장
    private fun saveFineDustInfoSettings() {
        val savedNotificationEnabled = sharedPreferences.getBoolean("notification_enabled", false)
        val savedLocation = sharedPreferences.getString("saved_location", "")

        // 사용자 위치를 TM 좌표로 변환합니다
        getTm()

        // API를 호출하여 TM 좌표를 기반으로 측정소 이름을 가져옵니다
        val call: Call<MYModel> = MyApplication.retroInterface.getRetrofit(
            tmX.toString(),
            tmY.toString(),
            "json",
            "uItfMom3tDSQvZa3Xm2GwUrA5YidOSP4H1qHM/rkupqT9pT5TNa4zyQWdXFnbKlKSqBZsEqJtZrQfYYrPHAwgg==",
            "1.1"
        )

        call?.enqueue(object : retrofit2.Callback<MYModel> {
            override fun onResponse(call: Call<MYModel>, response: Response<MYModel>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && responseBody.response.body.items.isNotEmpty()) {
                        val firstItem = responseBody.response.body.items[0].stationName.toString()

                        // API를 호출하여 선택된 측정소의 미세먼지 정보를 가져옵니다
                        getstationFineDustInfo(firstItem) { pm10Value, pm25Value ->
                            Log.d("stationDust", "첫 번째 item의 stationName: $firstItem")
                            // 미세먼지 정보를 가져왔으므로 알림 내용을 설정합니다.
                            contentText =
                                "알림 내용: $userSettingLocation 의 미세먼지 정보 - PM10: $pm10Value"

                            // AlarmManager를 사용하여 사용자가 설정한 시간에 알림 예약
                            if (savedNotificationEnabled) {
                                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                                val intent = Intent(applicationContext, AlarmActivity::class.java)
                                intent.action = "ACTION_SHOW_NOTIFICATION"
                                val pendingIntent = PendingIntent.getActivity(
                                    applicationContext,
                                    0,
                                    intent,
                                    PendingIntent.FLAG_IMMUTABLE
                                )

                                // timePicker에서 선택한 시간을 가져와서 밀리초로 변환
                                val calendar = Calendar.getInstance()
                                calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                                calendar.set(Calendar.MINUTE, timePicker.minute)
                                calendar.set(Calendar.SECOND, 0) // 초를 0으로 설정하여 정각에 트리거되도록 함
                                val selectedTimeInMillis = calendar.timeInMillis
                                Log.d("AlarmActivity", "밀리초로 변환한 값은 $selectedTimeInMillis 입니다.")

                                // 알림을 선택한 시간에 트리거하도록 알람 설정
                                alarmManager.setExact(
                                    AlarmManager.RTC_WAKEUP,
                                    selectedTimeInMillis,
                                    pendingIntent
                                )
                            } else {
                                // 사용자가 알림을 해제한 경우 이전에 예약한 알림 취소
                                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                                val intent = Intent(applicationContext, AlarmActivity::class.java)
                                intent.action = "ACTION_SHOW_NOTIFICATION"
                                val pendingIntent = PendingIntent.getActivity(
                                    applicationContext,
                                    0,
                                    intent,
                                    PendingIntent.FLAG_NO_CREATE
                                )
                                pendingIntent?.let {
                                    alarmManager.cancel(it)
                                }
                            }
                        }
                    } else {
                        Log.d("stationDust", "측정소 정보를 찾을 수 없습니다.")
                    }
                }
            }

            override fun onFailure(call: Call<MYModel>, t: Throwable) {
                Log.d("stationDust", "측정소 정보를 가져오지 못했습니다.")
                // 측정소 정보를 가져올 수 없는 경우 적절한 조치를 취하거나 오류 메시지를 표시합니다.
            }
        })
    }

    // 사용자가 설정한 위치에 따른 미세먼지 수치 설정 저장
    private fun saveFineDustlevelSettings(){
        val savedNotificationEnabled = sharedPreferences.getBoolean("notification_enabled", false)
        val savedLocation = sharedPreferences.getString("saved_location", "")

        contentText = "알림 내용: $savedLocation 의 미세먼지 정보 - PM10: $pm10Value, PM2.5: $pm25Value"

        // AlarmManager를 사용하여 사용자가 설정한 시간에 알림 예약
        if (savedNotificationEnabled) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, AlarmActivity::class.java)
            intent.action = "ACTION_SHOW_NOTIFICATION"
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            // timePicker에서 선택한 시간을 가져와서 밀리초로 변환
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            calendar.set(Calendar.MINUTE, timePicker.minute)
            calendar.set(Calendar.SECOND, 0) // 초를 0으로 설정하여 정각에 트리거되도록 함
            val selectedTimeInMillis = calendar.timeInMillis
            Log.d("AlarmActivity", "밀리초로 변환한 값은 $selectedTimeInMillis 입니다.")

            // 알림을 선택한 시간에 트리거하도록 알람 설정
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, selectedTimeInMillis, pendingIntent)
        } else {
            // 사용자가 알림을 해제한 경우 이전에 예약한 알림 취소
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, AlarmActivity::class.java)
            intent.action = "ACTION_SHOW_NOTIFICATION"
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_NO_CREATE)
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

    private fun showNotification() {
        // 알림 채널 생성 (Android 8.0 이상에서 필요)
        createNotificationChannel()

        // 사용자가 설정한 시간과 위치 정보 가져오기
        val location = locationEditText.text.toString()

        // 알림 생성
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("알림 제목")
            .setContentText(contentText) // contentText 변수 사용
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)

        // 알림을 띄우기 전에 이전에 생성된 알림을 취소합니다.
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.cancel(NOTIFICATION_ID)

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
    }

    private fun createNotificationChannel() {
        // Android 8.0 이상에서 알림 채널을 생성해야 함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "NotificationChannelName"
            val descriptionText = "Notification Channel Description"
            val importance = NotificationManager.IMPORTANCE_HIGH // 중요도 설정을 IMPORTANCE_HIGH로 변경
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

    // 사용자가 설정한 위치를 위도, 경도 좌표로 변환
    private fun convertAddressToLatLng(): LatLng {
        val geocoder = Geocoder(context, Locale.KOREA)

        try {
            val addressList = geocoder.getFromLocationName(userSettingLocation, 1)

            if (addressList != null && addressList.isNotEmpty()) {
                val latitude = addressList[0].latitude
                val longitude = addressList[0].longitude
                return LatLng(latitude, longitude)
            } else {
                throw IllegalArgumentException("Invalid address or unable to find location for the given address: $userSettingLocation")
            }
        } catch (e: IOException) {
            throw IllegalArgumentException("Error converting address to LatLng: ${e.message}")
        }
    }

    // TM 좌표로 변환
    private fun getTm() {
        // 1. 사용자 입력 위치 정보를 WGS84 좌표로 변환
        val userLatLng = convertAddressToLatLng()

        // 2. 변환된 WGS84 좌표를 TM 좌표로 변환합니다.
        tmX = userLatLng.longitude
        tmY = userLatLng.latitude
    }

    private fun getstationFineDustInfo(stationName: String, callback: (pm10Value: Double, pm25Value: Double) -> Unit) {
        val call: Call<MyAModel> = MyApplication.retroInterface3.getRetrofit3(
            stationName,
            "month",
            "1",
            "100",
            "json",
            "uItfMom3tDSQvZa3Xm2GwUrA5YidOSP4H1qHM/rkupqT9pT5TNa4zyQWdXFnbKlKSqBZsEqJtZrQfYYrPHAwgg=="
        )

        call.enqueue(object : Callback<MyAModel> {
            override fun onResponse(call: Call<MyAModel>, response: Response<MyAModel>) {
                if (response.isSuccessful) {
                    val fineDustData = response.body()
                    if (fineDustData != null && fineDustData.response.body.items.isNotEmpty()) {
                        pm10Value = fineDustData.response.body.items[0].pm10Value?.toDouble() ?: 0.0
                        pm25Value = fineDustData.response.body.items[0].pm25Value?.toDouble() ?: 0.0
                        callback(pm10Value, pm25Value)
                    }
                }
            }
            override fun onFailure(call: Call<MyAModel>, t: Throwable) {
                Log.d("fineDust", "미세먼지 정보를 가져오지 못했습니다.")
                // 미세먼지 정보를 가져오지 못해도 콜백을 호출하여 빈 값으로 처리
                callback(0.0, 0.0)
            }
        })
    }
}