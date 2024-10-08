package com.duncboi.realsquabble.registration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.duncboi.realsquabble.political.Political
import com.duncboi.realsquabble.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_email_verification_registration.*
import kotlinx.android.synthetic.main.waiting_for_verification.view.*
import kotlinx.coroutines.*

class EmailVerificationRegistration : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val args: EmailVerificationRegistrationArgs by navArgs()
    private var job: Job? = null

    override fun onPause() {
        super.onPause()
        job?.cancel()
        stopEmailVerificationWaiter = true
    }

    override fun onStop() {
        super.onStop()
        stopEmailVerificationWaiter = true
        val user = FirebaseAuth.getInstance().currentUser
        if(user != null){
            if(!user.isEmailVerified){
                user.delete()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_email_verification_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultConstraint()
        auth = Firebase.auth

        val email = args.email
        val password = args.password
        et_email_verification_email.setText(email)

        et_email_verification_email.addTextChangedListener(object: TextWatcher {
            private var searchFor = ""

            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val searchText = p0.toString().trim().toLowerCase()
                if (searchText == searchFor)
                    return
                searchFor = searchText
                defaultConstraint()
                b_email_verification_send.isClickable = false
                pb_email_verification_progress.visibility = View.VISIBLE
                job = CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    if (searchText != searchFor)
                        return@launch
                    if (searchText == "") {
                        onEmailEmpty()
                    } else if (!isEmailValid(searchText)) {
                        onIncorrectEmailFormat()
                    } else {
                        val usernameQuery =
                            FirebaseDatabase.getInstance().reference.child("Users")
                                .orderByChild("email").equalTo(searchText)
                        usernameQuery.addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.childrenCount > 0) {
                                    onEmailAlreadyExists()
                                    pb_email_verification_progress.visibility = View.INVISIBLE
                                } else {
                                    onEmailDoesntExist()
                                    pb_email_verification_progress.visibility = View.INVISIBLE
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }
            }
        })

        b_email_verification_send.setOnClickListener {

            val email = et_email_verification_email.text.toString().trim()

            val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("email").equalTo(email)
            emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {

                    if(isEmailValid(email) && snapshot.childrenCount <= 0){
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
            })}
        tv_email_verification_previous.setOnClickListener {
            closeKeyboard()
            findNavController().popBackStack()
        }

    }

    //dialog functions
    private fun sendingEmailDialog(): AlertDialog {
        val sendingEmail = activity?.let { AlertDialog.Builder(it) }
        sendingEmail!!.setView(R.layout.sending_email)
        sendingEmail.setCancelable(false)
        return sendingEmail.create()
    }
    private fun waitingForVerificationDialog(): AlertDialog {
        closeKeyboard()
        val waitingForVerificationBuilder = activity?.let { AlertDialog.Builder(it) }
        val view = LayoutInflater.from(activity).inflate(R.layout.waiting_for_verification, null)
        waitingForVerificationBuilder!!.setView(view)
        waitingForVerificationBuilder.setCancelable(false)
        val waitingForVerificationDialog = waitingForVerificationBuilder.create()
        view.b_waiting_for_verification_cancel.setOnClickListener {
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
            val username = args.username
            val password = args.password
            runEmailVerificationWaiter(email, password, username, waitingForVerificationDialog)

        }?.addOnFailureListener {
            sendingEmailDialog.dismiss()
            onEmailVerificationFailedToSend(it)
        }
    }
    fun isEmailValid(email: CharSequence): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    private fun startNextActivity() {
        val intent = Intent(activity, Political::class.java)
        startActivity(intent)
        activity?.finish()
    }
    private fun uploadUserToDatabase(user: HashMap<String, Any?>){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(user["uid"].toString()).setValue(user).addOnCompleteListener {
            if (it.isSuccessful){
                startNextActivity()
            }
        }.addOnFailureListener {
            errorConstraint()
            tv_email_verification_error.text = "${it.message}"
        }
    }

    //constraint functions
    private fun closeKeyboard(){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun defaultConstraint(){
        b_email_verification_send.setBackgroundResource(R.drawable.greyed_out_button)
        b_email_verification_send.alpha = 0.5f
        b_email_verification_send.isClickable = true
        b_email_verification_sign_in.isClickable = false
        b_email_verification_send.visibility = View.VISIBLE
        b_email_verification_sign_in.visibility = View.INVISIBLE
        tv_email_verification_error.visibility = View.GONE
        iv_email_verification_checkmark.visibility = View.INVISIBLE
        iv_email_verification_x.visibility = View.INVISIBLE

    }
    private fun errorConstraint(){
        b_email_verification_send.isClickable = true
        b_email_verification_sign_in.isClickable = false
        b_email_verification_send.visibility = View.VISIBLE
        b_email_verification_sign_in.visibility = View.INVISIBLE
        tv_email_verification_error.visibility = View.VISIBLE
    }

    //stop functions
    private var stopEmailVerificationWaiter = false

    private fun runEmailVerificationWaiter(email: String, password: String?, username: String?, waitingForVerificationDialog: AlertDialog) {
        job = CoroutineScope(Dispatchers.IO).launch {
            emailVerificationWaiterLogic(email,password,username, waitingForVerificationDialog)
        }
    }

    //coroutine logic

    private suspend fun emailVerificationWaiterLogic(email: String, password: String?, username: String?, waitingForVerificationDialog: AlertDialog) {
        while(!stopEmailVerificationWaiter){
            delay(200)
            val mAuth = FirebaseAuth.getInstance()
            mAuth.currentUser?.reload()
            val uid = mAuth.currentUser?.uid
            val emailVerified = mAuth.currentUser?.isEmailVerified
            val userHash = HashMap<String, Any?>()
            userHash["category"] = ""
            userHash["economicScore"] = ""
            userHash["socialScore"] = ""
            userHash["anonymous"] = "OFF"
            userHash["email"] = "$email"
            userHash["name"] = ""
            userHash["uid"] = "$uid"
            userHash["username"] = "$username"
            userHash["bio"] = ""
            userHash["uri"]= ""
            userHash["password"] = "$password"
            userHash["status"] = "OFFLINE"
            userHash["userTime"] = "0"
            userHash["groupId"] = ""
            userHash["alignmentTime"] = ""
            if(emailVerified == true){
                waitingForVerificationDialog.dismiss()
                stopEmailVerificationWaiter = true
                job?.cancel()
                uploadUserToDatabase(userHash)
            }
        }}

    //error functions

    private fun onEmailDoesntExist() {
        defaultConstraint()
        b_email_verification_send.setBackgroundResource(R.drawable.rounded_button)
        b_email_verification_send.alpha = 1f
        iv_email_verification_checkmark.visibility = View.VISIBLE
        iv_email_verification_x.visibility = View.INVISIBLE
    }
    private fun onIncorrectEmailFormat() {
        errorConstraint()
        b_email_verification_send.setBackgroundResource(R.drawable.greyed_out_button)
        b_email_verification_send.alpha = 0.5f
        tv_email_verification_error.text = "Email formatted incorrectly"
        iv_email_verification_checkmark.visibility = View.INVISIBLE
        iv_email_verification_x.visibility = View.VISIBLE
    }
    private fun onEmailEmpty() {
        errorConstraint()
        b_email_verification_send.setBackgroundResource(R.drawable.greyed_out_button)
        b_email_verification_send.alpha = 0.5f
        tv_email_verification_error.text = "Please enter email"
        iv_email_verification_checkmark.visibility = View.INVISIBLE
        iv_email_verification_x.visibility = View.VISIBLE
    }
    private fun onEmailAlreadyExists() {
        errorConstraint()
        b_email_verification_send.setBackgroundResource(R.drawable.greyed_out_button)
        b_email_verification_send.alpha = 0.5f
        b_email_verification_send.isClickable = false
        b_email_verification_sign_in.isClickable = true
        b_email_verification_send.visibility = View.INVISIBLE
        b_email_verification_sign_in.visibility = View.VISIBLE
        b_email_verification_sign_in.setOnClickListener {
            val email = et_email_verification_email.text.toString()
            val bundle = Bundle()
            bundle.putString("email", email)
            findNavController().navigate(R.id.action_emailVerificationRegistration_to_login, bundle)
        }
        b_email_verification_send.alpha = 0f
        b_email_verification_send.isClickable = false
        iv_email_verification_x.bringToFront()
        tv_email_verification_error.text = "Email linked to an existing account"
        iv_email_verification_checkmark.visibility = View.INVISIBLE
        iv_email_verification_x.visibility = View.VISIBLE
    }
    private fun onEmailVerificationFailedToSend(it: Exception) {
        errorConstraint()
//        stopOnClickEmailCheck = false
//        runOnClickEmailCheck()
        closeKeyboard()
        b_email_verification_send.setBackgroundResource(R.drawable.greyed_out_button)
        b_email_verification_send.alpha = 0.5f
        Toast.makeText(activity, "Email Failed to Send", Toast.LENGTH_SHORT).show()
        tv_email_verification_error.text = "${it.message}"
    }
    private fun onCreateUserFailed(it: Exception) {
        errorConstraint()
//        stopOnClickEmailCheck = false
//        runOnClickEmailCheck()
        closeKeyboard()
        b_email_verification_send.setBackgroundResource(R.drawable.greyed_out_button)
        b_email_verification_send.alpha = 0.5f
        tv_email_verification_error.text = "${it.message}"
    }
}