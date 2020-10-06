package com.duncboi.realsquabble

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentAdapter(context: Context, commentList: List<Comments>): RecyclerView.Adapter<CommentAdapter.ViewHolder?>() {

    private val context: Context?
    private val commentList: List<Comments>
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    init {
        this.context = context
        this.commentList = commentList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.comment, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = commentList[position]

        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(comment.getUser()!!)

        holder.text_message!!.text = comment.getComment()

        userRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val uri = snapshot.child("uri").getValue(String::class.java)
                val username = snapshot.child("username").getValue(String::class.java)
                val name = snapshot.child("name").getValue(String::class.java)

                if (name != "") holder.username!!.text = name
                else holder.username!!.text = username
                if (uri != null && uri != ""){
                    Picasso.get().load(uri).into(holder.profileImage)
                }
            }
        })
    }
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView? = null
        var text_message: TextView? = null
        var minutesAgo: TextView? = null
        var username: TextView? = null

        init {
            profileImage = itemView.findViewById(R.id.civ_comment_profile_pic)
            text_message = itemView.findViewById(R.id.tv_comment_text)
            minutesAgo = itemView.findViewById(R.id.tv_comment_time)
            username = itemView.findViewById(R.id.tv_comment_username)
        }
    }

}