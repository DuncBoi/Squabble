package com.duncboi.realsquabble.registration

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import kotlinx.android.synthetic.main.fragment_email_registration.*
import kotlinx.android.synthetic.main.fragment_forgot_password.*
import kotlinx.android.synthetic.main.sending_email.view.*
import kotlinx.coroutines.*

class ForgotPassword : Fragment() {

    private val args: ForgotPasswordArgs by navArgs()
    private var job: Job? = null

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
        job?.cancel()
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
        defaultConstraint()
        auth = Firebase.auth

        tv_forgot_password_previous.setOnClickListener {
            findNavController().popBackStack()
        }

        et_fp_email.addTextChangedListener(object: TextWatcher {
            private var searchFor = ""
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val searchText = p0.toString().trim().toLowerCase()
                if (searchText == searchFor)
                    return
                searchFor = searchText
                defaultConstraint()
                pb_forgot_password_progress.visibility = View.VISIBLE
                job = CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    if (searchText != searchFor)
                        return@launch
                    if (searchText == "") {
                        onEmailEmpty()
                        pb_forgot_password_progress.visibility = View.INVISIBLE
                    } else if (!isEmailValid(searchText)) {
                        onIncorrectEmailFormat()
                        pb_forgot_password_progress.visibility = View.INVISIBLE
                    } else {
                        val usernameQuery =
                            FirebaseDatabase.getInstance().reference.child("Users")
                                .orderByChild("email").equalTo(searchText)
                        usernameQuery.addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.childrenCount > 0) {
                                    onEmailInDatabase()
                                    pb_forgot_password_progress.visibility = View.INVISIBLE
                                } else {
                                    onEmailNotInDatabase()
                                    pb_forgot_password_progress.visibility = View.INVISIBLE
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }
            }
        })

        b_fp_send_email.setOnClickListener {

            val email = et_fp_email.text.toString().trim().toLowerCase()

            val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("email").equalTo(email)
            emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {

                    for (i in snapshot.children){
                        val password = i.child("password").getValue(String::class.java)
                        if (password != null){
                            if (password == "google"){
                                val builder = AlertDialog.Builder(activity)
                                builder.setTitle("Email Failed")
                                builder.setCancelable(false)
                                builder.setMessage("Your account is linked to Google, please return to the login screen and sign in with Google")
                                builder.setPositiveButton("Ok") { _: DialogInterface?, _: Int ->
                                    findNavController().popBackStack()
                                }
                                builder.show()
                            }
                            else if (password == "phone"){
                                val builder = AlertDialog.Builder(activity)
                                builder.setTitle("Email sent to $email")
                                builder.setCancelable(false)
                                builder.setMessage("Your account is linked to a phone number, please return to the login screen and sign in with a phone number")
                                builder.setPositiveButton("Ok") { _: DialogInterface?, _: Int ->
                                    findNavController().popBackStack()
                                }
                                builder.show()
                            }
                            else{
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
                                        onIncorrectEmailFormat()
                                    }
                                    else{
                                        onEmailEmpty()
                                    }
                                }
                            }
                        }
                    }
                }
            })
        }

        tv_fp_back_to_login.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    fun isEmailValid(email: CharSequence): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showEmailSentDialog(email: String) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Email sent to $email")
        builder.setCancelable(false)
        builder.setMessage("Check your inbox for a password reset link.  After you have reset it, you may use it to log in")
        builder.setPositiveButton("Ok") { _: DialogInterface?, _: Int ->
            findNavController().popBackStack()
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

    private fun defaultConstraint(){
        iv_fp_checkmark.visibility = View.INVISIBLE
        iv_fp_x.visibility = View.INVISIBLE
        tv_fp_error_message.visibility = View.GONE

    }
    private fun errorConstraint(){
        tv_fp_error_message.visibility = View.VISIBLE
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
        iv_fp_checkmark.visibility = View.VISIBLE
        iv_fp_x.visibility = View.INVISIBLE
    }
    private fun onIncorrectEmailFormat() {
        errorConstraint()
        iv_fp_checkmark.visibility = View.INVISIBLE
        iv_fp_x.visibility = View.VISIBLE
        tv_fp_error_message.text = "Email formatted incorrectly"
        tv_fp_error_message.setTextColor(Color.parseColor("#eb4b4b"))
    }
    private fun onEmailEmpty() {
        errorConstraint()
        iv_fp_checkmark.visibility = View.INVISIBLE
        iv_fp_x.visibility = View.VISIBLE
        tv_fp_error_message.text = "Please enter email"
        tv_fp_error_message.setTextColor(Color.parseColor("#eb4b4b"))
    }
    private fun onEmailNotInDatabase() {
        errorConstraint()
        iv_fp_checkmark.visibility = View.INVISIBLE
        iv_fp_x.visibility = View.VISIBLE
        tv_fp_error_message.text = "User does not exist"
        tv_fp_error_message.setTextColor(Color.parseColor("#eb4b4b"))
    }

}