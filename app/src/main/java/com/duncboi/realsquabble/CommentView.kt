package com.duncboi.realsquabble

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_comment_view.*

class CommentView : Fragment() {

    private var commentAdapter: CommentAdapter? = null
    private var comments: List<Comments>? = null
    private var recyclerView: RecyclerView? = null

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

        comments = ArrayList()
        recyclerView = view.findViewById(R.id.comment_recycler)
        recyclerView!!.hasFixedSize()
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        b_comment_view_send.setOnClickListener {
            val message = et_comment_view_comment.text.toString().trim()
            val time = System.currentTimeMillis()

            val commentsRef = postRef.child(args.groupId).child(args.postId).child("comments")
            val commentsKey = commentsRef.push().key!!

            val commentHash = HashMap<String, Any?> ()
            commentHash["user"] = currentUser
            commentHash["timePosted"] = "$time"
            commentHash["comment"] = message

            commentsRef.child(commentsKey).setValue(commentHash).addOnCompleteListener {
                et_comment_view_comment.setText("")
                retrieveComments()
            }
        }
    }

    private fun retrieveComments(){
            val refUsers = FirebaseDatabase.getInstance().reference.child("Posts").child(args.groupId).child(args.postId).child("comments")
            refUsers.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    (comments as ArrayList<Comments>).clear()
                    for (snapshot in p0.children) {
                        val comment: Comments? = snapshot.getValue(Comments::class.java)
                        if (comment != null) {
                            (comments as ArrayList<Comments>).add(comment)
                        }
                    }
                    commentAdapter = activity?.let { CommentAdapter(it, comments!!) }
                    recyclerView?.adapter = commentAdapter
                }


            })
    }


}