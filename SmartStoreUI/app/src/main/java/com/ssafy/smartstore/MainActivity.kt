package com.ssafy.smartstore

import android.Manifest
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.RemoteException
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.gun0912.tedpermission.provider.TedPermissionProvider.context
import com.kakao.sdk.user.UserApiClient
import com.ssafy.smartstore.database.OrderDTO4
import com.ssafy.smartstore.database.UserDTO
import com.ssafy.smartstore.databinding.ActivityMainBinding
import com.ssafy.smartstore.service.OrderService
import com.ssafy.smartstore.service.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.altbeacon.beacon.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "HomeActivity_??????"
class MainActivity : AppCompatActivity() , BeaconConsumer {

    private lateinit var binding:ActivityMainBinding

    //beacon
    private lateinit var beaconManager: BeaconManager
    private val region = Region("altbeacon",null,null,null)
    private lateinit var bluetoothManager: BluetoothManager
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var needBLERequest = true
    private val STORE_DISTANCE = 2

    private val PERMISSIONS_CODE = 100
    //?????? ????????? ?????? ??????
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private lateinit var dialog: Dialog
    private var cnt = 0
    private var dis = 0.0
    private var handler = Handler();
    private lateinit var pendingIntent:PendingIntent
    private var imgUrl = ""

    var orderList:List<OrderDTO4> = arrayListOf()

