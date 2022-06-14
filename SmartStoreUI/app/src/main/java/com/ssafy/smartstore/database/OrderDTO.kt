package com.ssafy.smartstore.database

import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "t_order")
data class OrderDTO(var userId:String="", var orderTable:String="",
                    @ColumnInfo(defaultValue = "2021-05-10") var orderTime: String,
                    @ColumnInfo(defaultValue = "N") var completed: String){
    @PrimaryKey(autoGenerate = true)
    var id = 0

//    var detail= ""
//    var stamps = 0
//
//    constructor(orderId: Int, userId: String, orderTable: String, orderTime: String, completed: String, detail: String, stamps: Int):
//            this(userId, orderTable, orderTime, completed){
//                this.orderId = orderId
//                this.detail = detail
//                this.stamps = stamps
//            }
    //	private Integer id;
    //	private String userId;
    //	private String orderTable;
    //	private Date orderTime;
    //	private Character completed;
    //	private List<OrderDetail> details;
    //	private Stamp stamp;
    constructor(id:Int, userId: String,orderTable: String,orderTime: String,
                completed: String):this(userId,orderTable,orderTime,completed){
                        this.id = id
    }
}

data class OrderDTO2(var userId:String="", var orderTable:String="",
                     var orderTime: String,
                     var completed: String,
                     var details:List<OrderDetailDTO>,
                     var stamp:StampDTO)

data class OrderDTO3(var img:String, var quantity:Int, var totalprice:Int, var o_id:Int,var name:String, var stamp:Int,
    var order_time:String, var completed: String, var type:String, var unitprice:Int, var p_id:Int, var order_table: String)

data class OrderDTO4(var img:String, var quantity:Int, var user_id: String, var price:Int, var o_id:Int,
    var name:String, var order_time:String, var completed: String, var type:String, var p_id:Int)