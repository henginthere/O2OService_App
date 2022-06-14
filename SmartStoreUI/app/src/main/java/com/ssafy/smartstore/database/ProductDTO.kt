package com.ssafy.smartstore.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "t_product")
data class ProductDTO(var name:String, var type:String, var price:Int, var img:String) {
    @PrimaryKey(autoGenerate = true)
    var id = 0


    constructor(id:Int, name:String, type:String, price:Int, img:String):
            this(name, type, price, img){
        this.id = id
    }

//    var avg = 0.0
//    var user_Id = ""
//    var sells = 0
//    var rating = 0
//    var commentId=0
//    var comment=""
//    var userName=""
//    var commentCnt=0
//    constructor(img: String,avg: Double, user_Id:String, sells:Int = 0, price:Int, name:String,
//            rating:Int, commentId:Int, comment:String, type:String, userName:String, commentCnt:Int):
//            this(name, type,price,img){
//        this.img = img
//        this.avg = avg
//        this.user_Id = user_Id
//        this.sells = sells
//        this.price = price
//        this.name = name
//        this.rating = rating
//        this.commentId=commentId
//        this.userName = userName
//        this.commentCnt = commentCnt
//            }
}

data class ProductDTOForEmpty(var name:String, var type:String, var price:Int, var img:String,var commentCnt:Int) {
    @PrimaryKey(autoGenerate = true)
    var id = 0


    constructor(id: Int, name: String, type: String, price: Int, img: String,commentCnt:Int) :
            this(name, type, price, img,commentCnt) {
        this.id = id
    }
}