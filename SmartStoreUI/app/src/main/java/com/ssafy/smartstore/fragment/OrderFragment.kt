package com.ssafy.smartstore.fragment

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide.with
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.ssafy.smartstore.*
import com.ssafy.smartstore.database.OrderDetailDTO
import com.ssafy.smartstore.database.OrderDetailForShoppingList
import com.ssafy.smartstore.database.ProductDTO
import com.ssafy.smartstore.service.ProductService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.*
import kotlin.math.*


private const val TAG = "orderfragment"
class OrderFragment : Fragment() {
    private var repo = MobileCafeRepository.get()
    private lateinit var myListView: RecyclerView
    private lateinit var myAdapter: OrderAdapter
    private lateinit var ctx: Context
    private var order_img_List:List<ProductDTO> = arrayListOf()
    private lateinit var speechRecognizer: SpeechRecognizer
    private val T = 6372.8 * 1000

    private var mLocationListener: LocationListener? = null

    //프래그먼트를 액티비티에 붙일 때
    override fun onAttach(context: Context) {
        super.onAttach(context)
        //context받아옴
        ctx = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var rootview = inflater.inflate(R.layout.fragment_order, container, false)

        var btnShoppingList = rootview.findViewById<ImageButton>(R.id.btn_shoppingList)
        var imgMap = rootview.findViewById<ImageView>(R.id.img_map)

        var btnSpeech = rootview.findViewById<ImageButton>(R.id.btn_speech)

        var tvDistance = rootview.findViewById<TextView>(R.id.txt_distance)


        Log.d(TAG, "onCreateView: 현재 위치 ${LoginActivity.current_lan}")
        var distance = getDistance(LoginActivity.current_lan,LoginActivity.current_lon,LoginActivity.store_lan, LoginActivity.store_lon)

        if(distance<1000){
            tvDistance.text = "매장과의 거리가 ${distance}m 입니다"
        }
        else{
            var tmp = distance /1000
            tvDistance.text = "매장과의 거리가 ${tmp}km 입니다"

        }





        getAllProducts(rootview)//productDTO
//        initAdapter(rootview)

        btnShoppingList.setOnClickListener {
            var intent = Intent(ctx, ShoppingListActivity::class.java)
            startActivity(intent)
        }

        btnSpeech.setOnClickListener {

            //권한 설정
            requestPermission()
            //Log.d(TAG, "onCreateView: 마이크 클릭")

            // RecognizerIntent 생성
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.ssafy.smartstore")    // 여분의 키
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")         // 언어 설정

            // 새 SpeechRecognizer 를 만드는 팩토리 메서드
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(ctx)
            speechRecognizer.setRecognitionListener(recognitionListener)    // 리스너 설정
            speechRecognizer.startListening(intent)                         // 듣기 시작

        }



        //구글 맵 연결
        imgMap.setOnClickListener{
            //showDialog()

            var intent = Intent(ctx, MapActivity::class.java)
            startActivity(intent)

        }

        return rootview
    }



    // 권한 설정 메소드
    private fun requestPermission() {
        // 버전 체크, 권한 허용했는지 체크
        if (Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "requestPermission: 음성 녹음 권한 OK")
        }
        else{
            //2. 권한이 없다면
            // 3-1. 사용자가 권한이 없는 경우에는
            val permissionListener = object : PermissionListener {
                // 권한 얻기에 성공했을 때 동작 처리
                override fun onPermissionGranted() {
                    Log.d(TAG, "onPermissionGranted: 음성 녹음 권한 OK")
                }
                // 권한 얻기에 실패했을 때 동작 처리
                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(ctx,
                        "오디오 사용이 거부되었습니다.",
                        Toast.LENGTH_SHORT).show()
                }
            }

