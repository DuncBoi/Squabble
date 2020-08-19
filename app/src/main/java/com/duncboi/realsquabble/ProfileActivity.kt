package com.duncboi.realsquabble

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_profile.*


class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        verifyUserIsLoggedIn()


        //action bar
        var actionBar = supportActionBar
        actionBar?.setTitle("Squabble")
        actionBar?.hide()

        //logout button clicked
        b_logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, FirstActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()

        }
    }

    //user stays logged in after app restart
    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        val user = FirebaseAuth.getInstance().currentUser
        // if user is not email verified and user is not null then delete them from auth
        if(user != null){
            if(!user!!.isEmailVerified){
                FirebaseAuth.getInstance().currentUser!!.delete()
                val intent = Intent(this, FirstActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
        }
            else {

        }}
        else{
            val intent = Intent(this, FirstActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

}