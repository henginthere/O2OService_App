package com.ssafy.smartstore.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "t_comment")
data class CommentDTO(var userId:String, var productId:Int, var rating:Int,  var comment:String){
    @PrimaryKey(autoGenerate = true)
    var id = 0

    constructor(id:Int, userId:String, productId:Int, rating:Int, comment:String):
            this(userId, productId, rating, comment){
                this.id = id
            }
}

data class CommentDTOForUserId(var user_id:String, var product_id: Int, var rating: Int, var comment: String,var commentId:Int){
    var id = 0

    constructor(id:Int, user_id:String, product_id:Int, rating:Int, comment:String,commentId: Int):
            this(user_id, product_id, rating, comment,commentId){
        this.id = id
    }
}

data class CommentDTOForUpdate(var id:Int,var userId: String,var productId: Int, var rating: Int, var comment: String){

}