package com.ssafy.smartstore.Util

import java.text.SimpleDateFormat
import java.util.*

class Utils {

    //ex ) Utils.formatter().format(System.currentTimeMillis())

    //java의 static method와 동일
    companion object {
        fun formatter(): SimpleDateFormat{
            val formatter = SimpleDateFormat("yyyy-MM-dd")
            formatter.timeZone = TimeZone.getTimeZone("GMT+9")

            return formatter
        }
    }
}
