package com.duncboi.realsquabble.profile

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.duncboi.realsquabble.*
import com.duncboi.realsquabble.Adapters.PostAdapter
import com.duncboi.realsquabble.HolderClass.ActualTrending
import com.duncboi.realsquabble.HolderClass.Group
import com.duncboi.realsquabble.HolderClass.PartyGroup
import com.duncboi.realsquabble.HolderClass.VideoChat
import com.duncboi.realsquabble.notifications.Token
import com.duncboi.realsquabble.political.Political
import com.duncboi.realsquabble.registration.Registration
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId


class ProfileActivity : AppCompatActivity(), PostAdapter.onLoadMoreItemsListener {

    interface MyInterface {
        fun myAction()
    }

    override fun onPause() {
        super.onPause()
        status("OFFLINE")
    }

    override fun onResume() {
        super.onResume()
        status("ONLINE")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        val navView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        val navController = Navigation.findNavController(this@ProfileActivity, R.id.fragment)
        NavigationUI.setupWithNavController(navView, navController)
        updateToken(FirebaseInstanceId.getInstance().token, FirebaseAuth.getInstance().currentUser!!.uid)
        }

    private fun status(status: String){
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null){
            FirebaseDatabase.getInstance().reference.child("Users").child(user.uid).child("status").ref.setValue(
                "$status"
            )
        }
    }

    private fun updateToken(token: String?, currentUser: String){
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val token1 = Token(token!!)
        ref.child(currentUser).setValue(token1)
    }

    override fun onLoadMoreItems(nav: String){
        Log.d("MooseCock", "$nav")
        if (nav == "group") {
            setListener(Group())
            listener?.myAction()
        }
        else if (nav == "party"){
            setListener(PartyGroup())
            listener?.myAction()
        }
        else{
            setListener(ActualTrending())
            listener?.myAction()
        }
    }

    private var listener: MyInterface? = null

    private fun setListener(listener: MyInterface?) {
        this.listener = listener
    }
}