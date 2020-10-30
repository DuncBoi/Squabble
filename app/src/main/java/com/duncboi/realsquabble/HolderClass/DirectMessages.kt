package com.duncboi.realsquabble.HolderClass

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.duncboi.realsquabble.Adapters.UserAdapter
import com.duncboi.realsquabble.ModelClasses.Posts
import com.duncboi.realsquabble.ModelClasses.Users
import com.duncboi.realsquabble.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_direct_messages.*

class DirectMessages : Fragment() {

    private var userAdapter: UserAdapter? = null
    private var users: List<Users>? = null
    private var unreadUsers: List<Users>? = null
    private var recyclerView: RecyclerView? = null
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_direct_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        direct_messaging_swipe_refresh.setOnRefreshListener{
            retrieveAllUsers()
        }
        direct_messaging_swipe_refresh.setColorSchemeColors(Color.parseColor("#bf55ec"))
        direct_messaging_swipe_refresh.setDistanceToTriggerSync(20)
        direct_messaging_swipe_refresh.setSize(SwipeRefreshLayout.DEFAULT)

        users = ArrayList()
        unreadUsers = ArrayList()
        recyclerView = view.findViewById(R.id.unread_messages_recycler)
        recyclerView!!.hasFixedSize()
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        retrieveAllUsers()
    }

    private fun retrieveAllUsers(){
        val refUsers = FirebaseDatabase.getInstance().reference.child("ChatList").child(currentUser)
        refUsers.onDisconnect().cancel()
        refUsers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    (users as ArrayList<Users>).clear()
                    (unreadUsers as ArrayList<Users>).clear()
                    for (i in snapshot.children) {
                    val uid = i.key
                    if (uid != null) {
                        val uidRef = FirebaseDatabase.getInstance().reference.child("Users").child(uid)
                        uidRef.onDisconnect().cancel()
                            uidRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(error: DatabaseError) {}
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (pb_direct_messages_progress != null) {
                                        pb_direct_messages_progress.visibility = View.GONE
                                        direct_messaging_swipe_refresh.isRefreshing = false
                                    }
                                    val user = snapshot.getValue(Users::class.java)
                                    if (user != null) {
                                        val isSeen = i.child("isSeen").getValue(Boolean::class.java)
                                        val timeOfMessage = i.child("timeOfMessage").getValue(String::class.java)
                                        if (timeOfMessage != null) {
                                            if (isSeen != null) {
                                                if (isSeen == true) {
                                                    user.setTimeOfMessage(timeOfMessage.toLong())
                                                    (users as ArrayList<Users>).add(user)
                                                } else {
                                                    user.setTimeOfMessage(timeOfMessage.toLong())
                                                    user.setUnread(false)
                                                    (users as ArrayList<Users>).add(user)
                                                }
                                            } else {
                                                user.setTimeOfMessage(timeOfMessage.toLong())
                                                (users as ArrayList<Users>).add(user)
                                            }
                                        }

                                        val ordered = orderUsers((users as ArrayList<Users>))

                                        userAdapter =
                                            activity?.let { UserAdapter(it, ordered, "dm") }
                                        recyclerView?.adapter = userAdapter
                                    }
                                }
                            })
                    }
                }
                }
                else{
                    pb_direct_messages_progress.visibility = View.GONE
                    tv_direct_messages_no_messages.visibility = View.VISIBLE
                    b_direct_messages_new_message.visibility = View.VISIBLE
                    b_direct_messages_new_message.setOnClickListener {
                        findNavController().navigate(R.id.action_defaultMessenger_to_directMessanger)
                    }
                }
            }
        })
    }

    private fun orderUsers(userList: List<Users>): List<Users>{
        val sorted = userList.sortedWith(compareBy<Users> {it.getUnread()!!.equals(true)}.thenByDescending {it.getTimeOfMessage()})
        return sorted
    }
}