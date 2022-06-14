package com.ssafy.smartstore

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.smartstore.LoginActivity.Companion.tmpOrderList2
import com.ssafy.smartstore.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.LinkedHashMap
import kotlin.concurrent.thread

private const val TAG = "HomeRecentList_싸피"

class HomeRecentListAdapter(var context: Context, val resource: Int, objects: LinkedHashMap<Int, MutableList<OrderDTO4>>, var mapKey:MutableList<Int>)
    : RecyclerView.Adapter<HomeHolder>() {

    //사용하고자 하는 데이터
    var orderList = objects

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(resource, parent, false)

        return HomeHolder(itemView)
    }

    override fun onBindViewHolder(holder: HomeHolder, position: Int) {
        //한번에 orderId별로 다 가져와서 두개 이상이면 ~외 ~잔, 이름, 가격 , 날짜 출력
        var cur_info = orderList.getValue(mapKey[position])


        var img = cur_info[0].img

        var resId = context.resources.getIdentifier(
            img.substring(0, img.length - 4),
            "drawable",
            "com.ssafy.smartstore"
        )
        holder.productsImg.setImageResource(resId)


        var sum = 0
        var totalQuantity = 0
        if(cur_info.size >= 2){
            for(i in cur_info.indices){
                sum += cur_info[i].price * cur_info[i].quantity
                totalQuantity += cur_info[i].quantity
            }
            holder.productsName.text = "${cur_info[0].name} 외 ${totalQuantity}잔"
        }else{
            sum += cur_info[0].price * cur_info[0].quantity
            holder.productsName.text = "${cur_info[0].name}"
        }


        holder.orderPrice.text = "${sum}원"


        holder.orderDate.text = cur_info[0].order_time.substring(0,10)

        holder.itemView.setOnClickListener {
            //최근 주문 클릭하면
            //tmp_list에 상품 담기게 하자
            //상품 담은 뒤에 shoppingListActivity로 넘어가게 한다.
            //println("????? : ${listData[position].id}")
            Log.d(TAG, "onBindViewHolder: ${cur_info}")

            for(i in cur_info.indices){
                val orderProduct = OrderDetailDTO(-1,cur_info[i].p_id,Integer.parseInt(cur_info[i].quantity.toString()))

                //data class OrderDetailForShoppingList(var img:String,var name: String, var quantity:Int,var price:Int,
                //                       var totalprice: Int,var productId: Int)
                var newOrderP = OrderDetailForShoppingList(
                    cur_info[i].img, cur_info[i].name, cur_info[i].quantity, cur_info[i].price,
                    cur_info[i].price*cur_info[i].quantity, cur_info[i].p_id, cur_info[i].o_id
                )

                tmpOrderList2.add(newOrderP)
            }

            var intent = Intent(this.context, ShoppingListActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return orderList.size
    }
}

class HomeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var productsImg: ImageView = itemView!!.findViewById(R.id.stuff_img)
    var productsName: TextView = itemView!!.findViewById(R.id.txt_order)
    var orderPrice: TextView = itemView!!.findViewById(R.id.txt_price)
    var orderDate: TextView = itemView!!.findViewById(R.id.txt_orderDate)

}