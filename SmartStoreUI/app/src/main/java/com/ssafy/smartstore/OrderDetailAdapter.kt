package com.ssafy.smartstore

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.smartstore.LoginActivity.Companion.userName
import com.ssafy.smartstore.database.OrderDetailDTO
import com.ssafy.smartstore.database.OrderDetailDTO2
import com.ssafy.smartstore.database.ProductDTO

//주문할 목록들 관련 어댑터
class OrderDetailAdapter(var context: Context, val resource: Int, objects:List<OrderDetailDTO2>, products:List<ProductDTO>) : RecyclerView.Adapter<OrderDetailHolder>() {
    //사용하고자 하는 데이터
    var listData = products
    var detailListData = objects

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.shopping_list_item,parent,false)
        return OrderDetailHolder(view)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onBindViewHolder(holder: OrderDetailHolder, position: Int) {
        try{
            var img = listData[position].img
            var resId= context.resources.getIdentifier(img.substring(0,img.length-4),"drawable", "com.ssafy.smartstore")
            holder.productImg.setImageResource(resId)
            holder.productName.text = listData[position].name
            holder.productPrice.text = listData[position].price.toString()+"원"
            holder.productPriceTotal.text = (detailListData[position].quantity * listData[position].price).toString()+"원"
            holder.productQuantity.text = detailListData[position].quantity.toString()
            holder.btnRemove.isVisible=false


        }catch (e:Exception){
            e.printStackTrace()
        }
    }

}

class OrderDetailHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
    var productImg: ImageView = itemView!!.findViewById(R.id.order_coffee_img)
    var productName: TextView = itemView!!.findViewById(R.id.order_coffee_name)
    var productPrice: TextView = itemView!!.findViewById(R.id.order_coffee_price)
    var productPriceTotal: TextView = itemView!!.findViewById(R.id.order_coffee_price_total)
    var productQuantity: TextView = itemView!!.findViewById(R.id.order_coffee_quantity)
    var btnRemove: Button = itemView!!.findViewById(R.id.btn_order_remove)

}

