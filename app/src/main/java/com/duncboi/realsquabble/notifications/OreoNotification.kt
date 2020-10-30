package com.duncboi.realsquabble.notifications

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.os.Build

class OreoNotification(base: Context?): ContextWrapper(base) {

    private var notificationManager: NotificationManager? = null

    companion object{
        private const val CHANNEL_ID = "com.duncboi.realsquabble"
        private const val CHANNEL_NAME = "Squabble"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel()
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel(){
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)

        channel.enableLights(false)
        channel.enableVibration(false)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        getManager!!.createNotificationChannel(channel)
    }

    val getManager: NotificationManager? get(){
        if (notificationManager == null){
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        }
        return notificationManager
    }

    @TargetApi(Build.VERSION_CODES.O)
    fun getOreoNotification(title: String?, body: String?, pendingIntent:PendingIntent?, icon: String?): Notification.Builder {
        return Notification.Builder(applicationContext, CHANNEL_ID).setContentIntent(pendingIntent)
            .setContentText(title).setContentText(body).setSmallIcon(icon!!.toInt()).setAutoCancel(true)
    }

}