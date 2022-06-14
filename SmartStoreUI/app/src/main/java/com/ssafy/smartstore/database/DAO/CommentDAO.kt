package com.ssafy.smartstore.database.DAO

import androidx.room.*
import com.ssafy.smartstore.database.CommentDTO

@Dao
interface CommentDAO {

    @Query("SELECT * FROM t_comment WHERE productId = (:productId)")
    suspend fun getComments(productId: Int): List<CommentDTO>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun commentInsert(comment: CommentDTO)

    @Update
    suspend fun commentUpdate(comment: CommentDTO)

    @Delete
    suspend fun commentDelete(comment: CommentDTO)

}