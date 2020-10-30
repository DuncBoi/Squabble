package com.duncboi.realsquabble.registration

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.profile.ProfileActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_username_registration.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class Login : Fragment() {

    private val args: LoginArgs by navArgs()
    private var job: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onStart() {
        super.onStart()
        val email = args.email
        if (email != "email"){
            et_login_email.setText(email)
        }
    }

    override fun onPause() {
        super.onPause()
        job?.cancel()
    }

    private var googleSignInClient: GoogleSignInClient? = null
    private val RC_SIGN_IN = 1000

    private var onClickUsername = ""
    private var onClickPassword = ""
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        val doubleErrorSet = ConstraintSet()
        doubleErrorSet.clone(login_constraint)

        defaultConstraint()

        et_login_email.addTextChangedListener(object: TextWatcher {
            private var searchFor = ""

            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val searchText = p0.toString().trim().toLowerCase()
                if (searchText == searchFor)
                    return
                searchFor = searchText
                pb_login_email_progress.visibility = View.VISIBLE
                iv_login_email_checkmark.visibility = View.INVISIBLE
                iv_login_email_x.visibility = View.INVISIBLE
                iv_login_password_x.visibility = View.INVISIBLE
                tv_login_password_error.visibility = View.INVISIBLE
                job = CoroutineScope(Main).launch {
                    delay(1000)
                    if (searchText != searchFor)
                        return@launch
                    if (!isEmailValid(searchText)) {
                        if (searchText.isEmpty()) {
                            onUsernameEmpty()
                            pb_login_email_progress.visibility = View.VISIBLE
                            pb_login_email_progress.visibility = View.GONE
                        } else if (searchText.length > 50) {
                            withContext(Main) {
                                onUsernameTooLong()
                                pb_login_email_progress.visibility = View.GONE
                            }
                        } else {
                            val usernameQuery = FirebaseDatabase.getInstance().reference.child("Users")
                                .orderByChild("username").equalTo(searchText)
                            usernameQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(usernameSnapshot: DataSnapshot) {
                                    if (usernameSnapshot.childrenCount > 0) {
                                        onUsernameExists()
                                        pb_login_email_progress.visibility = View.GONE
                                    } else {
                                        defaultConstraint()
                                        pb_login_email_progress.visibility = View.GONE
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {}
                            })
                        }
                    } else {
                        if (searchText.length > 50) {
                            onEmailTooLong()
                            pb_login_email_progress.visibility = View.GONE
                        } else {
                            val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("email").equalTo(searchText)
                            emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.childrenCount > 0) {
                                        pb_login_email_progress.visibility = View.GONE
                                        onEmailAlreadyExists()
                                    } else {
                                        pb_login_email_progress.visibility = View.GONE
                                        defaultConstraint()
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {}
                            })
                        }
                    }
                }
            }
        })
        createRequest()

        b_login_google.setOnClickListener {
            signInGoogle()
        }

        b_login_phone.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("from", "login")
            findNavController().navigate(R.id.action_login_to_phoneAuthentication, bundle)
        }

                b_login_login.setOnClickListener {
                    val username = et_login_email.text.toString().trim()
                    val password = et_login_password.text.toString().trim()
                    val lowerCaseUsername = username.toLowerCase()
                    onClickUsername = lowerCaseUsername
                    if(!isEmailValid(lowerCaseUsername)){
                        val usernameQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("username").equalTo(lowerCaseUsername)
                        usernameQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(snapshot.childrenCount > 0 && lowerCaseUsername.isNotEmpty() && lowerCaseUsername.length <= 50){
                                    for (i in snapshot.children) {
                                        val email = i.child("email").getValue(String::class.java)
                                        val databasePassword =
                                            i.child("password").getValue(String::class.java)
                                        if (email != null && password == databasePassword) {
                                            loginUser(email, password)
                                        } else {
                                            if (databasePassword == "google") {
                                                passwordErrorConstraint()
                                                tv_login_password_error.text =
                                                    "Must sign in with Google"
                                                iv_login_password_x.visibility = View.VISIBLE
                                            } else if (databasePassword == "phone") {
                                                passwordErrorConstraint()
                                                tv_login_password_error.text =
                                                    "Must sign in with Phone Number"
                                                iv_login_password_x.visibility = View.VISIBLE
                                            } else {
                                                if (password.isEmpty()) {
                                                    passwordErrorConstraint()
                                                    tv_login_password_error.text =
                                                        "Please enter password"
                                                    iv_login_password_x.visibility = View.VISIBLE
                                                    et_login_password.addTextChangedListener(object :
                                                        TextWatcher {
                                                        override fun beforeTextChanged(
                                                            p0: CharSequence?,
                                                            p1: Int,
                                                            p2: Int,
                                                            p3: Int
                                                        ) {
                                                        }

                                                        override fun afterTextChanged(p0: Editable?) {}
                                                        override fun onTextChanged(
                                                            p0: CharSequence?,
                                                            p1: Int,
                                                            p2: Int,
                                                            p3: Int
                                                        ) {
                                                            iv_login_password_x.visibility =
                                                                View.GONE
                                                            val text = p0.toString().trim()
                                                            if (text.isEmpty()) {
                                                                passwordErrorConstraint()
                                                                tv_login_password_error.text =
                                                                    "Please enter password"
                                                                iv_login_password_x.visibility =
                                                                    View.VISIBLE
                                                            } else {
                                                                defaultConstraint()
                                                                iv_login_email_checkmark.visibility =
                                                                    View.VISIBLE
                                                            }
                                                        }
                                                    })
                                                } else {
                                                    passwordErrorConstraint()
                                                    tv_login_password_error.text =
                                                        "Incorrect Password"
                                                    iv_login_password_x.visibility = View.VISIBLE
                                                    et_login_password.addTextChangedListener(object :
                                                        TextWatcher {
                                                        override fun beforeTextChanged(
                                                            p0: CharSequence?,
                                                            p1: Int,
                                                            p2: Int,
                                                            p3: Int
                                                        ) {
                                                        }

                                                        override fun afterTextChanged(p0: Editable?) {}
                                                        override fun onTextChanged(
                                                            p0: CharSequence?,
                                                            p1: Int,
                                                            p2: Int,
                                                            p3: Int
                                                        ) {
                                                            iv_login_password_x.visibility =
                                                                View.GONE
                                                            val text = p0.toString().trim()
                                                            if (text.isEmpty()) {
                                                                passwordErrorConstraint()
                                                                tv_login_password_error.text =
                                                                    "Please enter password"
                                                                iv_login_password_x.visibility =
                                                                    View.VISIBLE
                                                            } else {
                                                                defaultConstraint()
                                                                iv_login_email_checkmark.visibility =
                                                                    View.VISIBLE
                                                            }
                                                        }
                                                    })
                                                }
                                            }
                                        }
                                    }
                                }
                                else{
                                    if (lowerCaseUsername.length > 50) {
                                        onEmailTooLong()
                                    } else {
                                        val emailQuery = FirebaseDatabase.getInstance().reference.child("Users")
                                            .orderByChild("email").equalTo(lowerCaseUsername)
                                        emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                    if (snapshot.childrenCount <= 0) {
                                                        if (onClickUsername == lowerCaseUsername) {
                                                            onUsernameDoesntExist()
                                                        } else {
                                                            onClickUsername = "#"
                                                            defaultConstraint()
                                                        }
                                                    }
                                                }
                                                override fun onCancelled(error: DatabaseError) {
                                                }
                                            })
                                        }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })}
                    else{
                        val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("email").equalTo(lowerCaseUsername)
                        emailQuery.addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(snapshot.childrenCount > 0 && lowerCaseUsername.isNotEmpty() && lowerCaseUsername.length <= 50){
                                    for (i in snapshot.children){
                                        val databasePassword = i.child("password").getValue(String::class.java)
                                        if (password == databasePassword) {
                                            loginUser(lowerCaseUsername, password)
                                        }
                                        else {
                                            if (databasePassword == "google") {
                                                passwordErrorConstraint()
                                                tv_login_password_error.text = "Must sign in with Google"
                                                iv_login_password_x.visibility = View.VISIBLE
                                            } else if (databasePassword == "phone") {
                                                passwordErrorConstraint()
                                                tv_login_password_error.text = "Must sign in with Phone Number"
                                                iv_login_password_x.visibility = View.VISIBLE
                                            } else {
                                                if (password.isEmpty()) {
                                                    passwordErrorConstraint()
                                                    tv_login_password_error.text = "Please enter password"
                                                    iv_login_password_x.visibility = View.VISIBLE
                                                    et_login_password.addTextChangedListener(object : TextWatcher {

                                                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                                                        override fun afterTextChanged(p0: Editable?) {}
                                                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                                                            iv_login_password_x.visibility =
                                                                View.GONE
                                                            val text = p0.toString().trim()
                                                            if (text.isEmpty()) {
                                                                passwordErrorConstraint()
                                                                tv_login_password_error.text =
                                                                    "Please enter password"
                                                                iv_login_password_x.visibility =
                                                                    View.VISIBLE
                                                            } else {
                                                                defaultConstraint()
                                                                iv_login_email_checkmark.visibility =
                                                                    View.VISIBLE
                                                            }
                                                        }
                                                    })
                                                } else {
                                                    passwordErrorConstraint()
                                                    tv_login_password_error.text =
                                                        "Incorrect Password"
                                                    iv_login_password_x.visibility = View.VISIBLE
                                                    et_login_password.addTextChangedListener(object :
                                                        TextWatcher {
                                                        override fun beforeTextChanged(
                                                            p0: CharSequence?,
                                                            p1: Int,
                                                            p2: Int,
                                                            p3: Int
                                                        ) {
                                                        }

                                                        override fun afterTextChanged(p0: Editable?) {}
                                                        override fun onTextChanged(
                                                            p0: CharSequence?,
                                                            p1: Int,
                                                            p2: Int,
                                                            p3: Int
                                                        ) {
                                                            iv_login_password_x.visibility =
                                                                View.GONE
                                                            val text = p0.toString().trim()
                                                            if (text.isEmpty()) {
                                                                passwordErrorConstraint()
                                                                tv_login_password_error.text =
                                                                    "Please enter password"
                                                                iv_login_password_x.visibility =
                                                                    View.VISIBLE
                                                            } else {
                                                                defaultConstraint()
                                                                iv_login_email_checkmark.visibility =
                                                                    View.VISIBLE
                                                            }
                                                        }
                                                    })
                                                }
                                            }
                                        }
                                    }
                                }
                                else{
                                    if (snapshot.childrenCount <= 0){
                                        onUsernameDoesntExist()
                                    }
                                    else{
                                        onEmailTooLong()
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })}
                }

                tv_login_backtoregistration.setOnClickListener {
                    findNavController().navigate(R.id.action_login_to_usernameRegistration)
                }

                tv_forgot_password.setOnClickListener {
                    val email = et_login_email.text.toString().trim()
                    val bundle = Bundle()
                    bundle.putString("email", email)
                    findNavController().navigate(R.id.action_login_to_forgotPassword, bundle)

                }
            }

            private fun createRequest() {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                googleSignInClient = activity?.let { GoogleSignIn.getClient(it, gso) }
            }

            private fun signInGoogle() {
                var signInIntent = googleSignInClient?.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }

            private fun firebaseAuthWithGoogle(idToken: String) {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val loadingDialog = loadingDialog()
                loadingDialog.show()
                activity?.let {
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener(it) { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                val email = user?.email
                                val usernameQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("email").equalTo(email)
                                usernameQuery.addListenerForSingleValueEvent(object : ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if(snapshot.childrenCount <= 0) {
                                            loadingDialog.dismiss()
                                            displayGoogleEmailExistsDialog()
                                            GoogleSignIn.getClient(activity!!, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut()
                                            auth.currentUser!!.delete()
                                        } else{
                                            loadingDialog.dismiss()
                                            startNextActivity()}
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                            } else {
                                loadingDialog.dismiss()
                                Toast.makeText(activity, "${task.exception}", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }

            private fun loadingDialog(): androidx.appcompat.app.AlertDialog {
                val loading = activity?.let { androidx.appcompat.app.AlertDialog.Builder(it) }
                loading!!.setView(R.layout.google_loading)
                loading.setCancelable(false)
                val loadingDialog = loading.create()
                return loadingDialog
            }

            private fun displayGoogleEmailExistsDialog(){
                val builder = activity?.let { AlertDialog.Builder(it) }
                builder!!.setTitle("Google sign in failed")
                builder.setMessage("You must register an account linked to this email before logging in")
                builder.setCancelable(false)
                builder.setPositiveButton("Ok"){ dialog, _ ->
                    dialog.dismiss()
                }
                builder.setNegativeButton("Register"){ _, _ ->
                    findNavController().navigate(R.id.action_login_to_usernameRegistration)
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }

            override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
                super.onActivityResult(requestCode, resultCode, data)

                if (requestCode == RC_SIGN_IN) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    try {
                        val account = task.getResult(ApiException::class.java)!!
                        firebaseAuthWithGoogle(account.idToken!!)
                    } catch (e: ApiException) {
                        Log.d("Moose", "Google sign in failed", e)
                    }
                }
            }

            //firebase user login
            private fun loginUser(email: String, password: String) {
                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
                val loggingIndialog = loggingInDialog()
                loggingIndialog.show()
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (!user!!.isEmailVerified) {
                            loggingIndialog.dismiss()
                            closeKeyboard()
                            Toast.makeText(activity, "Your email has not been verified, please do so now", Toast.LENGTH_LONG).show()
                            val bundle = Bundle()
                            val email = et_login_email.text.toString().trim()
                            bundle.putString("email", email)
                            findNavController().navigate(R.id.action_login_to_emailVerificationRegistration, bundle)
                        } else {
                            loggingIndialog.dismiss()
                            Toast.makeText(activity, "Signed in", Toast.LENGTH_SHORT).show()
                            startNextActivity()
                        }
                    }
                }
                    .addOnFailureListener {
                        loggingIndialog.dismiss()
                        passwordErrorLogic(it)
                        FirebaseAuth.getInstance().signOut()
                    }
            }

            private fun startNextActivity() {
                val intent = Intent(activity, ProfileActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }

            private fun loggingInDialog(): androidx.appcompat.app.AlertDialog {
                val sendingEmail = activity?.let { androidx.appcompat.app.AlertDialog.Builder(it) }
                sendingEmail!!.setView(R.layout.logging_in)
                sendingEmail.setCancelable(false)
                val sendingEmailDialog = sendingEmail.create()
                return sendingEmailDialog
            }

    private fun closeKeyboard(){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

            private fun defaultConstraint() {
                val set = ConstraintSet()
                val loginLayout = login_constraint
                iv_login_email_x.visibility = View.INVISIBLE
                iv_login_email_checkmark.visibility = View.INVISIBLE
                set.clone(loginLayout)
                set.clear(tv_login_email_error.id, ConstraintSet.TOP)
                set.connect(tv_login_email_error.id, ConstraintSet.TOP, et_login_email.id, ConstraintSet.TOP)
                set.clear(login_passwordTIL.id, ConstraintSet.TOP)
                set.connect(login_passwordTIL.id, ConstraintSet.TOP, et_login_email.id, ConstraintSet.BOTTOM, 24)
                set.clear(tv_login_password_error.id, ConstraintSet.TOP)
                set.connect(tv_login_password_error.id, ConstraintSet.TOP, login_passwordTIL.id, ConstraintSet.TOP)
                set.connect(b_login_login.id, ConstraintSet.TOP, login_passwordTIL.id, ConstraintSet.BOTTOM, 24)
                set.connect(tv_forgot_password.id, ConstraintSet.TOP, b_login_login.id, ConstraintSet.BOTTOM, 200)
                set.applyTo(loginLayout)
            }

            private fun errorConstraint() {
                val defaultSet = ConstraintSet()
                val loginLayout = login_constraint
                defaultSet.clone(loginLayout)
                defaultSet.clear(tv_login_email_error.id, ConstraintSet.TOP)
                defaultSet.clear(login_passwordTIL.id, ConstraintSet.TOP)
                defaultSet.clear(b_login_login.id, ConstraintSet.TOP)
                defaultSet.connect(tv_login_email_error.id, ConstraintSet.TOP, et_login_email.id, ConstraintSet.BOTTOM, 6)
                defaultSet.connect(login_passwordTIL.id, ConstraintSet.TOP, tv_login_email_error.id, ConstraintSet.BOTTOM, 8)
                defaultSet.connect(b_login_login.id, ConstraintSet.TOP, login_passwordTIL.id, ConstraintSet.BOTTOM, 24)
                defaultSet.applyTo(loginLayout)
            }

            private fun passwordErrorConstraint() {
                val set = ConstraintSet()
                val loginLayout = login_constraint
                set.clone(loginLayout)
                set.clear(tv_login_password_error.id, ConstraintSet.TOP)
                set.clear(b_login_login.id, ConstraintSet.TOP)
                set.connect(tv_login_password_error.id, ConstraintSet.TOP, login_passwordTIL.id, ConstraintSet.BOTTOM, 6)
                set.connect(b_login_login.id, ConstraintSet.TOP, tv_login_password_error.id, ConstraintSet.BOTTOM, 8)
                set.applyTo(loginLayout)
            }


            private fun passwordErrorLogic(it: Exception) {
                val password = et_login_password.text.toString().trim()
                    if (it.message == "The password is invalid or the user does not have a password.") {
                        if (onClickPassword == password) {
                            passwordErrorConstraint()
                            iv_login_password_x.visibility = View.VISIBLE
                            tv_login_password_error.text = "Incorrect password"
                        } else {
                            defaultConstraint()
                            iv_login_email_checkmark.visibility = View.VISIBLE
                            onClickPassword = "|"
                        }
                    } else if (it.message == "We have blocked all requests from this device due to unusual activity. Try again later. [ Too many unsuccessful login attempts. Please try again later. ]") {
                        val builder = activity?.let { it1 -> AlertDialog.Builder(it1) }
                        builder!!.setTitle("Too Many Sign In Attempts")
                        builder.setCancelable(false)
                        builder.setMessage("You have exceeded the maximum number of failed sign in attempts allowed.  Please try again later or reset your password.")
                        builder.setPositiveButton("Ok") { _: DialogInterface?, _: Int ->

                        }
                        builder.setNegativeButton("Reset Password") { _: DialogInterface?, _: Int ->
                            findNavController().navigate(R.id.action_login_to_forgotPassword)
                        }
                        builder.show()
                    } else {
                        Toast.makeText(activity, "${it.message}", Toast.LENGTH_SHORT).show()
                        closeKeyboard()
                    }
            }


            private fun isEmailValid(email: CharSequence): Boolean {
                return Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }

            private fun onEmailTooLong() {
                errorConstraint()
                iv_login_email_checkmark.visibility = View.INVISIBLE
                iv_login_email_x.visibility = View.VISIBLE
                tv_login_email_error.setTextColor(Color.parseColor("#eb4b4b"))
                tv_login_email_error.text = "Email length too long"
            }

            private fun onUsernameTooLong() {
                errorConstraint()
                iv_login_email_checkmark.visibility = View.INVISIBLE
                iv_login_email_x.visibility = View.VISIBLE
                tv_login_email_error.setTextColor(Color.parseColor("#eb4b4b"))
                tv_login_email_error.text = "Username length too long"
            }

            private fun onUsernameExists() {
                defaultConstraint()
                iv_login_email_checkmark.visibility = View.VISIBLE
                iv_login_email_x.visibility = View.INVISIBLE
            }

            private fun onUsernameEmpty() {
                errorConstraint()
                iv_login_email_checkmark.visibility = View.INVISIBLE
                iv_login_email_x.visibility = View.VISIBLE
                tv_login_email_error.text = "Please enter email / username"
                tv_login_email_error.setTextColor(Color.parseColor("#eb4b4b"))
            }

            private fun onEmailAlreadyExists() {
                defaultConstraint()
                iv_login_email_checkmark.visibility = View.VISIBLE
                iv_login_email_x.visibility = View.INVISIBLE
            }

            private fun onUsernameDoesntExist() {
                errorConstraint()
                iv_login_email_checkmark.visibility = View.INVISIBLE
                iv_login_email_x.visibility = View.VISIBLE
                tv_login_email_error.text = "User does not exist"
                tv_login_email_error.setTextColor(Color.parseColor("#eb4b4b"))
            }
            }