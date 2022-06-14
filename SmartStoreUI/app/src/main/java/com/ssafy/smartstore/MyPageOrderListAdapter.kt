package com.ssafy.smartstore

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.smartstore.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class MyPageOrderListAdapter(var context: Context, val resource: Int,  objects2:List<OrderDTO3>)
    : RecyclerView.Adapter<MyPageHolder>() {

//    private var repo = MobileCafeRepository.get()
    //사용하고자 하는 데이터
//    var orderList = objects
    var orderDetailList = objects2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPageHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(resource, parent, false)

        return MyPageHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyPageHolder, position: Int) {
        //한번에 orderId별로 다 가져와서 두개 이상이면 ~외 ~잔, 이름, 가격 , 날짜 출력

            var cur_info = orderDetailList[position]

            CoroutineScope(Dispatchers.Main).launch {

                var img = cur_info.img
                var resId= context.resources.getIdentifier(img.substring(0,img.length-4),"drawable", "com.ssafy.smartstore")
                holder.productsImg.setImageResource(resId)

                if(cur_info.quantity >= 2){
                    holder.productsName.text = "${cur_info.name} ${cur_info.quantity}잔"
                }else{
                    holder.productsName.text = "${cur_info.name}"
                }


                var sum = cur_info.unitprice * cur_info.quantity
                holder.orderPrice.text = "${sum}원"

                holder.orderDate.text = cur_info.order_time.substring(0,10)
            }


    }

    override fun getItemCount(): Int {
        return orderDetailList.size
    }
}

class MyPageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var productsImg: ImageView = itemView!!.findViewById(R.id.stuff_img)
    var productsName: TextView = itemView!!.findViewById(R.id.txt_order)
    var orderPrice: TextView = itemView!!.findViewById(R.id.txt_price)
    var orderDate: TextView = itemView!!.findViewById(R.id.txt_orderDate)

}