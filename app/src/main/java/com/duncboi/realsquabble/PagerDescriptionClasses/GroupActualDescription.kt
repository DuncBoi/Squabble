package com.duncboi.realsquabble.PagerDescriptionClasses

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.duncboi.realsquabble.ModelClasses.Groups
import com.duncboi.realsquabble.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_group_actual_description.*
import kotlinx.android.synthetic.main.fragment_profile.*

class GroupActualDescription(group: Groups) : Fragment() {

    private val group: Groups

    init {
        this.group = group
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_actual_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (group.getDescription() != ""){
            tv_group_description_bio.visibility = View.VISIBLE
            tv_group_description_bio.text = group.getDescription()
        }

        if (group.getStateLocation() == "Not Specified"){
            tv_group_description_location.text = "Not Specified"
        }
        else{
            if (group.getCountyLocation() != "Not Specified"){
                tv_group_description_location.text = "${group.getCountyLocation()}, ${group.getStateLocation()}"
            }
            else{
                tv_group_description_location.text = "${group.getStateLocation()}"
            }
        }

        val groupRef = FirebaseDatabase.getInstance().reference.child("Group Chat List").child(group.getChatId()!!)
        groupRef.onDisconnect().cancel()
        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val length = snapshot.child("alignment").childrenCount.toInt()
                if (length == 1) {
                    rl_group_actual_description_alignment2.visibility = View.GONE
                    val alignment =
                        snapshot.child("alignment").child("0").getValue(String::class.java)
                    tv_actual_group_description_alignment.text = alignment
                    when (alignment) {
                        "Moderate" -> {
                            iv_actual_group_description_single_alignment.setImageResource(
                                R.drawable.moderate_banner
                            )
                        }
                        "Conservative" -> {
                            iv_actual_group_description_single_alignment.setImageResource(
                                R.drawable.conservative_banner
                            )
                        }
                        "Liberal" -> {
                            iv_actual_group_description_single_alignment.setImageResource(
                                R.drawable.ic_liberal_banner
                            )
                        }
                        "Libertarian" -> {
                            iv_actual_group_description_single_alignment.setImageResource(
                                R.drawable.libertarian_banner
                            )
                        }
                        "Authoritarian" -> {
                            iv_actual_group_description_single_alignment.setImageResource(
                                R.drawable.authoritarian_banner
                            )
                        }
                    }
                }
                else{
                    val alignment1 = snapshot.child("alignment").child("0").getValue(String::class.java)
                    val alignment2 = snapshot.child("alignment").child("1").getValue(String::class.java)

                    tv_actual_group_description_alignment.text = alignment1
                    tv_actual_group_description_alignment2.text = alignment2

                    when (alignment1) {
                        "Moderate" -> {
                            iv_actual_group_description_single_alignment.setImageResource(
                                R.drawable.moderate_banner
                            )
                        }
                        "Conservative" -> {
                            iv_actual_group_description_single_alignment.setImageResource(
                                R.drawable.conservative_banner
                            )
                        }
                        "Liberal" -> {
                            iv_actual_group_description_single_alignment.setImageResource(
                                R.drawable.ic_liberal_banner
                            )
                        }
                        "Libertarian" -> {
                            iv_actual_group_description_single_alignment.setImageResource(
                                R.drawable.libertarian_banner
                            )
                        }
                        "Authoritarian" -> {
                            iv_actual_group_description_single_alignment.setImageResource(
                                R.drawable.authoritarian_banner
                            )
                        }
                    }
                    when (alignment2) {
                        "Moderate" -> {
                            iv_actual_group_description_single_alignment2.setImageResource(
                                R.drawable.moderate_banner
                            )
                        }
                        "Conservative" -> {
                            iv_actual_group_description_single_alignment2.setImageResource(
                                R.drawable.conservative_banner
                            )
                        }
                        "Liberal" -> {
                            iv_actual_group_description_single_alignment2.setImageResource(
                                R.drawable.ic_liberal_banner
                            )
                        }
                        "Libertarian" -> {
                            iv_actual_group_description_single_alignment2.setImageResource(
                                R.drawable.libertarian_banner
                            )
                        }
                        "Authoritarian" -> {
                            iv_actual_group_description_single_alignment2.setImageResource(
                                R.drawable.authoritarian_banner
                            )
                        }
                    }
                }
            }
        })

    }

    //        groupChatListener = groupChatList.child(args.groupId).addValueEventListener(object : ValueEventListener{
//            override fun onCancelled(error: DatabaseError) {}
//            override fun onDataChange(snapshot: DataSnapshot) {
//
//                val group = snapshot.getValue(Groups::class.java)
//
//                val location = snapshot.child("filter").child("location").getValue(String::class.java)
//

//
//
//
//                if (alignmentList.size == 1){
//                    when (alignmentList[0]) {
//                        "Libertarian" -> iv_group_description_banner.setImageResource(R.drawable.libertarian_banner)
//                        "Authoritarian" -> iv_group_description_banner.setImageResource(R.drawable.authoritarian_banner)
//                        "Conservative" ->  iv_group_description_banner.setImageResource(R.drawable.conservative_banner)
//                        "Liberal" -> iv_group_description_banner.setImageResource(R.drawable.ic_liberal_banner)
//                        "Moderate" -> iv_group_description_banner.setImageResource(R.drawable.moderate_banner)
//                        else -> iv_group_description_banner.setImageResource(R.drawable.people_icon)
//                    }
//                }
//                else if (alignmentList.size == 2) {
//                    iv_group_description_2_banner.visibility = View.VISIBLE
//                    iv_group_description_banner_2_2.visibility = View.VISIBLE
//                    iv_group_description_plus.visibility = View.VISIBLE
//                    iv_group_description_banner_holder.visibility = View.VISIBLE
//
//                    iv_group_description_banner.visibility = View.INVISIBLE
//
//                    when (alignmentList[0]) {
//                        "Libertarian" -> iv_group_description_2_banner.setImageResource(R.drawable.libertarian_banner)
//                        "Authoritarian" -> iv_group_description_2_banner.setImageResource(R.drawable.authoritarian_banner)
//                        "Conservative" ->  iv_group_description_2_banner.setImageResource(R.drawable.conservative_banner)
//                        "Liberal" -> iv_group_description_2_banner.setImageResource(R.drawable.ic_liberal_banner)
//                        "Moderate" -> iv_group_description_2_banner.setImageResource(R.drawable.moderate_banner)
//                        else -> iv_group_description_2_banner.setImageResource(R.drawable.people_icon)
//                    }
//                    when (alignmentList[1]) {
//                        "Libertarian" -> iv_group_description_banner_2_2.setImageResource(R.drawable.libertarian_banner)
//                        "Authoritarian" -> iv_group_description_banner_2_2.setImageResource(R.drawable.authoritarian_banner)
//                        "Conservative" ->  iv_group_description_banner_2_2.setImageResource(R.drawable.conservative_banner)
//                        "Liberal" -> iv_group_description_banner_2_2.setImageResource(R.drawable.ic_liberal_banner)
//                        "Moderate" -> iv_group_description_banner_2_2.setImageResource(R.drawable.moderate_banner)
//                        else -> iv_group_description_banner_2_2.setImageResource(R.drawable.people_icon)
//                    }
//                }
//
//
//
//
//            }
//
//        })
}