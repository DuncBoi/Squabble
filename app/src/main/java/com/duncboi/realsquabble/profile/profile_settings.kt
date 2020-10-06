package com.duncboi.realsquabble.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.political.Political
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

        val reference = FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("anonymous")
        reference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val anonymous = snapshot.getValue<String>().toString()
                if (anonymous == "ON") s_profile_settings_anonymous_switch.isChecked = true
                else if (anonymous == "OFF") s_profile_settings_anonymous_switch.isChecked = false
                else s_profile_settings_anonymous_switch.isChecked = false
            }
        })


        view.iv_profile_settings_back_button.setOnClickListener {
            findNavController().navigate(R.id.action_profile_settings_to_default_profile)
        }

        view.s_profile_settings_anonymous_switch.setOnClickListener{
            anonymousLogic2()
        }

        view.tv_profile_settings_anonymous_mode.setOnClickListener {
            anonymousLogic()
        }

        view.b_settings_retake_quiz.setOnClickListener {
            val intent = Intent(activity, Political::class.java)
            startActivity(intent)
            activity?.finish()
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
        val anonRef  = FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("anonymous").ref
        if(s_profile_settings_anonymous_switch.isChecked){
            s_profile_settings_anonymous_switch.isChecked = false
            anonRef.setValue("OFF")
        }
        else{
            s_profile_settings_anonymous_switch.isChecked = true
            anonRef.setValue("ON")
        }
    }
    private fun anonymousLogic2(){
        val anonRef  = FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("anonymous").ref
        if(!s_profile_settings_anonymous_switch.isChecked){
            anonRef.setValue("OFF")
        }
        else{
            anonRef.setValue("ON")

        }
    }
}