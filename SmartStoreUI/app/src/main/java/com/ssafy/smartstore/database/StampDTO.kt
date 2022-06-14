package com.ssafy.smartstore.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "t_stamp")
data class StampDTO(
    var userId:String,//유저아이디
    var orderId:Int,//주문 아이디
    var quantity:Int // 주문 개수
){
    @PrimaryKey(autoGenerate = true)
    var id = -1

    //id추가한 생성자
    constructor(id: Int, userId:String, orderId:Int, quantity: Int): this(userId,orderId,quantity){
        this.id = id
    }

}
