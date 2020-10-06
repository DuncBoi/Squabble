package com.duncboi.realsquabble

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.fragment_phone_authentication.*
import java.util.concurrent.TimeUnit


class PhoneAuthentication : Fragment() {

    private var verificationIdGlobal: String? = null
    private lateinit var sendingCode: AlertDialog
    private val args: PhoneAuthenticationArgs by navArgs()

    override fun onResume() {
        super.onResume()
        if (args.phoneNumber != "phonenumber"){
            et_phone_phone.setText("${args.phoneNumber}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_phone_authentication, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        et_phone_phone.bringToFront()
        textView14.bringToFront()
        defaultConstraint()

        super.onViewCreated(view, savedInstanceState)
        et_phone_phone.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(p0: Editable?) {

                val databaseInput = p0.toString()

                if (databaseInput.isEmpty()){
                    errorConstraint()
                    tv_phone_phone_taken.text = "Please enter phone number"
                }
                 else if (databaseInput.length == 10){
                    formatPhone(databaseInput)

                    tv_phone_formatted_phone.setOnClickListener {
                            openKeyboard()
                            et_phone_phone.bringToFront()
                            textView14.bringToFront()
                        }
                    }
                else{
                    defaultConstraint()
                    b_phone_next.setBackgroundResource(R.drawable.greyed_out_button)
                    b_phone_next.alpha = 0.5F
                    b_phone_next.isClickable = true
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }}
        )

        tv_phone_previous.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("username", "${args.username}")
            findNavController().navigate(R.id.action_phoneAuthentication_to_emailRegistration, bundle)
        }

        b_phone_next.setOnClickListener {
            if (et_phone_phone.text.toString().length != 10) {
                errorConstraint()
                tv_phone_phone_taken.text = "Phone number formatted incorrectly"
            } else {

                sendingCode = sendingCode()
                sendingCode.show()

                formatPhone(et_phone_phone.text.toString())
                val phoneNumber = "+1${et_phone_phone.text.toString().trim()}"
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

    private fun sendingCode(): AlertDialog {
        val sendingCode = activity?.let { AlertDialog.Builder(it) }
        sendingCode!!.setView(R.layout.sending_verification_code)
        sendingCode.setCancelable(false)
        return sendingCode.create()
    }

    private fun formatPhone(userOutput: String) {
        val output = userOutput.replaceFirst("(\\d{3})(\\d{3})(\\d+)".toRegex(), "($1) $2-$3")
        defaultConstraint()
        tv_phone_formatted_phone.bringToFront()
        textView14.bringToFront()
        iv_phone_checkmark.alpha = 1f
        iv_phone_checkmark.bringToFront()
        iv_phone_x.alpha = 0F
        b_phone_next.alpha = 1f
        b_phone_next.isClickable
        b_phone_next.setBackgroundResource(R.drawable.rounded_button)
        tv_phone_formatted_phone.text = "$output"
        closeKeyboard()
    }

    private fun closeKeyboard(){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun openKeyboard(){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }
private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
    override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
        phoneAuthCredential.let {
            Log.d("Moose", "bruh")
        }
    }

    override fun onVerificationFailed(exception: FirebaseException) {
        Log.d("Moose", "$exception")
        sendingCode.dismiss()
        if (exception is FirebaseTooManyRequestsException){
            errorConstraint()
            tv_phone_phone_taken.text = "Too many verification codes sent to this phone number, please try again later"
        }
        else{
            if (exception.toString() == "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The format of the phone number provided is incorrect. Please enter the phone number in a format that can be parsed into E.164 format. E.164 phone numbers are written in the format [+][country code][subscriber number including area code]."){
                errorConstraint()
                tv_phone_phone_taken.text = "Not a valid phone number"
            }
            else{
            errorConstraint()
            tv_phone_phone_taken.text = "$exception"}
        }
    }

    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
        super.onCodeSent(verificationId, token)
        verificationIdGlobal = verificationId
        sendingCode.dismiss()
        Toast.makeText(activity, "Verification Code Sent", Toast.LENGTH_LONG).show()
        val bundle = Bundle()
        val username = args.username
        bundle.putString("username", username)
        bundle.putString("phoneNumber", "${et_phone_phone.text}")
        bundle.putString("verificationid", "$verificationId")
        findNavController().navigate(R.id.action_phoneAuthentication_to_verificationCode, bundle)
    }
}


    private fun defaultConstraint(){
        et_phone_phone.bringToFront()
        textView14.bringToFront()
        b_phone_next.isClickable = true
        val set = ConstraintSet()
        val phoneLayout = phone_constraint
        set.clone(phoneLayout)
        set.clear(b_phone_next.id, ConstraintSet.TOP)
        set.connect(b_phone_next.id, ConstraintSet.TOP, et_phone_phone.id, ConstraintSet.BOTTOM, 24)
        set.clear(tv_phone_phone_taken.id, ConstraintSet.TOP)
        set.connect(tv_phone_phone_taken.id, ConstraintSet.TOP, et_phone_phone.id, ConstraintSet.TOP)
        set.applyTo(phoneLayout)
    }
    private fun errorConstraint(){
        iv_phone_x.bringToFront()
        iv_phone_x.alpha = 1F
        iv_phone_checkmark.alpha = 0f
        b_phone_next.setBackgroundResource(R.drawable.greyed_out_button)
        b_phone_next.alpha = 0.5F
        b_phone_next.isClickable = false
        val set = ConstraintSet()
        val phoneLayout = phone_constraint
        set.clone(phoneLayout)
        set.clear(tv_phone_phone_taken.id, ConstraintSet.TOP)
        set.connect(tv_phone_phone_taken.id, ConstraintSet.TOP, et_phone_phone.id, ConstraintSet.BOTTOM, 6)
        set.clear(b_phone_next.id, ConstraintSet.TOP)
        set.connect(b_phone_next.id, ConstraintSet.TOP, tv_phone_phone_taken.id, ConstraintSet.BOTTOM, 12)
        set.applyTo(phoneLayout)
    }

}