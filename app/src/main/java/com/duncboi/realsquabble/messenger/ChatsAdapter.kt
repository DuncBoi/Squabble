package com.duncboi.realsquabble.messenger

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.duncboi.realsquabble.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_d_m.*

class ChatsAdapter(
    context: Context,
    chatList: List<Chat>,
    receiverUser: Users
) : RecyclerView.Adapter<ChatsAdapter.ViewHolder?>()
{
    private val context: Context?
    private val chatList: List<Chat>
    private val receiverUser: Users?
    var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    init {
        this.chatList = chatList
        this.context = context
        this.receiverUser = receiverUser
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return if (position == 1){
            val view: View = LayoutInflater.from(context).inflate(R.layout.message_right, parent, false)
            ViewHolder(view)
        }
        else{
            val view: View = LayoutInflater.from(context).inflate(R.layout.message_left, parent, false)
            ViewHolder(view)
        }
    }

    override fun getItemCount(): Int    {
        return chatList.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat: Chat = chatList[position]

        //fix
        if (holder.profileImage != null && receiverUser!!.getUri() != "") {
            Picasso.get().load(receiverUser.getUri()).into(holder.profileImage)
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
        }
        else{
            holder.text_message!!.text = chat.getMessage()
        }
//is seen logic: waych coding cafe 54 mins episode 11
        if (holder.minutesAgo == null){
        if (position == chatList.size -1){
            if (chat.getIsSeen()){
                holder.text_seen!!.text = "Seen"
                if (chat.getMessage().equals("sent you an image.") && !chat.getUrl().equals("")){
                    val lp: RelativeLayout.LayoutParams? = holder.text_seen!!.layoutParams as RelativeLayout.LayoutParams?
                    lp!!.setMargins(0, 550, 10, 0)
                    holder.text_seen!!.layoutParams = lp
                }
            }
            else{
                holder.text_seen!!.text = "Delivered"
                if (chat.getMessage().equals("sent you an image.") && !chat.getUrl().equals("")){
                    val lp: RelativeLayout.LayoutParams? = holder.text_seen!!.layoutParams as RelativeLayout.LayoutParams?
                    lp!!.setMargins(0, 550, 10, 0)
                    holder.text_seen!!.layoutParams = lp
                }
            }
        }
        else{
            holder.text_seen!!.visibility = View.GONE
        }
    }
        }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var profileImage: CircleImageView? = null
        var text_message: TextView? = null
        var left_image: ImageView? = null
        var right_image: ImageView? = null
        var text_seen: TextView? = null
        var minutesAgo: TextView? = null
        var rightCard: CardView? = null
        var leftCard: CardView? = null

        init {
            profileImage = itemView.findViewById(R.id.civ_message_left_profile_picture)
            text_message = itemView.findViewById(R.id.tv_show_text_message)
            minutesAgo = itemView.findViewById(R.id.tv_message_left_minutes_ago)
            left_image = itemView.findViewById(R.id.iv_left_message_image)
            right_image = itemView.findViewById(R.id.iv_right_message_image)
            text_seen = itemView.findViewById(R.id.tv_message_right_seen)
            rightCard = itemView.findViewById(R.id.cv_right_card_view)
            leftCard = itemView.findViewById(R.id.cv_left_card_view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return  if (chatList[position].getSender().equals(firebaseUser!!.uid)){
            1
        }
        else{
            0
        }
    }
}