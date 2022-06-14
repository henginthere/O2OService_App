package com.ssafy.smartstore.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.ssafy.smartstore.database.CommentDTO
import com.ssafy.smartstore.database.CommentDTOForUserId
import com.ssafy.smartstore.database.ProductDTO
import com.ssafy.smartstore.database.ProductDTOForEmpty
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductService {

    //메뉴 정보 모두 반환
    @GET("product")
    fun selectAllProduct(): Call<List<ProductDTO>>

    //메뉴 정보 하나 반환
    @GET("product/{productId}")
    fun selectProduct(@Path("productId") productId:Int): Call<List<ProductDTO>>

    //메뉴 정보 하나 반환
    @GET("product/{productId}")
    fun selectProductForEmpty(@Path("productId") productId:Int): Call<List<ProductDTOForEmpty>>

    //메뉴 정보 하나 반환
    @GET("product/{productId}")
    fun selectComment(@Path("productId") productId:Int): Call<List<CommentDTO>>


    @GET("product/{productId}")
    fun selectComment2(@Path("productId") productId:Int): Call<List<CommentDTOForUserId>>

}