package com.ssafy.smartstore

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

private const val TAG: String = "hi~"
//메인화면 알림창 어댑터
class NotificationAdapter(var context: Context, val resource: Int, objects:MutableList<String>) : RecyclerView.Adapter<ViewHolder>() {
    //사용하고자 하는 데이터
    var listData = objects

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(resource, parent, false)
        Log.d(TAG, "onMessageReceived: ${listData.size} ")
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.content.text = listData[position]

//        holder.bindOnItemClickListener(onItemClickListener)
    }

    override fun getItemCount(): Int {
        return listData.size
    }
}
//
class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var content: TextView = itemView!!.findViewById(R.id.tv_notiContent)
}