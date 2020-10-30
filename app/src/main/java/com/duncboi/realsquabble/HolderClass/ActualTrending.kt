package com.duncboi.realsquabble.HolderClass

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.duncboi.realsquabble.Adapters.PostAdapter
import com.duncboi.realsquabble.Adapters.PostAdapterVM
import com.duncboi.realsquabble.BuildConfig
import com.duncboi.realsquabble.ModelClasses.Posts
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.profile.ProfileActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_actual_trending.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class ActualTrending : Fragment(), ProfileActivity.MyInterface {

    private var posts: List<Posts>? = null
    private var groupId2 = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_actual_trending, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //clearApplicationData()
        PostAdapterVM.trendingOrderedPosts = ArrayList()
        posts = ArrayList()
        PostAdapterVM.trendingPaginatorPosts = ArrayList()
        PostAdapterVM.trendingPosition = 0
        PostAdapterVM.trendingPag = ArrayList()
        CoroutineScope(Main).launch {
            delay(100)
            retrieveTrendingPosts(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        CoroutineScope(Main).launch {
            delay(300)
            if (!PostAdapterVM.trendingOrderedPosts.isNullOrEmpty()) {
                val query = FirebaseDatabase.getInstance().reference.child("PostsRef").child("all")
                    .orderByChild(
                        "zscore"
                    )
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot3: DataSnapshot) {
                        if (PostAdapterVM.trendingRecyclerView != null) {
                            for (i in snapshot3.children) {
                                var groupId = i.child("groupId").getValue(String::class.java)
                                if (groupId != null) {
                                    groupId2 = groupId
                                }
                                if (groupId != null) {
                                    if (pb_trending_progress != null) pb_trending_progress.visibility =
                                        View.GONE
                                    PostAdapterVM.trendingPostAdapter = activity?.let {
                                        PostAdapter(
                                            it,
                                            (PostAdapterVM.trendingPaginatorPosts as ArrayList<Posts>),
                                            groupId2,
                                            "Group Posts",
                                            "trending"
                                        )
                                    }
                                    PostAdapterVM.trendingRecyclerView?.adapter =
                                        PostAdapterVM.trendingPostAdapter
                                    PostAdapterVM.trendingRecyclerView?.scrollToPosition(
                                        PostAdapterVM.trendingPosition
                                    )
                                }
                            }
                        }
                    }
                })
            }
        }

        PostAdapterVM.trendingRecyclerView = trending_recycler
        PostAdapterVM.trendingRecyclerView!!.hasFixedSize()
        PostAdapterVM.trendingRecyclerView!!.layoutManager = LinearLayoutManager(context)

        trending_swipe_refresh.setOnRefreshListener{
            PostAdapterVM.trendingOrderedPosts = ArrayList()
            posts = ArrayList()
            PostAdapterVM.trendingPag = ArrayList()
            PostAdapterVM.trendingPaginatorPosts = ArrayList()
            PostAdapterVM.trendingRecyclerView = view.findViewById(R.id.trending_recycler)
            CoroutineScope(Main).launch {
                delay(100)
                retrieveTrendingPosts(true)
            }
        }
        trending_swipe_refresh.setColorSchemeColors(Color.parseColor("#bf55ec"))
        trending_swipe_refresh.setDistanceToTriggerSync(20)
        trending_swipe_refresh.setSize(SwipeRefreshLayout.DEFAULT)
    }

    private fun retrieveTrendingPosts(onCreate: Boolean){
        val query = FirebaseDatabase.getInstance().reference.child("PostsRef").child("all").orderByChild(
            "zscore"
        )
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot3: DataSnapshot) {
                (posts as ArrayList<Posts>).clear()
                        for (i in snapshot3.children) {
                            var groupId = i.child("groupId").getValue(String::class.java)
                            if (groupId != null) {
                                groupId2 = groupId
                            }
                            if (groupId != null && PostAdapterVM.trendingRecyclerView != null) {
                                FirebaseDatabase.getInstance().reference.child("Posts")
                                    .child("Group Posts")
                                    .child(
                                        groupId
                                    ).child(i.key!!).addListenerForSingleValueEvent(object :
                                        ValueEventListener {
                                        override fun onCancelled(error: DatabaseError) {}
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val post = snapshot.getValue(Posts::class.java)
                                            if (post != null) {
                                                (posts as ArrayList<Posts>).add(post)
                                            }
                                            else{
                                                snapshot.ref.removeValue()
                                            }
                                            if (pb_trending_progress != null) pb_trending_progress.visibility =
                                                View.GONE

                                            PostAdapterVM.trendingOrderedPosts =
                                                orderPosts((posts as ArrayList<Posts>))

                                            Log.d("trending", "trendingOrdered: ${PostAdapterVM.trendingOrderedPosts}")

                                            var iterations = PostAdapterVM.trendingOrderedPosts!!.size

                                            if (iterations > 10) {
                                                iterations = 10
                                            }
                                            PostAdapterVM.trendingmResults = 10
                                            (PostAdapterVM.trendingPaginatorPosts as ArrayList<Posts>).clear()
                                            for (i in 0..iterations - 1) {
                                                (PostAdapterVM.trendingPaginatorPosts as ArrayList<Posts>).add(
                                                    PostAdapterVM.trendingOrderedPosts!![i]
                                                )
                                            }

                                            if (trending_swipe_refresh != null) trending_swipe_refresh.isRefreshing = false
                                            PostAdapterVM.trendingPostAdapter = activity?.let {
                                                PostAdapter(
                                                    it,
                                                    (PostAdapterVM.trendingPaginatorPosts as ArrayList<Posts>),
                                                    groupId2,
                                                    "Group Posts",
                                                    "trending"
                                                )
                                            }
                                            PostAdapterVM.trendingRecyclerView?.adapter =
                                                PostAdapterVM.trendingPostAdapter
                                        }
                                    })
                            }
                        }
            }
                        })
                    }

    private fun orderPosts(postList: List<Posts>): List<Posts>{
        val sorted = postList.sortedWith(compareByDescending<Posts> { it.getZscore() })
        return sorted
    }

    fun displayMorePhotos(){
        Log.d("trending", "more")
        try {
            if (PostAdapterVM.trendingOrderedPosts!!.size > PostAdapterVM.trendingmResults!! && PostAdapterVM.trendingOrderedPosts!!.isNotEmpty()) {
                var iterations = 0
                if (PostAdapterVM.trendingOrderedPosts!!.size > (PostAdapterVM.trendingmResults!! + 10)) {
                    iterations = 10
                } else {
                    iterations = PostAdapterVM.trendingOrderedPosts!!.size - PostAdapterVM.trendingmResults!!
                }

                for (i in PostAdapterVM.trendingmResults!!..PostAdapterVM.trendingmResults!! + iterations - 1) {
                    (PostAdapterVM.trendingPaginatorPosts as ArrayList<Posts>).add(PostAdapterVM.trendingOrderedPosts!![i])
                }

                PostAdapterVM.trendingmResults = PostAdapterVM.trendingmResults!! + iterations

                PostAdapterVM.trendingRecyclerView?.post(object : Runnable
                {
                    override fun run() {
                        PostAdapterVM.trendingPostAdapter?.notifyDataSetChanged()
                    }
                })

            } else {
                Log.d("trending", "cooch")
            }
        }
        catch (e: NullPointerException){
            Log.d("trending", "Nullpointer: $e")
        }
    }

    override fun myAction() {
        displayMorePhotos()
    }
}