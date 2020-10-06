package com.duncboi.realsquabble

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duncboi.realsquabble.messenger.UserAdapter
import com.duncboi.realsquabble.messenger.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_direct_messanger.*
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class DirectMessanger : Fragment() {

    private var userAdapter: UserAdapter? = null
    private var users: List<Users>? = null
    private var recyclerView: RecyclerView? = null
    private var searchEditText: EditText? = null
    private val args: DirectMessangerArgs by navArgs()
    private var job: Job? = null
    private var stopCoroutine = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_direct_messanger, container, false)
    }

    override fun onPause() {
        super.onPause()
        stopCoroutine = true
        job?.cancel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.dm_recycler_view)
        recyclerView!!.hasFixedSize()
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        searchEditText = view.findViewById(R.id.et_direct_messenger_search_bar)

        users = ArrayList()

        if (args.type == "viewmembers"){
            retrieveGroupUsers()
        }
        else{
            retrieveAllUsers()
        }

        if (args.type == "addmembers"){
            b_direct_messenger_create_group.visibility = View.VISIBLE
        }

        b_direct_messenger_create_group.setOnClickListener {
            if (Constants.groupUsers!!.size >= 2) {
                findNavController().navigate(R.id.action_directMessanger_to_createGroup)
                Toast.makeText(activity, "Added users", Toast.LENGTH_SHORT).show()
            }
            else {
                stopCoroutine = false
                job = CoroutineScope(Main).launch {
                    while (!stopCoroutine) {
                        delay(200)
                        if (Constants.groupUsers!!.size >= 2) {
                            job?.cancel()
                            stopCoroutine = true
                            tv_direct_message_error.visibility = View.GONE
                        } else {
                            tv_direct_message_error.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        iv_direct_message_back.setOnClickListener {
            if (args.type == "addmembers"){
                Constants.groupUsers.clear()
                findNavController().navigate(R.id.action_directMessanger_to_createGroup)}
            else if (args.type == "viewmembers"){
                findNavController().navigate(R.id.action_directMessanger_to_createGroup)
            }
            else if (args.type == "dm"){
                findNavController().navigate(R.id.action_directMessanger_to_defaultMessenger)
            }
            else if (args.type == "profile"){
                val bundle = Bundle()
                findNavController().navigate(R.id.action_directMessanger_to_otherProfile)
            }
        }

        searchEditText!!.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(cs: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchForUsers(cs.toString().toLowerCase().trim())
            }

        })

    }


    private fun retrieveAllUsers(){

        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
        val refUsers = FirebaseDatabase.getInstance().reference.child("Users")
        refUsers.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (searchEditText!!.text.toString() == ""){
                    (users as ArrayList<Users>).clear()
                    for (snapshot in  p0.children){
                        val user: Users? = snapshot.getValue(Users::class.java)
                        if (!(user!!.getUid()).equals(firebaseUser)){
                            (users as ArrayList<Users>).add(user)
                        }
                    }

                    userAdapter = activity?.let { UserAdapter(it, users!!, false, "${args.type}") }
                    recyclerView?.adapter = userAdapter

                }
            }


        })

    }

    private fun retrieveGroupUsers() {

        val refUsers = FirebaseDatabase.getInstance().reference.child("Users")
        refUsers.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (searchEditText!!.text.toString() == ""){
                    (users as ArrayList<Users>).clear()
                    for (snapshot in  p0.children){
                        val user: Users? = snapshot.getValue(Users::class.java)
                        for (uid in Constants.groupUsers) {
                            if (uid == user!!.getUid()) {
                                (users as ArrayList<Users>).add(user)
                            }
                        }

                    }

                    userAdapter = activity?.let { UserAdapter(it, users!!, false, "${args.type}") }
                    recyclerView?.adapter = userAdapter

                }
            }


        })
    }

    private fun searchForUsers(str:String){
        var firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid

        val queryUsers = FirebaseDatabase.getInstance().reference.child("Users")

        queryUsers.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                (users as ArrayList<Users>).clear()
                for (snapshot in  p0.children) {
                    val user: Users? = snapshot.getValue(Users::class.java)
                    if (user!!.getName().toString().toLowerCase()
                            .contains(str) || user.getUsername().toString().toLowerCase()
                            .contains(str)
                    ) {
                        if (!(user.getUid()).equals(firebaseUserID)) {
                            (users as ArrayList<Users>).add(user)
                        }
                    }
                }
                userAdapter = UserAdapter(activity!!, users!!, false, "${args.type}")
                recyclerView?.adapter = userAdapter
            }

        })
    }



}