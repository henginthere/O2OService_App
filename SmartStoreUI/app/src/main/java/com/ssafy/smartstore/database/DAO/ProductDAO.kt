package com.ssafy.smartstore.database.DAO

import androidx.room.*
import com.ssafy.smartstore.database.ProductDTO

@Dao
interface ProductDAO {

    @Query("SELECT * FROM t_product WHERE id = (:id)")
    suspend fun getProduct(id: Int): ProductDTO


//    @Query("SELECT * FROM t_product")
//    suspend fun getAllProduct(): List<ProductDTO>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun productInsert(productDTO: ProductDTO)

    @Update
    suspend fun productUpdate( productDTO: ProductDTO)

    @Delete
    suspend fun productDelete(productDTO: ProductDTO)



}