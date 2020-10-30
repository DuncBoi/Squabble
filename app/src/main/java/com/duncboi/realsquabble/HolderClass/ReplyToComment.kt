package com.duncboi.realsquabble.HolderClass

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duncboi.realsquabble.Adapters.CommentAdapter
import com.duncboi.realsquabble.ModelClasses.Comments
import com.duncboi.realsquabble.ModelClasses.Data
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.notifications.*
import com.duncboi.realsquabble.political.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_reply_to_comment.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReplyToComment : Fragment() {

    private var commentAdapter: CommentAdapter? = null
    private var comments: List<Comments>? = null
    private var recyclerView: RecyclerView? = null
    private var comment: Comments? = null
    var apiService: APIService? = null

    private val args: ReplyToCommentArgs by navArgs()
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    private val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reply_to_comment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iv_reply_to_comment_back.setOnClickListener {
            findNavController().popBackStack()
        }

        apiService = Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)

        comments = ArrayList()
        recyclerView = view.findViewById(R.id.recycler_reply_to_comment)
        recyclerView!!.hasFixedSize()
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        retrieveComments()

        b_reply_message_send.setOnClickListener {
            val message = et_reply_to_comment.text.toString().trim()
            if (message != "") {
                val time = System.currentTimeMillis()

                val commentsRef = postRef.child(args.from).child(args.groupId).child(args.postId).child("comments").child(args.commentId).child("comments")
                val commentsKey = commentsRef.push().key!!

                val commentHash = HashMap<String, Any?>()
                commentHash["user"] = currentUser
                commentHash["timePosted"] = "$time"
                commentHash["comment"] = message
                commentHash["commentId"] = commentsKey
                commentHash["likeNumber"] = 0

                commentsRef.child(commentsKey).setValue(commentHash).addOnCompleteListener {
                    if (et_reply_to_comment != null) {
                        closeKeyboard()
                        et_reply_to_comment.setText("")
                        retrieveComments()
                        FirebaseDatabase.getInstance().reference.child("Users")
                            .child(currentUser).child("username")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(error: DatabaseError) {}
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val username = snapshot.getValue(String::class.java)
                                    FirebaseDatabase.getInstance().reference.child("Posts")
                                        .child(args.from).child(args.groupId).child(args.postId)
                                        .child("creator")
                                        .addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onCancelled(error: DatabaseError) {}
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                val creator =
                                                    snapshot.getValue(String::class.java)
                                                if (creator != null) {
                                                    sendNotification(
                                                        creator,
                                                        username,
                                                        args.postId,
                                                        args.groupId,
                                                        args.commentId
                                                    )
                                                }
                                            }
                                        })
                                }
                            })
                    }
                }
            }
        }

        val commentIdRef = FirebaseDatabase.getInstance().reference.child("Posts").child(args.from).child(args.groupId).child(args.postId).child("comments").child(args.commentId)
        commentIdRef.onDisconnect().cancel()
            commentIdRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                comment = snapshot.getValue(Comments::class.java)

        if (comment != null) {

            val commentLikeRef =
                FirebaseDatabase.getInstance().reference.child("Likes").child("commentLikes")
                    .child(args.groupId).child(args.postId).child("comments")
                    .child(comment!!.getCommentId()!!)
                    .child("likes")

            commentLikeRef.onDisconnect().cancel()
            commentLikeRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    var tally = 0
                    for (i in snapshot.children) {
                        val num = i.getValue(Int::class.java)
                        if (num != null) {
                            tally += num
                        }
                    }
                    FirebaseDatabase.getInstance().reference.child("Posts").child(args.from).child(args.groupId)
                        .child(args.postId).child("comments").child(comment!!.getCommentId()!!)
                        .child("likeNumber").setValue(tally)
                }
            })

            commentLikeRef.onDisconnect().cancel()
            commentLikeRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    var tally = 0
                    for (i in snapshot.children) {
                        val num = i.getValue(Int::class.java)
                        val user = i.key
                        if (num != null) {
                            tally += num
                            if (user == currentUser) {
                                when (num) {
                                    1 -> {
                                        if(iv_reply_to_comment_heart != null) {
                                            iv_reply_to_comment_heart_fill!!.visibility =
                                                View.VISIBLE
                                            iv_reply_to_comment_heart!!.visibility = View.INVISIBLE
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (tally >= 1000) {
                        val returnTally = Constants.wrap_number(tally.toDouble())
                        tv_reply_to_comment_like_count?.text = "$returnTally"
                    } else {
                        tv_reply_to_comment_like_count?.text = "$tally"
                    }
                }

            })

            iv_reply_to_comment_heart!!.setOnClickListener {
                commentLikeRef.child(currentUser).setValue(1).addOnCompleteListener {
                    iv_reply_to_comment_heart!!.visibility = View.INVISIBLE
                    iv_reply_to_comment_heart_fill!!.visibility = View.VISIBLE
                }
                val userRef2 = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser)
                userRef2.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val username = snapshot.child("username").getValue(String::class.java)
                        if (comment!!.getUser() != currentUser) {
                            likedCommentSendNotification(
                                comment!!.getUser()!!,
                                username,
                                comment!!.getCommentId()!!,
                                args.groupId,
                                args.postId
                            )
                        }
                    }
                })
            }

            iv_reply_to_comment_heart_fill!!.setOnClickListener {
                commentLikeRef.child(currentUser).removeValue().addOnCompleteListener {
                    if (iv_reply_to_comment_heart != null) {
                        iv_reply_to_comment_heart!!.visibility = View.VISIBLE
                        iv_reply_to_comment_heart_fill!!.visibility = View.INVISIBLE
                    }
                }
                val numberUnreadRef = FirebaseDatabase.getInstance().reference.child("Unread Notifications").child(comment!!.getUser()!!).child("numberUnread")
                numberUnreadRef.removeValue()
                FirebaseDatabase.getInstance().reference.child("Notifications").child(comment!!.getUser()!!).child(comment!!.getCommentId()!!).removeValue()
            }

           tv_reply_to_comment_text!!.text = comment!!.getComment()
            val timePosted = comment!!.getTimePosted()?.toLong()
            val currentTime = System.currentTimeMillis()
            if (timePosted != null) {
                val millisElapsed = currentTime - timePosted
                val minutesElapsed = millisElapsed / 60000
                val hoursElapsed = minutesElapsed / 60
                val daysElapsed = hoursElapsed / 24
                val weeksElapsed = daysElapsed / 7
                val monthsElapsed = weeksElapsed / 4
                val yearsElapsed = monthsElapsed / 12
                if (yearsElapsed > 0) tv_reply_to_comment_time!!.text = "• ${yearsElapsed}years ago •"
                else if (monthsElapsed > 0) tv_reply_to_comment_time!!.text =
                    "• ${monthsElapsed}months ago •"
                else if (weeksElapsed > 0) tv_reply_to_comment_time!!.text = "• ${weeksElapsed}w ago •"
                else if (daysElapsed > 0) tv_reply_to_comment_time!!.text = "• ${daysElapsed}d ago •"
                else if (hoursElapsed > 0) tv_reply_to_comment_time!!.text = "• ${hoursElapsed}h ago •"
                else if (minutesElapsed > 0) tv_reply_to_comment_time!!.text = "• ${minutesElapsed}m ago •"
                else tv_reply_to_comment_time!!.text = "• Just now •"
            }

            val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(comment?.getUser()!!)
            userRef.onDisconnect().cancel()
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    val uri = snapshot.child("uri").getValue(String::class.java)
                    val username = snapshot.child("username").getValue(String::class.java)
                    val name = snapshot.child("name").getValue(String::class.java)
                    val anonymous = snapshot.child("anonymous").getValue(String::class.java)
                    if (civ_reply_to_comment_profile_pic != null) {
                        if (currentUser == comment!!.getUser()) {
                            tv_reply_to_comment_username!!.text = "You"
                            et_reply_to_comment.hint = "Reply to yourself"
                            tv_reply_to_comment_username!!.setTextColor(Color.parseColor("#4885ed"))
                        }
                        else {
                            if (anonymous != "ON") {
                                if (name != "") {
                                    tv_reply_to_comment_username!!.text = name
                                    et_reply_to_comment.hint = "Reply to $name"
                                } else {
                                    tv_reply_to_comment_username!!.text = username
                                    et_reply_to_comment.hint = "Reply to $username"
                                }
                                if (uri != null && uri != "") {
                                    Picasso.get().load(uri).into(civ_reply_to_comment_profile_pic)
                                }
                                civ_reply_to_comment_profile_pic.setOnClickListener {
                                    val bundle = Bundle()
                                    bundle.putString("uid", "${comment!!.getUser()}")
                                    if (comment!!.getUser() != currentUser) findNavController().navigate(
                                        R.id.action_replyToComment_to_otherProfile,
                                        bundle
                                    )
                                }

                                tv_reply_to_comment_username.setOnClickListener {
                                    val bundle = Bundle()
                                    bundle.putString("uid", "${comment!!.getUser()}")
                                    if (comment!!.getUser() != currentUser) findNavController().navigate(
                                        R.id.action_replyToComment_to_otherProfile,
                                        bundle
                                    )
                                }
                            } else {
                                tv_reply_to_comment_username!!.text = "Anonymous"
                            }
                        }
                    }
                }
            })
        }
            }
        })
    }

    private fun retrieveComments(){
        val refUsers = FirebaseDatabase.getInstance().reference.child("Posts").child(args.from).child(args.groupId).child(args.postId).child("comments").child(args.commentId).child("comments")
        refUsers.onDisconnect().cancel()
        refUsers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                (comments as ArrayList<Comments>).clear()
                for (snapshot in p0.children) {
                    val comment: Comments? = snapshot.getValue(Comments::class.java)
                    if (comment != null) {
                        (comments as ArrayList<Comments>).add(comment)
                    }
                }
                val sortedList = orderComments(comments as ArrayList<Comments>)
                commentAdapter = activity?.let { CommentAdapter(it, sortedList, args.postId, args.groupId, "commentComment", args.commentId, args.from) }
                recyclerView?.adapter = commentAdapter
            }
        })
    }

    private fun orderComments(commentList: List<Comments>): List<Comments>{
        val sorted = commentList.sortedWith(compareByDescending<Comments> {it.getCommentId().equals("poo")}.thenByDescending{it.getUser().equals(currentUser)}.thenByDescending{it.getLikeNumber()}.thenByDescending {it.getTimePosted()})
        return sorted
    }

    private fun closeKeyboard(){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun likedCommentSendNotification(
        receiverUid: String,
        username: String?,
        likedCommentId: String,
        groupId: String,
        postId: String
    ){
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")

        val query = ref.orderByKey().equalTo(receiverUid)

        query.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children){
                    val token: Token? = dataSnapshot.getValue(Token::class.java)

                    if (receiverUid != currentUser) {
                        FirebaseDatabase.getInstance().reference.child("Users").child(currentUser)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(error: DatabaseError) {}
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val anonymous =
                                        snapshot.child("anonymous").getValue(String::class.java)
                                    if (anonymous != null) {
                                        val data: Data
                                        if (anonymous == "ON") data = Data(currentUser, R.mipmap.squabble_icon, "Anonymous liked your comment", "New Message", receiverUid, postId, groupId, likedCommentId)
                                        else data = Data(currentUser, R.mipmap.squabble_icon, "$username liked your comment", "New Message", receiverUid, postId, groupId, likedCommentId)

                                        val sender = Sender(data, token!!.getToken().toString())

                                        apiService!!.sendNotificaiton(sender).enqueue(object :
                                            Callback<MyResponse> {
                                            override fun onResponse(call: Call<MyResponse>, response: Response<MyResponse>) {
                                                if (response.code() == 200){
                                                    if (response.body()!!.success != 1){
                                                        Log.d("Notification", "failed notification")
                                                    }
                                                }
                                            }

                                            override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                                            }
                                        })
                                    }
                                }
                            })
                    }
                }
            }
        })
    }

    private fun sendNotification(
        receiverUid: String,
        username: String?,
        likedCommentId: String,
        groupId: String,
        commentsKey: String
    ){
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")

        val query = ref.orderByKey().equalTo(receiverUid)

        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val token: Token? = dataSnapshot.getValue(Token::class.java)

                    if (receiverUid != currentUser) {
                        FirebaseDatabase.getInstance().reference.child("Users").child(currentUser)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(error: DatabaseError) {}
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val anonymous =
                                        snapshot.child("anonymous").getValue(String::class.java)
                                    if (anonymous != null) {
                                        val data: Data
                                        if (anonymous == "ON") data = Data(
                                            currentUser,
                                            R.mipmap.squabble_icon,
                                            "Anonymous replied to your comment",
                                            "New Message",
                                            receiverUid,
                                            likedCommentId,
                                            groupId,
                                            commentsKey
                                        )
                                        else data = Data(
                                            currentUser,
                                            R.mipmap.squabble_icon,
                                            "$username replied to your comment",
                                            "New Message",
                                            receiverUid,
                                            likedCommentId,
                                            groupId,
                                            commentsKey
                                        )

                                        val sender = Sender(data, token!!.getToken().toString())

                                        apiService!!.sendNotificaiton(sender)
                                            .enqueue(object : Callback<MyResponse> {
                                                override fun onResponse(
                                                    call: Call<MyResponse>,
                                                    response: Response<MyResponse>
                                                ) {
                                                    if (response.code() == 200) {
                                                        if (response.body()!!.success != 1) {
                                                            Log.d(
                                                                "Notification",
                                                                "failed notification"
                                                            )
                                                        }
                                                    }
                                                }

                                                override fun onFailure(
                                                    call: Call<MyResponse>,
                                                    t: Throwable
                                                ) {
                                                }
                                            })
                                    }
                                }
                            })
                    }
                }
            }
        })
    }


}
