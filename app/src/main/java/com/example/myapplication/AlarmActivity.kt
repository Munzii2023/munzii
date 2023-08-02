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
import android.graphics.BitmapFactory
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
import androidx.lifecycle.lifecycleScope
import com.gun0912.tedpermission.provider.TedPermissionProvider.context
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.hyosang.coordinate.CoordPoint
import kr.hyosang.coordinate.TransCoord
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
    private lateinit var notificationStatus: LinearLayout
    private lateinit var checkBoxBad: CheckBox
    private lateinit var checkBoxVeryBad: CheckBox
    private lateinit var checkBoxNormal: CheckBox
    private lateinit var checkBoxGood: CheckBox
    private lateinit var saveButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var context: Context
    // 알림 내용
    private var contentText: String = "알림내용: "
    // 사용자가 설정한 위치의 TM 좌표
    private var tmX by Delegates.notNull<Double>()
    private var tmY by Delegates.notNull<Double>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        // context 변수 초기화
        context = this // this는 AlarmActivity의 Context를 가리킵니다.

        notificationSwitch = findViewById(R.id.notificationSwitch)
        notificationSettingsLayout = findViewById(R.id.notificationSettingsLayout)
        locationEditText = findViewById(R.id.locationEditText)
        timePicker = findViewById(R.id.timePicker)
        notificationStatus = findViewById(R.id.notificationStatus)
        checkBoxBad = findViewById(R.id.checkBoxBad)
        checkBoxVeryBad = findViewById(R.id.checkBoxVeryBad)
        checkBoxNormal = findViewById(R.id.checkBoxNormal)
        checkBoxGood = findViewById(R.id.checkBoxGood)
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

        val savedFineDustStatus = sharedPreferences.getString("saved_fine_dust_status", "")

        // 저장된 값이 있는지 확인하고 해당하는 체크 박스를 선택합니다
        if (savedFineDustStatus != null && savedFineDustStatus.isNotEmpty()) {
            val statusList = savedFineDustStatus.split(",")
            for (status in statusList) {
                when (status) {
                    "매우 나쁨" -> checkBoxVeryBad.isChecked = true
                    "나쁨" -> checkBoxBad.isChecked = true
                    "보통" -> checkBoxNormal.isChecked = true
                    "좋음" -> checkBoxGood.isChecked = true
                }
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

            saveFineDustInfoSettings()
            saveFineDustLevelSettings()
        }
    }

    private fun saveNotificationSettings() {
        // 사용자가 설정한 시간과 위치를 SharedPreferences에 저장
        val isNotificationEnabled = notificationSwitch.isChecked
        val time = "${timePicker.hour}:${timePicker.minute}"
        val location = locationEditText.text.toString()

        // 체크박스의 값 확인
        val checkBoxGoodChecked = checkBoxGood.isChecked
        val checkBoxNormalChecked = checkBoxNormal.isChecked
        val checkBoxBadChecked = checkBoxBad.isChecked
        val checkBoxVeryBadChecked = checkBoxVeryBad.isChecked

        // 알림 받을 상태 선택 확인
        var selectedFineDustStatus = ""
        if (checkBoxGoodChecked) {
            selectedFineDustStatus += "좋음,"
        }
        if (checkBoxNormalChecked) {
            selectedFineDustStatus += "보통,"
        }
        if (checkBoxBadChecked) {
            selectedFineDustStatus += "나쁨,"
        }
        if (checkBoxVeryBadChecked) {
            selectedFineDustStatus += "매우 나쁨"
        }

        val editor = sharedPreferences.edit()
        editor.putBoolean("notification_enabled", isNotificationEnabled)
        editor.putString("saved_time", time)
        editor.putString("saved_location", location)
        editor.putString("saved_fine_dust_status", selectedFineDustStatus)

        editor.apply()
    }

    // 사용자가 설정한 위치에 따른 미세먼지 정보 저장
    private fun saveFineDustInfoSettings() {
        val savedNotificationEnabled = sharedPreferences.getBoolean("notification_enabled", false)
        val savedLocation = sharedPreferences.getString("saved_location", "")

        findNearestStation { firstItem ->
            val station = firstItem

            getstationFineDustInfo(station) { pm10value ->
                val fineDustStatus: String = when (pm10value.toInt()) {
                    in 0..15 -> "좋음"
                    in 16..35 -> "보통"
                    in 36..75 -> "나쁨"
                    else -> "매우 나쁨"
                }

                contentText = "오늘의 미세먼지는 $fineDustStatus"
                Log.d("contentText", contentText)

                // AlarmManager를 사용하여 사용자가 설정한 시간에 알림 예약
                if (savedNotificationEnabled) {
                    val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                    val intent = Intent(this, AlarmActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

                    // timePicker에서 선택한 시간을 가져와서 밀리초로 변환
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                    calendar.set(Calendar.MINUTE, timePicker.minute)
                    calendar.set(Calendar.SECOND, 0) // 초를 0으로 설정하여 정각에 트리거되도록 함
                    val selectedTimeInMillis = calendar.timeInMillis

                    // 알림을 선택한 시간에 트리거하도록 알람 설정
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, selectedTimeInMillis, pendingIntent)

                    // 알림을 클릭했을 때, InfoActivity로 이동하도록 PendingIntent 설정
                    val infoIntent = Intent(this@AlarmActivity, InfoActivity::class.java)
                    intent.putExtra("pm10value", pm10value)
                    intent.putExtra("stationvalue", station)
                    intent.putExtra("addressvalue", savedLocation)

                    val infoPendingIntent = PendingIntent.getActivity(
                        this@AlarmActivity, 0, infoIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                } else {
                    // 사용자가 알림을 해제한 경우 이전에 예약한 알림 취소
                    val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                    val intent = Intent(this, AlarmActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_NO_CREATE)
                    pendingIntent?.let {
                        alarmManager.cancel(it)
                    }
                }
                // 사용자가 설정한 시간에 알림 예약하고, 알림을 표시하기 위해 showNotification() 함수를 호출합니다.
                showNotification(fineDustStatus)
            }
        }
    }

    // 사용자가 설정한 위치에 따른 미세먼지 수치 설정 저장
    private fun saveFineDustLevelSettings(){
        val savedNotificationEnabled = sharedPreferences.getBoolean("notification_enabled", false)
        val savedLocation = sharedPreferences.getString("saved_location", "")

        findNearestStation { firstItem ->
            val station = firstItem

            getstationFineDustInfo(station) { pm10value ->
                val fineDustStatus: String = when (pm10value.toInt()) {
                    in 0..15 -> "좋음"
                    in 16..35 -> "보통"
                    in 36..75 -> "나쁨"
                    else -> "매우 나쁨"
                }

                // 사용자가 설정한 미세먼지 상태를 가져옵니다.
                val prevFineDustStatus = sharedPreferences.getString("saved_fine_dust_status", "")

                if (savedNotificationEnabled) {
                    if(prevFineDustStatus != fineDustStatus) {
                        // 알림 내용 설정
                        contentText = "현재의 미세먼지는 $fineDustStatus 입니다"

                        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                        val intent = Intent(this, AlarmActivity::class.java)
                        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

                    }
                    // 알림을 클릭했을 때, InfoActivity로 이동하도록 PendingIntent 설정
                    val infoIntent = Intent(this@AlarmActivity, InfoActivity::class.java)
                    intent.putExtra("pm10value", pm10value)
                    intent.putExtra("stationvalue", station)
                    intent.putExtra("addressvalue", savedLocation)

                    val infoPendingIntent = PendingIntent.getActivity(
                        this@AlarmActivity, 0, infoIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                } else {
                    // 사용자가 알림을 해제한 경우 이전에 예약한 알림 취소
                    val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                    val intent = Intent(this, AlarmActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_NO_CREATE)
                    pendingIntent?.let {
                        alarmManager.cancel(it)
                    }
                }
                // 사용자가 설정한 시간에 알림 예약하고, 알림을 표시하기 위해 showNotification() 함수를 호출합니다.
                showNotification(fineDustStatus)
            }
        }
    }


    private fun displaySavedTimeAndLocation() {
        // SharedPreferences에서 저장된 시간과 위치를 가져옵니다
        val savedTime = sharedPreferences.getString("saved_time", "")
        val savedLocation = sharedPreferences.getString("saved_location", "")
        val savedFinedustStatus = sharedPreferences.getString("saved_fine_dust_status", "")

        // 저장된 시간과 위치를 Log로 출력합니다
        Log.d("sharedpreference", "저장된 상태 : $savedFinedustStatus")
    }

    private fun showNotification(fineDustStatus: String) {
        // 알림 채널 생성 (Android 8.0 이상에서 필요)
        createNotificationChannel()

        val savedLocation = sharedPreferences.getString("saved_location", "")

        val imageResId = when {
            fineDustStatus.isNotEmpty() -> when {
                fineDustStatus == "좋음" -> R.drawable.marker_good
                fineDustStatus == "보통" -> R.drawable.marker_soso
                fineDustStatus == "나쁨" -> R.drawable.marker_bad
                else -> R.drawable.marker_terri
            }
            else -> R.drawable.smile
        }

        // 알림 생성
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(savedLocation)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setLargeIcon(BitmapFactory.decodeResource(resources, imageResId))
            .setStyle(NotificationCompat.BigTextStyle().bigText("$savedLocation - $contentText")) // 알림 내용 옆에 이미지 표시
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
        val geocoder = Geocoder(context, Locale.KOREA) // context를 사용하여 Geocoder 초기화
        val userSettingLocation = locationEditText.text.toString()

        try {
            val addressList = geocoder.getFromLocationName(userSettingLocation, 1)

            if (addressList != null && addressList.isNotEmpty()) {
                val latitude = addressList[0].latitude
                val longitude = addressList[0].longitude
                Log.d("위도는 $latitude , 경도는 $longitude 입니다.", "")
                return LatLng(latitude, longitude)
            } else {
                throw IllegalArgumentException("Invalid address or unable to find location for the given address: $userSettingLocation")
            }
        } catch (e: IOException) {
            throw IllegalArgumentException("Error converting address to LatLng: ${e.message}")
        }
    }


    private fun getTm() {
        // 1. 사용자 입력 위치 정보를 위도, 경도 좌표로 변환
        val userLatLng = convertAddressToLatLng()

        // 2. 변환된 위도, 경도 좌표를 TM 좌표로 변환합니다.
        val wgsPt = CoordPoint(userLatLng.longitude, userLatLng.latitude)
        val tmPt = TransCoord.getTransCoord(wgsPt, TransCoord.COORD_TYPE_WGS84, TransCoord.COORD_TYPE_TM)

        // 3. TM 좌표로 변환된 값을 tmX와 tmY에 저장합니다.
        tmX = tmPt.x
        tmY = tmPt.y
        Log.d("tm", "$tmX,$tmY")
    }


    private fun findNearestStation(onStationDustComplete: (String) -> Unit) {
        getTm()
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
                    if (responseBody != null) {
                        val firstItem = responseBody.response.body.items[0].stationName
                        onStationDustComplete(firstItem.toString())
                        Log.d("stationDust", "해당 위치의 stationName: ${firstItem.toString()}")
                    } else {
                        Log.d("stationDust", "items 리스트가 비어있습니다.")
                    }
                }
            }

            override fun onFailure(call: Call<MYModel>, t: Throwable) {
                Log.d("stationDust", "측정소 정보를 가져오지 못했습니다.")
            }
        })
    }

    private fun getstationFineDustInfo(stationName: String, callback: (pm10: String) -> Unit) {
        val call: Call<MySModel> = MyApplication.retroInterface2.getRetrofit2(
            stationName, //측정소이름
            "month",
            "1",
            "100",
            "json",
            "uItfMom3tDSQvZa3Xm2GwUrA5YidOSP4H1qHM/rkupqT9pT5TNa4zyQWdXFnbKlKSqBZsEqJtZrQfYYrPHAwgg=="
        ) //call 객체에 초기화

        call?.enqueue(object: retrofit2.Callback<MySModel> {
            override fun onResponse(call: Call<MySModel>, response: Response<MySModel>) {
                if(response.isSuccessful) {
                    val pm10value= response.body()?.response?.body?.items?.get(0)?.pm10Value
                    Log.d("fineDust", "$pm10value")
                    callback(pm10value.toString())
                }
            }
            override fun onFailure(call: Call<MySModel>, t: Throwable) {
                Log.d("fineDust", "미세먼지 정보를 가져오지 못했습니다.")
            }
        })
    }
}
