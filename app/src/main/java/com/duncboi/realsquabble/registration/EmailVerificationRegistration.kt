package com.duncboi.realsquabble.registration

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
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
import com.duncboi.realsquabble.political.Political
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.UserInfo
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

    private var onClickEmail: String = ""
    private lateinit var auth: FirebaseAuth
    private val args: EmailVerificationRegistrationArgs by navArgs()

    override fun onPause() {
        super.onPause()
        Log.d("Moose", "onpause")
        stopOnClickEmailCheck = true
        stopLiveEmailCheck = true
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

    override fun onResume() {
        super.onResume()
        stopLiveEmailCheck = false
        runLiveEmailCheck()
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

        b_email_verification_send.setOnClickListener {

            stopLiveEmailCheck = true
            stopOnClickEmailCheck = false
            val email = et_email_verification_email.text.toString().trim()
            onClickEmail = email

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
            })}
        tv_email_verification_previous.setOnClickListener {
            closeKeyboard()
            val bundle = Bundle()
            val password = args.password
            val email = et_email_verification_email.text.toString().trim()
            val username = args.username
            bundle.putString("username", username)
            bundle.putString("email", email)
            bundle.putString("password", password)
            findNavController().navigate(R.id.action_emailVerificationRegistration_to_passwordRegistration, bundle)
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
            stopOnClickEmailCheck = false
            runOnClickEmailCheck()
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
    private fun uploadUserToDatabase(user: UserInfo){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(user.username!!).setValue(user).addOnCompleteListener {
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
        b_email_verification_send.isClickable = true
        b_email_verification_sign_in.isClickable = false
        b_email_verification_send.visibility = View.VISIBLE
        b_email_verification_sign_in.visibility = View.INVISIBLE
        val set = ConstraintSet()
        val emailLayout = email_verification_constraint
        iv_email_verification_x.alpha = 0F
        iv_email_verification_checkmark.alpha = 0F
        set.clone(email_verification_constraint)
        set.clear(tv_email_verification_error.id, ConstraintSet.TOP)
        set.connect(tv_email_verification_error.id, ConstraintSet.TOP,et_email_verification_email.id, ConstraintSet.TOP)
        set.connect(b_email_verification_send.id, ConstraintSet.TOP,et_email_verification_email.id, ConstraintSet.BOTTOM, 24)
        set.connect(tv_email_verification_previous.id, ConstraintSet.TOP,b_email_verification_send.id, ConstraintSet.BOTTOM, 200)
        set.applyTo(emailLayout)
    }
    private fun errorConstraint(){
        b_email_verification_send.isClickable = true
        b_email_verification_sign_in.isClickable = false
        b_email_verification_send.visibility = View.VISIBLE
        b_email_verification_sign_in.visibility = View.INVISIBLE
        val defaultSet = ConstraintSet()
        val emailLayout = email_verification_constraint
        defaultSet.clone(email_verification_constraint)
        defaultSet.clear(b_email_verification_send.id, ConstraintSet.TOP)
        defaultSet.clear(tv_email_verification_error.id, ConstraintSet.TOP)
        defaultSet.connect(tv_email_verification_error.id, ConstraintSet.TOP, et_email_verification_email.id, ConstraintSet.BOTTOM, 6)
        defaultSet.connect(b_email_verification_send.id, ConstraintSet.TOP, tv_email_verification_error.id, ConstraintSet.BOTTOM, 12)
        defaultSet.applyTo(emailLayout)
    }

    //stop functions
    private var stopOnClickEmailCheck = false
    private var stopLiveEmailCheck = false
    private var stopEmailVerificationWaiter = false

    //runner functions
    private fun runOnClickEmailCheck(){
        CoroutineScope(Dispatchers.Main).launch {
            onClickEmailCheckLogic()}
    }
    private fun runLiveEmailCheck(){
        CoroutineScope(Dispatchers.IO).launch {
            liveEmailCheckLogic()}
    }
    private fun runEmailVerificationWaiter(email: String, password: String?, username: String?, waitingForVerificationDialog: AlertDialog) {
        CoroutineScope(Dispatchers.IO).launch {
            emailVerificationWaiterLogic(email,password,username, waitingForVerificationDialog)
        }
    }

    //coroutine logic
    private suspend fun onClickEmailCheckLogic(){
        while (!stopOnClickEmailCheck){
            delay(200)
            val email = et_email_verification_email.text.toString().trim()
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

            val email = et_email_verification_email.text.toString().trim()
            val lowerCaseEmail = email.toLowerCase()

            if (!isEmailValid(lowerCaseEmail)) defaultMainThread()
            else{
                val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("email").equalTo(lowerCaseEmail)
                emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.childrenCount > 0) { onEmailAlreadyExists() }
                        else onEmailDoesntExist()
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }}
    }
    private suspend fun emailVerificationWaiterLogic(email: String, password: String?, username: String?, waitingForVerificationDialog: AlertDialog) {
        while(!stopEmailVerificationWaiter){
            delay(200)
            val mAuth = FirebaseAuth.getInstance()
            mAuth.currentUser?.reload()
            val uid = mAuth.currentUser?.uid
            val emailVerified = mAuth.currentUser?.isEmailVerified
            val userInfo =
                UserInfo(username, email, password, uid)
            if(emailVerified == true){
                waitingForVerificationDialog.dismiss()
                stopEmailVerificationWaiter = true
                uploadUserToDatabase(userInfo)}
        }}

    //error functions
    private suspend fun defaultMainThread() {
        withContext(Dispatchers.Main) {
            defaultConstraint()
            b_email_verification_send.setBackgroundResource(R.drawable.greyed_out_button)
            b_email_verification_send.alpha = 0.5f
        }
    }
    private fun onEmailDoesntExist() {
        defaultConstraint()
        b_email_verification_send.setBackgroundResource(R.drawable.rounded_button)
        b_email_verification_send.alpha = 1f
        iv_email_verification_checkmark.bringToFront()
        iv_email_verification_checkmark.alpha = 1F
        iv_email_verification_x.alpha = 0F
    }
    private fun onIncorrectEmailFormat() {
        errorConstraint()
        b_email_verification_send.setBackgroundResource(R.drawable.greyed_out_button)
        b_email_verification_send.alpha = 0.5f
        iv_email_verification_x.bringToFront()
        iv_email_verification_x.alpha = 1F
        iv_email_verification_checkmark.alpha = 0F
        tv_email_verification_error.text = "Email formatted incorrectly"
        tv_email_verification_error.setTextColor(Color.parseColor("#eb4b4b"))
    }
    private fun onEmailEmpty() {
        errorConstraint()
        b_email_verification_send.setBackgroundResource(R.drawable.greyed_out_button)
        b_email_verification_send.alpha = 0.5f
        iv_email_verification_x.bringToFront()
        iv_email_verification_x.alpha = 1F
        iv_email_verification_checkmark.alpha = 0F
        tv_email_verification_error.text = "Please enter email"
        tv_email_verification_error.setTextColor(Color.parseColor("#eb4b4b"))
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
        iv_email_verification_x.alpha = 1F
        iv_email_verification_checkmark.alpha = 0f
        tv_email_verification_error.text = "Email linked to an existing account"
        tv_email_verification_error.setTextColor(Color.parseColor("#eb4b4b"))
    }
    private fun onEmailVerificationFailedToSend(it: Exception) {
        errorConstraint()
        stopOnClickEmailCheck = false
        runOnClickEmailCheck()
        closeKeyboard()
        b_email_verification_send.setBackgroundResource(R.drawable.greyed_out_button)
        b_email_verification_send.alpha = 0.5f
        Toast.makeText(activity, "Email Failed to Send", Toast.LENGTH_SHORT).show()
        tv_email_verification_error.text = "${it.message}"
    }
    private fun onCreateUserFailed(it: Exception) {
        errorConstraint()
        stopOnClickEmailCheck = false
        runOnClickEmailCheck()
        closeKeyboard()
        b_email_verification_send.setBackgroundResource(R.drawable.greyed_out_button)
        b_email_verification_send.alpha = 0.5f
        iv_email_verification_x.alpha = 1F
        tv_email_verification_error.text = "${it.message}"
    }
}