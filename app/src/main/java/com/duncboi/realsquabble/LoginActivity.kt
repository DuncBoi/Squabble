package com.duncboi.realsquabble

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
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
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class LoginActivity: AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailAndUsernameLiveChecker.run()

        auth = Firebase.auth

        //Login Button Clicked
        b_login_login.setOnClickListener {
            loginUser()

        }

        //Dont have an account? button clicked
        tv_login_backtoregistration.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        //forget password button clicked
        tv_forgot_password.setOnClickListener {
            showRecoverPasswordDialog()
        }

        //shows forget password Activity
    }

    private fun showRecoverPasswordDialog() {
        val intent = Intent(this, ForgetPassword::class.java)
        startActivity(intent)
    }

//    //shows custom alert dialog
//    private fun showDialog(){
//        val builder = AlertDialog.Builder(this)
//        val customAlertView = layoutInflater.inflate(R.layout.success_alert_dialog, null)
//        builder.setView(customAlertView)
//        builder.setCancelable(false)
//        builder.show()
//
//    }

    //firebase user login
    private fun loginUser() {
        val email = et_login_email.text.toString().trim()
        val password = et_login_password.text.toString()
        if (TextUtils.isEmpty(email)) {
            closeKeyboard()
        }
        if (TextUtils.isEmpty(password)) {
            closeKeyboard()
        }
        when {
            TextUtils.isEmpty(email) -> Toast.makeText(
                this,
                "Email is required",
                Toast.LENGTH_SHORT
            ).show()
            TextUtils.isEmpty(password) -> Toast.makeText(
                this,
                "Password is required",
                Toast.LENGTH_SHORT
            ).show()

            else -> {
                val progressDialog = ProgressDialog(this@LoginActivity)
                progressDialog.setMessage("Logging In")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (!user!!.isEmailVerified) {
                            closeKeyboard()
                            Toast.makeText(
                                this,
                                "Your email has not been verified, please do so now",
                                Toast.LENGTH_LONG
                            ).show()
                            val intent = Intent(this, EmailVerification::class.java)
                            intent.putExtra("email", email)
                            startActivity(intent)
                            finish()
                        } else {
                            progressDialog.dismiss()
                            val intent = Intent(this, ProfileActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
                    .addOnFailureListener {
                        closeKeyboard()
                        Log.d("LoginActivity", "${it.message}")
                        if (it.message == "The email address is badly formatted.") {
                            Toast.makeText(this, "Email Formatted Incorrectly", Toast.LENGTH_SHORT)
                                .show()
                        }
                        if (it.message == "The password is invalid or the user does not have a password.") {
                            val builder = AlertDialog.Builder(this)
                            builder.setTitle("Incorrect Password")
                            builder.setCancelable(false)
                            builder.setMessage("The password you entered is incorrect.  Try again or reset your password.")
                            builder.setPositiveButton("Try Again", { _: DialogInterface?, _: Int ->
                            })
                            builder.setNegativeButton(
                                "Reset Password",
                                { _: DialogInterface?, _: Int ->
                                    val intent = Intent(this, ForgetPassword::class.java)
                                    startActivity(intent)
                                })
                            builder.show()
                        }
                        if (it.message == "There is no user record corresponding to this identifier. The user may have been deleted.") {
                            Toast.makeText(this, "User does not exist", Toast.LENGTH_SHORT).show()
                        }
                        if (it.message == "We have blocked all requests from this device due to unusual activity. Try again later. [ Too many unsuccessful login attempts. Please try again later. ]") {
                            val builder = AlertDialog.Builder(this)
                            builder.setTitle("Too Many Sign In Attempts")
                            builder.setCancelable(false)
                            builder.setMessage("You have exceeded the maximum number of failed sign in attempts allowed.  Please try again later or reset your password.")
                            builder.setPositiveButton("Ok", { _: DialogInterface?, _: Int ->
                            })
                            builder.setNegativeButton(
                                "Reset Password",
                                { _: DialogInterface?, _: Int ->
                                    val intent = Intent(this, ForgetPassword::class.java)
                                    startActivity(intent)
                                    finish()
                                })
                            builder.show()
                        } else {
                            Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                            closeKeyboard()
                        }
                        FirebaseAuth.getInstance().signOut()
                        progressDialog.dismiss()
                    }
            }
        }
    }

    //action bar back button
    @Override
    public override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val hideMe = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            hideMe.hideSoftInputFromWindow(view.windowToken, 0)
        }
        //else
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun defaultConstraint() {
        val set = ConstraintSet()
        val loginLayout = login_constraint
        iv_login_email_x.alpha = 0F
        iv_login_password_x.alpha = 0F
        iv_login_email_checkmark.alpha = 0F
        set.clone(loginLayout)
        set.clear(tv_login_email_error.id, ConstraintSet.TOP)
        set.connect(
            tv_login_email_error.id,
            ConstraintSet.TOP,
            et_login_email.id,
            ConstraintSet.TOP
        )
        set.clear(login_passwordTIL.id, ConstraintSet.TOP)
        set.connect(
            login_passwordTIL.id,
            ConstraintSet.TOP,
            et_login_email.id,
            ConstraintSet.BOTTOM,
            24
        )
        set.clear(tv_login_password_error.id, ConstraintSet.TOP)
        set.connect(
            tv_login_password_error.id,
            ConstraintSet.TOP,
            login_passwordTIL.id,
            ConstraintSet.TOP
        )
        set.connect(
            b_login_login.id,
            ConstraintSet.TOP,
            login_passwordTIL.id,
            ConstraintSet.BOTTOM,
            24
        )
        set.connect(
            tv_forgot_password.id,
            ConstraintSet.TOP,
            b_login_login.id,
            ConstraintSet.BOTTOM,
            200
        )
        set.applyTo(loginLayout)
    }

    private fun errorConstraint() {
        val defaultSet = ConstraintSet()
        val loginLayout = login_constraint
        defaultSet.clone(loginLayout)
        defaultSet.clear(tv_login_email_error.id, ConstraintSet.TOP)
        defaultSet.clear(tv_login_password_error.id, ConstraintSet.TOP)
        defaultSet.clear(login_passwordTIL.id, ConstraintSet.TOP)
        defaultSet.clear(b_login_login.id, ConstraintSet.TOP)
        defaultSet.connect(
            tv_login_email_error.id,
            ConstraintSet.TOP,
            et_login_email.id,
            ConstraintSet.BOTTOM,
            6
        )
        defaultSet.connect(
            tv_login_password_error.id,
            ConstraintSet.TOP,
            login_passwordTIL.id,
            ConstraintSet.BOTTOM,
            6
        )
        defaultSet.connect(
            login_passwordTIL.id,
            ConstraintSet.TOP,
            tv_login_email_error.id,
            ConstraintSet.BOTTOM,
            8
        )
        defaultSet.connect(
            b_login_login.id,
            ConstraintSet.TOP,
            tv_login_password_error.id,
            ConstraintSet.BOTTOM,
            8
        )
        defaultSet.applyTo(loginLayout)
    }

    val handler = Handler()
    private val emailAndUsernameLiveChecker: Runnable = object : Runnable {
        override fun run() {
            try {
                val email = et_login_email.text.toString().trim()
                val usernameQuery = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("username").equalTo(email)
                usernameQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.childrenCount > 0) {
                            Log.d("Login", "bruh")
                            defaultConstraint()
                            iv_login_email_checkmark.bringToFront()
                            iv_login_email_x.alpha = 0F
                            iv_login_email_checkmark.alpha = 1F
                            iv_login_password_x.alpha = 0F
                        }
                        else{
                            defaultConstraint()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }})
//                val emailQuery = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("email").equalTo(email)
//                emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        if (snapshot.childrenCount > 0) {
//                            defaultConstraint()
//                            iv_login_email_checkmark.bringToFront()
//                            iv_login_email_x.alpha = 0F
//                            iv_login_email_checkmark.alpha = 1F
//                            iv_login_password_x.alpha = 0F
//                        }
//                        else{
//                            defaultConstraint()
//                        }
//                }
//                        override fun onCancelled(error: DatabaseError) {
//                            TODO("Not yet implemented")
//                        }})
            }

            finally {
                    handler.postDelayed(this, 200)
            }
        }
    }
    val handler2 = Handler()
    private val usernameLiveChecker: Runnable = object : Runnable {
        override fun run() {
            try {
                val username = et_login_email.text.toString().trim()
                val usernameQuery = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("username").equalTo(username)
                usernameQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.childrenCount > 0) {
                            defaultConstraint()
                            iv_login_email_checkmark.bringToFront()
                            iv_login_email_x.alpha = 0F
                            iv_login_email_checkmark.alpha = 1F
                            iv_login_password_x.alpha = 0F
                        }
                        else{
                            defaultConstraint()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }})
            }

            finally {
                handler2.postDelayed(this, 200)
            }
        }
    }

}