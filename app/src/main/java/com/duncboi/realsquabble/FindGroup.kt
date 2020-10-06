package com.duncboi.realsquabble

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duncboi.realsquabble.messenger.UserAdapter
import com.duncboi.realsquabble.messenger.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_find_group.*

class FindGroup : Fragment() {

    var groupList: List<Groups>? = null
    var groupsAdapter: GroupAdapter? = null
    private var recyclerView: RecyclerView? = null
    val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.find_group_recycler)
        recyclerView!!.hasFixedSize()
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        retrieveGroups()

        et_find_group_search.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val text = p0.toString().toLowerCase().trim()
                searchForGroups(text)
            }
        })
    }

    private fun retrieveGroups() {
        groupList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Group Chat List")
        reference.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                (groupList as ArrayList<Groups>).clear()
                for (snapshot1 in snapshot.children){
                    val group = snapshot1.getValue(Groups::class.java)
                    if (group != null) {
                        (groupList as ArrayList<Groups>).add(group)
                    }
                    groupsAdapter = activity?.let { GroupAdapter(it, (groupList as ArrayList<Groups>)) }
                    recyclerView?.adapter = groupsAdapter
                }
            }
        })
    }

    private fun searchForGroups(str:String){
        val queryGroups = FirebaseDatabase.getInstance().reference.child("Group Chat List")

        queryGroups.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                (groupList as ArrayList<Groups>).clear()
                for (snapshot in  snapshot.children) {
                    val group: Groups? = snapshot.getValue(Groups::class.java)
                    if (group!!.getName().toString().toLowerCase().contains(str) || group.getDescription().toString().toLowerCase().contains(str)) {
                            (groupList as ArrayList<Groups>).add(group)
                    }
                }
                groupsAdapter = activity?.let { GroupAdapter(it, (groupList as ArrayList<Groups>)) }
                recyclerView?.adapter = groupsAdapter
            }

        })
    }

}