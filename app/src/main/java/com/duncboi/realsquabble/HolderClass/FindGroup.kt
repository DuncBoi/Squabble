package com.duncboi.realsquabble.HolderClass

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duncboi.realsquabble.Adapters.GroupAdapter
import com.duncboi.realsquabble.ModelClasses.Groups
import com.duncboi.realsquabble.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_find_group.*
import kotlin.math.acos

class FindGroup : Fragment() {

    var groupList: List<Groups>? = null
    var groupsAdapter: GroupAdapter? = null
    private var recyclerView: RecyclerView? = null
    val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    private var alignment: String? = ""
    private var myLongitude = 0.0
    private var myLatitude = 0.0
    var alignmentList = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupList = ArrayList()
        recyclerView = view.findViewById(R.id.find_group_recycler)
        recyclerView!!.hasFixedSize()
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        iv_find_group_back.setOnClickListener {
            findNavController().popBackStack()
        }

        retrieveGroups()

        FirebaseDatabase.getInstance().reference.child("Users").child(currentUser).child("groupId").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val groupId = snapshot.getValue(String::class.java)
                if (groupId == ""){
                    iv_create_group_create_group.setOnClickListener {
                        findNavController().navigate(R.id.action_findGroup_to_createGroup)
                    }
                }
                else{
                    iv_create_group_create_group.setOnClickListener {
                        Toast.makeText(activity, "Already in a group", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        cb_find_group_loaction.setOnClickListener{
            if (cb_find_group_loaction.isChecked){
                if (cb_find_group_joinable_groups.isChecked) {
                    if (ActivityCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        retrieveJoinableGroups(alignment!!)
                    } else {
                        cb_find_group_loaction.isChecked = false
                        requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
                    }
                }
                else{
                    if (ActivityCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        retrieveGroups()
                    } else {
                        cb_find_group_loaction.isChecked = false
                        requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
                        Log.d("mook", "no location perms")
                    }
                }
            }
            else{
                if (cb_find_group_joinable_groups.isChecked) {
                    retrieveJoinableGroups(alignment!!)
                }
                else{
                    retrieveGroups()
                }
            }
        }

        val locationRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser).child("Location")
        locationRef.onDisconnect().cancel()
            locationRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val longitude = snapshot.child("longitude").getValue(Double::class.java)
                val latitude = snapshot.child("latitude").getValue(Double::class.java)
                if (longitude != null && latitude != null && latitude.toString() != "" && longitude.toString() != ""){
                    myLatitude = latitude
                    myLongitude = longitude
                }
            }

        })

        cb_find_group_joinable_groups.setOnClickListener{
            if (cb_find_group_joinable_groups.isChecked){
                    val categoryRef = FirebaseDatabase.getInstance().reference.child("Users").child(
                        currentUser
                    ).child("category")
                    categoryRef.onDisconnect().cancel()
                    categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {}
                        override fun onDataChange(snapshot: DataSnapshot) {
                            alignment = snapshot.getValue(String::class.java)
                            if (alignment != "" && alignment != null) {
                                retrieveJoinableGroups(alignment!!)
                            }
                        }
                    })
            }
            else{
                retrieveGroups()
            }
        }

        et_find_group_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val text = p0.toString().toLowerCase().trim()
                searchForGroups(text)
            }
        })
    }

    private fun retrieveGroups() {
        val reference = FirebaseDatabase.getInstance().reference.child("Group Chat List")
        reference.onDisconnect().cancel()
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                (groupList as ArrayList<Groups>).clear()
                for (snapshot1 in snapshot.children) {
                    val group = snapshot1.getValue(Groups::class.java)
                    if (group != null) {
                        if (group.getLatitude().toString() != "unknown" && group.getLongitude().toString() != "unknown") {
                            val distance = distance(
                                myLatitude,
                                myLongitude,
                                group.getLatitude()!!.toDouble(),
                                group.getLongitude()!!.toDouble()
                            )
                            group.setDistance(distance)
                        }
                        (groupList as ArrayList<Groups>).add(group)
                    }
                }
                if (cb_find_group_loaction.isChecked) {
                    val ordered = orderByDistance((groupList as ArrayList<Groups>))
                    groupsAdapter = activity?.let { GroupAdapter(it, ordered) }
                    recyclerView?.adapter = groupsAdapter
                }
                else{
                    groupsAdapter = activity?.let { GroupAdapter(it, (groupList as ArrayList<Groups>)) }
                    recyclerView?.adapter = groupsAdapter
                }
            }
        })
    }

    private fun retrieveJoinableGroups(alignment: String){
        val ref = FirebaseDatabase.getInstance().reference.child("Group Chat List")
            ref.onDisconnect().cancel()
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    (groupList as ArrayList<Groups>).clear()
                    for (i in snapshot.children) {
                        for (i2 in i.child("alignment").children) {
                            val align = i2.getValue(String::class.java)
                            val group = i.getValue(Groups::class.java)
                            if (align == alignment && group != null) {
                                if (group.getLatitude().toString() != "unknown" && group.getLongitude().toString() != "unknown") {
                                    val distance = distance(
                                        myLatitude,
                                        myLongitude,
                                        group.getLatitude()!!.toDouble(),
                                        group.getLongitude()!!.toDouble()
                                    )
                                    group.setDistance(distance)
                                }
                                (groupList as ArrayList<Groups>).add(group)
                            }
                        }
                    }
                    if (cb_find_group_loaction.isChecked) {
                        val ordered = orderByDistance((groupList as ArrayList<Groups>))
                        groupsAdapter = activity?.let { GroupAdapter(it, ordered) }
                        recyclerView?.adapter = groupsAdapter
                    }
                    else{
                        groupsAdapter = activity?.let { GroupAdapter(it, (groupList as ArrayList<Groups>)) }
                        recyclerView?.adapter = groupsAdapter
                    }
                }
            })


    }

    private fun searchForGroups(str: String){
        if (cb_find_group_joinable_groups.isChecked != true) {
            val queryGroups = FirebaseDatabase.getInstance().reference.child("Group Chat List")
            queryGroups.onDisconnect().cancel()
            queryGroups.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    (groupList as ArrayList<Groups>).clear()
                    if (recyclerView != null) {
                        for (snapshot in snapshot.children) {
                            val group: Groups? = snapshot.getValue(Groups::class.java)
                            if (group != null && (group.getName().toString().toLowerCase()
                                    .contains(str) || group.getDescription().toString()
                                    .toLowerCase().contains(str))
                            ) {
                                if (group.getLatitude()
                                        .toString() != "unknown" && group.getLongitude()
                                        .toString() != "unknown"
                                ) {

                                    val distance = distance(
                                        myLatitude,
                                        myLongitude,
                                        group.getLatitude()!!.toDouble(),
                                        group.getLongitude()!!.toDouble()
                                    )
                                    group.setDistance(distance)
                                }
                                (groupList as ArrayList<Groups>).add(group)
                            }
                        }
                        if (cb_find_group_loaction != null) {
                            if (cb_find_group_loaction.isChecked) {
                                val ordered = orderByDistance((groupList as ArrayList<Groups>))
                                groupsAdapter = activity?.let { GroupAdapter(it, ordered) }
                                recyclerView?.adapter = groupsAdapter
                            } else {
                                groupsAdapter =
                                    activity?.let {
                                        GroupAdapter(
                                            it,
                                            (groupList as ArrayList<Groups>)
                                        )
                                    }
                                recyclerView?.adapter = groupsAdapter
                            }
                        }
                    }
                }
            })
        }
        else{
            val ref = FirebaseDatabase.getInstance().reference.child("Group Chat List")
            ref.onDisconnect().cancel()
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    (groupList as ArrayList<Groups>).clear()
                    for (i in snapshot.children) {
                        for (i2 in i.child("alignment").children) {
                            val align = i2.getValue(String::class.java)
                            val group = i.getValue(Groups::class.java)
                            if (group != null && (group.getName().toString().toLowerCase().contains(str) || group.getDescription().toString().toLowerCase().contains(str)) && align == alignment) {
                                if (group.getLatitude().toString() != "unknown" && group.getLongitude().toString() != "unknown") {
                                    val distance = distance(
                                        myLatitude,
                                        myLongitude,
                                        group.getLatitude()!!.toDouble(),
                                        group.getLongitude()!!.toDouble()
                                    )
                                    group.setDistance(distance)
                                }
                                (groupList as ArrayList<Groups>).add(group)
                            }
                        }
                    }
                    if (cb_find_group_loaction.isChecked) {
                        val ordered = orderByDistance((groupList as ArrayList<Groups>))
                        groupsAdapter = activity?.let { GroupAdapter(it, ordered) }
                        recyclerView?.adapter = groupsAdapter
                    }
                    else{
                        groupsAdapter = activity?.let { GroupAdapter(it, (groupList as ArrayList<Groups>)) }
                        recyclerView?.adapter = groupsAdapter
                    }
                }
            })
        }
    }

    private fun orderByDistance(groupList: List<Groups>): List<Groups>{
        val sorted = groupList.sortedWith(compareByDescending <Groups> {it.getDistance()})
        return sorted
    }

    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val theta = lon1 - lon2
        var dist = (Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + (Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta))))
        dist = acos(dist)
        dist = rad2deg(dist)
        dist *= 60 * 1.1515
        return dist
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            100 -> {
                if (!grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    retrieveGroups()
                } else {
                    if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        cb_find_group_loaction.isChecked = false
                        Toast.makeText(activity, "Permission Denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

}