package com.duncboi.realsquabble.Adapters

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.duncboi.realsquabble.political.Constants
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.ModelClasses.Chat
import com.duncboi.realsquabble.ModelClasses.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class GroupChatAdapter(
    context: Context,
    chatList: List<Chat>
) : RecyclerView.Adapter<GroupChatAdapter.ViewHolder?>() {
    private val context: Context?
    private val chatList: List<Chat>
    var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    init {
        this.chatList = chatList
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return if (position == 1) {
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.message_right, parent, false)
            ViewHolder(view)
        } else {
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.message_left, parent, false)
            ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val chat: Chat = chatList[position]

        if (Constants.isValidUrl(chat.getMessage()!!)) {
            holder.text_message!!.setPaintFlags(holder.text_message!!.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
            holder.text_message!!.setTextColor(Color.parseColor("#4885ed"))
            holder.itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("url", chat.getMessage()!!.trim())
                Navigation.findNavController(holder.itemView)
                    .navigate(R.id.action_topicHolder_to_web_view, bundle)
            }
        }
        else{
            holder.text_message!!.setPaintFlags(0)
            holder.text_message!!.setTextColor(Color.parseColor("#8a000000"))
        }

        //fix

        if (getItemViewType(position) == 0) {
            FirebaseDatabase.getInstance().reference.child("Users").child(chat.getSender()!!).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(Users::class.java)
                    if (user != null) {
                        if (user.getAnonymous() == "ON"){
                            holder.username!!.text = "Anonymous"
                        }
                        else{
                            if (chat.getUri() != "") Picasso.get().load(chat.getUri()).into(holder.profileImage)

                            if (chat.getSenderName() == ""){
                                holder.username!!.text = "${chat.getSenderUsername()}"
                            }
                            else{
                                holder.username!!.text = "${chat.getSenderName()}"
                            }
                        }
                    }
                }
            })
        }

        val timePosted = chat.getTimeOfMessage()?.toLong()
        val currentTime = System.currentTimeMillis()
        if (timePosted != null) {
            val millisElapsed = currentTime - timePosted
            val minutesElapsed = millisElapsed / 60000
            val hoursElapsed = minutesElapsed / 60
            val daysElapsed = hoursElapsed / 24
            val weeksElapsed = daysElapsed / 7
            val monthsElapsed = weeksElapsed / 4
            val yearsElapsed = monthsElapsed / 12
            if (yearsElapsed > 0) holder.text_seen!!.text =
                "(${yearsElapsed}y)"
            else if (monthsElapsed > 0) holder.text_seen!!.text =
                "(${monthsElapsed}mo)"
            else if (weeksElapsed > 0) holder.text_seen!!.text =
                "(${weeksElapsed}w)"
            else if (daysElapsed > 0) holder.text_seen!!.text =
                "(${daysElapsed}d)"
            else if (hoursElapsed > 0) holder.text_seen!!.text =
                "(${hoursElapsed}h)"
            else if (minutesElapsed > 0) holder.text_seen!!.text =
                "(${minutesElapsed}m)"
            else holder.text_seen!!.text = "(Just now)"
        }

        if (chat.getMessage().equals("sent you an image.") && !chat.getUrl().equals("")) {
            if (chat.getSender().equals(firebaseUser.uid)) {
                holder.text_message!!.visibility = View.GONE
                holder.right_image!!.visibility = View.VISIBLE
                holder.rightCard!!.visibility = View.VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.right_image)
            } else if (!chat.getSender().equals(firebaseUser.uid)) {
                holder.text_message!!.visibility = View.GONE
                holder.left_image!!.visibility = View.VISIBLE
                holder.leftCard!!.visibility = View.VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.left_image)
            }
        } else {
            holder.text_message!!.text = chat.getMessage()
        }

//is seen logic: waych coding cafe 54 mins episode 11
        if (holder.minutesAgo == null) {
            if (position == chatList.size - 1) {
                if (chat.getIsSeen()) {
                    holder.text_seen!!.text = "Seen"
                    if (chat.getMessage().equals("sent you an image.") && !chat.getUrl()
                            .equals("")
                    ) {
                        val lp: RelativeLayout.LayoutParams? =
                            holder.text_seen!!.layoutParams as RelativeLayout.LayoutParams?
                        lp!!.setMargins(0, 550, 10, 0)
                        holder.text_seen!!.layoutParams = lp
                    }
                } else {
                    holder.text_seen!!.text = "Delivered"
                    if (chat.getMessage().equals("sent you an image.") && !chat.getUrl()
                            .equals("")
                    ) {
                        val lp: RelativeLayout.LayoutParams? =
                            holder.text_seen!!.layoutParams as RelativeLayout.LayoutParams?
                        lp!!.setMargins(0, 550, 10, 0)
                        holder.text_seen!!.layoutParams = lp
                    }
                }
            } else {
                holder.text_seen!!.visibility = View.GONE
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView? = null
        var text_message: TextView? = null
        var left_image: ImageView? = null
        var right_image: ImageView? = null
        var text_seen: TextView? = null
        var minutesAgo: TextView? = null
        var rightCard: CardView? = null
        var leftCard: CardView? = null
        var username: TextView? = null

        init {
            profileImage = itemView.findViewById(R.id.civ_message_left_profile_picture)
            text_message = itemView.findViewById(R.id.tv_show_text_message)
            minutesAgo = itemView.findViewById(R.id.tv_message_left_minutes_ago)
            left_image = itemView.findViewById(R.id.iv_left_message_image)
            right_image = itemView.findViewById(R.id.iv_right_message_image)
            text_seen = itemView.findViewById(R.id.tv_message_right_seen)
            rightCard = itemView.findViewById(R.id.cv_right_card_view)
            leftCard = itemView.findViewById(R.id.cv_left_card_view)
            username = itemView.findViewById(R.id.tv_message_left_username)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatList[position].getSender().equals(firebaseUser.uid)) {
            1
        } else {
            0
        }
    }

}