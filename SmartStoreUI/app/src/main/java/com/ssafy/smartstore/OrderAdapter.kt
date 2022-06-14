package com.ssafy.smartstore


import android.R.drawable
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.smartstore.database.ProductDTO
import org.w3c.dom.Text


private const val TAG="OrderAdapter_싸피"
//주문할 목록들 관련 어댑터
class OrderAdapter(var context: Context, val resource: Int, list:List<ProductDTO>) : RecyclerView.Adapter<OrderHolder>() {
    //사용하고자 하는 데이터
    var listData = list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.order_img,parent,false)
        return OrderHolder(view)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onBindViewHolder(holder: OrderHolder, position: Int) {
        try{
            var img = listData[position].img
            var resId= context.resources.getIdentifier(img.substring(0,img.length-4),"drawable", "com.ssafy.smartstore")
            holder.productImg.setImageResource(resId)
            holder.productName.text = listData[position].name

            holder.productImg.setOnClickListener(View.OnClickListener {
                var intent = Intent(this.context, MenuDetailActivity::class.java)
                intent.putExtra("productId",listData[position].id.toString())
                println("????? : ${listData[position].id}")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent)
            })
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}

class OrderHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
    var productImg: ImageView = itemView!!.findViewById(R.id.stuff_img)
    var productName: TextView = itemView!!.findViewById(R.id.tv_name)
}
