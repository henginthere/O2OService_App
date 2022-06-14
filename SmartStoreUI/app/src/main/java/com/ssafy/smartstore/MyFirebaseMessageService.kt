package com.ssafy.smartstore

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ssafy.smartstore.LoginActivity.Companion.notiList

private const val TAG = "MyFCMService"
class MyFirebaseMessageService : FirebaseMessagingService() {
    //단순 service아닌 파이어베이스메세징서비스 상속함
    //온바인드 구현 필요 없음

    //새로 갱신된 등록 토큰 전달받을때 호출되는 콜백함수
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d(TAG, "onNewToken: $token")
    }

    //앱이 포그라운드 상태에서 푸시 메시지 전달받기 위해 여러 콜백함수 작성
    //RemoteMessage객체는 받은 메시지 객체
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "onMessageReceived: onM")
        //notification의 title과 body받아옴

        //홈에서 알림판 업데이트 되게 해야함.


        message.notification.let{ message ->
            val messageTitle = message!!.title
            val messageContent = message!!.body
            Log.d(TAG, "onMessageReceived: ${messageTitle} ./ ${messageContent}")
            notiList.add(messageContent.toString())

            val mainIntent = Intent(this, LoginActivity::class.java).apply{
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val mainPendingIntent = PendingIntent.getActivity(this, 0,mainIntent,0)

            //채널 아이디 지정 하면 해당 채널에 노티 뜸
            val builder = NotificationCompat.Builder(this, "ssafy_id")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(messageTitle)
                .setContentText(messageContent)
                .setAutoCancel(true)
                .setContentIntent(mainPendingIntent)

            NotificationManagerCompat.from(this).apply {
                notify(101,builder.build())
            }
        }
    }
}
