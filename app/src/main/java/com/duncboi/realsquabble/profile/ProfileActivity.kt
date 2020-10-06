package com.duncboi.realsquabble.profile

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.registration.Registration
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : AppCompatActivity() {

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
        verifyUserIsLoggedIn()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        val navView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        val navController = Navigation.findNavController(this, R.id.fragment)

        NavigationUI.setupWithNavController(navView, navController)

        }


    //user stays logged in after app restart
    private fun verifyUserIsLoggedIn() {
        val user = FirebaseAuth.getInstance().currentUser
        // if user is not email verified and user is not null then delete them from auth
        if(user != null){
            if (user.phoneNumber != null){
            }
            else if (!user.isEmailVerified){
                FirebaseAuth.getInstance().currentUser!!.delete()
                val intent = Intent(this, Registration::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
        }
        }
        else{
            val intent = Intent(this, Registration::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun status(status: String){
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null){
            FirebaseDatabase.getInstance().reference.child("Users").child(user.uid).child("status").ref.setValue("$status")
        }
    }

}