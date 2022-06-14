package com.ssafy.smartstore.fragment

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.facebook.login.Login
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.ssafy.smartstore.LoginActivity
import com.ssafy.smartstore.R
import java.io.IOException
import java.util.*

private const val TAG = "MapFragment_싸피"
class MapFragment : Fragment(),OnMapReadyCallback,GoogleMap.OnMarkerClickListener {

    private val UPDATE_INTERVAL = 1000 // 1초
    private val FASTEST_UPDATE_INTERVAL = 500 // 0.5초

    private var mMap: GoogleMap? = null
    private var currentMarker: Marker? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var mCurrentLocation: Location
    private lateinit var currentPosition: LatLng



    private lateinit var ctx: Context
    override fun onAttach(context: Context) {
        super.onAttach(context)
        ctx = context
        Log.d(TAG, "onAttach: ")
    }
    override fun onCreateView(


        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // Inflate the layout for this fragment
        var rootView =  inflater.inflate(R.layout.fragment_map, container, false)
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = UPDATE_INTERVAL.toLong()
            smallestDisplacement = 10.0f
            fastestInterval = FASTEST_UPDATE_INTERVAL.toLong()
        }

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.onCreate(savedInstanceState)
        mapFragment.getMapAsync(this)

        return rootView
    }

    override fun onMapReady(p0: GoogleMap) {
        Log.d(TAG, "onMapReady: ")
        mMap = p0
        val myLocation = LatLng(36.10830144233874, 128.41827450414362)
        LoginActivity.store_lan = 36.10830144233874
        LoginActivity.store_lon = 128.41827450414362
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(myLocation))
        mMap!!.moveCamera(CameraUpdateFactory.zoomTo(15f))

        //마커 출력
        val marker = MarkerOptions()
            .position(myLocation)
            .title("싸피벅스")
            .snippet("싸피벅스입니다.")
        mMap?.addMarker(marker)

        mMap?.setOnMarkerClickListener(this)

        //퍼미션 요청 대화상자 (권한이 없을때) & 실행 시 초기 위치 이동
        setDefaultLocation()

        if (checkPermission()) { // 1. 위치 퍼미션을 가지고 있는지 확인
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식)
            startLocationUpdates() // 3. 위치 업데이트 시작
        } else {  //2. 권한이 없다면
            // 3-1. 사용자가 권한이 없는 경우에는
            val permissionListener = object : PermissionListener {
                // 권한 얻기에 성공했을 때 동작 처리
                override fun onPermissionGranted() {
                    startLocationUpdates()
                }
                // 권한 얻기에 실패했을 때 동작 처리
                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(ctx,
                        "위치 권한이 거부되었습니다.",
                        Toast.LENGTH_SHORT).show()
                }
            }

            TedPermission.create()
                .setPermissionListener(permissionListener)
                .setDeniedMessage("[설정] 에서 위치 접근 권한을 부여해야만 사용이 가능합니다.")
                .setPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .check()
        }
    }

    private fun startLocationUpdates() {
        // 위치서비스 활성화 여부 check
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting()
        } else {
            if (checkPermission()) {
                mFusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.myLooper()!!
                )
                if (mMap != null) mMap!!.isMyLocationEnabled = true
                //2. 지도에 +.- 버튼 보이기.
                if (mMap != null) mMap!!.uiSettings.isZoomControlsEnabled = true
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (checkPermission()) {
            //1. 중복코드제거
            startLocationUpdates()
//            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
//            if (mMap != null && checkLocationServicesStatus()) mMap!!.isMyLocationEnabled = true
        }
    }

    override fun onStop() {
        super.onStop()
        mFusedLocationClient.removeLocationUpdates(locationCallback)

    }
    //위치정보 요청시 호출
    var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val locationList = locationResult.locations
            if (locationList.size > 0) {
                val location = locationList[locationList.size - 1]
                currentPosition = LatLng(location.latitude, location.longitude)
                LoginActivity.current_lan = location.latitude
                LoginActivity.current_lon = location.longitude

                val markerTitle: String = getCurrentAddress(currentPosition)
                val markerSnippet =
                    "위도: ${location.latitude.toString()}, 경도: ${location.longitude }"

                //현재 위치에 마커 생성하고 이동
                //setCurrentLocation(location, markerTitle, markerSnippet)
                mCurrentLocation = location

                //Log.d(TAG, "onLocationResult: 현재 위치 : $currentPosition")
            }
        }
    }
    fun getCurrentAddress(latlng: LatLng): String {
        //지오코더: GPS를 주소로 변환
        val geocoder = Geocoder(ctx, Locale.getDefault())
        val addresses: List<Address>?
        try {
            addresses = geocoder.getFromLocation(
                latlng.latitude,
                latlng.longitude,
                1
            )
        } catch (ioException: IOException) {
            //네트워크 문제
            Toast.makeText(ctx, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show()
            return "지오코더 사용불가"
        } catch (illegalArgumentException: IllegalArgumentException) {
            Toast.makeText(ctx, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show()
            return "잘못된 GPS 좌표"
        }

        return if (addresses == null || addresses.isEmpty()) {
            Toast.makeText(ctx, "주소 발견 불가", Toast.LENGTH_LONG).show()
            "주소 발견 불가"
        } else {
            val address = addresses[0]
            address.getAddressLine(0).toString()
        }
    }

    private fun checkLocationServicesStatus(): Boolean {
        val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    fun setCurrentLocation(location: Location, markerTitle: String?, markerSnippet: String?) {
        currentMarker?.remove()

        val currentLatLng = LatLng(location.latitude, location.longitude)
        Log.d(TAG, "setCurrentLocation: 현재 위치 경도, 위도 : ${location.latitude}, ${location.longitude}")

        //4. 마커 바꾸기
        val bitmapdraw = ResourcesCompat.getDrawable(resources, R.drawable.location_icon, activity?.theme) as BitmapDrawable
        val b = bitmapdraw.bitmap
        val marker = Bitmap.createScaledBitmap(b, 100, 100, false)

        val markerOptions = MarkerOptions()
        markerOptions.position(currentLatLng)
        markerOptions.title(markerTitle)
        markerOptions.snippet(markerSnippet)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(marker))
        markerOptions.draggable(true)

        currentMarker = mMap!!.addMarker(markerOptions)

        //3. animation으로 이동.
//        val cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng)
//        mMap!!.moveCamera(cameraUpdate)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f)
        mMap!!.animateCamera(cameraUpdate)
    }
    private fun setDefaultLocation() {
//        이전위치값 가져오기
        var lastLocation : Location
        if(checkPermission()) {
            mFusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if(location != null) lastLocation = location
            }
        }

        // 1. 중복코드제거
        //초기 위치를 서울로
//        val DEFAULT_LOCATION = LatLng(36.10830144233874, 128.41827450414362)
//        val markerTitle = "위치정보 가져올 수 없음"
//        val markerSnippet = "위치 퍼미션과 GPS 활성 여부 확인 필요"
//
//        var location = Location("")
//        location.latitude = DEFAULT_LOCATION.latitude
//        location.longitude = DEFAULT_LOCATION.longitude
//        setCurrentLocation(location, markerTitle, markerSnippet)

    }


    private fun checkPermission(): Boolean {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    /******** 위치서비스 활성화 여부 check *********/
    private val GPS_ENABLE_REQUEST_CODE = 2001
    private var needRequest = false

    private fun showDialogForLocationServiceSetting() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(ctx)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage(
            "앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
        )
        builder.setCancelable(true)
        builder.setPositiveButton("설정") { _, _ ->
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE)
        }
        builder.setNegativeButton("취소"
        ) { dialog, _ -> dialog.cancel() }
        builder.create().show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GPS_ENABLE_REQUEST_CODE ->
                //사용자가 GPS를 켰는지 검사함
                if (checkLocationServicesStatus()) {
                    needRequest = true
                    return
                }else{
                    Toast.makeText(ctx,
                        "위치 서비스가 꺼져 있어, 현재 위치를 확인할 수 없습니다.",
                        Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        Log.d(TAG, "onMarkerClick: 위치 클릭")
        showDialog()
        return true
    }

    private fun showDialog(){
        val linear = LinearLayout.inflate(ctx, R.layout.dialog_for_map,null)

        var dialog = Dialog(ctx)
        dialog.setContentView(linear)
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.create()
        dialog.show()

        //길찾기
        linear.findViewById<TextView>(R.id.tv_map).setOnClickListener{
            //지도로 연결
            val gmmIntentUri = Uri.parse("google.navigation:q=대한민국 경상북도 구미시 진미동 94")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)

        }

        //전화걸기
        linear.findViewById<TextView>(R.id.tv_call).setOnClickListener{
//            var call = Intent(Intent.ACTION_CALL, Uri.parse("tel: 1234556788"));
//            startActivity(call);

            var call = Intent(Intent.ACTION_DIAL, Uri.parse("tel:123456789"));
            startActivity(call)
        }
    }
}