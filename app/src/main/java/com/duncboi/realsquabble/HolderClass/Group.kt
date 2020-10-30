package com.duncboi.realsquabble.HolderClass

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.duncboi.realsquabble.Adapters.PostAdapter
import com.duncboi.realsquabble.Adapters.PostAdapterVM
import com.duncboi.realsquabble.Adapters.PostAdapterVM.groupOrderedPosts
import com.duncboi.realsquabble.Adapters.PostAdapterVM.groupPaginatorPosts
import com.duncboi.realsquabble.Adapters.PostAdapterVM.groupPosition
import com.duncboi.realsquabble.Adapters.PostAdapterVM.groupPostAdapter
import com.duncboi.realsquabble.Adapters.PostAdapterVM.groupRecyclerView
import com.duncboi.realsquabble.Adapters.PostAdapterVM.groupmResults
import com.duncboi.realsquabble.ModelClasses.Groups
import com.duncboi.realsquabble.ModelClasses.Posts
import com.duncboi.realsquabble.ModelClasses.Users
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_group.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.NullPointerException


class Group : Fragment(), ProfileActivity.MyInterface {

    private var posts: List<Posts>? = null
    val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser)
    var groupId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        groupOrderedPosts = ArrayList()
        posts = ArrayList()
        groupPaginatorPosts = ArrayList()
        groupPosition = 0
        CoroutineScope(Main).launch {
            delay(100)
            userRef.onDisconnect().cancel()
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    groupId = snapshot.child("groupId").getValue(String::class.java)
                    if (groupId == "" || groupId == null) {
                        if (pb_group_progress != null) {
                            pb_group_progress.visibility = View.GONE
                            tv_group_do_not_belong_to_group.visibility = View.VISIBLE
                            b_group_find_group.visibility = View.VISIBLE
                            b_group_create_group.visibility = View.VISIBLE
                            fab_group_create_post.visibility = View.GONE
                        }
                    } else {
                        retrieveAllPosts()
                    }
                }
            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupswiperefresh.setOnRefreshListener {
            groupOrderedPosts = ArrayList()
            posts = ArrayList()
            groupPaginatorPosts = ArrayList()
            groupRecyclerView = view.findViewById(R.id.recycler_group)
            CoroutineScope(Main).launch {
                delay(100)
                retrieveAllPosts()
            }
        }
        groupswiperefresh.setColorSchemeColors(Color.parseColor("#bf55ec"))
        groupswiperefresh.setDistanceToTriggerSync(20)
        groupswiperefresh.setSize(SwipeRefreshLayout.DEFAULT)

        groupRecyclerView = view.findViewById(R.id.recycler_group)
        groupRecyclerView!!.hasFixedSize()
        groupRecyclerView!!.layoutManager = LinearLayoutManager(context)

        CoroutineScope(Main).launch {
            delay(300)

            userRef.onDisconnect().cancel()
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    groupId = snapshot.child("groupId").getValue(String::class.java)
                    if (groupId == "" || groupId == null) {
                        if (pb_group_progress != null) {
                            pb_group_progress.visibility = View.GONE
                            tv_group_do_not_belong_to_group.visibility = View.VISIBLE
                            b_group_find_group.visibility = View.VISIBLE
                            b_group_create_group.visibility = View.VISIBLE
                            fab_group_create_post.visibility = View.GONE
                        }
                        b_group_find_group.setOnClickListener {
                            findNavController().navigate(R.id.action_groupHolder_to_findGroup)
                        }

                        b_group_create_group.setOnClickListener {
                            findNavController().navigate(R.id.action_groupHolder_to_createGroup)
                        }

                    } else {
                        if (imageView20 != null) {
                            imageView20.setOnClickListener {
                                if (groupId != "") {
                                    val bundle = Bundle()
                                    bundle.putString("groupId", groupId)
                                    findNavController().navigate(
                                        R.id.action_groupHolder_to_groupDescription,
                                        bundle
                                    )
                                }
                            }
                            if (!groupOrderedPosts.isNullOrEmpty()) {
                                groupPostAdapter = activity?.let {
                                    PostAdapter(
                                        it,
                                        (groupPaginatorPosts as ArrayList<Posts>),
                                        groupId!!,
                                        "Group Posts",
                                        "group"
                                    )
                                }
                                groupRecyclerView?.adapter = groupPostAdapter
                                groupRecyclerView?.scrollToPosition(groupPosition)
                            }
                            val user = snapshot.getValue(Users::class.java)
                            tv_group_do_not_belong_to_group.visibility = View.GONE
                            b_group_find_group.visibility = View.GONE
                            b_group_create_group.visibility = View.GONE
                            fab_group_create_post.visibility = View.VISIBLE

                            val groupIdRef =
                                FirebaseDatabase.getInstance().reference.child("Group Chat List")
                                    .child("${user?.getGroupId()}")
                            groupIdRef.onDisconnect().cancel()
                            groupIdRef.addListenerForSingleValueEvent(
                                object : ValueEventListener {
                                    override fun onCancelled(error: DatabaseError) {}
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val group = snapshot.getValue(Groups::class.java)
                                        if (group != null && tv_group_group_name != null) {
                                            tv_group_group_name.text = group.getName()
                                            if (group.getUri() != "") Picasso.get()
                                                .load(group.getUri())
                                                .into(
                                                    civ_group_group_image
                                                )
                                        }
                                    }

                                })
                        }
                    }
                }

            })

           if (fab_group_create_post != null) fab_group_create_post.setOnClickListener {
                findNavController().navigate(R.id.action_groupHolder_to_createPost)
            }
        }
            }


    private fun retrieveAllPosts(){
        if (groupId != "") {
            val refUsers = FirebaseDatabase.getInstance().reference.child("Posts").child("Group Posts").child(
                groupId!!
            )
            refUsers.onDisconnect().cancel()
            refUsers.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    (posts as ArrayList<Posts>).clear()
                    (groupPaginatorPosts as ArrayList<Posts>).clear()
                    if (groupRecyclerView != null) {
                        for (snapshot in p0.children) {
                            val post: Posts? = snapshot.getValue(Posts::class.java)
                            if (post != null) {
                                (posts as ArrayList<Posts>).add(post)
                            }
                        }

                        if (pb_group_progress != null) pb_group_progress.visibility = View.GONE
                        groupOrderedPosts = orderPosts((posts as ArrayList<Posts>))

                        var iterations = groupOrderedPosts!!.size

                        if (iterations > 10) {
                            iterations = 10
                        }
                        groupmResults = 10
                        for (i in 0..iterations - 1) {
                            (groupPaginatorPosts as ArrayList<Posts>).add(groupOrderedPosts!![i])
                        }

                        groupPostAdapter = activity?.let {
                            PostAdapter(
                                it,
                                (groupPaginatorPosts as ArrayList<Posts>),
                                groupId!!,
                                "Group Posts",
                                "group"
                            )
                        }
                        if (groupswiperefresh != null) {
                            groupswiperefresh.isRefreshing = false
                            pb_group_progress.visibility = View.GONE
                        }
                        groupRecyclerView?.adapter = groupPostAdapter
                    }
                }
            })
        }
    }

    private fun orderPosts(postList: List<Posts>): List<Posts>{
        val sorted = postList.sortedWith(compareByDescending<Posts> { it.getZscore() })
        return sorted
    }

    fun displayMorePhotos(){
        try {
            if (groupOrderedPosts!!.size > groupmResults!! && groupOrderedPosts!!.isNotEmpty()) {
                var iterations = 0
                if (groupOrderedPosts!!.size > (groupmResults!! + 10)) {
                    iterations = 10
                } else {
                    iterations = groupOrderedPosts!!.size - groupmResults!!
                }

                for (i in groupmResults!!..groupmResults!! + iterations - 1) {
                    (groupPaginatorPosts as ArrayList<Posts>).add(groupOrderedPosts!![i])
                }

                groupmResults = groupmResults!! + iterations
               groupRecyclerView?.post(object : Runnable
                {
                    override fun run() {
                        groupPostAdapter?.notifyDataSetChanged()
                    }
                })
            }
        }
        catch (e: NullPointerException){
            Log.d("PostAdapter", "Nullpointer: $e")
        }
    }

    override fun myAction() {
        displayMorePhotos()
    }
}

