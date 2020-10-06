package com.duncboi.realsquabble

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duncboi.realsquabble.messenger.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_group.*


class Group : Fragment() {

    private var postAdapter: PostAdapter? = null
    private var posts: List<Posts>? = null
    private var recyclerView: RecyclerView? = null
    val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser)
    var usersListener: ValueEventListener? = null
    var groupId: String? = ""


    override fun onPause() {
        super.onPause()
        if (usersListener != null) userRef.removeEventListener(usersListener!!)
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

        posts = ArrayList()
        recyclerView = view.findViewById(R.id.recycler_group)
        recyclerView!!.hasFixedSize()
        recyclerView!!.layoutManager = LinearLayoutManager(context)

            usersListener = userRef.addValueEventListener(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    groupId = snapshot.child("groupId").getValue(String::class.java)
                    if (groupId == "" || groupId == null){
                        tv_group_do_not_belong_to_group.visibility = View.VISIBLE
                        b_group_find_group.visibility = View.VISIBLE
                        fab_group_create_post.visibility = View.GONE

                        b_group_find_group.setOnClickListener {
                            findNavController().navigate(R.id.action_groupHolder_to_findGroup)
                        }

                    }
                    else{
                        retrieveAllPosts()
                        val user = snapshot.getValue(Users::class.java)
                        tv_group_do_not_belong_to_group.visibility = View.GONE
                        b_group_find_group.visibility = View.GONE
                        fab_group_create_post.visibility = View.VISIBLE

                        FirebaseDatabase.getInstance().reference.child("Group Chat List").child("${user?.getGroupId()}").addValueEventListener(object : ValueEventListener{
                            override fun onCancelled(error: DatabaseError) {}
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val group = snapshot.getValue(Groups::class.java)
                                if (group != null) {
                                    tv_group_group_name.text = group.getName()
                                    if (group.getUri() != "") Picasso.get().load(group.getUri()).into(civ_group_group_image)
                                }
                            }

                        })
                    }
                }

            })

            fab_group_create_post.setOnClickListener{
                findNavController().navigate(R.id.action_groupHolder_to_createPost)
            }


            }


    private fun retrieveAllPosts(){
        if (groupId != "") {
            val refUsers = FirebaseDatabase.getInstance().reference.child("Posts").child(groupId!!)
            refUsers.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    Log.d("poocow", p0.toString())
                    (posts as ArrayList<Posts>).clear()
                    for (snapshot in p0.children) {
                        val post: Posts? = snapshot.getValue(Posts::class.java)
                        if (post != null) {
                            (posts as ArrayList<Posts>).add(post)
                        }
                    }
                    postAdapter = activity?.let { PostAdapter(it, posts!!, groupId!!) }
                    recyclerView?.adapter = postAdapter
                }


            })
        }
        else{
            Log.d("poocow", "empty")
        }

    }




}

