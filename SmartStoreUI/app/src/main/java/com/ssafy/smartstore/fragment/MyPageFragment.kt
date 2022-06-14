package com.ssafy.smartstore.fragment

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.user.UserApiClient
import com.ssafy.smartstore.LoginActivity
import com.ssafy.smartstore.LoginActivity.Companion.userName
import com.ssafy.smartstore.LoginActivity.Companion.userStamp
import com.ssafy.smartstore.MobileCafeApplication
import com.ssafy.smartstore.MyPageOrderListAdapter
import com.ssafy.smartstore.R
import com.ssafy.smartstore.database.OrderDTO3
import com.ssafy.smartstore.service.OrderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "mypageFragment"
class MyPageFragment : Fragment() {
//    private var repo = MobileCafeRepository.get()
    private lateinit var myListView: RecyclerView
    private lateinit var myAdapter: MyPageOrderListAdapter
    private lateinit var ctx: Context
    private var orderDetailList:List<OrderDTO3> = arrayListOf()

    var order_id = LoginActivity.recent_order // 가장 최근 주문 order_id
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
        var rootview = inflater.inflate(R.layout.fragment_my_page, container, false)



        var txtName = rootview.findViewById<TextView>(R.id.txtName)
        var levelName = rootview.findViewById<TextView>(R.id.levelName)
        var btnLogout = rootview.findViewById<ImageView>(R.id.btn_logout)
        //유저이름 바꾸기
        txtName.text = userName

        //유저 단계 바꾸기
        levelName.text = calLevel(rootview)

        //로그아웃 버튼
        btnLogout.setOnClickListener{
            //sharedPreference초기화
            MobileCafeApplication.prefs.setString("id", "")
            MobileCafeApplication.prefs.setString("pw", "")

            //로그아웃 하기
            LoginActivity.userId = ""
            LoginActivity.userPW = ""
            LoginActivity.userName = ""
            LoginActivity.userStamp = 0
            LoginActivity.recent_order = -1

            UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
                if (error != null) {
                    //Toast.makeText(this, "토큰 정보 보기 실패", Toast.LENGTH_SHORT).show()
                } else if (tokenInfo != null) {
                 UserApiClient.instance.logout { error ->
                if (error != null) {
                    //Toast.makeText(context, "로그아웃 실패 $error", Toast.LENGTH_SHORT).show()
                }else {
                    ///Toast.makeText(context, "로그아웃 성공", Toast.LENGTH_SHORT).show()
                }
                    }
                }
            }

            if(Firebase.auth.currentUser != null){
                Toast.makeText(context, "구글 로그아웃 성공", Toast.LENGTH_SHORT).show()
                Firebase.auth.signOut()
            }
//                val intent = Intent(context, LoginActivity::class.java)
//                startActivity(intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP))
                //finish()

            Log.d(TAG, "onCreateView: 로그아웃 성공")

            var intent = Intent(ctx, LoginActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            //startActivity(intent)
//            activity?.supportFragmentManager
//                ?.beginTransaction()
//                ?.remove(this)
//                ?.commit()

        }


            getOrderDetail(rootview)

//        //주문내역 보이기
//        initAdapter(rootview)

