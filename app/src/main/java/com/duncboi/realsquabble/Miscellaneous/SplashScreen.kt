package com.duncboi.realsquabble.Miscellaneous

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.political.Political
import com.duncboi.realsquabble.profile.ProfileActivity
import com.duncboi.realsquabble.registration.Registration
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId

class SplashScreen : AppCompatActivity() {

    lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        handler = Handler(Looper.getMainLooper())

        handler.postDelayed({

            verifyUserIsLoggedIn()

        }, 2000)
    }

    private fun verifyUserIsLoggedIn() {
        val user = FirebaseAuth.getInstance().currentUser
        // if user is not email verified and user is not null then delete them from auth
        if(user != null){
            val categoryRef = FirebaseDatabase.getInstance().reference.child("Users").child(user.uid)
            categoryRef.onDisconnect().cancel()
                categoryRef.addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val category = snapshot.child("category").getValue(String::class.java)
                        if (category == null || category == "") {
                            val intent = Intent(this@SplashScreen, Political::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        }
                        else {
                            val intent = Intent(this@SplashScreen, ProfileActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                            }
                    }
                })
        }
        else{
            val intent = Intent(this, Registration::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }


}