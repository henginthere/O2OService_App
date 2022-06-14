package com.ssafy.smartstore

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.smartstore.LoginActivity.Companion.tmpOrderList2
//import com.ssafy.smartstore.LoginActivity.Companion.tmpOrderList
import com.ssafy.smartstore.LoginActivity.Companion.userId
import com.ssafy.smartstore.Util.Utils
import com.ssafy.smartstore.database.*
import com.ssafy.smartstore.databinding.ActivityShoppingListBinding
import com.ssafy.smartstore.databinding.ShoppingListItemBinding
import com.ssafy.smartstore.service.OrderService
import com.ssafy.smartstore.service.ProductService
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.sqrt


// F09: 상품관리 - 상품별 정보 조회 - 상품별로 이름, 이미지, 단가, 총 주문 수량을 출력한다.
// 장바구니 화면

private const val TAG = "ShoppingListActivity_싸피"
class ShoppingListActivity : AppCompatActivity() {
    private lateinit var myListView: RecyclerView
    private lateinit var ShoppingListAdapter: ShoppingListAdapter
    private lateinit var nfcAdapter: NfcAdapter

    private var orderTable = "init"
    lateinit var binding: ActivityShoppingListBinding
    private lateinit var binding1: ShoppingListItemBinding
    lateinit var orderDTO:OrderDTO2
    var check = 0

    private lateinit var accelerometerSensor: Sensor
    private lateinit var sensorManager: SensorManager
    private lateinit var sensorEventListener: SensorEventListener

    var isNfc = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        binding = ActivityShoppingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        shoppingListAdapter()

        //nfc
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if(nfcAdapter==null){
            finish()
        }
        var recI = intent
        orderTable = processIntent(recI)
        Log.d(TAG, "onCreate: 등록된 테이블 : $orderTable")

        calPrice()

        binding.btnAddOrder.setOnClickListener {

            //주문하기 버튼 눌렀을 때 table 번호 등록이 안되어있다면 nfc 태깅요청

            if (orderTable == "init") {
                val builder = AlertDialog.Builder(this)

                builder.setTitle("알림").setMessage("Table NFC를 먼저 찍어주세요.")

                builder.setPositiveButton("확인",
                    DialogInterface.OnClickListener { dialog, id ->

                    })
                builder.show()
                Log.d(TAG, "onCreate: 테이블 번호 없음")

            } else {
                println("?????")
                var detail = mutableListOf<OrderDetailDTO>()
                var stamp: StampDTO
                //orderDetail테이블 만들어서 order에 추가
                var stampSum = 0
                var orderId = 0
                for (i in 0 until tmpOrderList2.size) {
                    //order_id, product_id, quantity
                    var orderDetail = OrderDetailDTO(
                        0, tmpOrderList2[i].orderId, tmpOrderList2[i].productId,
                        tmpOrderList2[i].quantity
                    )
                    detail.add(orderDetail)
                    stampSum += tmpOrderList2[i].quantity
                    orderId = tmpOrderList2[i].orderId
                }
                //	private Integer id;
                //	private String userId;
                //	private String orderTable;
                //	private Date orderTime;
                //	private Character completed;
                //	private List<OrderDetail> details;
                //	private Stamp stamp;
                var curTime = Utils.formatter().format(System.currentTimeMillis())
                println("details: size : ${detail.size}")
                println("details info : ${detail[0].order_id} ${detail[0].quantity} ${detail[0].productId}")
                //userId(order.getUserId()).quantity(quantitySum).orderId(order.getId()).build(
                stamp = StampDTO(userId, stampSum, orderId)
                orderDTO = OrderDTO2(userId, orderTable, "${curTime}", "N", detail, stamp)

                updateShoppingList()

                isNfc = true
            }

            if(isNfc){
                finish()
                isNfc=false
            }
        }

        //흔들기 모션 감지로 주문하기

        //가속도 센서
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorEventListener = AccelerometerListener()

        sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorEventListener)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(sensorEventListener)
    }

    private inner class AccelerometerListener : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {

            //흔들림 감지 시 기준이 되는 힘
            val SHAKE_GRAVITY = 3.0F

            var x = event.values[0]
            var y = event.values[1]
            var z = event.values[2]

            var gX = x / SensorManager.GRAVITY_EARTH
            var gY = y / SensorManager.GRAVITY_EARTH
            var gZ = z / SensorManager.GRAVITY_EARTH

            var gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

            if (gForce > SHAKE_GRAVITY) {
                Log.d(TAG, "onSensorChanged: $gForce")
                //주문하기 버튼 눌렀을 때 table 번호 등록이 안되어있다면 nfc 태깅요청

                if (orderTable == "init") {
                    val builder = AlertDialog.Builder(this@ShoppingListActivity)

                    builder.setTitle("알림").setMessage("Table NFC를 먼저 찍어주세요.")

                    builder.setPositiveButton("확인",
                        DialogInterface.OnClickListener { dialog, id ->

                        })
                    builder.show()
                    Log.d(TAG, "onCreate: 테이블 번호 없음")

                } else {
                    println("?????")
                    var detail = mutableListOf<OrderDetailDTO>()
                    var stamp: StampDTO
                    //orderDetail테이블 만들어서 order에 추가
                    var stampSum = 0
                    var orderId = 0
                    for (i in 0 until tmpOrderList2.size) {
                        //order_id, product_id, quantity
                        var orderDetail = OrderDetailDTO(
                            0, tmpOrderList2[i].orderId, tmpOrderList2[i].productId,
                            tmpOrderList2[i].quantity
                        )
                        detail.add(orderDetail)
                        stampSum += tmpOrderList2[i].quantity
                        orderId = tmpOrderList2[i].orderId
                    }
                    //	private Integer id;
                    //	private String userId;
                    //	private String orderTable;
                    //	private Date orderTime;
                    //	private Character completed;
                    //	private List<OrderDetail> details;
                    //	private Stamp stamp;
                    var curTime = Utils.formatter().format(System.currentTimeMillis())
                    println("details: size : ${detail.size}")
                    println("details info : ${detail[0].order_id} ${detail[0].quantity} ${detail[0].productId}")
                    //userId(order.getUserId()).quantity(quantitySum).orderId(order.getId()).build(
                    stamp = StampDTO(userId, stampSum, orderId)
                    orderDTO = OrderDTO2(userId, "", "${curTime}", "N", detail, stamp)


                    updateShoppingList()
                }
            }

        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
    }


        private fun processIntent(intent: Intent):String{
        //1. intent에서 NdefMessage 배열 데이터를 가져온다
        val rawMsg = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)

        //2. Data를 변환
        if(rawMsg!=null){
            val msgArr = arrayOfNulls<NdefMessage>(rawMsg.size)

            for(i in rawMsg.indices){
                msgArr[i] = rawMsg[i] as NdefMessage?
            }

            //3. NdefMessage에서 NdefRecode -> payload
            var payload = msgArr[0]!!.records[0].payload


            //tv.setText("태그 데이터: ${String(payload)}")

            //enssafy 되어 있는 값에서 encoding 부분 제거하고 출력
            Log.d(TAG, "processIntent: 태그 데이터: ${String(payload,3,payload.size-3)}")
            Toast.makeText(
                applicationContext,
                "${String(payload,3,payload.size-3)} 테이블이 등록 되었습니다.",
                Toast.LENGTH_SHORT
            ).show()
            return String(payload,3,payload.size-3)
        }
        return "init"
    }

    private fun calPrice(){
        var totalQuantity = 0
        var totalPrice = 0

        CoroutineScope(Dispatchers.IO).launch {
            for (i in 0 until tmpOrderList2.size) {
                totalQuantity += tmpOrderList2[i].quantity
                totalPrice+=tmpOrderList2[i].price* tmpOrderList2[i].quantity
            }
            binding.orderQuantityTotal.text = "총 ${totalQuantity}개"
            binding.orderPriceTotal.text = "${totalPrice}원"
        }

    }

    private fun shoppingListAdapter() {

            // 1. ListView 객체 생성
            myListView = findViewById(R.id.shopping_listView)
            myListView.layoutManager = LinearLayoutManager(
                applicationContext,
                LinearLayoutManager.VERTICAL, false
            )

            // 2. Adapter 객체 생성(한 행을 위해 반복 생성할 Layout과 데이터 전달)
            ShoppingListAdapter = ShoppingListAdapter(
                applicationContext,
                R.layout.shopping_list_item,
                tmpOrderList2
            )


            // 3. ListView와 Adapter 연결
            myListView.adapter = ShoppingListAdapter

            ShoppingListAdapter.notifyDataSetChanged()

    }

    private fun updateShoppingList(){
        println("orderDTO????? : ${orderDTO.details[0].productId}")
        val service = MobileCafeApplication.retrofit.create(OrderService::class.java)
        service.insertOrder(orderDTO).enqueue(object : Callback<Int> {
            override fun onResponse(
                call: Call<Int>,
                response: Response<Int>
            ) {
                //정상일 경우 가져옴
                if (response.code() == 200) {
//                    result = response.body()!!
                    check = response.body()!!
                    Toast.makeText(this@ShoppingListActivity,"주문이 완료되었습니다.",Toast.LENGTH_SHORT).show()
     //               tmpOrderList2.clear()

                    var intent = Intent(applicationContext, OrderDetailActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP //액티비티 스택제거
                    intent.putExtra("orderId",check)
                    intent.putExtra("orderTime","${Utils.formatter().format(System.currentTimeMillis())}")
                    startActivity(intent)
                    Log.d(TAG, "onResponse: ${check}")
                }
                else {
                    Log.d(TAG, "shoppingplist - onResponse : Error code ${response.code()}")
                }
            }
            override fun onFailure(call: Call<Int>, t: Throwable) {
                t.printStackTrace()
                Log.d(TAG, "onFailure: 쇼핑리스트 업뎃 오류")
            }
        })
    }
}