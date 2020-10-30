package com.duncboi.realsquabble.HolderClass

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duncboi.realsquabble.Adapters.UserAdapter
import com.duncboi.realsquabble.ModelClasses.Users
import com.duncboi.realsquabble.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class GroupMembers(groupId: String) : Fragment() {

    private val groupId: String

    init {
        this.groupId = groupId
    }

    private var userAdapter: UserAdapter? = null
    private var users: List<Users>? = null
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_members, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        users = ArrayList()
        recyclerView = view.findViewById(R.id.group_members_recycler)
        recyclerView!!.hasFixedSize()
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        retrieveAllUsers()

    }

    private fun retrieveAllUsers(){

        val refUsers = FirebaseDatabase.getInstance().reference.child("Group Chat List").child(groupId).child("members")
        refUsers.onDisconnect().cancel()
        refUsers.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                (users as ArrayList<Users>).clear()
                for (snapshot in  p0.children){
                    val uid = snapshot.key
                    if (uid != null) {
                        FirebaseDatabase.getInstance().reference.child("Users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onCancelled(error: DatabaseError) {}
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val user = snapshot.getValue(Users::class.java)
                                if (user != null) {
                                    (users as ArrayList<Users>).add(user)
                                }
                                userAdapter = activity?.let { UserAdapter(it, users!!, "group") }
                                recyclerView?.adapter = userAdapter
                            }

                        })
                    }

                }

            }


        })

    }
}