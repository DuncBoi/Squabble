package com.duncboi.realsquabble.Adapters

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.ModelClasses.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.math.acos

class UserAdapter(context: Context, users: List<Users>, type: String): RecyclerView.Adapter<UserAdapter.ViewHolder?>() {

    private val context: Context
    private val users: List<Users>
    private val type: String
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    init {
        this.users = users
        this.context = context
        this.type = type
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.recycler_view_user, viewGroup, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val user: Users = users[i]

        if (user.getUnread() == false){
            holder.status.setImageResource(R.drawable.unread_icon)
        }
        else{
            if (user.getStatus() == "ONLINE"){ holder.status.setImageResource(R.drawable.online)}
        }

        if (user.getUid() == currentUser){
            if (user.getUri() != "") Picasso.get().load(user.getUri()).into(holder.profileImageView)
        }
        else {
            if (user.getAnonymous() != "ON") {
                if (user.getUri() != "") Picasso.get().load(user.getUri())
                    .into(holder.profileImageView)
                holder.itemView.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("uid", "${user.getUid()}")
                        if (type == "dm") {
                            findNavController(holder.itemView).navigate(
                                R.id.action_defaultMessenger_to_DM,
                                bundle
                            )
                        } else if (type == "group") {
                            findNavController(holder.itemView).navigate(
                                R.id.action_groupDescription_to_otherProfile,
                                bundle
                            )
                        } else if (type == "search") {
                            findNavController(holder.itemView).navigate(
                                R.id.action_directMessanger_to_otherProfile,
                                bundle
                            )
                        }
                }
            } else {
                val bundle = Bundle()
                bundle.putString("uid", "${user.getUid()}")
                if (type == "dm") {
                    holder.itemView.setOnClickListener {
                        findNavController(holder.itemView).navigate(
                            R.id.action_defaultMessenger_to_DM,
                            bundle
                        )
                    }
                } else {
                    holder.itemView.setOnClickListener {
                        Toast.makeText(context, "Anonymous", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val chatListRef = FirebaseDatabase.getInstance().reference.child("ChatList").child(FirebaseAuth.getInstance().currentUser!!.uid).child(user.getUid()!!)
        chatListRef.onDisconnect().cancel()
            chatListRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                val message = snapshot.child("last message").getValue(String::class.java)
                val timeOfMessage = snapshot.child("timeOfMessage").getValue(String::class.java)
                val timePosted = timeOfMessage?.toLong()
                val currentTime = System.currentTimeMillis()
                if (timePosted != null){
                    val millisElapsed = currentTime - timePosted
                    val minutesElapsed = millisElapsed/60000
                    val hoursElapsed = minutesElapsed/60
                    val daysElapsed = hoursElapsed/24
                    val weeksElapsed = daysElapsed/7
                    val monthsElapsed = weeksElapsed/4
                    val yearsElapsed = monthsElapsed/12
                    if (yearsElapsed > 0) holder.time.text = "• ${yearsElapsed} years ago •"
                    else if (monthsElapsed > 0) holder.time.text = "• ${monthsElapsed} months ago •"
                    else if (weeksElapsed > 0) holder.time.text = "• ${weeksElapsed}w ago •"
                    else if (daysElapsed > 0) holder.time.text = "• ${daysElapsed}d ago •"
                    else if (hoursElapsed > 0) holder.time.text = "• ${hoursElapsed}h ago •"
                    else if (minutesElapsed > 0) holder.time.text = "• ${minutesElapsed}m ago •"
                    else holder.time.text = "• Just now •"
                }
                if (message != null) {
                    holder.usernameTxt.text = "-${message}"
                }
                else{
                    if (user.getStatus() == "ONLINE"){
                        holder.usernameTxt.text = "Online"
                    }
                    else{
                        holder.usernameTxt.text = "Offline"
                    }
                }
            }
        })
        if (user.getUid() == currentUser){
            holder.nameTxt.text = "You"
            holder.nameTxt.setTextColor(Color.parseColor("#4885ed"))
        }
        else {
            if (user.getAnonymous() == "OFF") {
                if (user.getName() == "") holder.nameTxt.text = "${user.getUsername()}"
                else holder.nameTxt.text = user.getName()
            } else {
                holder.nameTxt.text = "Anonymous"
            }
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var usernameTxt: TextView
        var profileImageView: CircleImageView
        var nameTxt: TextView
        var time: TextView
        var status: ImageView
        var checkBox: CheckBox

        init {
            usernameTxt = itemView.findViewById(R.id.tv_recycler_username)
            profileImageView = itemView.findViewById(R.id.cv_recycler_user_profile_pic)
            nameTxt = itemView.findViewById(R.id.tv_recycler_name)
            time = itemView.findViewById(R.id.tv_recycler_user_time)
            status = itemView.findViewById(R.id.iv_recycler_status)
            checkBox = itemView.findViewById(R.id.cb_user_recycler)
        }
    }

}


