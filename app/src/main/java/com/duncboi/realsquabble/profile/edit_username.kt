package com.duncboi.realsquabble.profile

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import kotlinx.android.synthetic.main.fragment_edit_username.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main


class edit_username : Fragment() {

    val args: edit_usernameArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_username, container, false)

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        et_change_username_username.setText(args.username)

        val bio = args.bio
        val name = args.name

        tv_edit_username_done.setOnClickListener {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
            stopLiveUsernameCheck = true
            val username = et_change_username_username.text.toString().trim()
            val bundle = Bundle()
            bundle.putString("bio", bio)
            Log.d("Moose", "$name")
            bundle.putString("username", username)
            bundle.putString("name", name)
            findNavController().navigate(R.id.edit_username_to_editProfile, bundle)}

        iv_change_username_back_button.setOnClickListener{
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
            stopLiveUsernameCheck = true
            val bundle = Bundle()
            bundle.putString("bio", bio)
            bundle.putString("username", args.username)
            bundle.putString("name", name)
            findNavController().navigate(R.id.edit_username_to_editProfile, bundle)}

        defaultConstraint()
        runLiveUsernameCheck()
    }

    private fun defaultConstraint(){
        tv_edit_username_done.text = ""
        tv_edit_username_done.isEnabled = false
        et_change_username_username.bringToFront()
        et_change_username_username.setCompoundDrawablesWithIntrinsicBounds(0, 0,
            R.drawable.pen, 0)
        val set = ConstraintSet()
        val usernameLayout = edit_username_constraint
        set.clone(usernameLayout)
        set.clear(tv_change_username_error_message.id, ConstraintSet.TOP)
        set.connect(tv_change_username_error_message.id,ConstraintSet.TOP,et_change_username_username.id,ConstraintSet.TOP)
        set.connect(tv_change_username_subtext.id,ConstraintSet.TOP,et_change_username_username.id,ConstraintSet.BOTTOM, 24)
        set.applyTo(usernameLayout)
    }
    private fun errorConstraint(){
        tv_edit_username_done.text = ""
        tv_edit_username_done.isEnabled = false
        val defaultSet = ConstraintSet()
        val usernameLayout = edit_username_constraint
        defaultSet.clone(usernameLayout)
        defaultSet.clear(tv_change_username_subtext.id, ConstraintSet.TOP)
        defaultSet.clear(tv_change_username_error_message.id, ConstraintSet.TOP)
        defaultSet.connect(tv_change_username_error_message.id, ConstraintSet.TOP, et_change_username_username.id, ConstraintSet.BOTTOM, 6)
        defaultSet.connect(tv_change_username_subtext.id, ConstraintSet.TOP, tv_change_username_error_message.id, ConstraintSet.BOTTOM, 12)
        defaultSet.applyTo(usernameLayout)
    }

    //Start Next Activity

    //Coroutine stop variables
    private var stopLiveUsernameCheck = false

    //Coroutine Runner Functions
    private fun runLiveUsernameCheck(){
        CoroutineScope(Dispatchers.Main).launch {
            liveUsernameCheck()}
    }
    private suspend fun liveUsernameCheck() {
        withContext(Dispatchers.IO){
            usernameCheckLogic()
        }
    }

    private suspend fun usernameCheckLogic() {
        while (!stopLiveUsernameCheck) {

            delay(200)

            val username = et_change_username_username.text.toString().trim()
            val lowerCaseUsername = username.toLowerCase()

            if(lowerCaseUsername == args.username){
                withContext(Main){
                onUsernameAvailable(lowerCaseUsername)}
            }
            else if(lowerCaseUsername.isEmpty()) {
                defaultMainThread()
            }
            else if (username.length > 20){
                withContext(Dispatchers.Main){
                    onUsernameTooLong()}
            }
            else{
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
            }}}

    //Error Functions
    private suspend fun defaultMainThread() {
        withContext(Dispatchers.Main) {
            defaultConstraint()
        }
    }
    private fun onUsernameTooLong(){
        errorConstraint()
        et_change_username_username.setCompoundDrawablesWithIntrinsicBounds(0, 0,
            R.drawable.x, 0)
        tv_change_username_error_message.setTextColor(Color.parseColor("#eb4b4b"))
        tv_change_username_error_message.text = "Username length too long"
    }
    private fun onUsernameAvailable(username: String) {
        errorConstraint()
        tv_edit_username_done.text = "Done"
        tv_edit_username_done.isEnabled = true
        et_change_username_username.setCompoundDrawablesWithIntrinsicBounds(0, 0,
            R.drawable.checkmark, 0)
        tv_change_username_error_message.setTextColor(Color.parseColor("#38c96d"))
        tv_change_username_error_message.text = "@$username is available"
    }
    private fun onUsernameUnavailable(username: String) {
        errorConstraint()
        et_change_username_username.setCompoundDrawablesWithIntrinsicBounds(0, 0,
            R.drawable.x, 0)
        tv_change_username_error_message.setTextColor(Color.parseColor("#eb4b4b"))
        tv_change_username_error_message.text = "@$username is unavailable"
    }
    private fun onUsernameEmpty() {
        errorConstraint()
        et_change_username_username.setCompoundDrawablesWithIntrinsicBounds(0, 0,
            R.drawable.x, 0)
        tv_change_username_error_message.text = "Please enter username"
        tv_change_username_error_message.setTextColor(Color.parseColor("#eb4b4b"))
    }
    }