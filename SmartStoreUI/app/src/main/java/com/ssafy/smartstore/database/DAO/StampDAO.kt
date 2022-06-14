package com.ssafy.smartstore.database.DAO

import androidx.room.Dao
import androidx.room.Query
import com.ssafy.smartstore.database.CommentDTO
import com.ssafy.smartstore.database.StampDTO

@Dao
interface StampDAO {
    @Query("SELECT * FROM t_stamp WHERE userId = (:userId)")
    suspend fun getStampsByUserId(userId: String): List<StampDTO>

}