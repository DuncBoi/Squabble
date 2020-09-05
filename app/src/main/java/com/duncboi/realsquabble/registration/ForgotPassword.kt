package com.duncboi.realsquabble.registration

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.duncboi.realsquabble.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_forgot_password.*
import kotlinx.android.synthetic.main.sending_email.view.*
import kotlinx.coroutines.*

class ForgotPassword : Fragment() {

    private val args: ForgotPasswordArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    private lateinit var auth: FirebaseAuth
    private var onClickEmail: String = ""

    override fun onPause() {
        super.onPause()
        stopLiveEmailCheck = true
        stopOnClickEmailCheck = true
    }

    override fun onResume() {
        super.onResume()
        if (args.email != "email"){
            et_fp_email.setText("${args.email}")
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onEmailPassed()
        runLiveEmailCheck()
        defaultConstraint()
        auth = Firebase.auth

        b_fp_send_email.setOnClickListener {

            stopLiveEmailCheck = true
            stopOnClickEmailCheck = false
            val email = et_fp_email.text.toString().trim().toLowerCase()
            onClickEmail = email

            val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("email").equalTo(email)
            emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {

                    if(isEmailValid(email) && snapshot.childrenCount > 0){

                        val sendingEmailDialog = sendingEmailDialog()
                        sendingEmailDialog!!.show()

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
            val bundle = Bundle()
            val email = et_fp_email.text.toString().trim().toLowerCase()
            bundle.putString("email", email)
            findNavController().navigate(R.id.action_forgotPassword_to_login)
        }
    }

    fun isEmailValid(email: CharSequence): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private var stopOnClickEmailCheck = false
    private var stopLiveEmailCheck = false

    private fun runOnClickEmailCheck(){
        CoroutineScope(Dispatchers.Main).launch {
            onClickEmailCheckLogic()}
    }
    private fun runLiveEmailCheck(){
        CoroutineScope(Dispatchers.IO).launch {
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
                val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("email").equalTo(lowerCaseEmail)
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
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Email Sent")
        builder.setCancelable(false)
        builder.setMessage("Check your inbox for a password reset link.  After you have reset it, you may use it to log in")
        builder.setPositiveButton("Ok") { _: DialogInterface?, _: Int ->
            val bundle = Bundle()
            bundle.putString("email", email)
            findNavController().navigate(R.id.action_forgotPassword_to_login)
        }
        builder.show()
    }

    private fun onEmailPassed() {
        val emailToReset = args.email
        if (emailToReset != "email") {
            et_fp_email.setText(emailToReset)
        }
    }

    private fun closeKeyboard(){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
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

    private fun sendingEmailDialog(): AlertDialog? {
        val sendingEmail = activity?.let { AlertDialog.Builder(it) }
        val view = LayoutInflater.from(activity).inflate(R.layout.sending_email, null)
        sendingEmail!!.setView(view)
        view.tv_sending_email.text = "Sending password reset email"
        sendingEmail.setCancelable(false)
        val sendingEmailDialog = sendingEmail.create()
        return sendingEmailDialog
    }
    private suspend fun defaultMainThread() {
        withContext(Dispatchers.Main) {
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
        tv_fp_error_message.text = "Email formatted incorrectly"
        tv_fp_error_message.setTextColor(Color.parseColor("#eb4b4b"))
    }
    private fun onEmailEmpty() {
        errorConstraint()
        iv_fp_x.bringToFront()
        iv_fp_x.alpha = 1F
        iv_fp_checkmark.alpha = 0F
        tv_fp_error_message.text = "Please enter email"
        tv_fp_error_message.setTextColor(Color.parseColor("#eb4b4b"))
    }
    private fun onEmailNotInDatabase() {
        errorConstraint()
        iv_fp_x.bringToFront()
        iv_fp_x.alpha = 1F
        iv_fp_checkmark.alpha = 0f
        tv_fp_error_message.text = "User does not exist"
        tv_fp_error_message.setTextColor(Color.parseColor("#eb4b4b"))
    }

}