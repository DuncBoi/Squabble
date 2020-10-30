package com.duncboi.realsquabble.notifications

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.duncboi.realsquabble.ModelClasses.Notifications
import com.duncboi.realsquabble.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val groupId = remoteMessage.data["groupId"]

        val user = remoteMessage.data["user"]

        val body = remoteMessage.data["body"]

        val id = remoteMessage.data["id"]

        var sent = remoteMessage.data["sent"]

        var commentId = remoteMessage.data["commentId"]

        val currentUser = FirebaseAuth.getInstance().currentUser

        val timeOfNotification = System.currentTimeMillis().toString()

        if (currentUser != null && currentUser.uid != user) {
            val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(user!!)
                userRef.addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val uri = snapshot.child("uri").getValue(String::class.java)
                        val anonymous = snapshot.child("anonymous").getValue(String::class.java)
                        val notificationHash = HashMap<String, Any?>()
                        if (id != "" && id != null) {
                            if (anonymous == "ON"){
                                notificationHash["uri"] = ""
                                notificationHash["message"] = body!!
                                notificationHash["id"] = id
                                notificationHash["timeOfPost"] = timeOfNotification
                                notificationHash["senderId"] = user
                                notificationHash["groupId"] = groupId
                                notificationHash["commentId"] = commentId
                            }
                            else {
                                notificationHash["uri"] = uri
                                notificationHash["message"] = body!!
                                notificationHash["id"] = id
                                notificationHash["timeOfPost"] = timeOfNotification
                                notificationHash["senderId"] = user
                                notificationHash["groupId"] = groupId
                                notificationHash["commentId"] = commentId
                            }

                                    val notificationRef =
                                        FirebaseDatabase.getInstance().reference.child("Notifications")
                                            .child(
                                                currentUser.uid
                                            )
                                    val numberUnreadRef =
                                        FirebaseDatabase.getInstance().reference.child("Unread Notifications").child(currentUser.uid).child("numberUnread")

                                    notificationRef.child(id).setValue(notificationHash)

                                    numberUnreadRef.addListenerForSingleValueEvent(object :
                                        ValueEventListener {
                                        override fun onCancelled(error: DatabaseError) {}
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val number = snapshot.getValue(Long::class.java)
                                            if (number != null) {
                                                numberUnreadRef.setValue(number + 1)
                                            } else {
                                                numberUnreadRef.setValue(1)
                                            }
                                        }

                                    })
                        }
                    }

                })
        }


        val sharedPref = getSharedPreferences("PREFS", Context.MODE_PRIVATE)

        val currentOnlineUser = sharedPref.getString("currentUser", "none")

        if (currentUser != null && sent == currentUser.uid){

            if (currentOnlineUser != user){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    sendOreoNotification(remoteMessage)
                }
                else{
                    sendNotification(remoteMessage)
                }
            }

        }
    }

    private fun sendNotification(remoteMessage: RemoteMessage){
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        val notification = remoteMessage.notification
        val j = user!!.replace("[\\D]".toRegex(), "").toInt()

        val intent = Intent(this, ProfileActivity::class.java)
        val bundle = Bundle()
        bundle.putString("userid", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT)

       val builder: NotificationCompat.Builder = NotificationCompat.Builder(this)
           .setSmallIcon(icon!!.toInt())
           .setContentText(title)
           .setContentText(body)
           .setAutoCancel(true)
           .setContentIntent(pendingIntent)

        val noti = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        var i = 0
        if (j>0){
            i = j
        }

        noti.notify(i, builder.build())
    }

    private fun sendOreoNotification(remoteMessage: RemoteMessage){
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        val notification = remoteMessage.notification
        val j = user!!.replace("[\\D]".toRegex(), "").toInt()

        val intent = Intent(this, ProfileActivity::class.java)
        val bundle = Bundle()
        bundle.putString("userid", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT)


        val oreoNotification = OreoNotification(this)

        val builder: Notification.Builder = oreoNotification.getOreoNotification(
            title,
            body,
            pendingIntent,
            icon
        )


        var i = 0
        if (j>0){
            i = j
        }

        oreoNotification.getManager!!.notify(i, builder.build())
    }
}