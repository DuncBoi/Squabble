package com.duncboi.realsquabble.messenger

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.duncboi.realsquabble.Constants
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.User
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(context: Context, users: List<Users>, isChatCheck:Boolean, type: String): RecyclerView.Adapter<UserAdapter.ViewHolder?>() {

    private val context: Context
    private val users: List<Users>
    private val isChatCheck: Boolean
    private val type: String
    init {
        this.users = users
        this.context = context
        this.isChatCheck = isChatCheck
        this.type = type
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.recycler_view_user, viewGroup, false)
        return UserAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val user: Users = users[i]

        var banner = 0
        if (type == "addmembers"){
            holder.checkBox.visibility = View.VISIBLE
            for (member in Constants.groupUsers){
                if (member == user.getUid()){
                    holder.itemView.setBackgroundColor(Color.parseColor("#17bf55ec"))
                    holder.checkBox.isChecked = true
                }
            }
        }

        if (user.getStatus() == "ONLINE"){
            holder.status.setImageResource(R.drawable.online)}

        holder.itemView.setOnClickListener{
            if (type == "dm"){
                val bundle = Bundle()
                bundle.putString("uid", "${user.getUid()}")
                findNavController(holder.itemView).navigate(R.id.action_directMessanger_to_DM, bundle)}
            else if (type == "profile"){
                val bundle = Bundle()
                bundle.putString("uid", "${user.getUid()}")
                findNavController(holder.itemView).navigate(R.id.action_directMessanger_to_otherProfile, bundle)
            }
            else if (type == "addmembers"){

                if (holder.checkBox.isChecked){
                    Constants.groupUsers.remove("${user.getUid()}")
                    holder.itemView.setBackgroundResource(R.drawable.rounded_edittext_register_login)
                    holder.checkBox.isChecked = false
                }
                else{
                    Constants.groupUsers.add("${user.getUid()}")
                    holder.itemView.setBackgroundColor(Color.parseColor("#17bf55ec"))
                    holder.checkBox.isChecked = true
                }
            }
        }
        Log.d("Moose", "poo: ${user.getUri()}")
        if (user.getUri() != "") {
            Picasso.get().load(user.getUri()).into(holder.profileImageView)
        }

        when (user.getCategory()){
            "Moderate" -> banner = R.drawable.moderate_banner
            "Conservative" -> banner = R.drawable.conservative_banner
            "Liberal" -> banner = R.drawable.ic_liberal_banner
            "Libertarian" -> banner = R.drawable.libertarian_banner
            "Authoritarian" -> banner = R.drawable.authoritarian_banner
        }

        holder.banner.setImageResource(banner)

        if (user.getName() == ""){
            holder.nameTxt.text = "@${user.getUsername()}"
            holder.nameTxt.setTextColor(Color.parseColor("#bf55ec"))
            if (user.getStatus() == "ONLINE"){
                holder.usernameTxt.text = "Online"
                holder.usernameTxt.setTextColor(Color.parseColor("#38c96d"))
                }
            else{
                holder.usernameTxt.text = "Offline"
                holder.usernameTxt.setTextColor(Color.parseColor("#8a000000"))
            }
        }
        else{
            holder.usernameTxt.text = "@${user.getUsername()}"
            holder.nameTxt.text = user.getName()
         }}

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var usernameTxt: TextView
        var profileImageView: CircleImageView
        var nameTxt: TextView
        var banner: ImageView
        var status: ImageView
        var checkBox: CheckBox

        init {
            usernameTxt = itemView.findViewById(R.id.tv_recycler_username)
            profileImageView = itemView.findViewById(R.id.cv_recycler_user_profile_pic)
            nameTxt = itemView.findViewById(R.id.tv_recycler_name)
            banner = itemView.findViewById(R.id.iv_recycler_political_banner)
            status = itemView.findViewById(R.id.iv_recycler_status)
            checkBox = itemView.findViewById(R.id.cb_user_recycler)
        }
    }

}


