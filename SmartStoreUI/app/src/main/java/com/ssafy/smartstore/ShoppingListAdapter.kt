package com.ssafy.smartstore

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
//import com.ssafy.smartstore.LoginActivity.Companion.tmpOrderList
import com.ssafy.smartstore.LoginActivity.Companion.tmpOrderList2
import com.ssafy.smartstore.database.OrderDetailDTO
import com.ssafy.smartstore.database.OrderDetailForShoppingList
import com.ssafy.smartstore.database.ProductDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.w3c.dom.Text

private const val TAG="ShoppingListAdapter_싸피"
class ShoppingListAdapter(var context: Context, val resource: Int, objects:MutableList<OrderDetailForShoppingList>)
    : RecyclerView.Adapter<ShoppingListHolder>() {
    //사용하고자 하는 데이터

    var detailListData = objects


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.shopping_list_item,parent,false)
        return ShoppingListHolder(view)
    }

    override fun getItemCount(): Int {
        return tmpOrderList2.size
    }

    override fun onBindViewHolder(holder: ShoppingListHolder, position: Int) {
        try{

            var img = tmpOrderList2[position].img
            var resId= context.resources.getIdentifier(img.substring(0,img.length-4),"drawable", "com.ssafy.smartstore")
            holder.productImg.setImageResource(resId)

            holder.productName.text = tmpOrderList2[position].name
            holder.productPrice.text = tmpOrderList2[position].price.toString()+"원"
            holder.productPriceTotal.text = detailListData[position].totalprice.toString()+"원"
            holder.productQuantity.text = detailListData[position].quantity.toString()
            CoroutineScope(Dispatchers.Main).launch {
                CoroutineScope(Dispatchers.Main).async {
                    holder.btnOrderRemove.setOnClickListener() {
                        Log.d(TAG, "onBindViewHolder: 지우기 버튼 누름")
                        Log.d(TAG, "지우기 전 목록: ${tmpOrderList2}")

                        tmpOrderList2.removeAt(position)
                        notifyItemRemoved(position)
                        Log.d(TAG, "지운 뒤 목록: : ${tmpOrderList2}")

                    }
                }.await()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}

class ShoppingListHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
    var productImg: ImageView = itemView!!.findViewById(R.id.order_coffee_img)
    var productName: TextView = itemView!!.findViewById(R.id.order_coffee_name)
    var productPrice: TextView = itemView!!.findViewById(R.id.order_coffee_price)
    var productPriceTotal: TextView = itemView!!.findViewById(R.id.order_coffee_price_total)
    var productQuantity: TextView = itemView!!.findViewById(R.id.order_coffee_quantity)
    var btnOrderRemove: Button = itemView!!.findViewById(R.id.btn_order_remove)
}
