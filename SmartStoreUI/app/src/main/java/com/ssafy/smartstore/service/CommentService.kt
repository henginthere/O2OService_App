package com.ssafy.smartstore.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.room.Delete
import com.ssafy.smartstore.database.CommentDTO
import com.ssafy.smartstore.database.CommentDTOForUpdate
import com.ssafy.smartstore.database.CommentDTOForUserId
import com.ssafy.smartstore.database.ProductDTO
import retrofit2.Call
import retrofit2.http.*

interface CommentService {
//	private Integer id;
//	private String userId;
//	private Integer productId;
//	private Double rating;
//	private String comment;

    @POST("comment")
    fun insertComment(@Body commentDTO:CommentDTO): Call<Boolean>

    @PUT("comment")
    fun updateComment(@Body commentDTO: CommentDTOForUpdate): Call<Boolean>

    @DELETE("comment/{id}")
    fun deleteComment(@Path("id") id:Int): Call<Boolean>
}