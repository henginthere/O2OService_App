package com.ssafy.smartstore.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "t_order_detail")
data class OrderDetailDTO(var order_id: Int,var productId:Int, var quantity:Int): Serializable {

    @PrimaryKey(autoGenerate = true)
    var dId = 0

    constructor(dId: Int, order_id: Int, productId: Int, quantity: Int): this(order_id, productId, quantity){
        this.dId = dId
    }
}


data class OrderDetailDTO2(var o_id: Int, var p_id:Int, var quantity: Int):Serializable

data class OrderDetailForShoppingList(var img:String,var name: String, var quantity:Int,var price:Int,
                                      var totalprice: Int,var productId: Int, var orderId: Int)