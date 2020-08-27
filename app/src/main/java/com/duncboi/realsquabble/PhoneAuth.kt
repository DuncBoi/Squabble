package com.duncboi.realsquabble

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_phone_auth.*
import java.util.concurrent.TimeUnit

class PhoneAuth : AppCompatActivity() {
    private var verificationIdGlobal: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_auth)
        b_phone_send.setOnClickListener {
        val phoneNumber = et_phone_phone.text.toString().trim()

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks) // OnVerificationStateChangedCallbacks
    }
        b_phone_sign_in.setOnClickListener {
            val verificationCode = et_phone_verification_code.text.toString().trim()
            verificationIdGlobal?.let {
            val credential = PhoneAuthProvider.getCredential(it, verificationCode)
            addPhoneNumber(credential)
        }
    }}

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
            phoneAuthCredential.let {
                addPhoneNumber(phoneAuthCredential)
            }
            TODO("Not yet implemented")
        }

        override fun onVerificationFailed(exception: FirebaseException) {
            Log.d("Phone", "$exception")
            TODO("Not yet implemented")
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(verificationId, token)
            verificationIdGlobal = verificationId
        }
    }

    private fun addPhoneNumber(phoneAuthCredential: PhoneAuthCredential){
        FirebaseAuth.getInstance().currentUser?.updatePhoneNumber(phoneAuthCredential)?.addOnCompleteListener {task ->
            if (task.isSuccessful){
                Log.d("Phone", "task successful")
            }
            else{
                Log.d("Phone", "${task.exception}")
            }
        }
    }
}
