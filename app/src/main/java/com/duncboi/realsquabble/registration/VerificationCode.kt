package com.duncboi.realsquabble.registration

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.political.Constants
import com.duncboi.realsquabble.political.Political
import com.duncboi.realsquabble.profile.ProfileActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_email_verification_registration.*
import kotlinx.android.synthetic.main.fragment_verification_code.*
import kotlinx.coroutines.Job
import java.util.concurrent.TimeUnit

class VerificationCode : Fragment() {

    val args: VerificationCodeArgs by navArgs()
    private lateinit var auth: FirebaseAuth
    private var poo: CountDownTimer? = null

    override fun onPause() {
        super.onPause()
        poo?.cancel()
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

        firstPinView.setAnimationEnable(true)
        auth = Firebase.auth
        val pn = args.phoneNumber
        val phoneNumber = "+1 ${pn.replaceFirst("(\\d{3})(\\d{3})(\\d+)".toRegex(), "($1) $2-$3")}"

        tv_verification_phone_number.text = phoneNumber

        firstPinView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                iv_verification_error.visibility = View.INVISIBLE
                tv_verification_error.visibility = View.INVISIBLE
                val text = p0.toString()
                if (text.length == 6) {
                    closeKeyboard()
                    pb_verification_progress.visibility = View.VISIBLE
                    val verificationCode = firstPinView.text.toString().trim()
                    verifyCode(verificationCode, args.verificationid)
                } else {
                    pb_verification_progress.visibility = View.INVISIBLE
                }
            }

        })

        tv_verification_previous.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun verifyCode(code: String, resend: String) {
            if (resend != "verificationid") {
                val credential = PhoneAuthProvider.getCredential(resend, code)
                addPhoneNumber(credential)
            }
    }

    private fun addPhoneNumber(phoneAuthCredential: PhoneAuthCredential) {
        auth.signInWithCredential(phoneAuthCredential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                pb_verification_progress.visibility = View.INVISIBLE
                val firebaseUser = task.result.user
                if (firebaseUser != null) {
                    FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
                        .addListenerForSingleValueEvent(
                            object : ValueEventListener {
                                override fun onCancelled(error: DatabaseError) {}
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    Log.d("snapshot", "snapshot: $snapshot")
                                    if (!snapshot.exists()) {
                                        val userHash = HashMap<String, Any?>()
                                        userHash["category"] = ""
                                        userHash["economicScore"] = ""
                                        userHash["socialScore"] = ""
                                        userHash["anonymous"] = "OFF"
                                        userHash["email"] = args.phoneNumber
                                        userHash["name"] = ""
                                        userHash["uid"] = "${firebaseUser.uid}"
                                        userHash["username"] = args.username
                                        userHash["bio"] = ""
                                        userHash["uri"] = ""
                                        userHash["password"] = "phone"
                                        userHash["status"] = "OFFLINE"
                                        userHash["userTime"] = "0"
                                        userHash["groupId"] = ""
                                        userHash["alignmentTime"] = ""
                                        uploadUserToDatabase(userHash)
                                    } else {
                                        val intent = Intent(activity, ProfileActivity::class.java)
                                        startActivity(intent)
                                        activity?.finish()
                                    }
                                }
                            })
                }
            } else {
                pb_verification_progress.visibility = View.INVISIBLE
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Log.d("Moose", "${task.exception.toString()}")
                    if (task.exception.toString() == "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The sms code has expired. Please re-send the verification code to try again.") {
                        Toast.makeText(
                            activity,
                            "Code has expired, try again in 20 seconds",
                            Toast.LENGTH_LONG
                        ).show()
                        findNavController().popBackStack()
                    } else {
                        iv_verification_error.visibility = View.VISIBLE
                        tv_verification_error.visibility = View.VISIBLE
                        tv_verification_error.text = "Invalid verification code"
                    }
                    // Invalid request
                    // ...
                } else if (task.exception is FirebaseTooManyRequestsException) {
                    Toast.makeText(
                        activity,
                        "You have sent too many verification codes, try again later",
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().popBackStack()
                    // The SMS quota for the project has been exceeded
                    // ...
                } else {
                    tv_verification_error.text = "${task.exception}"
                }
                Log.d("Moose", "${task.exception}")
            }
        }
    }

    private fun uploadUserToDatabase(user: HashMap<String, Any?>){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(user["uid"].toString()).setValue(user).addOnCompleteListener {
            if (it.isSuccessful){
                val intent = Intent(activity, Political::class.java)
                startActivity(intent)
                activity?.finish()
            }
        }.addOnFailureListener {
            tv_verification_error.text = "${it.message}"
        }
    }


    private fun closeKeyboard(){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

}