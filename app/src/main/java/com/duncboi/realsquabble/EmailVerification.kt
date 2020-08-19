package com.duncboi.realsquabble

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Patterns
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
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

class EmailVerification : AppCompatActivity() {
    override fun onStop() {
        handler2.removeCallbacks(emailVerificationValid)
        handler3.removeCallbacks(validRunnable)
        handler.removeCallbacks(emailVerificationRunnable)
        val user = FirebaseAuth.getInstance().currentUser
        super.onStop()
        if(user != null){
            if(!user.isEmailVerified){
                user.delete()
        }
    }
            }
    private var progressDialog2: ProgressDialog? = null
    private var isEmailVerified:Boolean = false
    private var bruhEmail: String = ""
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_verification)
        emailVerificationValid.run()
        auth = Firebase.auth
        val email2 = intent.getStringExtra("email")
        val username = intent.getStringExtra("username")
        val password = intent.getStringExtra("password")
        et_email_verification_email.setText(email2)
        b_email_verification_send.setOnClickListener {
            val email = et_email_verification_email.text.toString().trim()
            bruhEmail = email
            handler2.removeCallbacks(emailVerificationValid)
            val usernameQuery2 = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("email").equalTo(email)
            usernameQuery2.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(isEmailValid(email) && snapshot.childrenCount <= 0){
                        handler2.removeCallbacks(emailVerificationValid)
                        handler3.removeCallbacks(validRunnable)
                        handler.removeCallbacks(emailVerificationRunnable)
                        if (password != null) {
                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                                if (it.isSuccessful){
                                    sendEmailVerification()
                                }
                            }.addOnFailureListener {
                                iv_email_verification_x.alpha = 1F
                                errorConstraint()
                                tv_email_verification_error.setText("${it.message}")
                            }
                        }
                    }
                    else{
                        if (!isEmailValid(email)){
                            validRunnable.run()
                        }
                        else{
                            emailVerificationValid.run()
                        }}
                }
            })}
        tv_email_verification_previous.setOnClickListener {
            finish()
        }
    }

    private fun sendEmailVerification(){
        Log.d("Emailverification", "send")
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Sending Verification Email")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val user = FirebaseAuth.getInstance().currentUser
        user?.sendEmailVerification()?.addOnCompleteListener {
            if(it.isSuccessful) {
                progressDialog.dismiss()
                progressDialog2 = ProgressDialog(this)
                progressDialog2?.setTitle("Waiting for Verification")
                progressDialog2?.setMessage("Check your inbox for a link to verify your email address")
                progressDialog2?.setCancelable(false)
                progressDialog2!!.setButton(
                    DialogInterface.BUTTON_NEGATIVE, "Cancel", DialogInterface.OnClickListener { _, _ ->
                        user.delete()
                        progressDialog2!!.dismiss() //dismiss dialog
                    })
                progressDialog2?.show()
                startRepeating()
            }
        }?.addOnFailureListener {
            errorConstraint()
            Toast.makeText(this, "Email Failed to Send", Toast.LENGTH_SHORT).show()
            tv_email_verification_error.setText("${it.message}")
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
    private fun uploadUserToDatabase(user: com.duncboi.realsquabble.UserInfo){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        val userId = ref.push().key
        if (userId != null) {
            ref.child(userId).setValue(user).addOnCompleteListener {
                if (it.isSuccessful){
                val intent = Intent(this, Political::class.java)
                startActivity(intent)
                finish()}
            }.addOnFailureListener {
                errorConstraint()
                tv_email_verification_error.setText("${it.message}")
            }
        }

    }

    fun startRepeating() {
        emailVerificationRunnable.run()
    }
    fun stopRepeating(){
        handler.removeCallbacks(emailVerificationRunnable)
    }
    var stopped = false
    val handler = Handler()
    private val emailVerificationRunnable: Runnable = object : Runnable {
        override fun run() {
            try{
                bruh()
                }
            finally {
                if(!stopped){
                handler.postDelayed(this, 2000)}
            }
            }
        }
    public fun bruh(){
        val email = et_email_verification_email.text.toString().trim()
        val username = intent.getStringExtra("username")
        val password = intent.getStringExtra("password")
        Log.d("EmailVerification", "Runnable Waiting")
        val mAuth = FirebaseAuth.getInstance()
        mAuth.getCurrentUser()?.reload()
        val uid = mAuth.currentUser?.uid
        val emailVerified = mAuth.currentUser?.isEmailVerified
        val userInfo = UserInfo(username, email, password, uid)
        if(emailVerified == true){
            stopped = true
            Toast.makeText(this, "Email Verified", Toast.LENGTH_LONG).show();
            progressDialog2?.dismiss()
            handler.removeCallbacks(emailVerificationRunnable)
            uploadUserToDatabase(userInfo)
            Log.d("EmailVerification", "bruh")
        }
    }
    private fun defaultConstraint(){
        val set = ConstraintSet()
        val emailLayout = email_verification_constraint
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
    fun isEmailValid(email: CharSequence?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    val handler2 = Handler()
    var stopped2 = false
    private val emailVerificationValid: Runnable = object : Runnable {
        override fun run() {
            try{
                handler.removeCallbacks(validRunnable)
                val email = et_email_verification_email.text.toString().trim()
                val usernameQuery = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("email").equalTo(email)
                usernameQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.childrenCount > 0){
                            iv_email_verification_x.bringToFront()
                            iv_email_verification_x.alpha = 1F
                            iv_email_verification_checkmark.alpha = 0f
                            errorConstraint()
                            tv_email_verification_error.setText("Email linked to an existing account")
                            tv_email_verification_error.setTextColor(Color.parseColor("#eb4b4b"))
                        }
                        else if (!isEmailValid(email)){
                            defaultConstraint()
                        }
                        else{
                            defaultConstraint()
                            iv_email_verification_checkmark.bringToFront()
                            iv_email_verification_checkmark.alpha = 1F
                            iv_email_verification_x.alpha = 0F
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }})}
            finally {
                if(!stopped2){
                    handler2.postDelayed(this, 200)} } }
            }
    val handler3 = Handler()
    var stopped3 = false
    private val validRunnable: Runnable = object : Runnable {
        override fun run() {
            try{
                handler2.removeCallbacks(emailVerificationValid)
                val email2 = et_email_verification_email.text.toString().trim()
                if (!isEmailValid(email2)){
                    if(email2.isEmpty()){
                        errorConstraint()
                        iv_email_verification_x.bringToFront()
                        iv_email_verification_x.alpha = 1F
                        tv_email_verification_error.setText("Please enter email")
                        tv_email_verification_error.setTextColor(Color.parseColor("#eb4b4b"))
                    }
                    else {
                        //MAKES ERROR MESSAGE SAY EMAIL FORMATTED INCORRECTLY ONLY WHEN HITTING NEXT FOR THE FIRST TIME JUST ERASE THE IF STATEMENT TO CHANGE BACK (KEEP THE ELSE CONTENT BUT DELETE ELSE())
                        if(email2 != bruhEmail){
                            bruhEmail = "*"
                            defaultConstraint()
                        }
                        else{
                            iv_email_verification_x.bringToFront()
                            iv_email_verification_x.alpha = 1F
                            errorConstraint()
                            tv_email_verification_error.setText("Email formatted incorrectly")
                            tv_email_verification_error.setTextColor(Color.parseColor("#eb4b4b"))}
                    }
                }
                else{
                    emailVerificationValid.run()
                } }
            finally {
                if(!stopped3){
                    handler3.postDelayed(this, 100)} } } }


}