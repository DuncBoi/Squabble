package com.duncboi.realsquabble.Adapters

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.Target
import com.duncboi.realsquabble.political.Constants
import com.duncboi.realsquabble.ModelClasses.Chat
import com.duncboi.realsquabble.ModelClasses.Users
import com.duncboi.realsquabble.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_view_image.*

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
            val view: View = LayoutInflater.from(context).inflate(
                R.layout.message_right,
                parent,
                false
            )
            ViewHolder(view)
        }
        else{
            val view: View = LayoutInflater.from(context).inflate(
                R.layout.message_left,
                parent,
                false
            )
            ViewHolder(view)
        }
    }

    override fun getItemCount(): Int    {
        return chatList.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val chat: Chat = chatList[position]

        if (getItemViewType(position) == 0) {
            if (receiverUser!!.getAnonymous() != "ON") {
                if (receiverUser!!.getUri() != "") {
                    Picasso.get().load(receiverUser.getUri()).into(holder.profileImage)
                    holder.username!!.text = "${receiverUser.getUsername()}"
                }
            }
            else{
                holder.username!!.text = "Anonymous"
            }
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


        if (Constants.isValidUrl(chat.getMessage()!!)) {
            holder.text_message!!.setPaintFlags(holder.text_message!!.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
            holder.text_message!!.setTextColor(Color.parseColor("#4885ed"))
            holder.itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("url", chat.getMessage()!!.trim())
                findNavController(holder.itemView).navigate(R.id.action_DM_to_web_view, bundle)
            }
        }
        else{
            holder.text_message!!.setPaintFlags(0)
            holder.text_message!!.setTextColor(Color.parseColor("#8a000000"))
        }

        holder.text_message!!.visibility = View.VISIBLE
        holder.text_seen!!.visibility = View.VISIBLE

        if (!chat.getUrl().equals("")) {
            holder.itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("uri", "${chat.getUrl()}")
                findNavController(holder.itemView).navigate(R.id.action_DM_to_viewImage, bundle)
            }
            if (chat.getSender().equals(firebaseUser.uid)) {
                holder.text_message!!.visibility = View.GONE
                holder.right_image!!.visibility = View.VISIBLE
                holder.rightCard!!.visibility = View.VISIBLE
                holder.rightCard!!.layout(0,0,0,0)
                holder.right_image!!.layout(0,0,0,0)
                Glide.with(context!!)
                    .load(chat.getUrl())
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .listener(object : com.bumptech.glide.request.RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            holder.imageLoader!!.visibility = View.GONE
                            return false
                        }
                    })
                    .into(holder.right_image!!)
            } else if (!chat.getSender().equals(firebaseUser.uid)) {
                holder.text_message!!.visibility = View.GONE
                holder.left_image!!.visibility = View.VISIBLE
                holder.leftCard!!.visibility = View.VISIBLE
                holder.leftCard!!.layout(0,0,0,0)
                holder.left_image!!.layout(0,0,0,0)
                Glide.with(context!!)
                    .load(chat.getUrl())
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .listener(object : com.bumptech.glide.request.RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            holder.imageLoader!!.visibility = View.GONE
                            return false
                        }
                    })
                    .into(holder.left_image!!)
            }
        }
        else{
            if (holder.leftCard != null) {
                holder.left_image!!.visibility = View.GONE
                holder.leftCard!!.visibility = View.GONE
            }
            if (holder.rightCard != null) {
                holder.right_image!!.visibility = View.GONE
                holder.rightCard!!.visibility = View.GONE
            }
            holder.text_message!!.text = chat.getMessage()
        }
//is seen logic: waych coding cafe 54 mins episode 11
        if (holder.minutesAgo == null){
        if (position == chatList.size -1){
            holder.text_seen!!.text = "Delivered"
            if (!chat.getUrl().equals("")){
                holder.imageDelivered!!.visibility = View.VISIBLE
                holder.text_seen!!.visibility = View.GONE
            }
        }
        else{
            holder.text_seen!!.visibility = View.GONE
            holder.imageDelivered!!.visibility = View.GONE
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
        var username: TextView? = null
        var imageLoader: ProgressBar? = null
        var imageDelivered: TextView? = null

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
            imageLoader = itemView.findViewById(R.id.message_image_progress)
            imageDelivered = itemView.findViewById(R.id.tv_message_right_image_delivered)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatList[position].getSender().equals(firebaseUser.uid)){
            1
        }
        else{
            0
        }
    }
}