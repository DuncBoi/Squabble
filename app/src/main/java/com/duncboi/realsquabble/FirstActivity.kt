package com.duncboi.realsquabble

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_first.*

class FirstActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        //action bar
        var actionBar = supportActionBar
        actionBar?.setTitle("Welcome")

        //login button
        b_first_login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        //register button
        b_first_register.setOnClickListener {
            val intent = Intent (this, Username::class.java)
            startActivity(intent)
        }
    }
}