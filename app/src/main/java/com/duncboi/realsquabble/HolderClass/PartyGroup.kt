package com.duncboi.realsquabble.HolderClass

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.duncboi.realsquabble.Adapters.PostAdapter
import com.duncboi.realsquabble.Adapters.PostAdapterVM
import com.duncboi.realsquabble.ModelClasses.Posts
import com.duncboi.realsquabble.ModelClasses.Users
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_party_group.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.NullPointerException

class PartyGroup : Fragment(), ProfileActivity.MyInterface {

    private var posts: List<Posts> = ArrayList()
    private var party = ""

    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    private val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_party_group, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PostAdapterVM.partyOrderedPosts = ArrayList()
        posts = ArrayList()
        PostAdapterVM.partyPaginatorPosts = ArrayList()
        PostAdapterVM.partyPosition = 0
        CoroutineScope(Main).launch {
            delay(100)
            userRef.onDisconnect().cancel()
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(Users::class.java)
                    if (user != null && civ_party_group_profile_pic != null) {
                        val group = user.getGroupId()
                        if (group != null) {
                            when (user.getCategory()) {
                                "Moderate" -> {
                                    tv_party_group_group_name.text = "Moderate"
                                    party = "Moderate"
                                    civ_party_group_profile_pic.setImageResource(R.drawable.moderate_banner)
                                    retrieveTopGroupPosts("Moderate", true)
                                }
                                "Conservative" -> {
                                    tv_party_group_group_name.text = "Conservative"
                                    party = "Conservative"
                                    civ_party_group_profile_pic.setImageResource(R.drawable.conservative_banner)
                                    retrieveTopGroupPosts("Conservative", true)
                                }
                                "Liberal" -> {
                                    tv_party_group_group_name.text = "Liberal"
                                    party = "Liberal"
                                    civ_party_group_profile_pic.setImageResource(R.drawable.ic_liberal_banner)
                                    retrieveTopGroupPosts("Liberal", true)
                                }
                                "Libertarian" -> {
                                    tv_party_group_group_name.text = "Libertarian"
                                    party = "Libertarian"
                                    civ_party_group_profile_pic.setImageResource(R.drawable.libertarian_banner)
                                    retrieveTopGroupPosts("Libertarian", true)
                                }
                                "Authoritarian" -> {
                                    tv_party_group_group_name.text = "Authoritarian"
                                    party = "Authoritarian"
                                    civ_party_group_profile_pic.setImageResource(R.drawable.authoritarian_banner)
                                    retrieveTopGroupPosts("Authoritarian", true)
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        partySwipeRefresh.setOnRefreshListener{
            PostAdapterVM.partyOrderedPosts = ArrayList()
            posts = ArrayList()
            PostAdapterVM.partyPaginatorPosts = ArrayList()
            PostAdapterVM.partyRecyclerView = view.findViewById(R.id.recycler_party_group)
            CoroutineScope(Main).launch {
                delay(100)
                retrieveTopGroupPosts(party, true)
            }
        }
        partySwipeRefresh.setColorSchemeColors(Color.parseColor("#bf55ec"))
        partySwipeRefresh.setDistanceToTriggerSync(20)
        partySwipeRefresh.setSize(SwipeRefreshLayout.DEFAULT)

        PostAdapterVM.partyRecyclerView = recycler_party_group
        PostAdapterVM.partyRecyclerView!!.hasFixedSize()
        PostAdapterVM.partyRecyclerView!!.layoutManager = LinearLayoutManager(context)

        CoroutineScope(Main).launch {
            delay(300)
            userRef.onDisconnect().cancel()
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(Users::class.java)
                    if (user != null && civ_party_group_profile_pic != null) {
                        val group = user.getGroupId()
                        if (group != null) {
                            when (user.getCategory()) {
                                "Moderate" -> {
                                    tv_party_group_group_name.text = "Moderate"
                                    party = "Moderate"
                                    civ_party_group_profile_pic.setImageResource(R.drawable.moderate_banner)
                                }
                                "Conservative" -> {
                                    tv_party_group_group_name.text = "Conservative"
                                    party = "Conservative"
                                    civ_party_group_profile_pic.setImageResource(R.drawable.conservative_banner)
                                }
                                "Liberal" -> {
                                    tv_party_group_group_name.text = "Liberal"
                                    party = "Liberal"
                                    civ_party_group_profile_pic.setImageResource(R.drawable.ic_liberal_banner)
                                }
                                "Libertarian" -> {
                                    tv_party_group_group_name.text = "Libertarian"
                                    party = "Libertarian"
                                    civ_party_group_profile_pic.setImageResource(R.drawable.libertarian_banner)
                                }
                                "Authoritarian" -> {
                                    tv_party_group_group_name.text = "Authoritarian"
                                    party = "Authoritarian"
                                    civ_party_group_profile_pic.setImageResource(R.drawable.authoritarian_banner)
                                }
                            }
                            if (party_progress_bar != null) party_progress_bar.visibility = View.GONE
                            PostAdapterVM.partyPostAdapter = activity?.let {
                                PostAdapter(
                                    it,
                                    (PostAdapterVM.partyPaginatorPosts as ArrayList<Posts>),
                                    group,
                                    "Group Posts",
                                    "party"
                                )
                            }
                            PostAdapterVM.partyRecyclerView?.adapter = PostAdapterVM.partyPostAdapter
                            PostAdapterVM.partyRecyclerView?.scrollToPosition(PostAdapterVM.partyPosition)
                        }
                    }
                }
            })
        }

            }

    private fun retrieveTopGroupPosts(party: String, onCreate: Boolean){
        val query = FirebaseDatabase.getInstance().reference.child("PostsRef").child(party).orderByChild("zscore")
        query.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot3: DataSnapshot) {
                (posts as ArrayList<Posts>).clear()
                (PostAdapterVM.partyPaginatorPosts as ArrayList<Posts>).clear()
                for (i in snapshot3.children) {
                        val groupId = i.child("groupId").getValue(String::class.java)
                        if (groupId != null) {
                            FirebaseDatabase.getInstance().reference.child("Posts")
                                .child("Group Posts").child(groupId).child(i.key!!)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(error: DatabaseError) {}
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val post = snapshot.getValue(Posts::class.java)
                                        if (partySwipeRefresh != null) {
                                            if (post != null) {
                                                (posts as ArrayList<Posts>).add(post)
                                            }
                                            if (party_progress_bar != null) party_progress_bar.visibility =
                                                View.GONE

                                            PostAdapterVM.partyOrderedPosts =
                                                orderPosts((posts as ArrayList<Posts>))

                                            var iterations = PostAdapterVM.partyOrderedPosts!!.size

                                            if (iterations > 10) {
                                                iterations = 10
                                            }
                                            PostAdapterVM.partymResults = 10
                                            (PostAdapterVM.partyPaginatorPosts as ArrayList<Posts>).clear()
                                            for (i in 0..iterations - 1) {
                                                (PostAdapterVM.partyPaginatorPosts as ArrayList<Posts>).add(
                                                    PostAdapterVM.partyOrderedPosts!![i]
                                                )
                                            }

                                            partySwipeRefresh.isRefreshing = false
                                            party_progress_bar.visibility = View.GONE
                                        PostAdapterVM.partyPostAdapter = activity?.let {
                                            PostAdapter(
                                                it,
                                                (PostAdapterVM.partyPaginatorPosts as ArrayList<Posts>),
                                                groupId,
                                                "Group Posts",
                                                "party"
                                            )
                                        }
                                        PostAdapterVM.partyRecyclerView?.adapter =
                                            PostAdapterVM.partyPostAdapter
                                        }
                                    }
                                })
                        }
                    }
            }
        })
    }

    private fun orderPosts(postList: List<Posts>): List<Posts>{
        val sorted = postList.sortedWith(compareByDescending<Posts> {it.getZscore()})
        return sorted
    }

    fun displayMorePhotos(){
        try {
            if (PostAdapterVM.partyOrderedPosts!!.size > PostAdapterVM.partymResults!! && PostAdapterVM.partyOrderedPosts!!.isNotEmpty()) {
                var iterations = 0
                if (PostAdapterVM.partyOrderedPosts!!.size > (PostAdapterVM.partymResults!! + 10)) {
                    iterations = 10
                } else {
                    iterations = PostAdapterVM.partyOrderedPosts!!.size - PostAdapterVM.partymResults!!
                }

                for (i in PostAdapterVM.partymResults!!..PostAdapterVM.partymResults!! + iterations - 1) {
                    (PostAdapterVM.partyPaginatorPosts as ArrayList<Posts>).add(PostAdapterVM.partyOrderedPosts!![i])
                }

                PostAdapterVM.partymResults = PostAdapterVM.partymResults!! + iterations
                PostAdapterVM.partyRecyclerView?.post(object : Runnable
                {
                    override fun run() {
                        PostAdapterVM.partyPostAdapter?.notifyDataSetChanged()
                    }
                })
            }
        }
        catch (e: NullPointerException){
            Log.d("party", "Nullpointer: $e")
        }
    }

    override fun myAction() {
        displayMorePhotos()
    }
}