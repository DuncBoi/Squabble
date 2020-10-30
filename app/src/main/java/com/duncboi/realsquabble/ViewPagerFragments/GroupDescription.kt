package com.duncboi.realsquabble.ViewPagerFragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager.widget.ViewPager
import com.duncboi.realsquabble.HolderClass.GroupMembers
import com.duncboi.realsquabble.ModelClasses.Groups
import com.duncboi.realsquabble.ModelClasses.Users
import com.duncboi.realsquabble.PagerDescriptionClasses.GroupActualDescription
import com.duncboi.realsquabble.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_group_description.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class GroupDescription : Fragment() {

    private val args: GroupDescriptionArgs by navArgs()
    val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    private var users: List<Users>? = null

    private val groupChatList = FirebaseDatabase.getInstance().reference.child("Group Chat List")
    private val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser)
    private var groupChatListener: ValueEventListener? = null

    var alignmentList = ArrayList<String>()

    override fun onPause() {
        super.onPause()
        if (groupChatListener != null) groupChatList.child(args.groupId)
            .removeEventListener(groupChatListener!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        users = ArrayList()

        iv_group_description_back.setOnClickListener {
            findNavController().popBackStack()
        }

        iv_group_description_menu.setOnClickListener {
            findNavController().navigate(R.id.action_groupDescription_to_findGroup2)
        }

        val groupIdRef = FirebaseDatabase.getInstance().reference.child("Group Chat List").child(args.groupId)
        groupIdRef.onDisconnect().cancel()
            groupIdRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot2: DataSnapshot) {
                    val group = snapshot2.getValue(Groups::class.java)
                    if (group != null && tv_group_description_name != null) {
                        tv_group_description_name.text = group.getName()
                        if (group.getUri() != "") Picasso.get().load(group.getUri())
                            .into(civ_group_description)

                        val memberCount = snapshot2.child("members").childrenCount
                        tv_group_description_member_count.text = "$memberCount members"

                                val currentUserRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser)
                                currentUserRef.onDisconnect().cancel()
                                    currentUserRef.addListenerForSingleValueEvent(object : ValueEventListener{
                                    override fun onCancelled(error: DatabaseError) {}
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val category = snapshot.child("category").getValue(String::class.java)
                                        val userGroup = snapshot.child("groupId").getValue(String::class.java)
                                        if (!userGroup.isNullOrBlank() && b_group_description_join_group != null) {
                                            if (group.getChatId() == userGroup) {
                                                b_group_description_join_group.text = "Leave"
                                                b_group_description_join_group.setBackgroundResource(
                                                    R.drawable.rounded_button_red
                                                )
                                                b_group_description_join_group.setOnClickListener {
                                                    val builder = AlertDialog.Builder(activity)
                                                    builder.setCancelable(false)
                                                    builder.setTitle("Leave Group?")
                                                    builder.setMessage("Are you sure you want to leave the group")
                                                    builder.setPositiveButton(
                                                        "Yes",
                                                        DialogInterface.OnClickListener { _: DialogInterface, _: Int ->
                                                            groupIdRef.child("members")
                                                                .child(currentUser).ref.removeValue()
                                                            if (snapshot2.child("members").childrenCount == 1.toLong()) {
                                                                CoroutineScope(IO).launch {
                                                                snapshot2.ref.removeValue()
                                                                FirebaseDatabase.getInstance().reference.child("Group Messaging").child(args.groupId).removeValue()
                                                                FirebaseDatabase.getInstance().reference.child("Posts").child("Group Posts").child(args.groupId).removeValue()
                                                                FirebaseDatabase.getInstance().reference.child("Likes").child("votes").child(args.groupId).removeValue()
                                                                FirebaseDatabase.getInstance().reference.child("Group Messaging").child(args.groupId).removeValue()
                                                                FirebaseDatabase.getInstance().reference.child("PostsRef").child("all").orderByChild("groupId").addListenerForSingleValueEvent(object : ValueEventListener{
                                                                    override fun onCancelled(error: DatabaseError) {}
                                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                                        for (i in snapshot.children) {
                                                                            if (i.child("groupId").getValue(String::class.java) == args.groupId) {
                                                                                i.ref.removeValue() } } }})
                                                                FirebaseDatabase.getInstance().reference.child("PostsRef").child("Moderate").orderByChild("groupId").addListenerForSingleValueEvent(object : ValueEventListener{
                                                                    override fun onCancelled(error: DatabaseError) {}
                                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                                        for (i in snapshot.children) {
                                                                            if (i.child("groupId").getValue(String::class.java) == args.groupId) {
                                                                                i.ref.removeValue() } } }})
                                                                FirebaseDatabase.getInstance().reference.child("PostsRef").child("Liberal").orderByChild("groupId").addListenerForSingleValueEvent(object : ValueEventListener{
                                                                    override fun onCancelled(error: DatabaseError) {}
                                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                                        for (i in snapshot.children) {
                                                                            if (i.child("groupId").getValue(String::class.java) == args.groupId) {
                                                                                i.ref.removeValue() } } }})
                                                                FirebaseDatabase.getInstance().reference.child("PostsRef").child("Conservative").orderByChild("groupId").addListenerForSingleValueEvent(object : ValueEventListener{
                                                                    override fun onCancelled(error: DatabaseError) {}
                                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                                        for (i in snapshot.children) {
                                                                            if (i.child("groupId").getValue(String::class.java) == args.groupId) {
                                                                                i.ref.removeValue() } } }})
                                                                FirebaseDatabase.getInstance().reference.child("PostsRef").child("Authoritarian").orderByChild("groupId").addListenerForSingleValueEvent(object : ValueEventListener{
                                                                    override fun onCancelled(error: DatabaseError) {}
                                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                                        for (i in snapshot.children) {
                                                                            if (i.child("groupId").getValue(String::class.java) == args.groupId) {
                                                                                i.ref.removeValue() } } }})
                                                                FirebaseDatabase.getInstance().reference.child("PostsRef").child("Libertarian").orderByChild("groupId").addListenerForSingleValueEvent(object : ValueEventListener{
                                                                    override fun onCancelled(error: DatabaseError) {}
                                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                                        for (i in snapshot.children) {
                                                                            if (i.child("groupId").getValue(String::class.java) == args.groupId) {
                                                                                i.ref.removeValue() } } }})
                                                                FirebaseDatabase.getInstance().reference.child("Liked Posts").addListenerForSingleValueEvent(object : ValueEventListener{
                                                                    override fun onCancelled(error: DatabaseError) {}
                                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                                        for (i in snapshot.children) {
                                                                            i.ref.orderByChild("groupId").addListenerForSingleValueEvent(object : ValueEventListener{
                                                                                override fun onCancelled(error: DatabaseError) {}
                                                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                                                    for (i in snapshot.children) {
                                                                                        if (i.child("groupId").getValue(String::class.java) == args.groupId) {
                                                                                            snapshot.ref.removeValue()
                                                                                        }
                                                                                    }
                                                                                }
                                                                            })
                                                                        }
                                                                    } })
                                                                    }
                                                            }

                                                            userRef.child("groupId").removeValue()
                                                            Toast.makeText(
                                                                activity,
                                                                "Left Group",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            findNavController().navigate(R.id.action_groupDescription_to_findGroup)
                                                        })
                                                    builder.setNegativeButton(
                                                        "Cancel",
                                                        DialogInterface.OnClickListener { dialogInterface: DialogInterface, _: Int ->
                                                            dialogInterface.cancel()
                                                        })
                                                    builder.create().show()
                                                }
                                            }
                                            else {
                                                b_group_description_join_group.text = "Join"
                                                b_group_description_join_group.setBackgroundResource(
                                                    R.drawable.greyed_out_button
                                                )
                                                b_group_description_join_group.setOnClickListener {
                                                    val builder = AlertDialog.Builder(activity)
                                                    builder.setCancelable(false)
                                                    builder.setTitle("Error joining group")
                                                    builder.setMessage("You are already in a group")
                                                    builder.setPositiveButton(
                                                        "Ok",
                                                        DialogInterface.OnClickListener { dialogInterface, i ->
                                                            dialogInterface.cancel()
                                                        })
                                                    builder.create().show()
                                                }
                                            }
                                        }
                                        else {
                                                for (i in snapshot2.child("alignment").children) {
                                                    val alignment = i.getValue(String::class.java)
                                                    if (alignment != null) {
                                                        alignmentList.add(alignment)
                                                    }
                                                }
                                                if (!alignmentList.contains(category)) {
                                                    b_group_description_join_group.text = "Join"
                                                    b_group_description_join_group.setBackgroundResource(
                                                        R.drawable.greyed_out_button
                                                    )
                                                    b_group_description_join_group.setOnClickListener {
                                                        val builder = AlertDialog.Builder(activity)
                                                        builder.setCancelable(false)
                                                        builder.setTitle("Error joining group")
                                                        builder.setMessage("You must belong to the same political party as this group in order to join.")
                                                        builder.setPositiveButton(
                                                            "Ok",
                                                            DialogInterface.OnClickListener { dialogInterface, i ->
                                                                dialogInterface.cancel()
                                                            })
                                                        builder.create().show()
                                                    }
                                                } else {
                                                    b_group_description_join_group.setOnClickListener {
                                                        userRef.child("groupId")
                                                            .setValue(group.getChatId())
                                                        snapshot2.child("members")
                                                            .child(currentUser).ref.setValue("member")
                                                        Toast.makeText(
                                                            activity,
                                                            "Joined Group",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        findNavController().navigate(R.id.action_groupDescription_to_groupHolder)
                                                    }
                                                }
                                            }
                                    }
                                })

                        val adapter = GroupHolder.MyViewPagerAdapter(childFragmentManager)
                        adapter.addFragment(GroupActualDescription(group), "Group Description")
                        adapter.addFragment(GroupMembers(args.groupId), "Group Members")
                        val mViewPager = (groupViewPager) as ViewPager
                        mViewPager.adapter = adapter
                        groupTabLayout.setupWithViewPager(mViewPager)
                            }
                }


            })


    }
}