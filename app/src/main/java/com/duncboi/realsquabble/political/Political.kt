package com.duncboi.realsquabble.political

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.duncboi.realsquabble.Constants
import com.duncboi.realsquabble.R

class Political : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_political)

        val firstFragment = PoliticalIntro()

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .replace(R.id.root_container, firstFragment)
            .commitAllowingStateLoss()

}}