            TedPermission.create()
                .setPermissionListener(permissionListener)
                .setDeniedMessage("[설정] 에서 오디오 접근 권한을 부여해야만 사용이 가능합니다.")
                .setPermissions(
                    Manifest.permission.RECORD_AUDIO
                )
                .check()
        }
    }

    private val recognitionListener: RecognitionListener = object : RecognitionListener {
        // 말하기 시작할 준비가되면 호출
        override fun onReadyForSpeech(params: Bundle) {
            Toast.makeText(ctx, "음성인식 시작", Toast.LENGTH_SHORT).show()
        }
        // 말하기 시작했을 때 호출
        override fun onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech: 듣고있음")
        }
        // 입력받는 소리의 크기를 알려줌
        override fun onRmsChanged(rmsdB: Float) {}
        // 말을 시작하고 인식이 된 단어를 buffer에 담음
        override fun onBufferReceived(buffer: ByteArray) {}
        // 말하기를 중지하면 호출
        override fun onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech: 끝")
        }
        // 오류 발생했을 때 호출
        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "오디오 에러"
                SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "퍼미션 없음"
                SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트웍 타임아웃"
                SpeechRecognizer.ERROR_NO_MATCH -> "찾을 수 없음"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RECOGNIZER 가 바쁨"
                SpeechRecognizer.ERROR_SERVER -> "서버가 이상함"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "말하는 시간초과"
                else -> "알 수 없는 오류임"
            }
            Log.d(TAG, "onError: $message")
        }
        // 인식 결과가 준비되면 호출
        override fun onResults(results: Bundle) {
            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줌
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            for (i in matches!!.indices) {
                Log.d(TAG, "onResults: ${matches[i]}")
                //binding.textView.text = matches[i]
            }

            val arr = matches[matches.size-1].split(" ")

            //제품 이름으로 제품 id 찾기, getproduct에서 아이디 값에 해당하는 것 가져오기
            var index = findId(arr[0])-1
            //order_img_List[index]
            Log.d(TAG, "onResults: 찾은 아이디 값: ${findId(arr[0])}")

            val quantity = arr[arr.size-1]
            Log.d(TAG, "onResults: $quantity")
            try{
                val inputQuantity = quantity.toInt()
                var newOrderP = OrderDetailForShoppingList(
                    order_img_List[index].img,
                    order_img_List[index].name,
                    inputQuantity,
                    order_img_List[index].price,
                    order_img_List[index].price * (quantity.toInt()),
                    order_img_List[index].id,
                    -1
                )
                LoginActivity.tmpOrderList2.add(newOrderP)
            }catch(e: NumberFormatException){
                Toast.makeText(ctx, "정확한 수량을 말해주세요.",Toast.LENGTH_SHORT).show()
            }



//            val orderProduct = OrderDetailDTO(-1,id,Integer.parseInt(quantity.toString()))
//            LoginActivity.tmpOrderList.add(orderProduct)
//
//            //Log.d(TAG, "onCreate: ${orderProduct.quantity}개 주문")
//            //Log.d(TAG, "onCreate: ${binding.coffeeQuantity.text}개 주문")
//            Toast.makeText(ctx, "장바구니 담기 완료",Toast.LENGTH_SHORT).show()
        }
        // 부분 인식 결과를 사용할 수 있을 때 호출
        override fun onPartialResults(partialResults: Bundle) {}
        // 향후 이벤트를 추가하기 위해 예약
        override fun onEvent(eventType: Int, params: Bundle) {}
    }

    private fun findId(string: String):Int{
        
        var findid = 0
        Log.d(TAG, "findId: $string")
        var nameList = mutableListOf<String>()
        for (element in order_img_List){
            nameList.add(element.name)
        }

        Log.d(TAG, "findId: $nameList")

        for(i in 0 until nameList.size){
            val tmp = nameList[i].split(" ")
            if(tmp[0].contains(string)||string.contains(tmp[0])){
                findid=i+1
            }
        }
        
        return findid

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
    private fun initAdapter(inflater: View){

            // 1. ListView 객체 생성
            myListView = inflater.findViewById(R.id.allOrder_listView)
            myListView.layoutManager = GridLayoutManager(ctx, 3)

            // 2. Adapter 객체 생성(한 행을 위해 반복 생성할 Layout과 데이터 전달)
            myAdapter = OrderAdapter(ctx, R.layout.order_img, order_img_List)

            // 3. ListView와 Adapter 연결
            myListView.adapter = myAdapter

        // 4. 행을 터치했을 때 실행하는 콜백 함수
//        val itemClickListener =  object : MemoAdapter.ItemClickListener {
//            override fun onClick(view: View, position: Int) {
//
//                val intent = Intent(view.context, MemoEditActivity::class.java)
//
//                intent.putExtra("todo", memolist[position].title)
//                intent.putExtra("detail",memolist[position].content)
//                intent.putExtra("curTime",memolist[position].date)
//                intent.putExtra("position",(position).toString())
//                println("$position : position")
//
//                ListMemoEditActivityLauncher.launch(intent)
//            }
//        }
//        myAdapter.onItemClickListener = itemClickListener
    }

    private fun getAllProducts(rootview:View){

        val service = MobileCafeApplication.retrofit.create(ProductService::class.java)
        service.selectAllProduct().enqueue(object : Callback<List<ProductDTO>> {
            override fun onResponse(
                call: Call<List<ProductDTO>>,
                response: Response<List<ProductDTO>>
            ) {
                //정상일 경우 가져옴
                if (response.code() == 200) {
//                    result = response.body()!!
                    order_img_List = response.body()!!
                    initAdapter(rootview)
                    Log.d(TAG, "onResponse: ${order_img_List}")
                }
                else {
                    Log.d(TAG, "getAllBoard - onResponse : Error code ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<ProductDTO>>, t: Throwable) {
                t.printStackTrace()
                Log.d(TAG, "onFailure: 메뉴정보  불러오기 오류")
            }
        })

    }

    //현재 위치와 매장 사이 거리 구하기
    private fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int{
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2))
        val c = 2 * asin(sqrt(a))
        return (T * c).toInt()
    }



}