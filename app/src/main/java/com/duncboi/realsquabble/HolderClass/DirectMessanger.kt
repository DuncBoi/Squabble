package com.duncboi.realsquabble.HolderClass

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duncboi.realsquabble.Adapters.UserAdapter
import com.duncboi.realsquabble.ModelClasses.Users
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.political.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_direct_messanger.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main

class DirectMessanger : Fragment() {

    private var userAdapter: UserAdapter? = null
    private var users: List<Users>? = null
    private var recyclerView: RecyclerView? = null
    private var searchEditText: EditText? = null
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

        retrieveAllUsers()

        iv_direct_message_back.setOnClickListener {
            findNavController().popBackStack()
        }

        b_direct_messenger_create_group.setOnClickListener {
                stopCoroutine = false
                job = CoroutineScope(Main).launch {
                    while (!stopCoroutine && tv_direct_message_error != null) {
                        delay(200)
                        if (Constants.groupUsers.size >= 2) {
                            job?.cancel()
                            stopCoroutine = true
                            tv_direct_message_error.visibility = View.GONE
                        } else {
                            tv_direct_message_error.visibility = View.VISIBLE
                        }
                    }
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
        refUsers.onDisconnect().cancel()
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

                    userAdapter = activity?.let { UserAdapter(it, users!!, "search") }
                    recyclerView?.adapter = userAdapter

                }
            }


        })

    }

    private fun searchForUsers(str:String){
        var firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid

        val queryUsers = FirebaseDatabase.getInstance().reference.child("Users")
        queryUsers.onDisconnect().cancel()
        queryUsers.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                (users as ArrayList<Users>).clear()
                if (userAdapter != null) {
                    for (snapshot in p0.children) {
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
                    userAdapter = UserAdapter(activity!!, users!!, "search")
                    recyclerView?.adapter = userAdapter
                }
            }

        })
    }



}