package com.example.myapplication

import DeliveryItem
import SensorData
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.ActivitySecondBinding
import com.example.myapplication.databinding.NavigationHeaderBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*
import kotlin.properties.Delegates

class SecondActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var binding: ActivitySecondBinding
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private var PERMISSION_REQUEST_CODE = 100;
    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private lateinit var mLocationSource: FusedLocationSource
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var marker : Marker

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val mMarkerList = arrayOfNulls<Marker>(700) //공공데이터에서 불러오는 미세먼지 마커들

    //현재 위치 저장
    private var lat by Delegates.notNull<Double>()
    private var lon by Delegates.notNull<Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySecondBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.btnPublicData.setOnClickListener {
            // SecondActivity를 종료하여 MainActivity로 이동
            //val intent = Intent(this, MainActivity::class.java)
            //startActivity(intent)
            finish()
        }
        apiTest()


        // 네이버 지도
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapView = findViewById(R.id.navermap_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // 위치를 반환하는 구현체인 FusedLocationSource 생성
        mLocationSource = FusedLocationSource(this, PERMISSION_REQUEST_CODE)

        binding.mapsSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                // 검색 버튼 누를 때 호출
                query?.let {
                    val latLng = getLatLngFromAddress(it)
                    if (latLng != null) {
                        // 마커 위치 변경
                        marker.position = latLng
                        naverMap.moveCamera(CameraUpdate.scrollTo(latLng))

                        // 주소 가져오기
                        val address = getAddress(latLng.latitude, latLng.longitude)
                        Log.d("mobileApp", address)
                    } else {
                        // 주소를 찾을 수 없는 경우 처리
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 검색창에서 글자가 변경이 일어날 때마다 호출
                return true
            }
        })
    }

    private fun apiTest() { //미세먼지 API 불러오기
        // Retrofit 초기화
        val retrofit = Retrofit.Builder()
            .baseUrl("https://dffg4e1d3b.execute-api.ap-northeast-2.amazonaws.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // ApiService 인터페이스 구현체 생성
        val apiService = retrofit.create(Deliveryinterface::class.java)

        // API 호출
        val call = apiService.getRetrofit5()
        call.enqueue(object : Callback<SensorData> {
            override fun onResponse(call: Call<SensorData>, response: Response<SensorData>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("deliverytest", "${call.request()}")
                    Log.d("deliverytest", "${response.body()}")
                    val pm10value= response.body()?.body?.get(0)?.pm10value
                    val pm25value= response.body()?.body?.get(0)?.pm25value

                    if (responseBody != null){
                        coroutineScope.launch {
                            for (i in responseBody.body.indices) {
                                val pm10 = responseBody.body[i].pm10value?.toInt()
                                val pm10_ =  responseBody.body[i].pm10value
                                val pm25 = responseBody.body[i].pm25value?.toInt()
                                val pm25_ =  responseBody.body[i].pm10value


                                mMarkerList[i] = Marker()
                                if (pm10 != null) {
                                    mMarkerList[i]?.width = 100
                                    mMarkerList[i]?.height = 100
                                    if (pm10!! <= 15) { //0~15 미세먼지 굿
                                        val lat = responseBody.body[i].latitude
                                        val lon = responseBody.body[i].longtitude
                                        val latlng = LatLng(lat.toDouble(), lon.toDouble())
                                        mMarkerList[i]?.position = latlng
                                        mMarkerList[i]?.icon =
                                            OverlayImage.fromResource(R.drawable.marker_good)
                                        mMarkerList[i]?.map = naverMap
                                    } else if (pm10!! <= 35 && pm10!! > 15) { //15~35
                                        val lat = responseBody.body[i].latitude
                                        val lon = responseBody.body[i].longtitude
                                        val latlng = LatLng(lat.toDouble(), lon.toDouble())
                                        mMarkerList[i]?.position = latlng
                                        mMarkerList[i]?.icon =
                                            OverlayImage.fromResource(R.drawable.marker_soso)
                                        mMarkerList[i]?.map = naverMap
                                    } else if ((pm10!! <= 75) && (pm10!! > 35)) {// 35~75
                                        val lat = responseBody.body[i].latitude
                                        val lon = responseBody.body[i].longtitude
                                        val latlng = LatLng(lat.toDouble(), lon.toDouble())
                                        mMarkerList[i]?.position = latlng
                                        mMarkerList[i]?.icon =
                                            OverlayImage.fromResource(R.drawable.marker_bad)
                                        mMarkerList[i]?.map = naverMap
                                    } else { //75~
                                        val lat = responseBody.body[i].latitude
                                        val lon = responseBody.body[i].longtitude
                                        val latlng = LatLng(lat.toDouble(), lon.toDouble())
                                        mMarkerList[i]?.position = latlng
                                        mMarkerList[i]?.icon =
                                            OverlayImage.fromResource(R.drawable.marker_terri)
                                        mMarkerList[i]?.map = naverMap
                                    }
                                }
                                //마커에 InfoActivity 클릭리스너
                                mMarkerList[i]?.setOnClickListener(object : Overlay.OnClickListener {
                                    override fun onClick(overlay: Overlay): Boolean {
                                        val intent = Intent(this@SecondActivity, InfoActivity::class.java)
                                        intent.putExtra("pm10value", pm10_)
                                        intent.putExtra("pm25value", pm25_)
                                        intent.putExtra("addressvalue", getAddress(mMarkerList[i]?.position!!.latitude, mMarkerList[i]?.position!!.longitude))

                                        startActivity(intent)
                                        return true
                                    }
                                })

                            }
                        }
                    }

                } else {
                    // 오류 처리
                }
            }

            override fun onFailure(call: Call<SensorData>, t: Throwable) {
                Log.d("mobileApp", "${t.toString()}")
            }
        })
    }

    override fun onMapReady(naverMap: NaverMap) { //네이버 지도의 이벤트를 처리하는 콜백함수

        // NaverMap 객체 받아서 NaverMap 객체에 위치 소스 지정
        // 지도상에 마커 표시
        marker = Marker()
        marker.position = LatLng( //마커가 위치한 좌표!!!!! => 여기 기준으로 주소 설정할 수 있도록 해야함
            naverMap.cameraPosition.target.latitude,
            naverMap.cameraPosition.target.longitude
        )
        marker.icon = OverlayImage.fromResource(R.drawable.baseline_place_24)
        marker.map = naverMap

        this.naverMap = naverMap
        naverMap.locationSource = mLocationSource
        naverMap.setLocationSource(mLocationSource)

        // 현재 위치 버튼 기능
        naverMap.uiSettings.isLocationButtonEnabled = true
        // 위치를 추적하면서 카메라도 따라 움직인다.
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        // 권한확인. 결과는 onRequestPermissionsResult 콜백 매서드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE)


        //마커 위치 근방의 택배차량 미세먼지 정보(Bounds를 설정하고 해당 영역 내의 데이터만 가져오는 코드를 작성)

        /* val cameraPosition = CameraPosition(
            LatLng(37.65178832823347, 127.01614801495204), //위치 지정
            16.0 // 줌레벨 => 추가로 기울임 각도, 방향 설정 가능
        )
        naverMap.cameraPosition = cameraPosition  //최초위치 설정 */

        // 카메라의 움직임에 대한 이벤트 리스너 인터페이스.
        naverMap.addOnCameraChangeListener { reason, animated ->
            Log.i("NaverMap", "카메라 변경 - reson: $reason, animated: $animated")
            marker.position = LatLng(
                // 현재 보이는 네이버맵의 정중앙 가운데로 마커 이동
                naverMap.cameraPosition.target.latitude,
                naverMap.cameraPosition.target.longitude
            )
        }

        // 카메라의 움직임 종료에 대한 이벤트 리스너 인터페이스.
        naverMap.addOnCameraIdleListener {
            marker.position = LatLng(
                naverMap.cameraPosition.target.latitude,
                naverMap.cameraPosition.target.longitude
            )
            Log.d("mobileApp", getAddress(naverMap.cameraPosition.target.latitude, naverMap.cameraPosition.target.longitude))
        }

        var currentLocation: Location?
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLocation = location
                    // 위치 오버레이의 가시성은 기본적으로 false로 지정되어 있습니다. 가시성을 true로 변경하면 지도에 위치 오버레이가 나타납니다.
                    // 파랑색 점, 현재 위치 표시
                    naverMap.locationOverlay.run {
                        isVisible = true
                        position = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)

                    }

                    // 카메라 현재위치로 이동
                    val cameraUpdate = CameraUpdate.scrollTo(
                        LatLng(
                            currentLocation!!.latitude,
                            currentLocation!!.longitude
                        )
                    )
                    naverMap.moveCamera(cameraUpdate)

                    // 빨간색 마커 현재위치로 변경
                    marker.position = LatLng(
                        naverMap.cameraPosition.target.latitude,
                        naverMap.cameraPosition.target.longitude
                    )
                }
            }

        naverMap.addOnLocationChangeListener { location ->
            lat = location.latitude
            lon = location.longitude
            //setMark(marker, lat, lon, R.drawable.baseline_place_24)
            //Log.d("mobileApp", getAddress(lat, lon))
        }

    }

    private fun getSido(address : String) {
        val words = address.split("\\s".toRegex()).toTypedArray()
        Log.d("mobileApp", words[2]) //현위치 구 불러오기

    }

    // 좌표 -> 주소 변환
    private fun getAddress(lat: Double, lng: Double): String {
        val geoCoder = Geocoder(this, Locale.KOREA)
        val address: ArrayList<Address>
        var addressResult = "주소를 가져 올 수 없습니다."
        try {
            //세번째 파라미터는 좌표에 대해 주소를 리턴 받는 갯수로
            //한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 최대갯수 설정
            address = geoCoder.getFromLocation(lat, lng, 1) as ArrayList<Address>
            if (address.size > 0) {
                // 주소 받아오기
                val currentLocationAddress = address[0].getAddressLine(0)
                    .toString()
                addressResult = currentLocationAddress

            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addressResult
    }

    // 주소 -> 좌표 변환
    private fun getLatLngFromAddress(address: String): LatLng? {
        val geocoder = Geocoder(this, Locale.KOREA)
        val addressList = geocoder.getFromLocationName(address, 1)
        return if (addressList != null && addressList.isNotEmpty()) {
            val latitude = addressList[0].latitude
            val longitude = addressList[0].longitude
            LatLng(latitude, longitude)
        } else {
            null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // request code와 권한획득 여부 확인
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                naverMap.setLocationTrackingMode(LocationTrackingMode.Follow)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

}