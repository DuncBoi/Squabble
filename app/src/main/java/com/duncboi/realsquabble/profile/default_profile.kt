package com.duncboi.realsquabble.profile

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.duncboi.realsquabble.Constants
import com.duncboi.realsquabble.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_default_profile.*
import kotlinx.android.synthetic.main.fragment_default_profile.view.*


class default_profile : Fragment() {

    private var usernamePassed: String? = null
    private var namePassed: String? = null
    private var bioPassed: String? = null

    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

    private var listener: ValueEventListener? = null
    private var listener2: ValueEventListener? = null
    private var listener3: ValueEventListener? = null

    private val followersRef = FirebaseDatabase.getInstance().reference.child("Follow").child("$currentUserUid").child("Following")
    private val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("uid").equalTo(currentUserUid)
    private val followingRef = FirebaseDatabase.getInstance().reference.child("Follow").child("$currentUserUid").child("Followers")

    override fun onPause() {
        super.onPause()
        if (listener != null) emailQuery.removeEventListener(listener!!)
        if (listener2 != null) followersRef.removeEventListener(listener2!!)
        if (listener3 != null) followingRef.removeEventListener(listener3!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_default_profile, container, false)

        val poooo = Constants.wrap_number(1084.0)
        Log.d("cow", poooo)

        view.iv_default_profile_followers_holder.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("type", "profile")
            findNavController().navigate(R.id.action_default_profile_to_directMessanger, bundle)
        }

        view.iv_default_profile_following_holder.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("type", "profile")
            findNavController().navigate(R.id.action_default_profile_to_directMessanger, bundle)
        }

        view.iv_edit_profile.setOnClickListener{
                val bundle = Bundle()
                bundle.putString("bio", bioPassed)
                bundle.putString("username", usernamePassed)
                bundle.putString("name", namePassed)
                findNavController().navigate(R.id.default_to_edit_profile, bundle)
        }

        view.iv_default_profile_settings.setOnClickListener {
            findNavController().navigate(R.id.action_default_profile_to_profile_settings)
        }
        if(currentUserUid != null){
            getFollowers()
            getFollowing()
            listener = emailQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val username = childSnapshot.child("username").getValue<String>().toString()
                    val name = childSnapshot.child("name").getValue<String>().toString()
                    val bio = childSnapshot.child("bio").getValue<String>().toString()
                    val uriString = childSnapshot.child("uri").getValue<String>().toString()
                    val category = childSnapshot.child("category").getValue<String>().toString()

                    when (category) {
                        "Libertarian" -> {
                            iv_default_profile_banner.setImageResource(R.drawable.libertarian_banner)
                        }
                        "Authoritarian" -> {
                            iv_default_profile_banner.setImageResource(R.drawable.authoritarian_banner)
                        }
                        "Conservative" -> {
                            iv_default_profile_banner.setImageResource(R.drawable.conservative_banner)
                        }
                        "Liberal" -> {
                            iv_default_profile_banner.setImageResource(R.drawable.ic_liberal_banner)
                        }
                        else -> {
                            iv_default_profile_banner.setImageResource(R.drawable.moderate_banner)
                        }
                    }

                    val uri = Uri.parse(uriString)
                    Picasso.get().load(uri).placeholder(R.drawable.profile_icon).into(civ_default_profile_picture)

                    usernamePassed = username
                    namePassed = name
                    bioPassed = bio
                    tv_default_profile_username.text = "@$username"
                    if (bio != "") tv_default_profile_bio.text = "$bio"
                    else tv_default_profile_bio.text = ""
                    if (name != ""){
                        view.tv_default_profile_name.text = "$name"}
                    else tv_default_profile_bio.text = ""
                }
            }
            override fun onCancelled(error: DatabaseError) {} })}

        //action bar


        return view
    }

    private fun getFollowers(){
        listener2 = followersRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    view?.tv_number_of_followers?.text = snapshot.childrenCount.toString()
                }
            }

        })
    }

    private fun getFollowing(){
        listener3 = followingRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    view?.tv_number_following?.text = snapshot.childrenCount.toString()
                }
            }

        })
    }

}