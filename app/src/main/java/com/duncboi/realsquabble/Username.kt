package com.duncboi.realsquabble

import android.app.Dialog
import android.app.PendingIntent.getActivity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_username.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Username : AppCompatActivity() {

    override fun onStart() {
        super.onStart()
        stopLiveUsernameCheck = false
        runLiveUsernameCheck()}

    override fun onStop() {
        super.onStop()
        stopOnClickUsernameCheck = true
        stopLiveUsernameCheck = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_username)

        lifecycle.addObserver(LifecycleObserver())
        defaultConstraint()

        b_username_next.setOnClickListener {
            val username = et_username_username.text.toString().trim()
            val lowerCaseUsername = username.toLowerCase()
            val usernameQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("username").equalTo(lowerCaseUsername)
            usernameQuery.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.childrenCount <= 0 && lowerCaseUsername.isNotEmpty() && lowerCaseUsername.length <= 20){
                        startNextActivity(lowerCaseUsername)
                    }
                    else{
                        stopOnClickUsernameCheck = false
                        runOnClickUsernameCheck()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
           })
        }
        tv_username_previous.setOnClickListener {
            finish()
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
        set.connect(tv_username_username_taken.id,ConstraintSet.TOP,et_username_username.id,ConstraintSet.TOP)
        set.connect(b_username_next.id,ConstraintSet.TOP,et_username_username.id,ConstraintSet.BOTTOM, 24)
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

    //Start Next Activity
    private fun startNextActivity(username: String) {
        val intent = Intent(this@Username, Email::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
    }

    //Coroutine stop variables
    private var stopLiveUsernameCheck = false
    private var stopOnClickUsernameCheck = false

    //Coroutine Runner Functions
    private fun runLiveUsernameCheck(){
        CoroutineScope(Main).launch {
            liveUsernameCheck()}
    }
    private suspend fun liveUsernameCheck() {
        withContext(IO){
            usernameCheckLogic()
        }
    }
    private fun runOnClickUsernameCheck(){
        CoroutineScope(Main).launch {
            onClickUsernameLogic()
        }}

    //Logic Functions
    private suspend fun onClickUsernameLogic(){
        stopLiveUsernameCheck = true
        while(!stopOnClickUsernameCheck) {
            delay(100)
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
    private suspend fun usernameCheckLogic() {
        while (!stopLiveUsernameCheck) {

            delay(10)

            val username = et_username_username.text.toString().trim()
            val lowerCaseUsername = username.toLowerCase()

            if(lowerCaseUsername.isEmpty()) {
                defaultMainThread()
            }
            else if (username.length > 20){
                withContext(Main){
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
        withContext(Main) {
            defaultConstraint()
        }
    }
    private fun onUsernameTooLong(){
        errorConstraint()
        iv_username_checkmark.alpha = 0F
        iv_username_x.alpha = 1F
        tv_username_username_taken.setTextColor(Color.parseColor("#eb4b4b"))
        tv_username_username_taken.text = "Username length too long"
    }
    private fun onUsernameAvailable(username: String) {
        errorConstraint()
        iv_username_checkmark.alpha = 1F
        iv_username_x.alpha = 0F
        tv_username_username_taken.setTextColor(Color.parseColor("#38c96d"))
        tv_username_username_taken.text = "@$username is available"
    }
    private fun onUsernameUnavailable(username: String) {
        iv_username_checkmark.alpha = 0F
        iv_username_x.alpha = 1F
        tv_username_username_taken.setTextColor(Color.parseColor("#eb4b4b"))
        tv_username_username_taken.text = "@$username is unavailable"
    }
    private fun onUsernameEmpty() {
        errorConstraint()
        iv_username_checkmark.alpha = 0F
        iv_username_x.alpha = 1F
        tv_username_username_taken.text = "Please enter username"
        tv_username_username_taken.setTextColor(Color.parseColor("#eb4b4b"))
    }

    }
