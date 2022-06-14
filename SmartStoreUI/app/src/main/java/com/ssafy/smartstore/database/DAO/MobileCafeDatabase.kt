package com.ssafy.smartstore.database.DAO

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ssafy.smartstore.database.*

@Database(entities = [CommentDTO::class, OrderDTO::class, OrderDetailDTO::class, ProductDTO::class, StampDTO::class, UserDTO::class], version = 1)
abstract class MobileCafeDatabase : RoomDatabase(){
    abstract fun commentDAO(): CommentDAO
    abstract fun orderDetailDAO(): OrderDetailDAO
    abstract fun orderDAO(): OrderDAO
    abstract fun productDAO(): ProductDAO
    abstract fun stampDAO(): StampDAO
    abstract fun userDAO(): UserDAO
}