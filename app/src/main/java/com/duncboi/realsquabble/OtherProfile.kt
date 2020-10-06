package com.duncboi.realsquabble

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.duncboi.realsquabble.messenger.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_other_profile.*

class OtherProfile : Fragment() {

    private var currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    val args: OtherProfileArgs by navArgs()
    val followingRef = currentUser?.uid.let { it1 ->
        FirebaseDatabase.getInstance().reference.child("Follow")
            .child(it1.toString()).child("Following")
    }
    var followingListener: ValueEventListener? = null

    override fun onPause() {
        super.onPause()
        if (followingListener != null) followingRef.removeEventListener(followingListener!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_other_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iv_other_profile_back.setOnClickListener {
            if (args.type == "dm"){
                val bundle = Bundle()
                bundle.putString("uid", "${args.uid}")
                findNavController().navigate(R.id.action_otherProfile_to_DM, bundle)
            }
            else if (args.type == "profile"){
                findNavController().navigate(R.id.action_otherProfile_to_directMessanger)
            }
        }

        iv_other_profile_following_holder.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("type", "profile")
            findNavController().navigate(R.id.action_otherProfile_to_directMessanger, bundle)
        }

        iv_other_profile_followers_holder.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("type", "profile")
            findNavController().navigate(R.id.action_otherProfile_to_directMessanger, bundle)
        }

        val uid = args.uid
        val reference = FirebaseDatabase.getInstance().reference.child("Users").child(uid)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: Users? = snapshot.getValue(Users::class.java)
                if (user != null) {
                    getFollowers(user, tv_other_profile_number_of_followers)
                    getFollowing(user, tv_other_profile_following_number)
                    if (user.getName() != "") {
                        tv_other_profile_name.text = user.getName()
                    }
                    if (user.getBio() != "") {
                        tv_other_profile_bio.text = user.getBio()
                    }
                    tv_other_profile_username.text = "@${user.getUsername()}"
                    when (user.getCategory()){
                        "Libertarian" -> iv_other_profile_banner.setImageResource(R.drawable.libertarian_banner)
                        "Liberal" -> iv_other_profile_banner.setImageResource(R.drawable.ic_liberal_banner)
                        "Conservative" -> iv_other_profile_banner.setImageResource(R.drawable.conservative_banner)
                        "Authoritarian" -> iv_other_profile_banner.setImageResource(R.drawable.authoritarian_banner)
                        "Moderate" -> iv_other_profile_banner.setImageResource(R.drawable.moderate_banner)
                    }

                    if (user.getUri() != "") {
                        Picasso.get().load(user.getUri()).placeholder(R.drawable.profile_icon)
                            .into(civ_other_profile_profile_picture)
                    }

                    checkFollowingStatus(user.getUid()!!, tv_other_profile_follow_text, iv_other_profile_follow_symbol, iv_other_profile_follow_holder)

                    iv_other_profile_follow_holder.setOnClickListener {
                        if (tv_other_profile_follow_text.text == "Follow"){
                            currentUser?.uid.let{ it1 ->
                                FirebaseDatabase.getInstance().reference.child("Follow").child(it1.toString()).child("Following").child(user.getUid().toString()).setValue(true).addOnCompleteListener {
                                    if (it.isSuccessful){
                                        currentUser?.uid.let{ it1 ->
                                            FirebaseDatabase.getInstance().reference.child("Follow").child(user.getUid().toString()).child("Followers").child(it1.toString()).setValue(true).addOnCompleteListener {
                                                if (it.isSuccessful){

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else{
                            currentUser?.uid.let{ it1 ->
                                FirebaseDatabase.getInstance().reference.child("Follow").child(it1.toString()).child("Following").child(user.getUid().toString()).removeValue().addOnCompleteListener {
                                    if (it.isSuccessful){
                                        currentUser?.uid.let{ it1 ->
                                            FirebaseDatabase.getInstance().reference.child("Follow").child(user.getUid().toString()).child("Followers").child(it1.toString()).removeValue().addOnCompleteListener {
                                                if (it.isSuccessful){

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        })
    }

    private fun checkFollowingStatus(
        uid: String,
        tvOtherProfileFollowText: TextView,
        ivOtherProfileFollowSymbol: ImageView,
        ivOtherProfileFollowHolder: ImageView
    ) {
        followingListener = followingRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("Moop", "$snapshot")
                if (snapshot.child(uid).exists()){
                    tvOtherProfileFollowText.text = "Following"
                    ivOtherProfileFollowSymbol.setImageResource(R.drawable.checkmark)
                }
                else{
                    tvOtherProfileFollowText.text = "Follow"
                    tvOtherProfileFollowText.setTextColor(Color.parseColor("#8a000000"))
                    ivOtherProfileFollowSymbol.setImageResource(R.drawable.follow_icon)
                }
            }

        })
    }

    private fun getFollowers(
        user: Users,
        tvOtherProfileNumberOfFollowers: TextView
    ) {

        val followersRef = FirebaseDatabase.getInstance().reference.child("Follow").child("${user.getUid()}").child("Followers")
        followersRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    tvOtherProfileNumberOfFollowers.text = snapshot.childrenCount.toString()
                }
            }

        })
    }

    private fun getFollowing(
        user: Users,
        tvOtherProfileFollowingNumber: TextView
    ) {

        val followersRef = FirebaseDatabase.getInstance().reference.child("Follow").child("${user.getUid()}").child("Following")
        followersRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    tvOtherProfileFollowingNumber.text = snapshot.childrenCount.toString()
                }
            }

        })
    }
}