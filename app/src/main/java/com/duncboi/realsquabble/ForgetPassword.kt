package com.duncboi.realsquabble

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_email.*
import kotlinx.android.synthetic.main.activity_email_verification.*
import kotlinx.android.synthetic.main.activity_forget_password.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.sending_email.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.lang.Exception

class ForgetPassword : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var onClickEmail: String = ""

    override fun onStop() {
        super.onStop()
        stopLiveEmailCheck = true
        stopOnClickEmailCheck = true
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        onEmailPassed()
        runLiveEmailCheck()
        defaultConstraint()
        auth = Firebase.auth

        b_fp_send_email.setOnClickListener {

            stopLiveEmailCheck = true
            stopOnClickEmailCheck = false
            val email = et_fp_email.text.toString().trim().toLowerCase()
            onClickEmail = email

            val emailQuery = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("email").equalTo(email)
            emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {

                    if(isEmailValid(email) && snapshot.childrenCount > 0){

                            val sendingEmailDialog = sendingEmailDialog()
                            sendingEmailDialog.show()

                            auth.sendPasswordResetEmail(email).addOnCompleteListener {

                                if (it.isSuccessful){
                                    sendingEmailDialog.dismiss()
                                    showEmailSentDialog(email)
                                }

                            }.addOnFailureListener {
                                sendingEmailDialog.dismiss()
                                errorConstraint()
                                tv_fp_error_message.text = it.message
                            }
                    }
                    else{
                        if (!isEmailValid(email)){
                            runOnClickEmailCheck()
                        }
                        else{
                            stopLiveEmailCheck = false
                            stopOnClickEmailCheck = true
                            runLiveEmailCheck()
                        }}
                }
            })
        }

        tv_fp_back_to_login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            val email = et_fp_email.text.toString().trim().toLowerCase()
            intent.putExtra("fpEmail", email)
            startActivity(intent)
            finish()
        }
    }

    fun isEmailValid(email: CharSequence?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private var stopOnClickEmailCheck = false
    private var stopLiveEmailCheck = false

    private fun runOnClickEmailCheck(){
        CoroutineScope(Dispatchers.Main).launch {
            onClickEmailCheckLogic()}
    }
    private fun runLiveEmailCheck(){
        CoroutineScope(IO).launch {
            liveEmailCheckLogic()}
    }

    //coroutine logic
    private suspend fun onClickEmailCheckLogic(){
        while (!stopOnClickEmailCheck){
            delay(200)
            val email = et_fp_email.text.toString().trim()
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
    private suspend fun liveEmailCheckLogic(){
        while(!stopLiveEmailCheck){
            delay(200)

            val email = et_fp_email.text.toString().trim()
            val lowerCaseEmail = email.toLowerCase()

            if (!isEmailValid(lowerCaseEmail)) defaultMainThread()
            else{
                val emailQuery = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("email").equalTo(lowerCaseEmail)
                emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        if(snapshot.childrenCount <= 0) { onEmailNotInDatabase() }
                        else onEmailInDatabase()

                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }}
    }

    private fun showEmailSentDialog(email: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Email Sent")
        builder.setCancelable(false)
        builder.setMessage("Check your inbox for a password reset link.  After you have reset it, you may use it to log in")
        builder.setPositiveButton("Ok") { _: DialogInterface?, _: Int ->
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("fpEmail", email)
            startActivity(intent)
        }
        builder.show()
    }

    private fun onEmailPassed() {
        val emailToReset = intent.getStringExtra("emailToReset")
        if (emailToReset != null) {
            et_fp_email.setText(emailToReset)
        }
    }

    private fun closeKeyboard(){
        val view = this.currentFocus
        if (view != null){
            val hideMe = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            hideMe.hideSoftInputFromWindow(view.windowToken, 0)
        }
        //else
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    //constraint functions
    private fun defaultConstraint(){
        val set = ConstraintSet()
        val fpLayout = fp_constraint
        et_fp_email.bringToFront()
        iv_fp_x.alpha = 0F
        iv_fp_checkmark.alpha = 0F
        set.clone(fpLayout)
        set.clear(tv_fp_error_message.id, ConstraintSet.TOP)
        set.connect(tv_fp_error_message.id, ConstraintSet.TOP,et_fp_email.id, ConstraintSet.TOP)
        set.connect(b_fp_send_email.id, ConstraintSet.TOP,et_fp_email.id, ConstraintSet.BOTTOM, 24)
        set.connect(tv_fp_back_to_login.id, ConstraintSet.TOP,b_fp_send_email.id, ConstraintSet.BOTTOM, 200)
        set.applyTo(fpLayout)
    }
    private fun errorConstraint(){
        val defaultSet = ConstraintSet()
        val fpLayout = fp_constraint
        defaultSet.clone(fpLayout)
        defaultSet.clear(b_fp_send_email.id, ConstraintSet.TOP)
        defaultSet.clear(tv_fp_error_message.id, ConstraintSet.TOP)
        defaultSet.connect(tv_fp_error_message.id, ConstraintSet.TOP, et_fp_email.id, ConstraintSet.BOTTOM, 6)
        defaultSet.connect(b_fp_send_email.id, ConstraintSet.TOP, tv_fp_error_message.id, ConstraintSet.BOTTOM, 12)
        defaultSet.applyTo(fpLayout)
    }

    private fun sendingEmailDialog(): androidx.appcompat.app.AlertDialog {
        val sendingEmail = androidx.appcompat.app.AlertDialog.Builder(this@ForgetPassword)
        val view = LayoutInflater.from(this).inflate(R.layout.sending_email, null)
        sendingEmail.setView(view)
        view.tv_sending_email.setText("Sending password reset email")
        sendingEmail.setCancelable(false)
        val sendingEmailDialog = sendingEmail.create()
        return sendingEmailDialog
    }
    private suspend fun defaultMainThread() {
        withContext(Main) {
            defaultConstraint()
        }
    }
    private fun onEmailInDatabase() {
        defaultConstraint()
        iv_fp_checkmark.bringToFront()
        iv_fp_checkmark.alpha = 1F
        iv_fp_x.alpha = 0F
    }
    private fun onIncorrectEmailFormat() {
        errorConstraint()
        iv_fp_x.bringToFront()
        iv_fp_x.alpha = 1F
        iv_fp_checkmark.alpha = 0F
        tv_fp_error_message.setText("Email formatted incorrectly")
        tv_fp_error_message.setTextColor(Color.parseColor("#eb4b4b"))
    }
    private fun onEmailEmpty() {
        errorConstraint()
        iv_fp_x.bringToFront()
        iv_fp_x.alpha = 1F
        iv_fp_checkmark.alpha = 0F
        tv_fp_error_message.setText("Please enter email")
        tv_fp_error_message.setTextColor(Color.parseColor("#eb4b4b"))
    }
    private fun onEmailNotInDatabase() {
        errorConstraint()
        iv_fp_x.bringToFront()
        iv_fp_x.alpha = 1F
        iv_fp_checkmark.alpha = 0f
        tv_fp_error_message.setText("User does not exist")
        tv_fp_error_message.setTextColor(Color.parseColor("#eb4b4b"))
    }
}