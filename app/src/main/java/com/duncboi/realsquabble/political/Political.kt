package com.duncboi.realsquabble.political

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.duncboi.realsquabble.Constants
import com.duncboi.realsquabble.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Political : AppCompatActivity() {

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
        setContentView(R.layout.activity_political)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        val firstFragment = PoliticalIntro()

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .replace(R.id.root_container, firstFragment)
            .commitAllowingStateLoss()

}

    private fun status(status: String){
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null){
            FirebaseDatabase.getInstance().reference.child("Users").child(user.uid).child("status").ref.setValue("$status")
        }
    }
}