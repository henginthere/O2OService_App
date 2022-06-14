package com.ssafy.smartstore.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.lang.reflect.Constructor

@Entity(tableName = "t_user")
data class UserDTO(
    @PrimaryKey
    var id: String,
    var name:String, // 유저 이름
    var pass:String, // 유저 비밀번호
    var stamps:Int,// 유저 스탬프 수
)