        return rootview
    }

    private fun calLevel(view: View): String{
        var curLevel = ""
        var required_stamp=10
        var current_level_stamp = userStamp - 50

        //총 잔수 확인

        //스탬프 개수따른 유저 레벨 출력
        if(userStamp == 0){
            curLevel = "나무";
        }
        else if(userStamp in 1..50){
            required_stamp = 10

            var remain = userStamp / required_stamp + 1
            curLevel = "씨앗${remain}단계"

            var remainQuantity = userStamp % required_stamp
            view.findViewById<TextView>(R.id.tv_remain_textView).text = "다음 레벨까지 ${required_stamp-remainQuantity}잔 남았습니다."

            var progress = view.findViewById<ProgressBar>(R.id.progressbar)
            progress.progress = remainQuantity
            progress.max = required_stamp

            var level = view.findViewById<TextView>(R.id.remainLevel)
            level.text = "${remainQuantity}/10"
        }
        else if(userStamp in 51..125){
             required_stamp = 15
             current_level_stamp = userStamp - 50

            var remain = current_level_stamp / required_stamp + 1
            curLevel = "꽃${remain}단계"

            var remainQuantity = current_level_stamp % required_stamp
            view.findViewById<TextView>(R.id.tv_remain_textView).text = "다음 레벨까지 ${required_stamp-remainQuantity}잔 남았습니다."

            var progress = view.findViewById<ProgressBar>(R.id.progressbar)
            progress.progress = remainQuantity
            progress.max = required_stamp

            var level = view.findViewById<TextView>(R.id.remainLevel)
            level.text = "${remainQuantity}/$required_stamp"
        }
        else if(userStamp in 126..225){
             required_stamp = 20
             current_level_stamp = userStamp - 125

            var remain = current_level_stamp / required_stamp + 1
            curLevel = "열매${remain}단계"

            var remainQuantity = current_level_stamp % required_stamp
            view.findViewById<TextView>(R.id.tv_remain_textView).text = "다음 레벨까지 ${required_stamp-remainQuantity}잔 남았습니다."

            var progress = view.findViewById<ProgressBar>(R.id.progressbar)
            progress.progress = remainQuantity
            progress.max = required_stamp

            var level = view.findViewById<TextView>(R.id.remainLevel)
            level.text = "${remainQuantity}/$required_stamp"
        }
        else if(userStamp in 226..350){
             required_stamp = 25
             current_level_stamp = userStamp - 225

            var remain = current_level_stamp / required_stamp + 1
            curLevel = "커피콩${remain}단계"

            var remainQuantity = current_level_stamp % required_stamp
            view.findViewById<TextView>(R.id.tv_remain_textView).text = "다음 레벨까지 ${required_stamp-remainQuantity}잔 남았습니다."

            var progress = view.findViewById<ProgressBar>(R.id.progressbar)
            progress.progress = remainQuantity
            progress.max = required_stamp

            var level = view.findViewById<TextView>(R.id.remainLevel)
            level.text = "${remainQuantity}/$required_stamp"
        }

        return curLevel
    }

    private fun initAdapter(inflater:View){
        CoroutineScope(Dispatchers.Main).launch {


//            //유저 아이디로 주문내역 가져와서
//             orderList:List<OrderDTO> = repo.getOrders(LoginActivity.userId)
//            //유저 아이디 수만큼 t_order_detail에서 가져와서 담기
//            var orderDetailList = arrayListOf<OrderDetailDTO>()
//            for(i in orderList.indices){
//                var list = repo.getOrderDetails(orderList[i].id)
//                for(j in list.indices){
//                    orderDetailList.add(list[j])
//                }
//            }

            // 1. ListView 객체 생성
            myListView = inflater.findViewById(R.id.listView)
            myListView.layoutManager = LinearLayoutManager(ctx,
                LinearLayoutManager.HORIZONTAL, false)

            // 2. Adapter 객체 생성(한 행을 위해 반복 생성할 Layout과 데이터 전달)
            myAdapter = MyPageOrderListAdapter(ctx, R.layout.recyclerview_order_list, orderDetailList)

            // 3. ListView와 Adapter 연결
            myListView.adapter = myAdapter
        }

    }

    private fun getOrderDetail(rootview:View){
        Log.d(TAG, "getOrderDetail: $order_id")

        val service = MobileCafeApplication.retrofit.create(OrderService::class.java)
        service.selectOrderDTO3(order_id).enqueue(object :
            Callback<List<OrderDTO3>> {
            override fun onResponse(
                call: Call<List<OrderDTO3>>,
                response: Response<List<OrderDTO3>>
            ) {
                //정상일 경우 가져옴
                if (response.code() == 200) {
                    var result = response.body()!!
                    orderDetailList = result
                    //주문내역 보이기
                    initAdapter(rootview)

//                    orderDetailAdapter()
                    println("onrespose detail : ${orderDetailList}")
                }
                else {
                    Log.d(TAG, "getAllBoard - onResponse : Error code ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<OrderDTO3>>, t: Throwable) {
                t.printStackTrace()
                Log.d(TAG, "onFailure: 주문상세  불러오기 오류")
            }
        })
    }


}