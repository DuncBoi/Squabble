package com.duncboi.realsquabble.HolderClass

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duncboi.realsquabble.Adapters.NotificationAdapter
import com.duncboi.realsquabble.ModelClasses.Notifications
import com.duncboi.realsquabble.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Inbox : Fragment() {

    var notificationAdapter: NotificationAdapter? = null
    var notificationList: List<Notifications>? = null
    lateinit var dmRecyclerView: RecyclerView
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inbox, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dmRecyclerView = view.findViewById(R.id.inbox_recycler)
        dmRecyclerView.setHasFixedSize(true)
        dmRecyclerView.layoutManager = LinearLayoutManager(context)
        notificationList = ArrayList()

        retrieveNotifications()
    }

    private fun retrieveNotifications() {
        val reference = FirebaseDatabase.getInstance().reference.child("Notifications").child(currentUser)
        reference.onDisconnect().cancel()
        reference.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                (notificationList as ArrayList<Notifications>).clear()
                for (snapshot1 in snapshot.children){
                    val notification = snapshot1.getValue(Notifications::class.java)
                    if (notification != null) {
                        (notificationList as ArrayList<Notifications>).add(notification)
                        Log.d("cheese", notification.toString())
                    }
                }
                val ordered = orderNotifications((notificationList as ArrayList<Notifications>))
                Log.d("cheese", "$ordered")
                notificationAdapter = activity?.let { NotificationAdapter(it, ordered) }
                dmRecyclerView.adapter = notificationAdapter
            }
        })
    }

    private fun orderNotifications(notificationList: List<Notifications>): List<Notifications>{
        val sorted = notificationList.sortedWith(compareByDescending<Notifications> {it.getTimeOfPost()})
        return sorted
    }

}