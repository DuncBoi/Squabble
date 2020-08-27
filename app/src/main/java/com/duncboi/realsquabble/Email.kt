package com.duncboi.realsquabble

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.duncboi.realsquabble.R.drawable
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
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_email.*
import kotlinx.android.synthetic.main.activity_email_verification.*
import kotlinx.android.synthetic.main.activity_username.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class Email : AppCompatActivity() {
    private var onClickEmail: String = ""
    var googleSignInClient: GoogleSignInClient? = null
    private val RC_SIGN_IN = 1000
    private lateinit var auth: FirebaseAuth
    override fun onStart() {
        super.onStart()
        val emailPassed = intent.getStringExtra("emailPassed")
        if (emailPassed != null) {
            et_email_email.setText(emailPassed)
        }
        stopLiveEmailCheck = false
        runLiveEmailCheck()
    }

    override fun onStop() {
        super.onStop()
        stopLiveEmailCheck = true
        stopOnClickEmailCheck = true
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email)

        defaultConstraint()
        auth = Firebase.auth
        createRequest()
        b_email_google.setOnClickListener {
            signInGoogle()
        }

        //On Click Listeners
        b_email_phone.setOnClickListener {
            val intent = Intent(this, PhoneAuth::class.java)
            startActivity(intent)
        }

        b_email_next.setOnClickListener {

            stopLiveEmailCheck = true
            val email = et_email_email.text.toString().trim()
            val lowerCaseEmail = email.toLowerCase()
            onClickEmail = email

            val emailQuery = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("email").equalTo(lowerCaseEmail)
            emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(isEmailValid(lowerCaseEmail) && snapshot.childrenCount <= 0) startNextActivity(lowerCaseEmail)
                    else{
                        if (!isEmailValid(lowerCaseEmail)){
                            stopOnClickEmailCheck = false
                            runOnClickEmailCheck()
            }
                        else{
                            stopLiveEmailCheck = false
                            runLiveEmailCheck()
                        }}
                    }
                   })
       }
        tv_email_previous.setOnClickListener {
            val usernameIntent = Intent(this, Username::class.java)
            val usernamePassed = intent.getStringExtra("usernamePassed")
            val passwordPassed = intent.getStringExtra("passwordPassed")
            val emailPassed = et_email_email.text.toString().trim().toLowerCase()
            usernameIntent.putExtra("usernamePassed", usernamePassed)
            usernameIntent.putExtra("emailPassed", emailPassed)
            usernameIntent.putExtra("passwordPassed", passwordPassed)
            startActivity(usernameIntent)
            finish()
        }
    }

    private fun createRequest() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signInGoogle() {
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }



    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val loadingDialog = loadingDialog()
        loadingDialog.show()
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val currentUser = auth.currentUser
                    val email = currentUser?.email
                    val emailQuery = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("email").equalTo(email)
                    emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {}
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(snapshot.childrenCount > 0){
                                FirebaseAuth.getInstance().signOut()
                                GoogleSignIn.getClient(this@Email, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut()
                                loadingDialog.dismiss()
                                displayErrorMessage()
                            }
                            else{
                                loadingDialog.dismiss()
                                val username = intent.getStringExtra("usernamePassed")
                                val uid = currentUser?.uid
                                val user = UserInfo(username, email, "google", uid)
                                uploadUserToDatabase(user)
                               }
                        }
                    })
                } else {
                    loadingDialog.dismiss()
                    // If sign in fails, display a message to the user.
                    // ...
                    Toast.makeText(this, "${task.exception}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun loadingDialog(): androidx.appcompat.app.AlertDialog {
        val loading = androidx.appcompat.app.AlertDialog.Builder(this@Email)
        loading.setView(R.layout.registering_account)
        loading.setCancelable(false)
        val loadingDialog = loading.create()
        return loadingDialog
    }

    private fun displayErrorMessage(){
        val builder = AlertDialog.Builder(this@Email)
        builder.setTitle("Google registration failed")
        builder.setMessage("There is already an account linked to this email address")
        builder.setCancelable(false)
        builder.setPositiveButton("Ok"){dialog, which ->
            dialog.dismiss()
        }
        builder.setNegativeButton("Sign in"){dialog,which ->
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun uploadUserToDatabase(user: com.duncboi.realsquabble.UserInfo){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(user.username!!).setValue(user).addOnCompleteListener {
            if (it.isSuccessful){
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }
        }.addOnFailureListener {
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.delete()
            GoogleSignIn.getClient(this@Email, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut()
            errorConstraint()
            closeKeyboard()
            Toast.makeText(this, "${it.message}", Toast.LENGTH_LONG).show()
        }
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

    //start next activity
    private fun startNextActivity(email: String) {
        val passwordStartIntent = Intent(this@Email, RegisterActivity::class.java)
        val username = intent.getStringExtra("usernamePassed")
        val passwordPassed = intent.getStringExtra("passwordPassed")
        passwordStartIntent.putExtra("passwordPassed", passwordPassed)
        passwordStartIntent.putExtra("usernamePassed", username)
        passwordStartIntent.putExtra("emailPassed", email)
        startActivity(passwordStartIntent)
        finish()
    }

    //random
    private fun closeKeyboard(){
        val view = this.currentFocus
        if (view != null){
            val hideMe = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            hideMe.hideSoftInputFromWindow(view.windowToken, 0)
        }
        //else
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }
    fun isEmailValid(email: CharSequence?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    //constraint functions
    private fun defaultConstraint(){
        tv_email_login.visibility = View.INVISIBLE
        val set = ConstraintSet()
        val emailLayout = email_constraint
        iv_email_x.alpha = 0F
        iv_email_checkmark.alpha = 0F
        set.clone(emailLayout)
        set.clear(tv_email_error.id, ConstraintSet.TOP)
        set.connect(tv_email_error.id, ConstraintSet.TOP,et_email_email.id, ConstraintSet.TOP)
        set.connect(b_email_next.id, ConstraintSet.TOP,et_email_email.id, ConstraintSet.BOTTOM, 24)
        set.connect(tv_email_previous.id, ConstraintSet.TOP,b_email_next.id, ConstraintSet.BOTTOM, 200)
        set.applyTo(emailLayout)
    }
    private fun errorConstraint(){
        tv_email_login.visibility = View.INVISIBLE
        val defaultSet = ConstraintSet()
        val emailLayout = email_constraint
        defaultSet.clone(emailLayout)
        defaultSet.clear(b_email_next.id, ConstraintSet.TOP)
        defaultSet.clear(tv_email_error.id, ConstraintSet.TOP)
        defaultSet.connect(tv_email_error.id, ConstraintSet.TOP, et_email_email.id, ConstraintSet.BOTTOM, 6)
        defaultSet.connect(b_email_next.id, ConstraintSet.TOP, tv_email_error.id, ConstraintSet.BOTTOM, 12)
        defaultSet.applyTo(emailLayout)
    }

    //stop functions
    private var stopOnClickEmailCheck = false
    private var stopLiveEmailCheck = false

    //runner functions
    private fun runOnClickEmailCheck(){
        CoroutineScope(Main).launch {
            onClickEmailCheckLogic()}
    }
    private fun runLiveEmailCheck(){
        CoroutineScope(Main).launch {
            liveEmailCheck()}
    }
    private suspend fun liveEmailCheck() {
        withContext(IO){
            liveEmailCheckLogic()
        }
    }

    //logic functions
    private suspend fun liveEmailCheckLogic(){
        while(!stopLiveEmailCheck){
            delay(200)

            val email = et_email_email.text.toString().trim()
            val lowerCaseEmail = email.toLowerCase()

            if (!isEmailValid(lowerCaseEmail)) defaultMainThread()
            else{
            val emailQuery = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("email").equalTo(lowerCaseEmail)
            emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if(snapshot.childrenCount > 0) { onEmailAlreadyExists() }
                    else onEmailDoesntExist()

                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }}
    }
    private suspend fun onClickEmailCheckLogic(){
        while (!stopOnClickEmailCheck){
            delay(200)
            val email = et_email_email.text.toString().trim()
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

    //error functions
    private suspend fun defaultMainThread() {
        withContext(Main) {
            defaultConstraint()
        }
    }
    private fun onEmailDoesntExist() {
        defaultConstraint()
        iv_email_checkmark.bringToFront()
        iv_email_checkmark.alpha = 1F
        iv_email_x.alpha = 0F
    }
    private fun onIncorrectEmailFormat() {
        errorConstraint()
        iv_email_x.bringToFront()
        iv_email_x.alpha = 1F
        iv_email_checkmark.alpha = 0F
        tv_email_error.setText("Email formatted incorrectly")
        tv_email_error.setTextColor(Color.parseColor("#eb4b4b"))
    }
    private fun onEmailEmpty() {
        errorConstraint()
        iv_email_x.bringToFront()
        iv_email_x.alpha = 1F
        iv_email_checkmark.alpha = 0F
        tv_email_error.setText("Please enter email")
        tv_email_error.setTextColor(Color.parseColor("#eb4b4b"))
    }
    private fun onEmailAlreadyExists() {
        errorConstraint()
        b_email_next.setText("Sign In")
        b_email_next.setOnClickListener {
            val email = et_email_email.text.toString()
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("fpEmail", email)
            startActivity(intent)
        }
        iv_email_x.bringToFront()
        iv_email_x.alpha = 1F
        iv_email_checkmark.alpha = 0f
        tv_email_error.setText("Email linked to an existing account")
        tv_email_error.setTextColor(Color.parseColor("#eb4b4b"))
    }

}