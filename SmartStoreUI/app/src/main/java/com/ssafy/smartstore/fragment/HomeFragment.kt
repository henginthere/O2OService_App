package com.ssafy.smartstore.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.collection.arrayMapOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.ssafy.smartstore.*
import com.ssafy.smartstore.LoginActivity.Companion.notiList
import com.ssafy.smartstore.LoginActivity.Companion.userId
import com.ssafy.smartstore.LoginActivity.Companion.userName
import com.ssafy.smartstore.Util.Utils
import com.ssafy.smartstore.database.*
import com.ssafy.smartstore.service.OrderService
import com.ssafy.smartstore.service.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.concurrent.thread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.HashMap

private const val TAG: String = "HomeFragment_싸피"
class HomeFragment : Fragment() {
    private lateinit var myListView: RecyclerView
    private lateinit var myAdapter: HomeRecentListAdapter

    private lateinit var notiListView: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter

    private lateinit var ctx: Context
    private var myFirebaseMessageService= MyFirebaseMessageService()
    private var userInfo : Map<String, Object> = mutableMapOf()

    private var orderList:List<OrderDTO4> = arrayListOf()

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
        var rootview =  inflater.inflate(R.layout.fragment_home, container, false)

        var txt_userName = rootview.findViewById<TextView>(R.id.txt_userName)

        txt_userName.text = userName



        val service = MobileCafeApplication.retrofit.create(UserService::class.java)
        service.getUser(LoginActivity.userId).enqueue(object :
            Callback<Map<String, Object>> {
            override fun onResponse(
                call: Call<Map<String, Object>>,
                response: Response<Map<String, Object>>
            ) {
                if (response.code() == 200) {
                    var check = response.body()!!
                    userInfo = check
                    var tmp = userInfo["user"] as Map<String, Any>
                    var tmp1 = userInfo["grade"] as Map<String, Any>
                    Log.d(TAG, "onResponse: ${check}")
                    Log.d(TAG, "onResponse: ${tmp["name"]}")
                    Log.d(TAG, "onResponse: ${tmp["title"]}")
                } else {
                    Log.d(
                        TAG,
                        "불러오기 오류 - onResponse : Error code ${response.code()}"
                    )
                }
            }

            override fun onFailure(
                call: Call<Map<String, Object>>,
                t: Throwable
            ) {
                Log.d(TAG, "onFailure: 사용자 정보 가져오기 오류")
            }

        })

        notiAdapter(rootview)
        //최근내역
        getRecentOrder(rootview)

        return rootview
    }

    private fun notiAdapter(inflater: View){
        // 1. ListView 객체 생성
        notiListView = inflater.findViewById(R.id.notify_listView)

        notiListView.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)

        // 2. Adapter 객체 생성(한 행을 위해 반복 생성할 Layout과 데이터 전달)
        notificationAdapter = NotificationAdapter(ctx, R.layout.notification_item,notiList)

        // 3. ListView와 Adapter 연결
        notiListView.adapter = notificationAdapter
    }


    private fun homeAdapter(inflater:View){
        CoroutineScope(Dispatchers.Main).launch {

            var map = LinkedHashMap<Int, MutableList<OrderDTO4>>()
            var list = mutableListOf<OrderDTO4>()
            var map_key = mutableListOf<Int>()

            for(i in orderList.indices){
                if(list.isEmpty()){
                    list.add(orderList[i])
                }else{
                    if(list[0].o_id == orderList[i].o_id){
                        list.add(orderList[i])
                    }else if(list[0].o_id != orderList[i].o_id){
                        map_key.add(list[0].o_id)
                        map.put(list[0].o_id, list)
                        list = mutableListOf()
                        list.add(orderList[i])
                    }
                }
            }
            println("list : ${list.size}")


            // 1. ListView 객체 생성
            myListView = inflater.findViewById(R.id.listView)

            myListView.layoutManager = LinearLayoutManager(ctx,
                LinearLayoutManager.HORIZONTAL, false)

            // 2. Adapter 객체 생성(한 행을 위해 반복 생성할 Layout과 데이터 전달)
            myAdapter = HomeRecentListAdapter(ctx, R.layout.recyclerview_recent_order_list,map,map_key)

            // 3. ListView와 Adapter 연결
            myListView.adapter = myAdapter
        }

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
    private fun getRecentOrder(rootview:View){
//        println("orderDTO????? : ${orderDTO.details[0].productId}")
        val service = MobileCafeApplication.retrofit.create(OrderService::class.java)
        service.selectRecentOrder(userId).enqueue(object : Callback<List<OrderDTO4>> {
            override fun onResponse(
                call: Call<List<OrderDTO4>>,
                response: Response<List<OrderDTO4>>
            ) {
                //정상일 경우 가져옴
                if (response.code() == 200) {
//                    result = response.body()!!
                    orderList = response.body()!!
                    Log.d(TAG, "onResponse: orderlist : {$orderList}")
                    homeAdapter(rootview)

                }
                else {
                    Log.d(TAG, "shoppingplist - onResponse : Error code ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<OrderDTO4>>, t: Throwable) {
                t.printStackTrace()
                Log.d(TAG, "onFailure: 최근 내용 업뎃 오류")
            }
        })
    }
}