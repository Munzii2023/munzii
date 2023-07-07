package com.example.myapplication

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.NavigationHeaderBinding
import com.google.android.material.navigation.NavigationView
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
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
    lateinit var mLocationSource: FusedLocationSource

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

        // 네이버 지도
        mapView = findViewById(R.id.navermap_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // 위치를 반환하는 구현체인 FusedLocationSource 생성
        mLocationSource = FusedLocationSource(this, PERMISSION_REQUEST_CODE)

        binding.mapsSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                // 검색 버튼 누를 때 호출

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 검색창에서 글자가 변경이 일어날 때마다 호출
                return true
            }
        })

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

    private fun setMark(marker: Marker, lat: Double, lng: Double, resourceID: Int) { //마커 띄우기
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
    }

    override fun onMapReady(naverMap: NaverMap) { //네이버 지도의 이벤트를 처리하는 콜백함수
        // NaverMap 객체 받아서 NaverMap 객체에 위치 소스 지정
        // 지도상에 마커 표시
        val marker = Marker()
        marker.position = LatLng(37.5670135, 126.9783740)
        marker.map = naverMap

        this.naverMap = naverMap
        naverMap.locationSource = mLocationSource
        //naverMap.setLocationSource(mLocationSource);

        // 권한확인. 결과는 onRequestPermissionsResult 콜백 매서드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);


        /* val cameraPosition = CameraPosition(
            LatLng(37.65178832823347, 127.01614801495204), //위치 지정
            16.0 // 줌레벨 => 추가로 기울임 각도, 방향 설정 가능
        )
        naverMap.cameraPosition = cameraPosition  //최초위치 설정 */

        naverMap.addOnLocationChangeListener { location ->
            lat = location.latitude
            lon = location.longitude
            setMark(marker, lat, lon, R.drawable.baseline_place_24)

            /* //마커 움직이는 거 어케하는지 모르겠어염..
            val location = LatLng(location.latitude, location.longitude)
            marker.position = location
            naverMap.moveCamera(CameraUpdate.scrollTo(location))
            */

            /*Toast.makeText(
                applicationContext,
                "$lat, $lon",
                Toast.LENGTH_SHORT
            ).show()*/
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