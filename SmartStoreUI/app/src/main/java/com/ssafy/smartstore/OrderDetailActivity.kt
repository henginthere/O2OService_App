package com.ssafy.smartstore

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.smartstore.LoginActivity.Companion.recent_order
import com.ssafy.smartstore.LoginActivity.Companion.tmpOrderList2
import com.ssafy.smartstore.database.OrderDetailDTO2
import com.ssafy.smartstore.database.ProductDTO
import com.ssafy.smartstore.databinding.ActivityOrderDetailBinding
import com.ssafy.smartstore.fragment.MyPageFragment
import com.ssafy.smartstore.service.OrderService
import com.ssafy.smartstore.service.ProductService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



// F08: 주문관리 - 주문 상세 조회 - 주문 번호에 기반하여 주문을 조회할 수 있다. 이때 주문 상세 항목들 어떤 상품이 몇개 주문 되었는지에 대한 정보도 포함한다.

//주문 가격들 안나옴 확인할 것
private const val TAG = "OrderDetailActivity_싸피"
class OrderDetailActivity : AppCompatActivity() {
    private lateinit var myListView: RecyclerView
    private lateinit var orderDetailAdapter : OrderDetailAdapter
    private lateinit var binding: ActivityOrderDetailBinding
    var getOrderIdInShoppingList = 0
    private var orderList:List<OrderDetailDTO2> = arrayListOf()
    private var product:MutableList<ProductDTO> = arrayListOf()

    var orderTime = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getOrderIdInShoppingList = intent.getIntExtra("orderId",0)
        recent_order = getOrderIdInShoppingList

        println("odidididid $getOrderIdInShoppingList")

        orderTime = intent.getStringExtra("orderTime").toString()

        getOrderDetail()

        binding.btnBackToHome.setOnClickListener{
            tmpOrderList2.clear()
            var intent = Intent(this, MainActivity::class.java)
//            intent.flags =
//                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP //액티비티 스택제거
            //startActivity(intent)
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }



    }

    private fun calculate(){
        CoroutineScope(Dispatchers.Main).launch {

//            orderList = repo.getOrderDetails(getOrderIdInShoppingList)
            var totalQuantity = 0
            var totalPrice = 0

                Log.d(TAG, "calculate: orderlist size : ${orderList.size}")
                for (i in orderList.indices) {
                    totalQuantity += orderList[i].quantity
                    totalPrice+=tmpOrderList2[i].price* tmpOrderList2[i].quantity
                    Log.d(TAG, "calculate: $totalPrice")
                    CoroutineScope(Dispatchers.Main).async {
                        getProduct(orderList[i].p_id)
                    }.await()
//
//                    CoroutineScope(Dispatchers.Main).async {
//                        println("price 왜 안나와? ${price}")
//                        totalPrice += price * totalQuantity
//
//                    }.await()
                }
            Log.d(TAG, "calculate: $totalPrice")

            CoroutineScope(Dispatchers.IO).async {
                binding.orderDate.text = orderTime
                binding.orderPrice.text = totalPrice.toString()+"원"
                orderDetailAdapter()
            }.await()
        }



    }


    private fun orderDetailAdapter(){

        CoroutineScope(Dispatchers.Main).launch {

            // 1. ListView 객체 생성
            myListView = findViewById(R.id.detail_listView)
            myListView.layoutManager = LinearLayoutManager(applicationContext,
                LinearLayoutManager.VERTICAL, false)

            // 2. Adapter 객체 생성(한 행을 위해 반복 생성할 Layout과 데이터 전달)
            orderDetailAdapter = OrderDetailAdapter(applicationContext, R.layout.shopping_list_item, orderList, product)

            // 3. ListView와 Adapter 연결
            myListView.adapter = orderDetailAdapter
        }
    }

    private fun getOrderDetail(){
        println("??과연?? : $getOrderIdInShoppingList")

        val service = MobileCafeApplication.retrofit.create(OrderService::class.java)
        service.selectOrderDetail(getOrderIdInShoppingList).enqueue(object :
            Callback<List<OrderDetailDTO2>> {
            override fun onResponse(
                call: Call<List<OrderDetailDTO2>>,
                response: Response<List<OrderDetailDTO2>>
            ) {
                //정상일 경우 가져옴
                if (response.code() == 200) {
                    var result = response.body()!!
                    orderList = result
                    calculate()
                    println("onrespose detail : ${orderList}")
                    Log.d(TAG, "onResponse??: ${orderList}")
                }
                else {
                    Log.d(TAG, "getAllBoard - onResponse : Error code ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<OrderDetailDTO2>>, t: Throwable) {
                t.printStackTrace()
                Log.d(TAG, "onFailure: 주문상세  불러오기 오류")
            }
        })
    }

    private fun getProduct(i:Int){
        val service = MobileCafeApplication.retrofit.create(ProductService::class.java)
        service.selectProduct(i).enqueue(object : Callback<List<ProductDTO>> {
            override fun onResponse(
                call: Call<List<ProductDTO>>,
                response: Response<List<ProductDTO>>
            ) {
                //정상일 경우 가져옴
                if (response.code() == 200) {
                    var result = response.body()!!
                    product.add(result[0])
                    orderDetailAdapter()
                }
                else {
                    Log.d(TAG, "getProduct - onResponse : Error code ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<ProductDTO>>, t: Throwable) {
                t.printStackTrace()
                Log.d(TAG, "onFailure: 상품정보  불러오기 오류")
            }
        })
    }

}