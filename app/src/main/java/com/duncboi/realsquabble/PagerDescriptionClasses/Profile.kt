package com.duncboi.realsquabble.PagerDescriptionClasses

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.duncboi.realsquabble.ModelClasses.Groups
import com.duncboi.realsquabble.ModelClasses.Users
import com.duncboi.realsquabble.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import java.lang.IllegalStateException

class Profile(uid: String, from: String) : Fragment() {

    private val uid: String = uid
    private val from: String = from

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(uid)
        userRef.onDisconnect().cancel()
            userRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)
                if (user != null) {
                    if (iv_profile_banner != null) {
                        modifyLayout(user)
                        if (user.getGroupId() != "") {
                            FirebaseDatabase.getInstance().reference.child("Group Chat List")
                                .child(user.getGroupId()!!)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(error: DatabaseError) {}
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val group = snapshot.getValue(Groups::class.java)
                                        if (group != null && civ_profile_image != null) {
                                            rl_profile_group_layout.visibility = View.VISIBLE
                                            tv_profile_group_text.visibility = View.VISIBLE

                                            rl_profile_group_layout.setOnClickListener {
                                                val bundle = Bundle()
                                                bundle.putString("groupId", "${group.getChatId()}")
                                                findNavController().navigate(R.id.action_default_profile_to_groupDescription, bundle)
                                            }

                                            if (group.getUri() != "") Picasso.get()
                                                .load(group.getUri())
                                                .into(civ_profile_image)
                                            tv_profile_name.text = group.getName()

                                            if (group.getStateLocation() == "Not Specified"){
                                                tv_profile_location.text = "Brooklyn, New York"
                                            }
                                            else{
                                                if (group.getCountyLocation() != "Not Specified"){
                                                    tv_profile_location.text = "${group.getCountyLocation()}, ${group.getStateLocation()}"
                                                }
                                                else{
                                                    tv_profile_location.text = "${group.getStateLocation()}"
                                                }
                                            }

                                            val groupRef = FirebaseDatabase.getInstance().reference.child("Group Chat List").child(group.getChatId()!!)
                                            groupRef.onDisconnect().cancel()
                                            groupRef.addListenerForSingleValueEvent(object : ValueEventListener{
                                                override fun onCancelled(error: DatabaseError) {}
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                        val alignment = snapshot.child("alignment").child("0").getValue(String::class.java)
                                                        try {
                                                            tv_profile_alignment.text = alignment
                                                            when (alignment) {
                                                                "Moderate" -> {
                                                                    iv_profilesingle_alignment.setImageResource(
                                                                        R.drawable.moderate_banner
                                                                    )
                                                                }
                                                                "Conservative" -> {
                                                                    iv_profilesingle_alignment.setImageResource(
                                                                        R.drawable.conservative_banner
                                                                    )
                                                                }
                                                                "Liberal" -> {
                                                                    iv_profilesingle_alignment.setImageResource(
                                                                        R.drawable.ic_liberal_banner
                                                                    )
                                                                }
                                                                "Libertarian" -> {
                                                                    iv_profilesingle_alignment.setImageResource(
                                                                        R.drawable.libertarian_banner
                                                                    )
                                                                }
                                                                "Authoritarian" -> {
                                                                    iv_profilesingle_alignment.setImageResource(
                                                                        R.drawable.authoritarian_banner
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        catch (e: IllegalStateException){}
                                                }
                                            })

                                            val memberCount =
                                                snapshot.child("members").childrenCount
                                            tv_profile_members.text = "$memberCount members"
                                        }
                                    }
                                })
                        }
                        iv_profile_alignment_holder.setOnClickListener {
                            val bundle = Bundle()
                            bundle.putString("uid", uid)
                            bundle.putString("from", from)
                            if (from == "profile") {
                                findNavController().navigate(
                                    R.id.action_default_profile_to_alignmentView,
                                    bundle
                                )
                            }
                            else findNavController().navigate(
                                R.id.action_otherProfile_to_alignmentView,
                                bundle
                            )
                        }
                    }
                }
            }
        })


    }

    private fun modifyLayout(user: Users) {
            when (user.getCategory()) {
                "Moderate" -> {
                    iv_profile_banner.setImageResource(R.drawable.moderate_banner)
                    tv_profile_alignment_text.text = "Moderate"
                }
                "Conservative" -> {
                    iv_profile_banner.setImageResource(R.drawable.conservative_banner)
                    tv_profile_alignment_text.text = "Conservative"
                }
                "Liberal" -> {
                    iv_profile_banner.setImageResource(R.drawable.ic_liberal_banner)
                    tv_profile_alignment_text.text = "Liberal"
                }
                "Libertarian" -> {
                    iv_profile_banner.setImageResource(R.drawable.libertarian_banner)
                    tv_profile_alignment_text.text = "Libertarian"
                }
                "Authoritarian" -> {
                    iv_profile_banner.setImageResource(R.drawable.authoritarian_banner)
                    tv_profile_alignment_text.text = "Populist"
                }
            }
            if (user.getBio() != "") {
                tv_profile_bio.text = user.getBio()
                tv_profile_bio_text.visibility = View.VISIBLE
                tv_profile_bio.visibility = View.VISIBLE
            }
    }


}