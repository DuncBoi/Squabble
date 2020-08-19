package com.duncboi.realsquabble

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_email.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class Email : AppCompatActivity() {
    private var onClickEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email)

        defaultConstraint()
        runLiveEmailCheck()

        //On Click Listeners
        b_email_phonelink.setOnClickListener {
            val intent = Intent(this, PhoneAuth::class.java)
            startActivity(intent)
        }
        b_email_next.setOnClickListener {

            stopLiveEmailCheck = true
            val email = et_email_email.text.toString().trim()
            val lowerCaseEmail = email.toLowerCase()
            onClickEmail = email

            val emailQuery = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("email").equalTo(lowerCaseEmail)
            emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(isEmailValid(lowerCaseEmail) && snapshot.childrenCount <= 0) startNextActivity(lowerCaseEmail)
                    else{
                        if (!isEmailValid(lowerCaseEmail)){
                            stopOnClickEmailCheck = false
                            runOnClickEmailCheck()
            }
                        else{
                            stopLiveEmailCheck = false
                            runLiveEmailCheck()
                        }}
                    }
                   })
       }
        tv_email_previous.setOnClickListener {
            finish()
        }
    }

    //start next activity
    private fun startNextActivity(email: String) {
        val passwordStartIntent = Intent(this@Email, RegisterActivity::class.java)
        val username = intent.getStringExtra("username")
        passwordStartIntent.putExtra("username", username)
        passwordStartIntent.putExtra("email", email)
        startActivity(passwordStartIntent)
    }

    //random
    private fun closeKeyboard(){
        val view = this.currentFocus
        if (view != null){
            val hideMe = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            hideMe.hideSoftInputFromWindow(view.windowToken, 0)
        }
        //else
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }
    fun isEmailValid(email: CharSequence?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    //constraint functions
    private fun defaultConstraint(){
        val set = ConstraintSet()
        val emailLayout = email_constraint
        iv_email_x.alpha = 0F
        iv_email_checkmark.alpha = 0F
        set.clone(emailLayout)
        set.clear(tv_email_error.id, ConstraintSet.TOP)
        set.connect(tv_email_error.id, ConstraintSet.TOP,et_email_email.id, ConstraintSet.TOP)
        set.connect(b_email_next.id, ConstraintSet.TOP,et_email_email.id, ConstraintSet.BOTTOM, 24)
        set.connect(tv_email_previous.id, ConstraintSet.TOP,b_email_next.id, ConstraintSet.BOTTOM, 200)
        set.applyTo(emailLayout)
    }
    private fun errorConstraint(){
        val defaultSet = ConstraintSet()
        val emailLayout = email_constraint
        defaultSet.clone(emailLayout)
        defaultSet.clear(b_email_next.id, ConstraintSet.TOP)
        defaultSet.clear(tv_email_error.id, ConstraintSet.TOP)
        defaultSet.connect(tv_email_error.id, ConstraintSet.TOP, et_email_email.id, ConstraintSet.BOTTOM, 6)
        defaultSet.connect(b_email_next.id, ConstraintSet.TOP, tv_email_error.id, ConstraintSet.BOTTOM, 12)
        defaultSet.applyTo(emailLayout)
    }

    //stop functions
    private var stopOnClickEmailCheck = false
    private var stopLiveEmailCheck = false

    //runner functions
    private fun runOnClickEmailCheck(){
        CoroutineScope(Main).launch {
            onClickEmailCheckLogic()}
    }
    private fun runLiveEmailCheck(){
        CoroutineScope(Main).launch {
            liveEmailCheck()}
    }
    private suspend fun liveEmailCheck() {
        withContext(IO){
            liveEmailCheckLogic()
        }
    }

    //logic functions
    private suspend fun liveEmailCheckLogic(){
        while(!stopLiveEmailCheck){
            delay(200)

            val email = et_email_email.text.toString().trim()
            val lowerCaseEmail = email.toLowerCase()

            if (!isEmailValid(lowerCaseEmail)) defaultMainThread()
            else{
            val emailQuery = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("email").equalTo(lowerCaseEmail)
            emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if(snapshot.childrenCount > 0) { onEmailAlreadyExists() }
                    else onEmailDoesntExist()

                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }}
    }
    private suspend fun onClickEmailCheckLogic(){
        while (!stopOnClickEmailCheck){
            delay(200)
            val email = et_email_email.text.toString().trim()
            if (!isEmailValid(email)){
                if(email.isEmpty()) onEmailEmpty()
                else {
                    //MAKES ERROR MESSAGE SAY EMAIL FORMATTED INCORRECTLY ONLY WHEN HITTING NEXT FOR THE FIRST TIME JUST ERASE THE IF STATEMENT TO CHANGE BACK (KEEP THE ELSE CONTENT BUT DELETE ELSE())
                    if(email != onClickEmail){
                        onClickEmail = "*"
                        defaultConstraint()
                    }
                    else onIncorrectEmailFormat()
                }
            }
            else{
                stopOnClickEmailCheck = true
                stopLiveEmailCheck = false
                runLiveEmailCheck()
            }
        }
    }

    //error functions
    private suspend fun defaultMainThread() {
        withContext(Main) {
            defaultConstraint()
        }
    }
    private fun onEmailDoesntExist() {
        defaultConstraint()
        iv_email_checkmark.bringToFront()
        iv_email_checkmark.alpha = 1F
        iv_email_x.alpha = 0F
    }
    private fun onIncorrectEmailFormat() {
        errorConstraint()
        iv_email_x.bringToFront()
        iv_email_x.alpha = 1F
        iv_email_checkmark.alpha = 0F
        tv_email_error.setText("Email formatted incorrectly")
        tv_email_error.setTextColor(Color.parseColor("#eb4b4b"))
    }
    private fun onEmailEmpty() {
        errorConstraint()
        iv_email_x.bringToFront()
        iv_email_x.alpha = 1F
        iv_email_checkmark.alpha = 0F
        tv_email_error.setText("Please enter email")
        tv_email_error.setTextColor(Color.parseColor("#eb4b4b"))
    }
    private fun onEmailAlreadyExists() {
        errorConstraint()
        iv_email_x.bringToFront()
        iv_email_x.alpha = 1F
        iv_email_checkmark.alpha = 0f
        tv_email_error.setText("Email linked to an existing account")
        tv_email_error.setTextColor(Color.parseColor("#eb4b4b"))
    }

}