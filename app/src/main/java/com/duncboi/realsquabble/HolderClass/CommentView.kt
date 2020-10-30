package com.duncboi.realsquabble.HolderClass

import android.content.Context
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
import com.duncboi.realsquabble.ModelClasses.Users
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.notifications.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_comment_view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CommentView : Fragment() {

    private var commentAdapter: CommentAdapter? = null
    private var comments: List<Comments>? = null
    private var recyclerView: RecyclerView? = null
    var apiService: APIService? = null

    private val args: CommentViewArgs by navArgs()
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    private val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_comment_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)

        iv_comment_view_back.setOnClickListener {
            findNavController().popBackStack()
        }

        comments = ArrayList()
        recyclerView = view.findViewById(R.id.comment_recycler)
        recyclerView!!.hasFixedSize()
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        CoroutineScope(Main).launch {
            delay(400)
            retrieveComments()

            b_comment_view_send.setOnClickListener {
                val message = et_comment_view_comment.text.toString().trim()
                if (message != "") {
                    val time = System.currentTimeMillis()

                    val commentsRef =
                        postRef.child(args.from).child(args.groupId).child(args.postId)
                            .child("comments")
                    val commentsKey = commentsRef.push().key!!

                    val commentHash = HashMap<String, Any?>()
                    commentHash["user"] = currentUser
                    commentHash["timePosted"] = "$time"
                    commentHash["comment"] = message
                    commentHash["commentId"] = commentsKey
                    commentHash["likeNumber"] = 0

                    commentsRef.child(commentsKey).setValue(commentHash).addOnCompleteListener {
                        if (et_comment_view_comment != null) {
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
                                                            commentsKey
                                                        )
                                                    }
                                                }
                                            })
                                    }
                                })
                            retrieveComments()
                            et_comment_view_comment.setText("")
                        }
                    }
                }
            }
        }

}

    private fun retrieveComments(){
        val refUsers = FirebaseDatabase.getInstance().reference.child("Posts").child(args.from).child(args.groupId).child(args.postId).child("comments")
        refUsers.onDisconnect().cancel()
            refUsers.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    (comments as ArrayList<Comments>).clear()
                    (comments as ArrayList<Comments>).add(Comments("awdawda", "pdwaopd", "23232", "poo", 23232))
                    for (snapshot in p0.children) {
                        val comment: Comments? = snapshot.getValue(Comments::class.java)
                        if (comment != null) {
                            (comments as ArrayList<Comments>).add(comment)
                        }
                    }
                    if (pb_comment_view_progress != null) pb_comment_view_progress.visibility = View.GONE
                    val sortedList = orderComments(comments as ArrayList<Comments>)
                    commentAdapter = activity?.let { CommentAdapter(it, sortedList,args.postId, args.groupId, "comment", "", args.from) }
                    recyclerView?.adapter = commentAdapter
                }
            })
    }

    private fun orderComments(commentList: List<Comments>): List<Comments>{
        val sorted = commentList.sortedWith(compareByDescending<Comments> {it.getCommentId().equals("poo")}.thenByDescending{it.getUser().equals(currentUser)}.thenByDescending{it.getLikeNumber()}.thenByDescending {it.getTimePosted()})
        return sorted
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
                                            "Anonymous commented on your post",
                                            "New Message",
                                            receiverUid,
                                            likedCommentId,
                                            groupId,
                                            commentsKey
                                        )
                                        else data = Data(
                                            currentUser,
                                            R.mipmap.squabble_icon,
                                            "$username commented on your post",
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