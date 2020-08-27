package com.duncboi.realsquabble

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_email_verification.*
import kotlinx.android.synthetic.main.waiting_for_verification.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.lang.Exception

class EmailVerification : AppCompatActivity() {

    override fun onStop() {
        stopOnClickEmailCheck = true
        stopLiveEmailCheck = true
        stopEmailVerificationWaiter = true

        val user = FirebaseAuth.getInstance().currentUser
        super.onStop()
        if(user != null){
            if(!user.isEmailVerified){
                user.delete()
        }
    }
            }

    override fun onStart() {
        super.onStart()
        stopLiveEmailCheck = false
        runLiveEmailCheck()
    }

    private var onClickEmail: String = ""
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_verification)

        defaultConstraint()
        auth = Firebase.auth

        val email = intent.getStringExtra("emailPassed")
        val password = intent.getStringExtra("passwordPassed")
        et_email_verification_email.setText(email)

        b_email_verification_send.setOnClickListener {

            stopLiveEmailCheck = true
            stopOnClickEmailCheck = false
            val email = et_email_verification_email.text.toString().trim()
            onClickEmail = email

            val emailQuery = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("email").equalTo(email)
            emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {

                    if(isEmailValid(email) && snapshot.childrenCount <= 0){

                        if (password != null) {

                            val sendingEmailDialog = sendingEmailDialog()
                            sendingEmailDialog.show()

                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener {

                                if (it.isSuccessful){
                                    sendEmailVerification(sendingEmailDialog)
                                }

                            }.addOnFailureListener {
                                sendingEmailDialog.dismiss()
                                onCreateUserFailed(it)
                            }
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
            })}
        tv_email_verification_previous.setOnClickListener {
            val username = intent.getStringExtra("usernamePassed")
            val passwordIntent = Intent(this, RegisterActivity::class.java)
            passwordIntent.putExtra("usernamePassed", username)
            finish()
        }

    }

    //dialog functions
    private fun sendingEmailDialog(): AlertDialog {
        val sendingEmail = AlertDialog.Builder(this@EmailVerification)
        sendingEmail.setView(R.layout.sending_email)
        sendingEmail.setCancelable(false)
        val sendingEmailDialog = sendingEmail.create()
        return sendingEmailDialog
    }
    private fun waitingForVerificationDialog(): AlertDialog {
        closeKeyboard()
        val waitingForVerificationBuilder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.waiting_for_verification, null)
        waitingForVerificationBuilder.setView(view)
        waitingForVerificationBuilder.setCancelable(false)
        val waitingForVerificationDialog = waitingForVerificationBuilder.create()
        view.b_waiting_for_verification_cancel.setOnClickListener {
            stopOnClickEmailCheck = false
            runOnClickEmailCheck()
            FirebaseAuth.getInstance().currentUser?.delete()
            waitingForVerificationDialog.dismiss()
        }
        return waitingForVerificationDialog
    }

    //logic functions
    private fun sendEmailVerification(sendingEmailDialog: AlertDialog){
        val waitingForVerificationDialog = waitingForVerificationDialog()
        val user = FirebaseAuth.getInstance().currentUser
        user?.sendEmailVerification()?.addOnCompleteListener {
            if(it.isSuccessful) {

                sendingEmailDialog.dismiss()
                waitingForVerificationDialog.show()
            }

            val email = et_email_verification_email.text.toString().trim()
            val username = intent.getStringExtra("usernamePassed")
            val password = intent.getStringExtra("passwordPassed")
            runEmailVerificationWaiter(email, password, username, waitingForVerificationDialog)

        }?.addOnFailureListener {
            sendingEmailDialog.dismiss()
            onEmailVerificationFailedToSend(it)
        }
    }
    fun isEmailValid(email: CharSequence?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    private fun startNextActivity() {
        val intent = Intent(this, Political::class.java)
        startActivity(intent)
        finishAffinity()
    }
    private fun uploadUserToDatabase(user: com.duncboi.realsquabble.UserInfo){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(user.username!!).setValue(user).addOnCompleteListener {
                if (it.isSuccessful){
                    startNextActivity()
                }
            }.addOnFailureListener {
                errorConstraint()
                tv_email_verification_error.setText("${it.message}")
            }
    }

    //constraint functions
    private fun closeKeyboard(){
        val view = this.currentFocus
        if (view != null){
            val hideMe = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            hideMe.hideSoftInputFromWindow(view.windowToken, 0)
        }
        //else
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }
    private fun defaultConstraint(){
        val set = ConstraintSet()
        val emailLayout = email_verification_constraint
        tv_email_verification_login.visibility = View.INVISIBLE
        iv_email_verification_x.alpha = 0F
        iv_email_verification_checkmark.alpha = 0F
        set.clone(email_verification_constraint)
        set.clear(tv_email_verification_error.id, ConstraintSet.TOP)
        set.connect(tv_email_verification_error.id, ConstraintSet.TOP,et_email_verification_email.id, ConstraintSet.TOP)
        set.connect(b_email_verification_send.id, ConstraintSet.TOP,et_email_verification_email.id, ConstraintSet.BOTTOM, 24)
        set.connect(tv_email_verification_previous.id, ConstraintSet.TOP,b_email_verification_send.id, ConstraintSet.BOTTOM, 200)
        set.applyTo(email_verification_constraint)
    }
    private fun errorConstraint(){
        val defaultSet = ConstraintSet()
        val emailLayout = email_verification_constraint
        defaultSet.clone(email_verification_constraint)
        defaultSet.clear(b_email_verification_send.id, ConstraintSet.TOP)
        defaultSet.clear(tv_email_verification_error.id, ConstraintSet.TOP)
        defaultSet.connect(tv_email_verification_error.id, ConstraintSet.TOP, et_email_verification_email.id, ConstraintSet.BOTTOM, 6)
        defaultSet.connect(b_email_verification_send.id, ConstraintSet.TOP, tv_email_verification_error.id, ConstraintSet.BOTTOM, 12)
        defaultSet.applyTo(email_verification_constraint)
    }

    //stop functions
    private var stopOnClickEmailCheck = false
    private var stopLiveEmailCheck = false
    private var stopEmailVerificationWaiter = false

    //runner functions
    private fun runOnClickEmailCheck(){
        CoroutineScope(Dispatchers.Main).launch {
            onClickEmailCheckLogic()}
    }
    private fun runLiveEmailCheck(){
        CoroutineScope(IO).launch {
            liveEmailCheckLogic()}
    }
    private fun runEmailVerificationWaiter(email: String, password: String?, username: String?, waitingForVerificationDialog: AlertDialog) {
        CoroutineScope(IO).launch {
            emailVerificationWaiterLogic(email,password,username, waitingForVerificationDialog)
        }
    }

    //coroutine logic
    private suspend fun onClickEmailCheckLogic(){
        while (!stopOnClickEmailCheck){
            delay(200)
            val email = et_email_verification_email.text.toString().trim()
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

            val email = et_email_verification_email.text.toString().trim()
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
    private suspend fun emailVerificationWaiterLogic(email: String, password: String?, username: String?, waitingForVerificationDialog: AlertDialog) {
        while(!stopEmailVerificationWaiter){
            delay(200)
            val mAuth = FirebaseAuth.getInstance()
            mAuth.getCurrentUser()?.reload()
            val uid = mAuth.currentUser?.uid
            val emailVerified = mAuth.currentUser?.isEmailVerified
            val userInfo = UserInfo(username, email, password, uid)
            if(emailVerified == true){
                waitingForVerificationDialog.dismiss()
                stopEmailVerificationWaiter = true
                uploadUserToDatabase(userInfo)}
        }}

    //error functions
    private suspend fun defaultMainThread() {
        withContext(Dispatchers.Main) {
            defaultConstraint()
        }
    }
    private fun onEmailDoesntExist() {
        defaultConstraint()
        iv_email_verification_checkmark.bringToFront()
        iv_email_verification_checkmark.alpha = 1F
        iv_email_verification_x.alpha = 0F
    }
    private fun onIncorrectEmailFormat() {
        errorConstraint()
        iv_email_verification_x.bringToFront()
        iv_email_verification_x.alpha = 1F
        iv_email_verification_checkmark.alpha = 0F
        tv_email_verification_error.setText("Email formatted incorrectly")
        tv_email_verification_error.setTextColor(Color.parseColor("#eb4b4b"))
    }
    private fun onEmailEmpty() {
        errorConstraint()
        iv_email_verification_x.bringToFront()
        iv_email_verification_x.alpha = 1F
        iv_email_verification_checkmark.alpha = 0F
        tv_email_verification_error.setText("Please enter email")
        tv_email_verification_error.setTextColor(Color.parseColor("#eb4b4b"))
    }
    private fun onEmailAlreadyExists() {
        tv_email_verification_login.visibility = View.VISIBLE
        tv_email_verification_login.setOnClickListener {
            val email = et_email_verification_email.text.toString()
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("fpEmail", email)
            startActivity(intent)
        }
        errorConstraint()
        iv_email_verification_x.bringToFront()
        iv_email_verification_x.alpha = 1F
        iv_email_verification_checkmark.alpha = 0f
        tv_email_verification_error.setText("Email linked to an existing account")
        tv_email_verification_error.setTextColor(Color.parseColor("#eb4b4b"))
    }
    private fun onEmailVerificationFailedToSend(it: Exception) {
        stopOnClickEmailCheck = false
        runOnClickEmailCheck()
        closeKeyboard()
        errorConstraint()
        Toast.makeText(this, "Email Failed to Send", Toast.LENGTH_SHORT).show()
        tv_email_verification_error.setText("${it.message}")
    }
    private fun onCreateUserFailed(it: Exception) {
        stopOnClickEmailCheck = false
        runOnClickEmailCheck()
        closeKeyboard()
        iv_email_verification_x.alpha = 1F
        errorConstraint()
        tv_email_verification_error.text = "${it.message}"
    }
}