package com.example.myapplication

import DeliveryItem
import MYModel
import MyAModel
import MyBModel
import MySModel
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.NavigationHeaderBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.*
import kr.hyosang.coordinate.CoordPoint
import kr.hyosang.coordinate.TransCoord
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.util.*
import kotlin.properties.Delegates
import android.view.View
import com.example.myapplication.PhotoActivity
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Array.get


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    OnMapReadyCallback {
    lateinit var binding: ActivityMainBinding
    lateinit var _binding : NavigationHeaderBinding
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private var PERMISSION_REQUEST_CODE = 100;
    private val PERMISSIONS = arrayOf(
        ACCESS_FINE_LOCATION,
        ACCESS_COARSE_LOCATION
    )
    private lateinit var mLocationSource: FusedLocationSource
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var marker : Marker
    private val mMarkerList = arrayOfNulls<Marker>(700) //공공데이터에서 불러오는 미세먼지 마커들

    //현재 TM 좌표
    private var tmX by Delegates.notNull<Double>()
    private var tmY by Delegates.notNull<Double>()
    //처음 위치 저장
    private lateinit var initialPosition : LatLng
    // activity 끝나기 전 마지막 위치 저장
    private var latelyPosition : LatLng = LatLng(37.5670135, 126.9783740)
    private var lat by Delegates.notNull<Double>()
    private var lon by Delegates.notNull<Double>()

    // 코루틴 생성
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    //floatingbutton
    private lateinit var mContext: Context
    private lateinit var fabMain: FloatingActionButton
    private lateinit var fabSub1: FloatingActionButton
    private lateinit var fabSub2: FloatingActionButton
    private lateinit var fabOpen: Animation
    private lateinit var fabClose: Animation
    private var isFabOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        _binding = NavigationHeaderBinding.bind(binding.mainDrawer.getHeaderView(0))

        setContentView(binding.root)
        binding.mainDrawer.setNavigationItemSelectedListener(this)

        binding.navBtn.setOnClickListener {
            binding.drawer.openDrawer(GravityCompat.START)
        }

        mContext = applicationContext
        fabOpen = AnimationUtils.loadAnimation(mContext, R.anim.fab_open)
        fabClose = AnimationUtils.loadAnimation(mContext, R.anim.fab_close)

        fabMain = findViewById(R.id.ic_main)
        fabSub1 = findViewById(R.id.fab_sub1)
        fabSub2 = findViewById(R.id.fab_sub2)

        fabSub1.visibility = View.GONE
        fabSub2.visibility = View.GONE

        binding.icMain.setOnClickListener { v: View ->
            when (v.id) {
                R.id.ic_main -> toggleFab()
                R.id.fab_sub1 -> {
                    toggleFab()
                }
                R.id.fab_sub2 -> {
                    toggleFab()
                }
            }
        }

        binding.fabSub1.setOnClickListener{
            val intent = Intent(this, PhotoActivity::class.java)
            intent.putExtra("fromCamera", true)
            startActivity(intent)
        }

        binding.fabSub2.setOnClickListener{
            val intent = Intent(this, PhotoActivity::class.java)
            intent.putExtra("fromGallery", true)
            startActivity(intent)
        }


        binding.btnDeliveryVehicle.setOnClickListener {
            // SecondActivity로 이동하는 인텐트 시작
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }

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
                    try {
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
                    catch (e: IllegalArgumentException) {
                        // Handle the error here, e.g., show a Toast or display an error message
                        e.message?.let { it1 -> Log.e("mobileApp", it1) }
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


    private fun toggleFab() {
        if (isFabOpen) {
            fabMain.setImageResource(R.drawable.baseline_camera_alt_24)
            fabSub1.visibility = View.GONE  // 수정된 부분
            fabSub2.visibility = View.GONE
            fabSub1.startAnimation(fabClose)
            fabSub2.startAnimation(fabClose)
            fabSub1.isClickable = false
            fabSub2.isClickable = false
            isFabOpen = false
        } else {
            fabMain.setImageResource(R.drawable.baseline_close_24)
            fabSub1.visibility = View.VISIBLE  // 수정된 부분
            fabSub2.visibility = View.VISIBLE
            fabSub1.startAnimation(fabOpen)
            fabSub2.startAnimation(fabOpen)
            fabSub1.isClickable = true
            fabSub2.isClickable = true
            isFabOpen = true
        }
    }



    //위도경도 좌표계 => tm좌표 변환 함수
    private fun getTm(){
        val wgsPt = CoordPoint(naverMap.cameraPosition.target.longitude, naverMap.cameraPosition.target.latitude)
        Log.d("mobileApp", wgsPt.x.toString())
        val tmPt = TransCoord.getTransCoord(wgsPt, TransCoord.COORD_TYPE_WGS84,TransCoord.COORD_TYPE_TM)
        Log.d("mobileApp", tmPt.x.toString())
        tmX = tmPt.x
        tmY = tmPt.y
    }



    private fun stationDust(onStationDustComplete: (String) -> Unit) { //측정소 API 불러오는 코드
        //var keyword = binding.edtProduct.text.toString()
        getTm()
        val call: Call<MYModel> = MyApplication.retroInterface.getRetrofit(
            tmX.toString(),
            tmY.toString(),
            "json",
            "uItfMom3tDSQvZa3Xm2GwUrA5YidOSP4H1qHM/rkupqT9pT5TNa4zyQWdXFnbKlKSqBZsEqJtZrQfYYrPHAwgg==",
            "1.1"
        ) //call 객체에 초기화
        Log.d("mobileApp", "${call.request()}")

        call?.enqueue(object: retrofit2.Callback<MYModel> {
            override fun onResponse(call: Call<MYModel>, response: Response<MYModel>) {
                if(response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody!=null) {
                        val firstItem = responseBody.response.body.items[0].stationName
                        onStationDustComplete(firstItem.toString())
                        Log.d("stationDust", "첫 번째 item의 stationName: ${firstItem.toString()}")
                    } else {
                        Log.d("stationDust", "items 리스트가 비어있습니다.")
                    }

                    //Log.d("mobileApp", "${response.body()?.body?.items?:emptyList()}")
                    //binding.retrofitRecyclerView.layoutManager = LinearLayoutManager(context)
                    //binding.
                    //    .adapter = RetrofitAdapter(this, response.body()!!.body.items)
                }
            }

            override fun onFailure(call: Call<MYModel>, t: Throwable) {
                Log.d("mobileApp", "${t.toString()}")
            }
        })
    }

    private fun stationFineDust(stationName: String, callback: (pm10: String, pm25: String) -> Unit) { //미세먼지 API 불러오기
        val call: Call<MySModel> = MyApplication.retroInterface2.getRetrofit2(
            stationName, //측정소이름
            "month",
            "1",
            "100",
            "json",
            "1.1",
            "uItfMom3tDSQvZa3Xm2GwUrA5YidOSP4H1qHM/rkupqT9pT5TNa4zyQWdXFnbKlKSqBZsEqJtZrQfYYrPHAwgg=="
        ) //call 객체에 초기화
        Log.d("mobileApp", "${call.request()}")

        call?.enqueue(object: retrofit2.Callback<MySModel> {
            override fun onResponse(call: Call<MySModel>, response: Response<MySModel>) {
                if(response.isSuccessful) {
                    Log.d("mobileApp", "${response.body()}")
                    val pm10value= response.body()?.response?.body?.items?.get(0)?.pm10Value
                    val pm25value= response.body()?.response?.body?.items?.get(0)?.pm25Value
                    callback(pm10value.toString(), pm25value.toString())
                    //binding.retrofitRecyclerView.layoutManager = LinearLayoutManager(context)
                    //binding.retrofitRecyclerView.adapter = MyRetrofitAdapter(this, response.body()!!.body.items)
                }
            }

            override fun onFailure(call: Call<MySModel>, t: Throwable) {
                Log.d("mobileApp", "${t.toString()}")
            }
        })
    }


    /*
        private fun allOfStation(stationName: String, sidoName: String): LatLng? = runBlocking {// 측정소 위치값 return 받을 때 응답속도가 느려서 자꾸 null이 반환되는 문제가 생겨서 동기화 함수 추가 작성함
        var coord: LatLng? = null

        val deferredCoord = async(Dispatchers.IO) {
            fetchStationCoordinates(stationName, sidoName)
        }

        coord = deferredCoord.await()

        return@runBlocking coord
    }*/

    private suspend fun fetchStationCoordinates(stationName: String, sidoName: String): LatLng? { //미세먼지->측정소 API 불러오기
        val call: Call<MyBModel> = MyApplication.retroInterface4.getRetrofit4(
            "uItfMom3tDSQvZa3Xm2GwUrA5YidOSP4H1qHM/rkupqT9pT5TNa4zyQWdXFnbKlKSqBZsEqJtZrQfYYrPHAwgg==",
            "json",
            "1",
            "1",
            sidoName,
            stationName
        )

        try { // api에서 반환해주는 dmx, dmy(위도와 경도) 값을 이용해 측정소 위치 return
            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.response.body.items.isNotEmpty()) {
                    val item = body.response.body.items[0]
                    val lat = item.dmX?.toDouble()
                    val lng = item.dmY?.toDouble()
                    return lat?.let { LatLng(it, lng ?: 0.0) }
                }
            }
        } catch (e: Exception) {
            Log.d("mobileApp", e.toString())
        }

        return null
    }

    private suspend fun fetchStationCoordinatesAsync(stationName: String, sidoName: String): LatLng? {
        return withContext(Dispatchers.IO) {

            fetchStationCoordinates(stationName, sidoName)
        }
    }

    private suspend fun allOfStationAsync(stationName: String, sidoName: String): LatLng? =
        fetchStationCoordinatesAsync(stationName.toString(), sidoName.toString())

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
        mapView.onResume()
        if (!::marker.isInitialized) {
            return
        }
        marker.position = latelyPosition
        updateNavigationHeader()
    }

    override fun onStart(){
        // Intent에서 finish() 돌아올 때 실행
        // onCreate -> onStart -> onCreateOptionsMenu
        super.onStart()
        mapView.onStart()
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.item1 -> {Log.d("mobileApp", "네비게이션 뷰 메뉴 1")}
            R.id.item2 -> {
                if (item.itemId === R.id.item2) {
                    val intent = Intent(this, AlarmActivity::class.java)
                    intent.putExtra("latelyPosition", latelyPosition)
                    startActivity(intent)
                }
            }
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
            R.id.item5 -> {
                if (item.itemId === R.id.item5) {
                    val intent = Intent(this, PhotoActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        return true
    }

    //측정소 마커 띄우기
    private fun addMarkers() {
        // 다중 마커
        val call: Call<MyAModel> = MyApplication.retroInterface3.getRetrofit3( // 통신 부분
            "전국", //측정소이름
            "1",
            "700",
            "json",
            "uItfMom3tDSQvZa3Xm2GwUrA5YidOSP4H1qHM/rkupqT9pT5TNa4zyQWdXFnbKlKSqBZsEqJtZrQfYYrPHAwgg==",
            "1.2"
        ) //call 객체에 초기화
        Log.d("markers", "${call.request()}")

        call?.enqueue(object: retrofit2.Callback<MyAModel> {
            override fun onResponse(call: Call<MyAModel>, response: Response<MyAModel>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        coroutineScope.launch {
                            for (i in responseBody.response.body.items.indices) {
                                val stationName = responseBody.response.body.items[i].stationName
                                val sidoName = responseBody.response.body.items[i].sidoName
                                val pm10 = responseBody.response.body.items[i].pm10Value
                                val pm25 = responseBody.response.body.items[i].pm25Value
                                mMarkerList[i] = Marker()
                                val coord =
                                    allOfStationAsync(stationName.toString(), sidoName.toString())
                                if (coord != null) {
                                    if (responseBody.response.body.items[i].pm10Value != null) {
                                        mMarkerList[i]?.width = 100
                                        mMarkerList[i]?.height = 100
                                        if (pm10!! <= 15.toString()) { //0~15 미세먼지 굿
                                            mMarkerList[i]?.position = coord
                                            mMarkerList[i]?.icon =
                                                OverlayImage.fromResource(R.drawable.marker_good)
                                            mMarkerList[i]?.map = naverMap
                                        } else if (pm10!! <= 35.toString() && pm10!! > 15.toString()) { //15~35
                                            mMarkerList[i]?.position = coord
                                            mMarkerList[i]?.icon =
                                                OverlayImage.fromResource(R.drawable.marker_soso)
                                            mMarkerList[i]?.map = naverMap
                                        } else if (pm10!! <= 75.toString() && pm10!! > 35.toString()) {// 35~75
                                            mMarkerList[i]?.position = coord
                                            mMarkerList[i]?.icon =
                                                OverlayImage.fromResource(R.drawable.marker_bad)
                                            mMarkerList[i]?.map = naverMap
                                        } else { //75~
                                            mMarkerList[i]?.position = coord
                                            mMarkerList[i]?.icon =
                                                OverlayImage.fromResource(R.drawable.marker_terri)
                                            mMarkerList[i]?.map = naverMap
                                        }
                                    } else {
                                        ///null 이면 마커 안찍음
                                    }
                                }
                                //마커에 InfoActivity 클릭리스너
                                mMarkerList[i]?.setOnClickListener(object : Overlay.OnClickListener {
                                override fun onClick(overlay: Overlay): Boolean {
                                    val intent = Intent(this@MainActivity, InfoActivity::class.java)
                                    intent.putExtra("pm10value", pm10)
                                    intent.putExtra("pm25value", pm25)
                                    intent.putExtra("stationvalue", stationName)
                                    intent.putExtra("addressvalue", getAddress(mMarkerList[i]?.position!!.latitude, mMarkerList[i]?.position!!.longitude))

                                    startActivity(intent)
                                    return true
                                }
                            })

                            }
                        }
                    } else {
                        // Handle the case when the response body is null
                    }
                } else {
                    // Handle the case when the response is not successful
                }
            }

            override fun onFailure(call: Call<MyAModel>, t: Throwable) {
                Log.e("mobileApp", "Retrofit onFailure: ${t.toString()}")
                // Handle the failure, e.g., show a Toast or display an error message
            }
        })
    }

    override fun onMapReady(naverMap: NaverMap) { //네이버 지도의 이벤트를 처리하는 콜백함수

        // NaverMap 객체 받아서 NaverMap 객체에 위치 소스 지정
        marker = Marker()
        // 지도상에 마커 표시
        marker.position = LatLng( //마커가 위치한 좌표!!!!! => 여기 기준으로 주소 설정할 수 있도록 해야함
            naverMap.cameraPosition.target.latitude,
            naverMap.cameraPosition.target.longitude
        )
        initialPosition = LatLng( // 초기 Latlng값 저장
            naverMap.cameraPosition.target.latitude,
            naverMap.cameraPosition.target.longitude
        )
        latelyPosition= LatLng( // 초기화
            naverMap.cameraPosition.target.latitude,
            naverMap.cameraPosition.target.longitude
        )
        marker.icon = OverlayImage.fromResource(R.drawable.baseline_place_24)
        marker.map = naverMap

        this.naverMap = naverMap
        naverMap.locationSource = mLocationSource
        naverMap.setLocationSource(mLocationSource)

        addMarkers()

        // 현재 위치 버튼 기능
        naverMap.uiSettings.isLocationButtonEnabled = true
        // 위치를 추적하면서 카메라도 따라 움직인다.
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        // 권한확인. 결과는 onRequestPermissionsResult 콜백 매서드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);

        // 카메라의 움직임에 대한 이벤트 리스너 인터페이스.
        naverMap.addOnCameraChangeListener { reason, animated ->
            //Log.i("NaverMap", "카메라 변경 - reson: $reason, animated: $animated")
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
            latelyPosition = marker.position
        }

        var currentLocation: Location?
        if (ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                ACCESS_COARSE_LOCATION
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

                    latelyPosition = LatLng(
                        naverMap.cameraPosition.target.latitude,
                        naverMap.cameraPosition.target.longitude
                    )
                }
            }

        naverMap.addOnLocationChangeListener { location ->
            lat = location.latitude
            lon = location.longitude

        }

        //marker누르면 상세정보 페이지 띄우기
        marker.setOnClickListener {
            stationDust { firstItem ->
                val station = firstItem
                val marker_address = getAddress(marker.position.latitude, marker.position.longitude)


                // stationFineDust 함수 사용
                stationFineDust(station) { pm10value, pm25value ->
                    // 콜백으로 전달된 pm10value를 이용하여 InfoActivity를 시작
                    val intent = Intent(this@MainActivity, InfoActivity::class.java)
                    intent.putExtra("pm10value", pm10value)
                    intent.putExtra("pm25value", pm25value)
                    intent.putExtra("stationvalue", station)
                    intent.putExtra("addressvalue", marker_address)

                    startActivity(intent)
                }
            }
            true
        }
    }


    // 좌표 -> 주소 변환     marker.setonclicklistener 에서 사용함
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
                val currentLocationAddress = address[0].getAddressLine(0).toString()
                val words = currentLocationAddress.split(" ")

                // 주소 데이터가 충분한지 확인 후 가공
                if (words.size >= 5) {
                    addressResult = words[1] + " " + words[2] + " " + words[3] + " " + words[4]
                } else {
                    addressResult = currentLocationAddress
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addressResult
    }

    // 주소 -> 좌표 변환
    private fun getLatLngFromAddress(address: String): LatLng {
        val geocoder = Geocoder(this, Locale.KOREA)
        //val addressList = geocoder.getFromLocationName(address, 1)
        val addressList = geocoder.getFromLocationName(address, 1)

        if (addressList != null && addressList.isNotEmpty()) {
            val latitude = addressList[0].latitude
            val longitude = addressList[0].longitude
            return LatLng(latitude, longitude)
        } else {
            throw IllegalArgumentException("Invalid address or unable to find location for the given address: $address")
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