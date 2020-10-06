package com.duncboi.realsquabble

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.duncboi.realsquabble.messenger.Users
import com.duncboi.realsquabble.political.Political
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_verification_code.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class VerificationCode : Fragment() {

    var job: Job? = null
    val args: VerificationCodeArgs by navArgs()
    private lateinit var auth: FirebaseAuth
    private var countdown = 60
    private var stopCountdown = false
    private var verificationId: String? = null

    override fun onPause() {
        super.onPause()
        job?.cancel()
        stopCountdown = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_verification_code, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        verificationId = args.verificationid
        firstPinView.setAnimationEnable(true)
        defaultConstraint()
        auth = Firebase.auth
        val pn = args.phoneNumber
        val phoneNumber = "+1 ${pn.replaceFirst("(\\d{3})(\\d{3})(\\d+)".toRegex(), "($1) $2-$3")}"
        tv_verification_phone_number.setText("${phoneNumber}")

        firstPinView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                defaultConstraint()
                val text = p0.toString()
                if (text.length == 6) {
                    closeKeyboard()
                    pb_verification_progress.visibility = View.VISIBLE
                    val verificationCode = firstPinView.text.toString().trim()
                    verifyCode(verificationCode, verificationId!!)
                } else {
                    pb_verification_progress.visibility = View.INVISIBLE
                }
            }

        })

        runCountdown()

        tv_verification_previous.setOnClickListener {
            job?.cancel()
            stopCountdown = true
            val bundle = Bundle()
            bundle.putString("username", "${args.username}")
            bundle.putString("phoneNumber", "${args.phoneNumber}")
            findNavController().navigate(
                R.id.action_verificationCode_to_phoneAuthentication,
                bundle
            )
        }
    }

    private fun runCountdown() {
        job = CoroutineScope(Main).launch {
            countdownLogic()
        }
    }

    private suspend fun countdownLogic() {
        while (!stopCountdown) {
            b_verification_next.alpha = 0.5F
            b_verification_next.isClickable = false
            b_verification_next.setBackgroundResource(R.drawable.greyed_out_button)
            tv_verification_countdown.setTextColor(Color.parseColor("#8a000000"))
            delay(1000)
            tv_verification_countdown.text = "Resend code in: $countdown"
            countdown -= 1
            if (countdown == 0) {
                b_verification_next.alpha = 1F
                b_verification_next.isClickable = true
                b_verification_next.setBackgroundResource(R.drawable.rounded_button)
                tv_verification_countdown.setTextColor(Color.parseColor("#eb4b4b"))
                stopCountdown = true
                defaultConstraint()
                tv_verification_countdown.text = "Resend Code"
                b_verification_next.setOnClickListener {
                    stopCountdown = false
                    runCountdown()
                    countdown = 60
                    val phoneNumber = "+1${args.phoneNumber}"
                    activity?.let { it1 ->
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber, // Phone number to verify
                            60, // Timeout duration
                            TimeUnit.SECONDS, // Unit of timeout
                            it1, // Activity (for callback binding)
                            callbacks
                        )
                    }
                }
            }
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
            phoneAuthCredential.let {
                Log.d("Moose", "bruh")
            }
        }

        override fun onVerificationFailed(exception: FirebaseException) {
            if (exception is FirebaseTooManyRequestsException){
                errorConstraint()
                tv_verification_error.text = "Too many verification codes sent to this phone number, please try again later"
            }
            else{
                if (exception.toString() == "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The format of the phone number provided is incorrect. Please enter the phone number in a format that can be parsed into E.164 format. E.164 phone numbers are written in the format [+][country code][subscriber number including area code]."){
                    errorConstraint()
                    tv_verification_error.text = "Not a valid phone number"
                }
                else{
                    errorConstraint()
                    tv_verification_error.text = "$exception"}
            }
        }

        override fun onCodeSent(cow: String, token: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(cow, token)
            closeKeyboard()
            verificationId = cow
            Toast.makeText(activity, "Verification Code Sent", Toast.LENGTH_LONG).show()
        }
    }

    private fun verifyCode(code: String, resend: String) {
            if (resend != "verificationid") {
                val credential = PhoneAuthProvider.getCredential(resend, code)
                addPhoneNumber(credential)
            }
    }

    private fun addPhoneNumber(phoneAuthCredential: PhoneAuthCredential){
        auth.signInWithCredential(phoneAuthCredential).addOnCompleteListener { task ->
            if (task.isSuccessful){
                stopCountdown = true
                job?.cancel()
                pb_verification_progress.visibility = View.INVISIBLE
                val uid = auth.currentUser!!.uid
                val user = Users("", "", "", "OFF", "${args.phoneNumber}", "", "$uid", "${args.username}", "", "", "phone", "OFFLINE", "0", "")
                uploadUserToDatabase(user)
                Log.d("Moose", "task successful")
            }
            else{
                errorConstraint()
                pb_verification_progress.visibility = View.INVISIBLE
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Log.d("Moose", "${task.exception.toString()}")
                    if(task.exception.toString() == "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The sms code has expired. Please re-send the verification code to try again."){
                        tv_verification_error.text = "Code has expired, please wait to resend the code and try again"}
                    else{
                        tv_verification_error.text = "Invalid verification code"
                    }
                    // Invalid request
                    // ...
                } else if (task.exception is FirebaseTooManyRequestsException) {
                    tv_verification_error.text = "You have sent too many verification codes, please try again later"
                    // The SMS quota for the project has been exceeded
                    // ...
                }
                else{
                    tv_verification_error.text = "${task.exception}"
                }
                Log.d("Moose", "${task.exception}")
            }
        }
    }

    private fun defaultConstraint(){
        iv_verification_error.visibility = View.INVISIBLE
        tv_verification_error.alpha = 0F
        val set = ConstraintSet()
        val phoneLayout = verification_constraint
        set.clone(phoneLayout)
        set.clear(b_verification_next.id, ConstraintSet.TOP)
        set.connect(b_verification_next.id, ConstraintSet.TOP, firstPinView.id, ConstraintSet.BOTTOM, 24)
        set.clear(tv_verification_error.id, ConstraintSet.TOP)
        set.connect(tv_verification_error.id, ConstraintSet.TOP, firstPinView.id, ConstraintSet.TOP)
        set.applyTo(phoneLayout)
    }

    private fun errorConstraint(){
        iv_verification_error.visibility = View.VISIBLE
        tv_verification_error.alpha = 1F
        val set = ConstraintSet()
        val phoneLayout = verification_constraint
        set.clone(phoneLayout)
        set.clear(tv_verification_error.id, ConstraintSet.TOP)
        set.connect(tv_verification_error.id, ConstraintSet.TOP, firstPinView.id, ConstraintSet.BOTTOM, 6)
        set.clear(b_verification_next.id, ConstraintSet.TOP)
        set.connect(b_verification_next.id, ConstraintSet.TOP, tv_verification_error.id, ConstraintSet.BOTTOM, 12)
        set.applyTo(phoneLayout)
    }

    private fun closeKeyboard(){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun uploadUserToDatabase(user: Users){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(user.getUid()!!).setValue(user).addOnCompleteListener {
            if (it.isSuccessful){
                val intent = Intent(activity, Political::class.java)
                startActivity(intent)
                activity?.finish()
            }
        }.addOnFailureListener {
            errorConstraint()
            tv_verification_error.text = "${it.message}"
        }
    }

}