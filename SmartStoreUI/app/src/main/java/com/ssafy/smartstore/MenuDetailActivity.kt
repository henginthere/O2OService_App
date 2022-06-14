package com.ssafy.smartstore

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
//import com.ssafy.smartstore.LoginActivity.Companion.tmpOrderList
import com.ssafy.smartstore.LoginActivity.Companion.userId
import com.ssafy.smartstore.database.*
import com.ssafy.smartstore.databinding.ActivityMenuDetailBinding
import com.ssafy.smartstore.databinding.CommentListBinding
import com.ssafy.smartstore.service.CommentService
import com.ssafy.smartstore.service.ProductService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.sqrt

// F06: 주문 관리 - 상품 주문 - 로그인한 사용자는 상품 상세 화면 에서 n개를 선정하여 장바구니에 등록할 수 있다. 로그인 한 사용자만 자기의 계정으로 구매를 처리할 수 있다.

//코멘트 관련 입, 수 , 삭제 해결하기
private const val TAG = "MenuDetailActivity_싸피"
class MenuDetailActivity : AppCompatActivity() {
    private lateinit var commentListView: RecyclerView
    private lateinit var commentAdapter: CommentListAdapter
    private lateinit var binding:ActivityMenuDetailBinding
    private lateinit var binding_commentList:CommentListBinding
    private lateinit var dialog: Dialog
    private var productList:List<ProductDTOForEmpty> = arrayListOf()
    private var commentList:List<CommentDTOForUserId> = arrayListOf()
    private lateinit var commentDTO:CommentDTO

    private lateinit var accelerometerSensor: Sensor
    private lateinit var sensorManager: SensorManager
    private lateinit var sensorEventListener: SensorEventListener

    private var id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        id = intent!!.getStringExtra("productId")!!.toInt()
        binding = ActivityMenuDetailBinding.inflate(layoutInflater)
        binding_commentList = CommentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CoroutineScope(Dispatchers.Main).launch {
            CoroutineScope(Dispatchers.Main).async {
                //모든 데이터 받아오기
                getProduct()
            }.await()
            CoroutineScope(Dispatchers.Main).async {
                //모든 데이터 받아오기
                getComment()
            }.await()

        }

        //흔들기 감지로 장바구니에 상품 추가
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorEventListener = AccelerometerListener()

        sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_UI)

        binding.btnPlus.setOnClickListener{
            var txt = binding.coffeeQuantity.text
            var num = Integer.parseInt(txt.toString())
            binding.coffeeQuantity.text = (num+1).toString()
        }
        binding.btnMinus.setOnClickListener{
            var txt = binding.coffeeQuantity.text
            var num = Integer.parseInt(txt.toString())
            if(num > 1){
                binding.coffeeQuantity.text = (num-1).toString()
            }
        }

        //평점 등록시
        binding.btnAddReview.setOnClickListener{
            showDialog()
        }


        binding.btnAddShoppingList.setOnClickListener {
            val orderQuantity = binding.coffeeQuantity.text

            println("${binding.coffeeImg.background}")


            //data class OrderDetailForShoppingList(var img:String,var name: String, var quantity:Int,var price:Int,
            //                                      var totalprice: Int,var productId: Int)
            var newOrderP = OrderDetailForShoppingList(
                productList[0].img,
                productList[0].name,
                binding.coffeeQuantity.text.toString().toInt(),
                productList[0].price,
                productList[0].price * (binding.coffeeQuantity.text.toString().toInt()),
                id,
                -1
            )
            LoginActivity.tmpOrderList2.add(newOrderP)

            //위코드 대신 아래 코드 사용
//            val orderProduct = OrderDetailDTO(-1,id,Integer.parseInt(orderQuantity.toString()))
//            tmpOrderList.add(orderProduct)

            //Log.d(TAG, "onCreate: ${orderProduct.quantity}개 주문")
            //Log.d(TAG, "onCreate: ${binding.coffeeQuantity.text}개 주문")
            Toast.makeText(this, "장바구니 담기 완료",Toast.LENGTH_SHORT).show()

            finish()
        }

        commentAdapter(id)


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

            if(gForce>SHAKE_GRAVITY){
                Log.d(TAG, "onSensorChanged: $gForce")
                var newOrderP = OrderDetailForShoppingList(
                    productList[0].img,
                    productList[0].name,
                    binding.coffeeQuantity.text.toString().toInt(),
                    productList[0].price,
                    productList[0].price * (binding.coffeeQuantity.text.toString().toInt()),
                    id,
                    -1
                )
                LoginActivity.tmpOrderList2.add(newOrderP)
                Toast.makeText(this@MenuDetailActivity, "장바구니 담기 완료",Toast.LENGTH_SHORT).show()

                finish()
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    // dialog01을 디자인하는 함수
    private fun showDialog(){
        dialog = Dialog(this);       // Dialog 초기화
        var linear = LinearLayout.inflate(this, R.layout.dialog_rating,null)
        dialog.setContentView(linear);             // xml 레이아웃 파일과 연결
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.create()
        dialog.show() // 다이얼로그 띄우기

        println("??????:다이얼로그는 뜸? ")

        //취소 버튼
        var noBtn = dialog.findViewById<TextView>(R.id.btn_cancel)
        noBtn.setOnClickListener{
//            binding.review.text.clear()
            dialog.dismiss()
        }

        //확인 버튼
        dialog.findViewById<TextView>(R.id.btn_upload).setOnClickListener{

            var rating = dialog.findViewById<RatingBar>(R.id.choose_rating_bar)
            var content = binding.review.text.toString()

            var comment = CommentDTO(0,userId,id,(rating.rating*2).toInt(),content)
            commentDTO = CommentDTO(userId,id,(rating.rating*2).toInt(),content)
            println("insert Comment? : ${comment.userId} ${comment.productId} ${comment.rating} ${comment.comment}")

            insertCommnet()

            binding.review.text.clear()

            //강제 화면 갱신 -> 나중에 고치기
            updateUI()

            dialog.dismiss()
        }
    }
    private fun calAvg(){
        CoroutineScope(Dispatchers.Main).async {

            val product = productList


            var img = product[0].img
            var resId = applicationContext.resources.getIdentifier(
                img.substring(0, img.length - 4),
                "drawable",
                "com.ssafy.smartstore"
            )
            binding.coffeeImg.setImageResource(resId)
            binding.coffeeName.text = product[0].name
            binding.coffeePrice.text = product[0].price.toString()


            if(product[0].commentCnt != 0){
                var sum = 0.0
                for (i in commentList.indices) {
                    sum += commentList[i].rating
                }
                var avg = (sum / commentList.size.toDouble() / 2).toInt()
                binding.tvProductRating.text = "${avg} 점"
                binding.ratingbar.rating = avg.toFloat()

                binding.tvIsFirstComment.visibility = View.GONE
            }
        }
    }

    private fun updateUI(){
        var intent = intent;
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP //액티비티 스택제거
        overridePendingTransition(0, 0); //인텐트 애니메이션 없애기
        startActivity(intent); //현재 액티비티 재실행 실시
        overridePendingTransition(0, 0); //인텐트 애니메이션 없애기
    }


    private fun commentAdapter(id:Int){
        CoroutineScope(Dispatchers.Main).async {


            // 1. ListView 객체 생성
            commentListView = findViewById(R.id.comment_list)
            commentListView.layoutManager = LinearLayoutManager(
                applicationContext,
                LinearLayoutManager.VERTICAL, false
            )

            // 2. Adapter 객체 생성(한 행을 위해 반복 생성할 Layout과 데이터 전달)
            commentAdapter =
                CommentListAdapter(applicationContext, R.layout.comment_list, commentList)

            // 3. ListView와 Adapter 연결
            commentListView.adapter = commentAdapter


            // 4. 행을 터치했을 때 실행하는 콜백 함수    -> 나중에 스와이프로 바꾸자
            val itemClickListener = object : CommentListAdapter.ItemClickListener {
                override fun onClick(view: View, position: Int) {

                    var comment = view.findViewById<TextView>(R.id.tv_comment)

                    //수정 버튼
                    view.findViewById<ImageButton>(R.id.btn_review_edit).setOnClickListener{
                        //객체 가져와서 수정모드로 변경
                        println("comment : ${comment.text}")
                        var cur_comment = commentList[position].comment
                        view.findViewById<EditText>(R.id.edit_comment).setText(cur_comment)

                        //수정 시
                        //리뷰텍스트, 수정,삭제 버튼 지우고, 완료 버튼만 남기기
                        view.findViewById<TextView>(R.id.tv_comment).visibility = View.GONE
                        view.findViewById<EditText>(R.id.edit_comment).visibility = View.VISIBLE
                        view.findViewById<FrameLayout>(R.id.frame_review_edit).visibility = View.GONE
                        view.findViewById<FrameLayout>(R.id.frame_review_remove).visibility = View.GONE
                        view.findViewById<FrameLayout>(R.id.frame_review_edit_complete).visibility = View.VISIBLE


                        //왜 이건 안되는거지"??
//                        binding_commentList.tvComment.visibility = View.GONE
//                        binding_commentList.editComment.visibility = View.VISIBLE
//                        binding_commentList.frameReviewEdit.visibility = View.GONE
//                        binding_commentList.frameReviewRemove.visibility = View.GONE
//                        binding_commentList.frameReviewEditComplete.visibility = View.VISIBLE
                    }

                    //수정완료시
                    view.findViewById<ImageButton>(R.id.btn_review_edit_complete).setOnClickListener{
                        //수정,삭제 버튼 다시 생성하고
                        var edit_comment = view.findViewById<EditText>(R.id.edit_comment).text.toString()

                        var cl = commentList[position]

                        println("cl : ${cl.commentId} ${cl.user_id} ${cl.product_id} ${cl.rating} ${edit_comment}")
                        var new_Comment = CommentDTOForUpdate(cl.commentId,cl.user_id,id,cl.rating,edit_comment)
                        updateComment(new_Comment)

                        //수정 완료 버튼 없애기
                        view.findViewById<TextView>(R.id.tv_comment).visibility = View.VISIBLE
                        view.findViewById<EditText>(R.id.edit_comment).visibility = View.GONE
                        view.findViewById<FrameLayout>(R.id.frame_review_edit).visibility = View.VISIBLE
                        view.findViewById<FrameLayout>(R.id.frame_review_remove).visibility = View.VISIBLE
                        view.findViewById<FrameLayout>(R.id.frame_review_edit_complete).visibility = View.GONE
                        updateUI()
                    }

                    //삭제버튼
                    view.findViewById<ImageButton>(R.id.btn_review_remove).setOnClickListener{
                        //객체 가져와서 삭제
                        println("????+ ${commentList[position].comment}")
                        deleteComment(commentList[position].commentId)
                        CoroutineScope(Dispatchers.IO).launch{
//                            repo.commentDelete(commentList[position])
                        }
                        updateUI()
                    }

                }
            }
            commentAdapter.onItemClickListener = itemClickListener
        }
    }


    private fun getProduct(){

        val service = MobileCafeApplication.retrofit.create(ProductService::class.java)
        service.selectProductForEmpty(id).enqueue(object : Callback<List<ProductDTOForEmpty>> {
            override fun onResponse(
                call: Call<List<ProductDTOForEmpty>>,
                response: Response<List<ProductDTOForEmpty>>
            ) {
                //정상일 경우 가져옴
                if (response.code() == 200) {
//                    result = response.body()!!
                    productList = response.body()!!
                    commentAdapter(id)
                    Log.d(TAG, "onResponse: ${productList}")
                }
                else {
                    Log.d(TAG, "getAllBoard - onResponse : Error code ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<ProductDTOForEmpty>>, t: Throwable) {
                t.printStackTrace()
                Log.d(TAG, "onFailure: 메뉴정보  불러오기 오류")
            }
        })
    }

    private fun getComment(){

        val service = MobileCafeApplication.retrofit.create(ProductService::class.java)
        service.selectComment2(id).enqueue(object : Callback<List<CommentDTOForUserId>> {
            override fun onResponse(
                call: Call<List<CommentDTOForUserId>>,
                response: Response<List<CommentDTOForUserId>>
            ) {
                //정상일 경우 가져옴
                if (response.code() == 200) {
//                    result = response.body()!!
                    commentList = response.body()!!

                    calAvg()//평점 계산
                    commentAdapter(id)

                    Log.d(TAG, "onResponse: ${commentList}")
                }
                else {
                    Log.d(TAG, "getAllBoard - onResponse : Error code ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<CommentDTOForUserId>>, t: Throwable) {
                t.printStackTrace()
                Log.d(TAG, "onFailure: 코멘트  불러오기 오류")
            }
        })
    }

    private fun insertCommnet(){
        println("???? : !?!?!?!")
        println("insertComment : 내부에 들어옴? : ${commentDTO.userId}")

        val service = MobileCafeApplication.retrofit.create(CommentService::class.java)
        service.insertComment(commentDTO).enqueue(object : Callback<Boolean> {
            override fun onResponse(
                call: Call<Boolean>,
                response: Response<Boolean>
            ) {
                //정상일 경우 가져옴
                if (response.code() == 200) {
//                    result = response.body()!!
                    var check = response.body()!!
                    updateUI()
//                    calAvg()//평점 계산
//                    commentAdapter(id)

                    Log.d(TAG, "onResponse: ${check}")
                }
                else {
                    Log.d(TAG, "코멘트 추가 오류 - onResponse : Error code ${response.code()}")
                }
            }
            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                t.printStackTrace()
                Log.d(TAG, "onFailure: 코멘트 추가 불러오기 오류")
            }
        })
    }

    private fun updateComment(nc:CommentDTOForUpdate){
        println("updateComment : 내부에 들어옴? : ${nc.userId} ")

        val service = MobileCafeApplication.retrofit.create(CommentService::class.java)
        service.updateComment(nc).enqueue(object : Callback<Boolean> {
            override fun onResponse(
                call: Call<Boolean>,
                response: Response<Boolean>
            ) {
                //정상일 경우 가져옴
                if (response.code() == 200) {
//                    result = response.body()!!
                    var check = response.body()!!
                    updateUI()
//                    calAvg()//평점 계산
//                    commentAdapter(id)

                    Log.d(TAG, "onResponse: ${check}")
                }
                else {
                    Log.d(TAG, "코멘트 업뎃 오류 - onResponse : Error code ${response.code()}")
                }
            }
            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                t.printStackTrace()
                Log.d(TAG, "onFailure: 코멘트 업뎃  불러오기 오류")
            }
        })
    }

    private fun deleteComment(index:Int){
        println("deleteComment : 내부에 들어옴? : ${index} ")

        val service = MobileCafeApplication.retrofit.create(CommentService::class.java)
        service.deleteComment(index).enqueue(object : Callback<Boolean> {
            override fun onResponse(
                call: Call<Boolean>,
                response: Response<Boolean>
            ) {
                //정상일 경우 가져옴
                if (response.code() == 200) {
//                    result = response.body()!!
                    var check = response.body()!!
                    updateUI()

                    Log.d(TAG, "삭제 onResponse: ${check}")
                }
                else {
                    Log.d(TAG, "코멘트 삭제 오류 - onResponse : Error code ${response.code()}")
                }
            }
            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                t.printStackTrace()
                Log.d(TAG, "onFailure: 코멘트 삭제  불러오기 오류")
            }
        })
    }
}
