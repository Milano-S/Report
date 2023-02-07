package com.example.feedem.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import com.example.feedem.activity.MainActivity
import com.example.feedem.api.FCMRegistrationInterface
import com.example.feedem.sharedPref.SharedPreference
import com.example.feedem.url.Urls
import com.example.freedem.R
import com.exclr8.n1reportmanagement.model.FCMResponse
import com.example.feedem.model.PushNotificationRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "MyFirebaseMsgService"
private const val CHANNEL_ID = "my_channel"

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        lateinit var notificationChannel: NotificationChannel
        lateinit var builder: Notification.Builder
        val channelId = "i.apps.notifications"
        val description = "Test notification"

        val url = remoteMessage.data["url"].toString()
        Log.i(TAG, url)
        SharedPreference(applicationContext).save("url", url)

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("url", url)

        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel =
                NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this, channelId)
                .setContentTitle(remoteMessage.data["reportName"])
                .setContentText(url)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
        } else {
            builder = Notification.Builder(this)
                .setContentTitle(remoteMessage.data["reportName"])
                .setContentText(url)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
        }
        notificationManager.notify(1234, builder.build())
    }

    fun sendRegistrationToServer(
        pushNotificationRequest: PushNotificationRequest,
        userTokenKey: String,
        context: Context
    ) {
        val request = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(Urls(context).baseUrl)
            .build()
            .create(FCMRegistrationInterface::class.java)

        val retrofitData = request.registerToken(
            pushNotificationRequest, userTokenKey
        )
        retrofitData.enqueue(object : Callback<FCMResponse> {
            override fun onResponse(call: Call<FCMResponse>, response: Response<FCMResponse>) {
                Log.d(TAG, response.body().toString())
            }

            override fun onFailure(call: Call<FCMResponse>, t: Throwable) {
                Log.d(TAG, t.message.toString())
            }
        })
    }
}