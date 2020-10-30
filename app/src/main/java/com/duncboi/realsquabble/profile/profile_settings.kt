package com.duncboi.realsquabble.profile

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import kotlinx.android.synthetic.main.fragment_change_password.view.*
import kotlinx.android.synthetic.main.fragment_profile_settings.*
import kotlinx.android.synthetic.main.fragment_profile_settings.view.*

class profile_settings : Fragment() {

    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_profile_settings, container, false)

        view.b_profile_settings_link.setOnClickListener {
            findNavController().navigate(R.id.action_profile_settings_to_link)
        }

        view.b_profile_settings_change_password.setOnClickListener {
            findNavController().navigate(R.id.action_profile_settings_to_change_password)
        }

        val reference = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser).child("anonymous")
        reference.onDisconnect().cancel()
        reference.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val anonymous = snapshot.getValue<String>().toString()
                if (anonymous == "ON") s_profile_settings_anonymous_switch.isChecked = true
                else if (anonymous == "OFF") s_profile_settings_anonymous_switch.isChecked = false
                else s_profile_settings_anonymous_switch.isChecked = false
            }
        })

        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser)
        userRef.onDisconnect().cancel()
            userRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val timeChanged = snapshot.child("alignmentTime").getValue(String::class.java)
                if (timeChanged != null && timeChanged != "") {
                    val timePosted = timeChanged.toLong()
                    val currentTime = System.currentTimeMillis()
                    val weekMillis = 1209600000
                    val millisElapsed = currentTime - timePosted
                    val timeLeft = weekMillis - millisElapsed
                    Log.d("prof", "$timeLeft")
                    if (timeLeft > 0) {
                        b_settings_retake_quiz.setBackgroundResource(R.drawable.error_edittext_register_login)
                        b_settings_retake_quiz.isClickable = false
                        tv_settings_alignment_time_left_text.visibility = View.VISIBLE
                        tv_settings_alignment_time_left.visibility = View.VISIBLE
                        val minutesElapsed = timeLeft / 60000
                        val hoursElapsed = minutesElapsed / 60
                        val daysElapsed = hoursElapsed / 24
                        val weeksElapsed = daysElapsed / 7
                        val monthsElapsed = weeksElapsed / 4
                        val yearsElapsed = monthsElapsed / 12
                        if (yearsElapsed > 0) tv_settings_alignment_time_left.text =
                            "(${yearsElapsed}y)"
                        else if (monthsElapsed > 0) tv_settings_alignment_time_left.text =
                            "(${monthsElapsed}mo)"
                        else if (daysElapsed > 0) tv_settings_alignment_time_left.text =
                            "(${daysElapsed}d)"
                        else if (hoursElapsed > 0) tv_settings_alignment_time_left.text =
                            "(${hoursElapsed}h)"
                        else if (minutesElapsed > 0) tv_settings_alignment_time_left.text =
                            "(${minutesElapsed}m)"
                        else tv_settings_alignment_time_left.text = "(Under a minute)"
                    }
                    else{
                        b_settings_retake_quiz.isClickable = true
                        view.b_settings_retake_quiz.setOnClickListener {
                            val intent = Intent(activity, Political::class.java)
                            startActivity(intent)
                            activity?.finish()
                        }
                    }
                }
                else { b_settings_retake_quiz.isClickable = true
                view.b_settings_retake_quiz.setOnClickListener {
                    val intent = Intent(activity, Political::class.java)
                    startActivity(intent)
                    activity?.finish()
                }
                }
            }
        }
        )
        view.iv_profile_settings_back_button.setOnClickListener {
            findNavController().popBackStack()
        }

        view.s_profile_settings_anonymous_switch.setOnClickListener{
            anonymousLogic2()
        }

        view.tv_profile_settings_anonymous_mode.setOnClickListener {
            anonymousLogic()
        }

        view.b_profile_settings_logout.setOnClickListener {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("Logout?")
            builder.setMessage("Are you sure you want to logout of this account?")
            builder.setCancelable(false)
            builder.setPositiveButton("Logout", DialogInterface.OnClickListener { dialogInterface, i ->
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
                Toast.makeText(activity, "Logout successful", Toast.LENGTH_SHORT).show()
            })
            builder.setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.cancel()
            })
            builder.create().show()
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