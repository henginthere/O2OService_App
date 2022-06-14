package com.ssafy.smartstore.database.DAO

import androidx.room.*
import com.ssafy.smartstore.database.OrderDTO
import com.ssafy.smartstore.database.OrderDetailDTO

@Dao
interface OrderDetailDAO {
    @Query("SELECT * FROM t_order_detail WHERE order_id = (:order_id)")
    suspend fun getOrderDetails(order_id:Int): List<OrderDetailDTO>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun orderDetailInsert(orderDetail: OrderDetailDTO)

    @Query("UPDATE t_order SET id=(:orderId) WHERE id=-1")
    suspend fun orderDetailUpdate(orderId: Int)

    @Query("SELECT MAX(id) FROM t_order")
    suspend fun getMaxOrderId():Int

}