package com.duncboi.realsquabble.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
    lateinit var usernamePassed:String
    lateinit var namePassed:String
    lateinit var bioPassed:String
    private var param1: String? = null
    private var param2: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_default_profile, container, false)
        view.b_edit_profile.setOnClickListener{
                val bundle = Bundle()
                bundle.putString("bio", bioPassed)
                bundle.putString("username", usernamePassed)
                bundle.putString("name", namePassed)
                findNavController().navigate(R.id.default_to_edit_profile, bundle)
        }

        view.iv_default_profile_settings.setOnClickListener {
            findNavController().navigate(R.id.action_default_profile_to_profile_settings)
        }
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if(currentUserUid != null){
        val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("uid").equalTo(currentUserUid)
        emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val username = childSnapshot.child("username").getValue<String>().toString()
                    val name = childSnapshot.child("name").getValue<String>().toString()
                    val bio = childSnapshot.child("bio").getValue<String>().toString()
                    val uriString = childSnapshot.child("uri").getValue<String>().toString()
                    if (uriString != "null"){
                    val uri = Uri.parse(uriString)
                    Picasso.get().load(uri).into(civ_default_profile_picture)
                    iv_profile_picture.alpha = 0f
                    tv_profile_picture_letter.alpha = 0f}
                    usernamePassed = username
                    namePassed = name
                    bioPassed = bio
                    val firstLetter = username[0]
                    tv_profile_picture_letter.text = "$firstLetter".split(' ').joinToString(" ") { it.capitalize() }
                    tv_default_profile_username.text = "@$username"
                    if (bio != "null") tv_default_profile_bio.text = "$bio"
                    else tv_default_profile_bio.text = ""
                    if (name != "null" && name != ""){
                        val firstLetter = name[0]
                        view.tv_profile_picture_letter.text = "$firstLetter".split(' ').joinToString(" ") { it.capitalize() }
                        view.tv_default_profile_name.text = "$name"}
                    else tv_default_profile_bio.text = ""
                }
            }
            override fun onCancelled(error: DatabaseError) {} })}

        //action bar


        return view
    }

}