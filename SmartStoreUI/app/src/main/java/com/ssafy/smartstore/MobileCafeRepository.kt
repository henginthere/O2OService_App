package com.ssafy.smartstore

import android.content.Context
import androidx.room.*
import com.ssafy.smartstore.database.*
import com.ssafy.smartstore.database.DAO.MobileCafeDatabase

// GalleryRepository는 싱글톤 (앱이 실행 되는 동안 하나의 인스턴스만 생성)
private const val DATABASE_NAME = "mobilecafe-database"
class MobileCafeRepository private constructor(context: Context) {

    private val database: MobileCafeDatabase = Room.databaseBuilder(
        context.applicationContext,
        MobileCafeDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val productDAO = database.productDAO()
//    private val userDAO = database.userDAO()
    private val commentDAO = database.commentDAO()
    private val orderDetailDAO = database.orderDetailDAO()
    private val orderDAO = database.orderDAO()
//    private val stampDAO = database.stampDAO()

    //여기에 각자 쓴 메서드 넣기


    //comment
    suspend fun commentUpdate(commentDTO: CommentDTO) = commentDAO.commentUpdate(commentDTO)
    suspend fun commentDelete(commentDTO: CommentDTO) = commentDAO.commentDelete(commentDTO)
//    suspend fun commentDeleteById(cId: Int) = commentDAO.commentDeleteById(cId)

    //order
    suspend fun getOrders(userId:String) : List<OrderDTO> = orderDAO.getOrders(userId)

    //orderDetail
    suspend fun getOrderDetails(orderId:Int): List<OrderDetailDTO> = orderDetailDAO.getOrderDetails(orderId)

    //product
    suspend fun getProduct(id: Int): ProductDTO = productDAO.getProduct(id)
//    suspend fun getAllProduct():List<ProductDTO> = productDAO.getAllProduct()

    //suspend fun checkUser(id:String): Int = userDAO.checkUser(id)

    companion object {
        private var INSTANCE: MobileCafeRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = MobileCafeRepository(context)
            }
        }

        fun get(): MobileCafeRepository {
            return INSTANCE ?:
            throw IllegalStateException("MobileCafeRepository must be initialized")
        }
    }
}
