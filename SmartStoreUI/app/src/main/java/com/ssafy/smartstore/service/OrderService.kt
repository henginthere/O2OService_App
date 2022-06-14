package com.ssafy.smartstore.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.ssafy.smartstore.database.*
import retrofit2.Call
import retrofit2.http.*

interface OrderService {

    @POST("order")
    fun insertOrder(@Body orderDTO: OrderDTO2): Call<Int>

    //주문상세내역 목록형태로 반환
    @GET("order/{orderId}")
    fun selectOrderDetail(@Path("orderId") orderId:Int): Call<List<OrderDetailDTO2>>

    //주문상세내역 목록형태로 반환
    @GET("order/{orderId}")
    fun selectOrderDTO3(@Path("orderId") orderId:Int): Call<List<OrderDTO3>>

    //최근 1개월주문정보 반환 -> 일단 보류
    @GET("order/byUser")
    fun selectRecentOrder(@Query("id") id:String): Call<List<OrderDTO4>>
}