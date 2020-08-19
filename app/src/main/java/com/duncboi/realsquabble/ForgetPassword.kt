package com.duncboi.realsquabble

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_forget_password.*
import kotlinx.android.synthetic.main.activity_login.*

class ForgetPassword : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        auth = Firebase.auth

        b_fp_send_email.setOnClickListener {
            closeKeyboard()
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Email Sending")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()
            val email = et_fp_email.text.toString()
            auth.sendPasswordResetEmail(email).addOnCompleteListener {
                if (it.isSuccessful){
                    Log.d("Forget Password", "Email Sent")
                    progressDialog.dismiss()
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Email Sent")
                    builder.setCancelable(false)
                    builder.setMessage("Check your inbox for a password reset link")
                    builder.setPositiveButton("Ok", { _: DialogInterface?, _: Int ->
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    })
                    builder.show()
                }

            } .addOnFailureListener{
                Log.d("Forget Password", "Email Failed to Send:" + it.message)
                progressDialog.dismiss()
                if (it.message == "The email address is badly formatted."){
                    tv_fp_error_message.setText("Email Formatted Incorrectly")
                }
                if (it.message == "There is no user record corresponding to this identifier. The user may have been deleted."){
                    tv_fp_error_message.setText("User does not exist")
                }
                if (it.message == "We have blocked all requests from this device due to unusual activity. Try again later."){
                    tv_fp_error_message.setText("You have exceeded the maximum number of password resets.  Please try again later")
                }
            }
        }

        tv_fp_back_to_login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
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
}