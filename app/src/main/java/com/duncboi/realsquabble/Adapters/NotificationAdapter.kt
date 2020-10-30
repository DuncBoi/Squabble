package com.duncboi.realsquabble.Adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.duncboi.realsquabble.ModelClasses.Notifications
import com.duncboi.realsquabble.ModelClasses.Users
import com.duncboi.realsquabble.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class NotificationAdapter(context: Context, notificationList: List<Notifications>): RecyclerView.Adapter<NotificationAdapter.ViewHolder?>() {

    private val notificationList: List<Notifications>
    private val context:Context

    init{
        this.notificationList = notificationList
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.recycler_notification, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notificationList[position]

        if (!notification.getMessage()!!.contains("Anonymous")) {
            holder.profileImage!!.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("uid", notification.getSenderId())
                findNavController(holder.itemView).navigate(
                    R.id.action_defaultMessenger_to_otherProfile,
                    bundle
                )
            }
        }

        if (notification.getUri() != "") Picasso.get().load(notification.getUri()).into(holder.profileImage)
        holder.message!!.text = notification.getMessage()

        val timePosted = notification.getTimeOfPost()?.toLong()
        val currentTime = System.currentTimeMillis()
        if (timePosted != null){
            val millisElapsed = currentTime - timePosted
            val minutesElapsed = millisElapsed/60000
            val hoursElapsed = minutesElapsed/60
            val daysElapsed = hoursElapsed/24
            val weeksElapsed = daysElapsed/7
            val monthsElapsed = weeksElapsed/4
            val yearsElapsed = monthsElapsed/12
            if (yearsElapsed > 0) holder.timeOfPost!!.text = "• ${yearsElapsed} years ago •"
            else if (monthsElapsed > 0) holder.timeOfPost!!.text = "• ${monthsElapsed} months ago •"
            else if (weeksElapsed > 0) holder.timeOfPost!!.text = "• ${weeksElapsed}w ago •"
            else if (daysElapsed > 0) holder.timeOfPost!!.text = "• ${daysElapsed}d ago •"
            else if (hoursElapsed > 0) holder.timeOfPost!!.text = "• ${hoursElapsed}h ago •"
            else if (minutesElapsed > 0) holder.timeOfPost!!.text = "• ${minutesElapsed}m ago •"
            else holder.timeOfPost!!.text = "• Just now •"
        }

        holder.itemView.setOnClickListener {
            if (notification.getMessage()!!.contains("a message")){
                val bundle = Bundle()
                bundle.putString("uid", "${notification.getSenderId()}")
                findNavController(holder.itemView).navigate(R.id.action_defaultMessenger_to_DM, bundle)
            }
            else if (notification.getMessage()!!.contains("upvoted your")){
                val bundle = Bundle()
                bundle.putString("postId", "${notification.getId()}")
                bundle.putString("from", "Group Posts")
                bundle.putString("groupId", "${notification.getGroupId()}")
                findNavController(holder.itemView).navigate(R.id.action_defaultMessenger_to_commentView, bundle)
            }
            else if (notification.getMessage()!!.contains("commented on") || notification.getMessage()!!.contains("liked your comment")){
                val bundle = Bundle()
                bundle.putString("postId", "${notification.getId()}")
                bundle.putString("commentId", "${notification.getCommentId()}")
                bundle.putString("from", "Group Posts")
                bundle.putString("groupId", "${notification.getGroupId()}")
                findNavController(holder.itemView).navigate(R.id.action_defaultMessenger_to_replyToComment, bundle)
            }
            else if (notification.getMessage()!!.contains("replied to")){
                val bundle = Bundle()
                bundle.putString("postId", "${notification.getId()}")
                bundle.putString("commentId", "${notification.getCommentId()}")
                bundle.putString("from", "Group Posts")
                bundle.putString("groupId", "${notification.getGroupId()}")
                bundle.putBoolean("scrollToComment", true)
                findNavController(holder.itemView).navigate(R.id.action_defaultMessenger_to_replyToComment, bundle)
            }
        }

    }


    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var profileImage: CircleImageView? = null
        var message: TextView? = null
        var timeOfPost: TextView? = null

        init {
            profileImage = itemView.findViewById(R.id.civ_recycler_notification_profile)
            message = itemView.findViewById(R.id.tv_recycler_notification_message)
            timeOfPost = itemView.findViewById(R.id.tv_recycler_notification_time_ago)

        }
    }


}