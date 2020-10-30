package com.duncboi.realsquabble.HolderClass

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duncboi.realsquabble.Adapters.PostAdapter
import com.duncboi.realsquabble.ModelClasses.Posts
import com.duncboi.realsquabble.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_liked_posts.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class likedPosts : Fragment() {

    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    private var postAdapter: PostAdapter? = null
    private var posts: List<Posts>? = null
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_liked_posts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        posts = ArrayList()
        recyclerView = view.findViewById(R.id.liked_posts_recycler)
        recyclerView!!.hasFixedSize()
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        CoroutineScope(Main).launch {
            delay (300)
            retrieveLikedPosts()
        }
    }

    private fun retrieveLikedPosts(){
        val refUsers = FirebaseDatabase.getInstance().reference.child("Liked Posts").child(currentUser)
        refUsers.onDisconnect().cancel()
            refUsers.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    (posts as ArrayList<Posts>).clear()
                    for (snapshot in p0.children) {
                        val postId = snapshot.child("postId").getValue(String::class.java)
                        val groupId = snapshot.child("groupId").getValue(String::class.java)
                        if (groupId != null && postId != null) {
                            FirebaseDatabase.getInstance().reference.child("Posts").child("Group Posts").child(groupId).child(postId).addListenerForSingleValueEvent(object : ValueEventListener{
                                override fun onCancelled(error: DatabaseError) {}
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val post = snapshot.getValue(Posts::class.java)
                                    if (post != null) {
                                        (posts as ArrayList<Posts>).add(post)
                                    }
                                    if (pb_liked_posts_progress != null) pb_liked_posts_progress.visibility = View.GONE
                                    val orderedPosts = orderPosts((posts as ArrayList<Posts>))
                                    postAdapter = activity?.let { PostAdapter(it, orderedPosts, post!!.getGroupId()!!, "Group Posts", "profile") }
                                    recyclerView?.adapter = postAdapter
                                }
                            })
                        }
                    }

                }


            })

    }

    private fun orderPosts(postList: List<Posts>): List<Posts>{
        val sorted = postList.sortedWith(compareByDescending<Posts> {it.getTimeOfPost()})
        return sorted
    }

}