package com.duncboi.realsquabble.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.registration.Registration
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.fragment_profile_settings.*
import kotlinx.android.synthetic.main.fragment_profile_settings.view.*

class profile_settings : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_profile_settings, container, false)

        val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("uid").equalTo(FirebaseAuth.getInstance().currentUser!!.uid)
        emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val anonymous = childSnapshot.child("anonymous").getValue<String>().toString()
                    if (anonymous == "ON") s_profile_settings_anonymous_switch.isChecked = true
                    else if (anonymous == "OFF") s_profile_settings_anonymous_switch.isChecked = false
                    else s_profile_settings_anonymous_switch.isChecked = false

                }}
            override fun onCancelled(error: DatabaseError) {} })

        view.iv_profile_settings_back_button.setOnClickListener {
            findNavController().navigate(R.id.action_profile_settings_to_default_profile)
        }

        view.s_profile_settings_anonymous_switch.setOnClickListener{
            anonymousLogic2()
        }

        view.tv_profile_settings_anonymous_mode.setOnClickListener {
            anonymousLogic()
        }

        view.b_profile_settings_logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            activity?.let { it1 ->
                GoogleSignIn.getClient(
                    it1,
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                ).signOut()
            }
            val intent = Intent(activity, Registration::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        return view
    }

    private fun anonymousLogic(){
        if(s_profile_settings_anonymous_switch.isChecked){
            s_profile_settings_anonymous_switch.isChecked = false
            val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("uid").equalTo(FirebaseAuth.getInstance().currentUser!!.uid)
            emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        childSnapshot.child("anonymous").ref.setValue("OFF")
                    }}
                override fun onCancelled(error: DatabaseError) {} })
        }
        else{
            s_profile_settings_anonymous_switch.isChecked = true
            val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("uid").equalTo(FirebaseAuth.getInstance().currentUser!!.uid)
            emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        childSnapshot.child("anonymous").ref.setValue("ON")
                    }}
                override fun onCancelled(error: DatabaseError) {} })
        }
    }
    private fun anonymousLogic2(){
        if(!s_profile_settings_anonymous_switch.isChecked){
            val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("uid").equalTo(FirebaseAuth.getInstance().currentUser!!.uid)
            emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        childSnapshot.child("anonymous").ref.setValue("OFF")
                    }}
                override fun onCancelled(error: DatabaseError) {} })
        }
        else{
            val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("uid").equalTo(FirebaseAuth.getInstance().currentUser!!.uid)
            emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        childSnapshot.child("anonymous").ref.setValue("ON")
                    }}
                override fun onCancelled(error: DatabaseError) {} })
        }
    }
}