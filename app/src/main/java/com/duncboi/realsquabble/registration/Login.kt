package com.duncboi.realsquabble.registration

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Login : Fragment() {

    private val args: LoginArgs by navArgs()

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
        stopLiveUsernameCheck = true
        stopOnClickUsernameCheck = true
        stopPasswordEmpty = true
        stopPasswordError = true
    }

    private var googleSignInClient: GoogleSignInClient? = null
    private val RC_SIGN_IN = 1000

    private var onClickUsername = ""
    private var onClickPassword = ""
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

                val doubleErrorSet = ConstraintSet()
                doubleErrorSet.clone(login_constraint)

                defaultConstraint()
                runLiveUsernameCheck()
                createRequest()

                b_login_google.setOnClickListener {
                    signInGoogle()
                }

                auth = Firebase.auth

                //Login Button Clicked
                b_login_login.setOnClickListener {
                    stopLiveUsernameCheck = true
                    val username = et_login_email.text.toString().trim()
                    val password = et_login_password.text.toString().trim()
                    val lowerCaseUsername = username.toLowerCase()
                    onClickUsername = lowerCaseUsername
                    if(!isEmailValid(lowerCaseUsername)){
                        val usernameQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("username").equalTo(lowerCaseUsername)
                        usernameQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(snapshot.childrenCount > 0 && lowerCaseUsername.isNotEmpty() && lowerCaseUsername.length <= 50){
                                    val email = snapshot.child("$lowerCaseUsername").child("email").value.toString()
                                    loginUser(email, password)
                                }
                                else{
                                    stopOnClickUsernameCheck = false
                                    runOnClickUsernameCheck()
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })}
                    else{
                        val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("email").equalTo(lowerCaseUsername)
                        emailQuery.addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(snapshot.childrenCount > 0 && lowerCaseUsername.length <= 50){
                                    loginUser(lowerCaseUsername, password)
                                }
                                else{
                                    stopOnClickUsernameCheck = false
                                    runOnClickUsernameCheck()
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })}
                }

                //Dont have an account? button clicked
                tv_login_backtoregistration.setOnClickListener {
                    findNavController().navigate(R.id.action_login_to_usernameRegistration)
                }

                //forget password button clicked
                tv_forgot_password.setOnClickListener {
                    showRecoverPasswordDialog()
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
                                // Sign in success, update UI with the signed-in user's information
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
                                // If sign in fails, display a message to the user.
                                // ...
                                Toast.makeText(activity, "${task.exception}", Toast.LENGTH_LONG).show()
                            }

                            // ...
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

                // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
                if (requestCode == RC_SIGN_IN) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    try {
                        // Google Sign In was successful, authenticate with Firebase
                        val account = task.getResult(ApiException::class.java)!!
                        Log.d("Moose", "firebaseAuthWithGoogle:" + account.id)
                        firebaseAuthWithGoogle(account.idToken!!)
                    } catch (e: ApiException) {
                        // Google Sign In failed, update UI appropriately
                        Log.d("Moose", "Google sign in failed", e)
                        // ...
                    }
                }
            }

            private fun showRecoverPasswordDialog() {
                val bundle = Bundle()
                val email = et_login_email.text.toString().trim()
                bundle.putString("email", email)
                findNavController().navigate(R.id.action_login_to_usernameRegistration)
            }

            //firebase user login
            private fun loginUser(email: String, password: String) {
                stopOnClickUsernameCheck = true
                stopLiveUsernameCheck = true
                onClickPassword = password
                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
                if (password.isEmpty()) {
                    stopPasswordEmpty = false
                    runPasswordEmpty()
                } else {
                    stopPasswordEmpty = true
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
                                findNavController().navigate(R.id.action_login_to_emailVerificationRegistration)
                            } else {
                                val emailQuery = FirebaseDatabase.getInstance().reference.child("Users")
                                    .orderByChild("email").equalTo(email)
                                emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for (childSnapshot in snapshot.children) {
                                            val databasePassword =
                                                childSnapshot.child("password").getValue<String>()
                                                    .toString()
                                            if (databasePassword != password) {
                                                snapshot.ref.child(childSnapshot.key!!)
                                                    .child("password").setValue("$password")
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                                loggingIndialog.dismiss()
                                Toast.makeText(activity, "Signed in", Toast.LENGTH_SHORT).show()
                                startNextActivity()
                            }
                        }
                    }
                        .addOnFailureListener {
                            loggingIndialog.dismiss()
                            stopPasswordError = false
                            runPasswordError(it)
                            FirebaseAuth.getInstance().signOut()
                        }
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
                iv_login_email_x.alpha = 0F
                iv_login_password_x.alpha = 0F
                iv_login_email_checkmark.alpha = 0F
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

            private var stopLiveUsernameCheck = false
            private var stopOnClickUsernameCheck = false
            private var stopPasswordError = false
            private var stopPasswordEmpty = false

            //Coroutine Runner Functions

            private fun runPasswordEmpty() {
                CoroutineScope(Main).launch {
                    passwordEmptyLogic()
                }
            }

            private fun runPasswordError(it: Exception) {
                CoroutineScope(Main).launch {
                    passwordErrorLogic(it)
                }
            }

            private fun runLiveUsernameCheck() {
                CoroutineScope(Main).launch {
                    liveUsernameCheck()
                }
            }

            private suspend fun liveUsernameCheck() {
                withContext(IO) {
                    usernameCheckLogic()
                }
            }

            private fun runOnClickUsernameCheck() {
                CoroutineScope(IO).launch {
                    onClickUsernameLogic()
                }
            }

            //Logic Functions
            private suspend fun passwordEmptyLogic() {
                while (!stopPasswordEmpty) {
                    delay(200)
                    onEmailEditTextClicked()
                    val password = et_login_password.text.toString().trim()
                    if (password.isEmpty()) {
                        if (onClickPassword == password) {
                            passwordErrorConstraint()
                            tv_login_password_error.text = "Please enter password"
                            iv_login_password_x.alpha = 1F
                        }
                    } else {
                        defaultConstraint()
                        iv_login_email_checkmark.alpha = 1F
                    }
                }
            }

            private fun onEmailEditTextClicked() {
                et_login_email.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        Log.d("Moose", "bruh")
                        stopPasswordEmpty = true
                        stopPasswordError = true
                        stopLiveUsernameCheck = false
                        runLiveUsernameCheck()
                    }
                }
            }

            private suspend fun passwordErrorLogic(it: Exception) {
                while (!stopPasswordError) {
                    delay(200)
                    onEmailEditTextClicked()
                    val password = et_login_password.text.toString().trim()
                    if (it.message == "The password is invalid or the user does not have a password.") {
                        if (onClickPassword == password) {
                            passwordErrorConstraint()
                            iv_login_password_x.bringToFront()
                            iv_login_password_x.alpha = 1F
                            tv_login_password_error.text = "Incorrect password"
                        } else {
                            defaultConstraint()
                            iv_login_email_checkmark.alpha = 1F
                            onClickPassword = "|"
                        }
                    } else if (it.message == "We have blocked all requests from this device due to unusual activity. Try again later. [ Too many unsuccessful login attempts. Please try again later. ]") {
                        stopPasswordError = true
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
                        stopPasswordError = true
                        stopLiveUsernameCheck = false
                        runLiveUsernameCheck()
                        Toast.makeText(activity, "${it.message}", Toast.LENGTH_SHORT).show()
                        closeKeyboard()
                    }
                }
            }

            private suspend fun onClickUsernameLogic() {
                stopLiveUsernameCheck = true
                while (!stopOnClickUsernameCheck) {
                    delay(100)
                    val username = et_login_email.text.toString().trim()
                    val lowerCaseUsername = username.toLowerCase()
                    if (!isEmailValid(lowerCaseUsername)) {
                        if (lowerCaseUsername.isEmpty()) {
                            withContext(Main) {
                                onUsernameEmpty()
                            }
                        } else if (lowerCaseUsername.length > 50) {
                            withContext(Main) {
                                onUsernameTooLong()
                            }
                        } else {
                            val usernameQuery = FirebaseDatabase.getInstance().reference.child("Users")
                                .orderByChild("username").equalTo(lowerCaseUsername)
                            usernameQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.childrenCount <= 0) {
                                        if (onClickUsername == lowerCaseUsername) {
                                            onUsernameDoesntExist()
                                        } else {
                                            onClickUsername = "^"
                                            defaultConstraint()
                                        }
                                    } else {
                                        stopOnClickUsernameCheck = true
                                        stopLiveUsernameCheck = false
                                        runLiveUsernameCheck()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                }
                            })
                        }
                    } else {
                        if (lowerCaseUsername.length > 50) {
                            withContext(Main) {
                                onEmailTooLong()
                            }
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
                                    } else {
                                        stopOnClickUsernameCheck = true
                                        stopLiveUsernameCheck = false
                                        runLiveUsernameCheck()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                }
                            })

                        }
                    }
                }
            }

            private suspend fun usernameCheckLogic() {
                stopPasswordEmpty = true
                stopPasswordError = true
                while (!stopLiveUsernameCheck) {

                    delay(200)

                    val username = et_login_email.text.toString().trim()
                    val lowerCaseUsername = username.toLowerCase()

                    if (!isEmailValid(lowerCaseUsername)) {
                        if (lowerCaseUsername.isEmpty()) {
                            defaultMainThread()
                        } else if (lowerCaseUsername.length > 50) {
                            withContext(Main) {
                                onUsernameTooLong()
                            }
                        } else {
                            val usernameQuery = FirebaseDatabase.getInstance().reference.child("Users")
                                .orderByChild("username").equalTo(lowerCaseUsername)
                            usernameQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(usernameSnapshot: DataSnapshot) {
                                    if (usernameSnapshot.childrenCount > 0) {
                                        onUsernameExists()
                                    } else {
                                        defaultConstraint()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                        }
                    } else {
                        if (lowerCaseUsername.length > 50) {
                            onEmailTooLong()
                        } else {
                            val emailQuery =
                                FirebaseDatabase.getInstance().reference.child("Users")
                                    .orderByChild("email").equalTo(lowerCaseUsername)
                            emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {

                                    if (snapshot.childrenCount > 0) {
                                        onEmailAlreadyExists()
                                    } else defaultConstraint()

                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                        }
                    }
                }
            }

            private fun isEmailValid(email: CharSequence): Boolean {
                return Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }

            //Error Functions
            private suspend fun defaultMainThread() {
                withContext(Main) {
                    defaultConstraint()
                }
            }

            private suspend fun onEmailTooLong() {
                withContext(Main) {
                    errorConstraint()
                    iv_login_email_x.bringToFront()
                    iv_login_email_checkmark.alpha = 0F
                    iv_login_email_x.alpha = 1F
                    tv_login_email_error.setTextColor(Color.parseColor("#eb4b4b"))
                    tv_login_email_error.text = "Email length too long"
                }
            }

            private fun onUsernameTooLong() {
                errorConstraint()
                iv_login_email_x.bringToFront()
                iv_login_email_checkmark.alpha = 0F
                iv_login_email_x.alpha = 1F
                tv_login_email_error.setTextColor(Color.parseColor("#eb4b4b"))
                tv_login_email_error.text = "Username length too long"
            }

            private fun onUsernameExists() {
                defaultConstraint()
                iv_login_email_checkmark.bringToFront()
                iv_login_email_checkmark.alpha = 1F
                iv_login_email_x.alpha = 0F
            }

            private fun onUsernameEmpty() {
                errorConstraint()
                iv_login_email_x.bringToFront()
                iv_login_email_checkmark.alpha = 0F
                iv_login_email_x.alpha = 1F
                tv_login_email_error.text = "Please enter email / username"
                tv_login_email_error.setTextColor(Color.parseColor("#eb4b4b"))
            }

            private fun onEmailAlreadyExists() {
                defaultConstraint()
                iv_login_email_checkmark.bringToFront()
                iv_login_email_checkmark.alpha = 1F
                iv_login_email_x.alpha = 0f
            }

            private fun onUsernameDoesntExist() {
                errorConstraint()
                iv_login_email_x.bringToFront()
                iv_login_email_x.alpha = 1F
                iv_login_email_checkmark.alpha = 0F
                tv_login_email_error.text = "User does not exist"
                tv_login_email_error.setTextColor(Color.parseColor("#eb4b4b"))
            }
            }