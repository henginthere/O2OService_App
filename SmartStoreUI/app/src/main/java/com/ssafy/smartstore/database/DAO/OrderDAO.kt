package com.ssafy.smartstore.database.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ssafy.smartstore.database.CommentDTO
import com.ssafy.smartstore.database.OrderDTO

@Dao
interface OrderDAO {
    @Query("SELECT * FROM t_order WHERE userId = (:userId) order by id desc")
    suspend fun getOrders(userId: String): List<OrderDTO>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun orderInsert(order: OrderDTO)
}