    private val locationManager by lazy {
        getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        setIntent(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment

        val navController = navHostFragment.navController

        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController)

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                this,
                requiredPermissions,
                PERMISSIONS_CODE
            )
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            pendingIntent = PendingIntent.getActivity(context,PERMISSIONS_CODE, intent,PendingIntent.FLAG_IMMUTABLE)
        }else{
            pendingIntent = PendingIntent.getActivity(context,PERMISSIONS_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))
        bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        startScan()

        //FCM ?????? ????????????
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener {
            if(!it.isSuccessful){
                return@OnCompleteListener
            }

            //????????? fcm ?????? ?????? ??????
            Log.d(TAG, "onCreate: ????????? ?????? ?????? : ${it.result}")
        })

        createNotiChanner("ssafy_id","ssafy")

        //?????? ?????? ?????? ????????????

        var current = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        LoginActivity.current_lan = current!!.latitude
        LoginActivity.current_lon = current!!.longitude



    }

    //26?????? ??????????????? ????????? ????????? ?????? ??????
    @RequiresApi(Build.VERSION_CODES.O)
    //notificationChanenl ??????
    private fun createNotiChanner(channelId: String, channelName: String) {
        println("notification?")
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }


    //?????? ?????? ?????? ?????? ?????? ?????? ??????
    //activityCompat.requestPermissions ?????? ?????? ??????
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            PERMISSIONS_CODE ->{
                if(grantResults.isNotEmpty()){
                    for((i,permission) in permissions.withIndex()){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            //?????? ?????? ??????
                            Log.i(TAG, "$permission ?????? ????????? ?????????????????????.")
                            finish()
                        }
                    }
                }
            }
        }
        //??????
    }



    //???????????? ????????? ??????
    private fun isEnableBLEService(): Boolean{
        if(!bluetoothAdapter!!.isEnabled){
            return false
        }
        return true
    }

    //bluetooth on/off?????? ?????? ??? ????????? ?????? ??????
    private fun requestEnableBLE(){
        val callBLEEnableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        requestBLEActivity.launch(callBLEEnableIntent)
        Log.d(TAG, "requestEnableBLE: ")
    }

    private val requestBLEActivity: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        //???????????? ???????????? ????????? ???????????? ??????
        if(isEnableBLEService()){
            needBLERequest = false
            startScan()
        }
    }




    private fun startScan(){
        //???????????? enable??????
        if(!isEnableBLEService()){
            requestEnableBLE()
            Log.d(TAG, "startScan: ????????????????????? ???????????????.")
            return
        }
        //?????? ?????? ?????? ?????? ??? gps enable ?????? ??????
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                this,
                requiredPermissions,
                PERMISSIONS_CODE
            )
        }
        Log.d(TAG, "startScan: beacon scan start")

        //beacon service bind
        beaconManager.bind(this)
    }


    override fun onBeaconServiceConnect(){
        beaconManager.addMonitorNotifier(object: MonitorNotifier {
            override fun didEnterRegion(region: Region?) {
                try{
                    Log.d(TAG, "????????? ??????!!!${region.toString()}")
                    beaconManager.startRangingBeaconsInRegion(region!!)
                }catch (e: RemoteException){
                    e.printStackTrace()
                }
            }

            override fun didExitRegion(region: Region?) {
                try{
                    Log.d(TAG, "?????? ?????? ?????????!!")
                    beaconManager.stopRangingBeaconsInRegion(region!!)
                }catch (e: RemoteException){
                    e.printStackTrace()
                }
            }

            override fun didDetermineStateForRegion(state: Int, region: Region?) {}
        })

        beaconManager.addRangeNotifier{ beacons, region ->
            for(beacon in beacons){
                //major, minor??? beacon??????, 1?????? ?????? ???????????? ????????? ??????
                if(isYourBeacon(beacon)){
                    dis = beacon.distance
                    println("!!!!")
                    update()
                    //????????? ????????? ?????? ??????
                    Log.d(TAG, "onBeaconServiceConnect: ")
                }
            }
        }

        try{
            beaconManager.startMonitoringBeaconsInRegion(region)
        }catch (e: RemoteException){
            e.printStackTrace()
        }
    }

    //??????????????? beacon?????????, ????????? ?????? ???????????? ??????
    private fun isYourBeacon(beacon: Beacon):Boolean{
        return (beacon.distance <= STORE_DISTANCE)
    }

    override fun onDestroy() {
        beaconManager.stopMonitoringBeaconsInRegion(region)
        beaconManager.stopRangingBeaconsInRegion(region)
        beaconManager.unbind(this)
        super.onDestroy()
    }

    fun update(){
        val updater = Runnable {
            kotlin.run {
//                CoroutineScope(Dispatchers.Main).launch {
//                    getRecentOrder()
                CoroutineScope(Dispatchers.Main).async {
                    println("cnt? ${cnt}")
                    if(cnt==0){
                        getRecentOrder()
                        println("order ????????????? ${orderList.size}")
                        showDialog()
                        cnt++;
                    }
                }

//                }
                print("??????????????? ?????? : ${dis.toString().substring(0..2)}")
            }
        }
        handler.post(updater)
    }


    private fun showDialog(){

        var resId = context.resources.getIdentifier(
            orderList[0].img.substring(0, orderList[0].img.length - 4),
            "drawable",
            "com.ssafy.smartstore"
        )

        val linear = LinearLayout.inflate(this, R.layout.dialog_for_beacon,null)

        linear.findViewById<ImageView>(R.id.img_stuff).setImageResource(resId)

        if(orderList.size >= 2){
            linear.findViewById<TextView>(R.id.tv_order_name).setText("${orderList[0].name}??? ${orderList.size-1}???")
        }else{
            linear.findViewById<TextView>(R.id.tv_order_name).setText("${orderList[0].name} ${orderList[0].quantity}???")
        }

        var sum = 0
        for(i in orderList.indices){
            sum += orderList[i].price * orderList[i].quantity
        }
        linear.findViewById<TextView>(R.id.tv_price).setText("${sum}???")

        linear.findViewById<TextView>(R.id.btn_ok).setOnClickListener{
            dialog.dismiss()
        }

        dialog = Dialog(this)
        dialog.setContentView(linear)
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.create()
        dialog.show()
    }

//    private suspend fun getRecentOrder(user_id: String):List<OrderDTO4>{
//        val api = MobileCafeApplication.retrofit.create(OrderService::class.java)
//        orderList = api.selectRecentOrder(user_id).execute().body()!!
//        println("${orderList}")
//        return orderList
//    }


    private fun getRecentOrder(){
//        println("orderDTO????? : ${orderDTO.details[0].productId}")
        val service = MobileCafeApplication.retrofit.create(OrderService::class.java)
        service.selectRecentOrder(LoginActivity.userId).enqueue(object : Callback<List<OrderDTO4>> {
            override fun onResponse(
                call: Call<List<OrderDTO4>>,
                response: Response<List<OrderDTO4>>
            ) {
                //????????? ?????? ?????????
                if (response.code() == 200) {
//                    result = response.body()!!
                    orderList = response.body()!!
                    println("orderlist : ${orderList}")
                }
                else {
                    Log.d(TAG, "shoppingplist - onResponse : Error code ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<OrderDTO4>>, t: Throwable) {
                t.printStackTrace()
                Log.d(TAG, "onFailure: ?????? ?????? ?????? ??????")
            }
        })
    }

}
