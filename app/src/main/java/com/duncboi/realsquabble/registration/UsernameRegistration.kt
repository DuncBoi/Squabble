package com.duncboi.realsquabble.registration

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.duncboi.realsquabble.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_username_registration.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

class UsernameRegistration : Fragment() {

    private val args: UsernameRegistrationArgs by navArgs()

    override fun onResume() {
        super.onResume()
        val username = args.username
        if (username != "null" && username != "username") {
            et_username_username.setText(username)
        }
        stopLiveUsernameCheck = false
        runLiveUsernameCheck()
    }

    override fun onPause() {
        super.onPause()
        closeKeyboard()
        stopLiveUsernameCheck = true
        stopOnClickUsernameCheck = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_username_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        defaultConstraint()

        b_username_next.setOnClickListener {
            stopLiveUsernameCheck = true
            val username = et_username_username.text.toString().trim()
            val lowerCaseUsername = username.toLowerCase()
            val usernameQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("username").equalTo(lowerCaseUsername)
            usernameQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.childrenCount <= 0 && lowerCaseUsername.isNotEmpty() && lowerCaseUsername.length <= 20){
                        val email = args.email
                        val bundle = Bundle()
                        val password = args.password
                        bundle.putString("password", password)
                        bundle.putString("email",email)
                        bundle.putString("username", lowerCaseUsername)
                        findNavController().navigate(R.id.action_usernameRegistration_to_emailRegistration, bundle)
                    }
                    else{
                        stopOnClickUsernameCheck = false
                        runOnClickUsernameCheck()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
        tv_username_previous.setOnClickListener {
            findNavController().navigate(R.id.action_usernameRegistration_to_first)
            closeKeyboard()
        }
    }

    //Constraint Layouts
    private fun defaultConstraint(){
        iv_username_checkmark.alpha = 0F
        iv_username_x.alpha = 0F
        val set = ConstraintSet()
        val usernameLayout = username_constraint
        set.clone(usernameLayout)
        set.clear(tv_username_username_taken.id, ConstraintSet.TOP)
        set.connect(tv_username_username_taken.id,
            ConstraintSet.TOP,et_username_username.id,
            ConstraintSet.TOP)
        set.connect(b_username_next.id,
            ConstraintSet.TOP,et_username_username.id,
            ConstraintSet.BOTTOM, 24)
        set.connect(tv_username_previous.id, ConstraintSet.TOP,b_username_next.id, ConstraintSet.BOTTOM, 200)
        set.applyTo(usernameLayout)
    }
    private fun errorConstraint(){
        val defaultSet = ConstraintSet()
        val usernameLayout = username_constraint
        defaultSet.clone(usernameLayout)
        defaultSet.clear(b_username_next.id, ConstraintSet.TOP)
        defaultSet.clear(tv_username_username_taken.id, ConstraintSet.TOP)
        defaultSet.connect(tv_username_username_taken.id, ConstraintSet.TOP, et_username_username.id, ConstraintSet.BOTTOM, 6)
        defaultSet.connect(b_username_next.id, ConstraintSet.TOP, tv_username_username_taken.id, ConstraintSet.BOTTOM, 12)
        defaultSet.applyTo(usernameLayout)
    }

    //Coroutine stop variables
    private var stopLiveUsernameCheck = false
    private var stopOnClickUsernameCheck = false

    //Coroutine Runner Functions
    private fun runLiveUsernameCheck(){
        CoroutineScope(IO).launch {
            usernameCheckLogic()}
    }

    private fun runOnClickUsernameCheck(){
        CoroutineScope(Dispatchers.Main).launch {
            onClickUsernameLogic()
        }}

    //Logic Functions
    private suspend fun onClickUsernameLogic(){
        stopLiveUsernameCheck = true
        while(!stopOnClickUsernameCheck) {
            delay(200)
            val username = et_username_username.text.toString().trim()
            if (username.isEmpty()) {
                onUsernameEmpty()
            } else {
                stopOnClickUsernameCheck = true
                stopLiveUsernameCheck = false
                runLiveUsernameCheck()
            }
        }
    }

    //Logic Functions
    private suspend fun usernameCheckLogic() {
        while (!stopLiveUsernameCheck) {

            delay(200)

            val username = et_username_username.text.toString().trim()
            val lowerCaseUsername = username.toLowerCase()

            when {
                lowerCaseUsername.isEmpty() -> {
                    defaultMainThread()
                }
                username.length > 20 -> {
                    withContext(Dispatchers.Main){
                        onUsernameTooLong()}
                }
                else -> {
                    val usernameQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("username").equalTo(lowerCaseUsername)
                    usernameQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.childrenCount > 0) {
                                onUsernameUnavailable(lowerCaseUsername)
                            } else {
                                onUsernameAvailable(lowerCaseUsername)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }
        }}

    private fun closeKeyboard(){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    //Error Functions
    private suspend fun defaultMainThread() {
        withContext(Dispatchers.Main) {
            b_username_next.alpha = 0.5f
            b_username_next.setBackgroundResource(R.drawable.greyed_out_button)
            defaultConstraint()
        }
    }
    private fun onUsernameTooLong(){
        errorConstraint()
        b_username_next.alpha = 0.5f
        b_username_next.setBackgroundResource(R.drawable.greyed_out_button)
        iv_username_checkmark.alpha = 0F
        iv_username_x.alpha = 1F
        tv_username_username_taken.setTextColor(Color.parseColor("#eb4b4b"))
        tv_username_username_taken.text = "Username length too long"
    }
    private fun onUsernameAvailable(username: String) {
        errorConstraint()
        b_username_next.alpha = 1.0f
        b_username_next.setBackgroundResource(R.drawable.rounded_button)
        iv_username_checkmark.alpha = 1F
        iv_username_x.alpha = 0F
        tv_username_username_taken.setTextColor(Color.parseColor("#38c96d"))
        tv_username_username_taken.text = "@$username is available"
    }
    private fun onUsernameUnavailable(username: String) {
        errorConstraint()
        b_username_next.alpha = 0.5f
        b_username_next.setBackgroundResource(R.drawable.greyed_out_button)
        iv_username_checkmark.alpha = 0F
        iv_username_x.alpha = 1F
        tv_username_username_taken.setTextColor(Color.parseColor("#eb4b4b"))
        tv_username_username_taken.text = "@$username is unavailable"
    }

    private fun onUsernameEmpty() {
        errorConstraint()
        b_username_next.alpha = 0.5f
        b_username_next.setBackgroundResource(R.drawable.greyed_out_button)
        iv_username_checkmark.alpha = 0F
        iv_username_x.alpha = 1F
        tv_username_username_taken.text = "Please enter username"
        tv_username_username_taken.setTextColor(Color.parseColor("#eb4b4b"))
    }
}
