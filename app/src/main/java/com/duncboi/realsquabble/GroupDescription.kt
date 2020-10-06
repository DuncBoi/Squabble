package com.duncboi.realsquabble

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_default_profile.*
import kotlinx.android.synthetic.main.fragment_group_description.*

class GroupDescription : Fragment() {

    private val args: GroupDescriptionArgs by navArgs()
    val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    private val groupChatList = FirebaseDatabase.getInstance().reference.child("Group Chat List")
    private val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser)
    private var groupChatListener: ValueEventListener? = null

    var alignmentList = ArrayList<String>()

    override fun onPause() {
        super.onPause()
        if (groupChatListener != null) groupChatList.child(args.groupId).removeEventListener(groupChatListener!!)
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


        groupChatListener = groupChatList.child(args.groupId).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {

                val group = snapshot.getValue(Groups::class.java)

                val location = snapshot.child("filter").child("location").getValue(String::class.java)

                for (i in snapshot.child("filter").child("alignment").children) {
                    val alignment = i.getValue(String::class.java)
                    if (alignment != null) {
                        alignmentList.add(alignment)
                    }
                }

                b_group_description_join_group.setOnClickListener {
                    if (group != null) {
                        userRef.child("groupId").setValue(group.getChatId())
                    }
                    snapshot.child("members").child("uid").ref.setValue(currentUser)
                    Toast.makeText(activity, "Joined Group", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_groupDescription_to_defaultMessenger)
                }

                for (i in snapshot.child("members").children){
                    val member = i.getValue(String::class.java)
                    if (member == currentUser){
                        b_group_description_join_group.text = "Leave"
                        b_group_description_join_group.setBackgroundResource(R.drawable.rounded_button_red)
                        b_group_description_join_group.setOnClickListener {
                            val builder = AlertDialog.Builder(activity)
                            builder.setCancelable(false)
                            builder.setTitle("Leave Group?")
                            builder.setMessage("Are you sure you want to leave the group")
                            builder.setPositiveButton("Yes", DialogInterface.OnClickListener{ _: DialogInterface, _: Int ->
                                i.ref.removeValue()
                                userRef.child("groupId").removeValue()
                                Toast.makeText(activity, "Left Group", Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.action_groupDescription_to_findGroup)
                            })
                            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener{ dialogInterface: DialogInterface, _: Int ->
                                dialogInterface.cancel()
                            })
                            builder.create().show()
                        }
                    }
                }


                tv_group_description_name.text = group!!.getName()
                tv_group_description_location.text = location
                if (group.getUri() != "") Picasso.get().load(group.getUri())
                    .into(civ_group_description)

                if (alignmentList.size == 1){
                    when (alignmentList[0]) {
                        "Libertarian" -> iv_group_description_banner.setImageResource(R.drawable.libertarian_banner)
                        "Authoritarian" -> iv_group_description_banner.setImageResource(R.drawable.authoritarian_banner)
                        "Conservative" ->  iv_group_description_banner.setImageResource(R.drawable.conservative_banner)
                        "Liberal" -> iv_group_description_banner.setImageResource(R.drawable.ic_liberal_banner)
                        "Moderate" -> iv_group_description_banner.setImageResource(R.drawable.moderate_banner)
                        else -> iv_group_description_banner.setImageResource(R.drawable.people_icon)
                    }
                }
                else if (alignmentList.size == 2) {
                    iv_group_description_2_banner.visibility = View.VISIBLE
                    iv_group_description_banner_2_2.visibility = View.VISIBLE
                    iv_group_description_plus.visibility = View.VISIBLE
                    iv_group_description_banner_holder.visibility = View.VISIBLE

                    iv_group_description_banner.visibility = View.INVISIBLE

                    when (alignmentList[0]) {
                        "Libertarian" -> iv_group_description_2_banner.setImageResource(R.drawable.libertarian_banner)
                        "Authoritarian" -> iv_group_description_2_banner.setImageResource(R.drawable.authoritarian_banner)
                        "Conservative" ->  iv_group_description_2_banner.setImageResource(R.drawable.conservative_banner)
                        "Liberal" -> iv_group_description_2_banner.setImageResource(R.drawable.ic_liberal_banner)
                        "Moderate" -> iv_group_description_2_banner.setImageResource(R.drawable.moderate_banner)
                        else -> iv_group_description_2_banner.setImageResource(R.drawable.people_icon)
                    }
                    when (alignmentList[1]) {
                        "Libertarian" -> iv_group_description_banner_2_2.setImageResource(R.drawable.libertarian_banner)
                        "Authoritarian" -> iv_group_description_banner_2_2.setImageResource(R.drawable.authoritarian_banner)
                        "Conservative" ->  iv_group_description_banner_2_2.setImageResource(R.drawable.conservative_banner)
                        "Liberal" -> iv_group_description_banner_2_2.setImageResource(R.drawable.ic_liberal_banner)
                        "Moderate" -> iv_group_description_banner_2_2.setImageResource(R.drawable.moderate_banner)
                        else -> iv_group_description_banner_2_2.setImageResource(R.drawable.people_icon)
                    }
                }




            }

        })
    }


}