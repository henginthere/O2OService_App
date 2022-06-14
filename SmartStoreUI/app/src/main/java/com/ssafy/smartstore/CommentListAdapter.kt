package com.ssafy.smartstore

import android.content.Context
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.smartstore.LoginActivity.Companion.userId
import com.ssafy.smartstore.database.CommentDTO
import com.ssafy.smartstore.database.CommentDTOForUserId

class CommentListAdapter(var context: Context, val resource: Int, objects:List<CommentDTOForUserId>)
    : RecyclerView.Adapter<commentHolder>() {
    //사용하고자 하는 데이터
    var commentList = objects

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): commentHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(resource, parent, false)
        return commentHolder(itemView)
    }

    override fun onBindViewHolder(holder: commentHolder, position: Int) {
        //자신의 상품평만 수정,삭제 버튼 보이기
        if(userId != commentList[position].user_id){
            println("userid 달라? : ${userId} ${commentList[position].user_id}")
            holder.btn_review_edit.visibility = View.INVISIBLE
            holder.btn_review_remove.visibility = View.INVISIBLE
        }
        //상품평 바인딩
        holder.comment.text = commentList[position].comment

        holder.bindOnItemClickListener(onItemClickListener)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    //클릭 인터페이스 정의 activity에서 만들기
    interface ItemClickListener{
        fun onClick(view: View, position: Int)
    }
    lateinit var onItemClickListener: ItemClickListener

}

class commentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    //코멘트
    var comment: TextView = itemView!!.findViewById(R.id.tv_comment)

    var btn_review_edit: ImageButton = itemView!!.findViewById(R.id.btn_review_edit)
    var btn_review_remove: ImageButton = itemView!!.findViewById(R.id.btn_review_remove)

    fun bindOnItemClickListener(onItemClickListener: CommentListAdapter.ItemClickListener) {
        itemView.setOnClickListener {
            onItemClickListener.onClick(it, adapterPosition)
        }
    }
}