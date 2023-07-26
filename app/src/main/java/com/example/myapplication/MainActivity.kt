package com.example.myapplication

import MyModel
import MySModel
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.NavigationHeaderBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.Tm128
import com.naver.maps.geometry.Tm128.valueOf
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import kr.hyosang.coordinate.*
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.util.*
import kotlin.properties.Delegates

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
    private val mMarkerList: List<Marker> = ArrayList() //공공데이터에서 불러오는 미세먼지 마커들

    //현재 TM 좌표
    private var tmX by Delegates.notNull<Double>()
    private var tmY by Delegates.notNull<Double>()
    //현재 위치 저장
    private var lat by Delegates.notNull<Double>()
    private var lon by Delegates.notNull<Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        _binding = NavigationHeaderBinding.bind(binding.mainDrawer.getHeaderView(0))

        setContentView(binding.root)
        binding.mainDrawer.setNavigationItemSelectedListener(this)

        binding.navBtn.setOnClickListener {
               binding.drawer.openDrawer(GravityCompat.START)
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
    private fun getSidoDust(addr : String) {
        stationFineDust(addr)
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

    private fun stationDust() { //측정소 API 불러오는 코드
        //var keyword = binding.edtProduct.text.toString()
        getTm()
        val call: Call<MyModel> = MyApplication.retroInterface.getRetrofit(
            tmX.toString(),
            tmY.toString(),
            "json",
            "uItfMom3tDSQvZa3Xm2GwUrA5YidOSP4H1qHM/rkupqT9pT5TNa4zyQWdXFnbKlKSqBZsEqJtZrQfYYrPHAwgg==",
            "1.1"
        ) //call 객체에 초기화
        Log.d("mobileApp", "${call.request()}")

        call?.enqueue(object: retrofit2.Callback<MyModel> {
            override fun onResponse(call: Call<MyModel>, response: Response<MyModel>) {
                if(response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody!=null) {
                        val firstItem = responseBody.response.body.items[0].stationName
                        stationFineDust(firstItem.toString())
                        Log.d("mobileApp", "첫 번째 item의 stationName: ${firstItem.toString()}")
                    } else {
                        Log.d("mobileApp", "items 리스트가 비어있습니다.")
                    }

                    //Log.d("mobileApp", "${response.body()?.body?.items?:emptyList()}")
                    //binding.retrofitRecyclerView.layoutManager = LinearLayoutManager(context)
                    //binding.
                    //    .adapter = RetrofitAdapter(this, response.body()!!.body.items)
                }
            }

            override fun onFailure(call: Call<MyModel>, t: Throwable) {
                Log.d("mobileApp", "${t.toString()}")
            }
        })
    }

    private fun stationFineDust(stationName : String) { //미세먼지 API 불러오기
        val call: Call<MySModel> = MyApplication.retroInterface2.getRetrofit2(
            stationName, //측정소이름
            "month",
            "1",
            "100",
            "json",
            "uItfMom3tDSQvZa3Xm2GwUrA5YidOSP4H1qHM/rkupqT9pT5TNa4zyQWdXFnbKlKSqBZsEqJtZrQfYYrPHAwgg==",
            "1.4"
        ) //call 객체에 초기화
        Log.d("mobileApp2", "${call.request()}")

        call?.enqueue(object: retrofit2.Callback<MySModel> {
            override fun onResponse(call: Call<MySModel>, response: Response<MySModel>) {
                if(response.isSuccessful) {
                    Log.d("mobileApp", "${response.body()}")
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
    private fun fetchFineDust(sidoName: String, searchCondition: String) {
    val retrofit = Retrofit.Builder()
        .baseUrl(DustAPI.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val dustApi = retrofit.create(DustAPI::class.java)
    dustApi.getFineDustbySido(sidoName, searchCondition).enqueue(object : Callback<FineDustResult> {
        override fun onResponse(call: Call<FineDustResult>, response: Response<FineDustResult>) {
            // 200은 성공을 의미합니다.
            if (response.code() == 200) {
                mResult = response.body()
                setDustOnView()
            }
        }

    override fun onFailure(call: Call<FineDustResult>, t: Throwable) {

       }
        })
    }

    //API에서 가져온 가게 좌표마다 marker 띄움
    private fun updateMapMarkers(result: StoreSaleResult) {
        resetMarkerList()
        if (result.stores != null && result.stores.size > 0) {
            for (mask in result.stores) {
                val marker = Marker()
                marker.tag = mask
                marker.position = LatLng(mask.lat, mask.lng)

                when (mask.remain_stat.toLowerCase()) {
                    "plenty" -> marker.icon = OverlayImage.fromResource(R.drawable.marker_green)
                    "some" -> marker.icon = OverlayImage.fromResource(R.drawable.marker_yellow)
                    "few" -> marker.icon = OverlayImage.fromResource(R.drawable.marker_red)
                    else -> marker.icon = OverlayImage.fromResource(R.drawable.marker_gray)
                }
                marker.anchor = PointF(0.5f, 1.0f)
                marker.map = mnaverMap
                marker.setOnClickListener(this)
                mMarkerList.add(marker)
            }
        }
    }
    */

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
            R.id.item2 -> {
                if (item.itemId === R.id.item2) {
                    val intent = Intent(this, AlarmActivity::class.java)
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
        }
        return true
    }

    /*private fun setMark(marker: Marker, lat: Double, lng: Double, resourceID: Int) { //마커 띄우기
        // 원근감 표시
        marker.isIconPerspectiveEnabled = true
        // 아이콘 지정
        marker.icon = OverlayImage.fromResource(resourceID)
        // 마커의 투명도
        marker.alpha = 0.8f
        // 마커 위치
        marker.position = LatLng(lat, lng)
        // 마커 우선순위
        marker.zIndex = 10
        // 마커 표시
        marker.map = naverMap
    }*/

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
        //stationDust()

        // 권한확인. 결과는 onRequestPermissionsResult 콜백 매서드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);


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
            //여기
            /*val address = getAddress(naverMap.cameraPosition.target.latitude, naverMap.cameraPosition.target.longitude)
            getSidoDust(getSido(address))*/
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
                }
            }

        naverMap.addOnLocationChangeListener { location ->
            lat = location.latitude
            lon = location.longitude
            stationDust()
            //setMark(marker, lat, lon, R.drawable.baseline_place_24)
            //Log.d("mobileApp", getAddress(lat, lon))
        }

    }

    private fun getSido(address : String) :String {
        val words = address.split("\\s".toRegex()).toTypedArray()
        Log.d("mobileApp", words[2]) //현위치 구 불러오기
        return words[2]
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