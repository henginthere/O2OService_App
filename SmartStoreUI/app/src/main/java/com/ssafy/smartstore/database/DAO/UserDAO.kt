package com.ssafy.smartstore.database.DAO

import androidx.room.*
import com.ssafy.smartstore.database.UserDTO

@Dao
interface UserDAO {

    @Query("SELECT * FROM t_user WHERE id = (:id)")
    suspend fun getUser(id: String): UserDTO

//    @Query("SELECT COUNT(*) FROM t_user WHERE id = (:id)")
//    suspend fun checkUser(id:String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun userInsert(userDTO: UserDTO)

}