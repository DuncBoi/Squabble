package com.duncboi.realsquabble.registration

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.duncboi.realsquabble.Miscellaneous.NetworkConnection
import com.duncboi.realsquabble.R


class Registration : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        val networkConnection = NetworkConnection(applicationContext)
        networkConnection.observe(this, Observer { isConnected ->
            if (!isConnected){
                hideKeyboard(this)
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Connection Lost")
                builder.setCancelable(false)
                builder.setMessage("You have lost connection to our servers, please connect to internet or restart Squabble and try again")
                builder.setPositiveButton("OK", DialogInterface.OnClickListener{ dialogInterface, i ->
                    dialogInterface.dismiss()
                })
                val dialog = builder.create()
                dialog.show()
            }
        })
       }
    override fun onBackPressed() {
        Log.d("Moose", "cow")
        val count = supportFragmentManager.backStackEntryCount
        if (count == 0) {
            super.onBackPressed()
            //additional code
        } else {
            supportFragmentManager.popBackStack()
        }
    }
    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}