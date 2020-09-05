package com.duncboi.realsquabble.registration

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
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
import com.duncboi.realsquabble.profile.ProfileActivity
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.UserInfo
import com.duncboi.realsquabble.political.Political
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
import kotlinx.android.synthetic.main.fragment_email_registration.*
import kotlinx.coroutines.*


class EmailRegistration : Fragment() {

    private val args: EmailRegistrationArgs by navArgs()

    private var onClickEmail: String = ""
    private var googleSignInClient: GoogleSignInClient? = null
    private val RC_SIGN_IN = 1000
    private lateinit var auth: FirebaseAuth

    override fun onResume() {
        super.onResume()
        val emailPassed = args.email
        if(emailPassed != "null" && emailPassed != "email"){
            et_email_email.setText(emailPassed)
        }
        stopLiveEmailCheck = false
        runLiveEmailCheck()
    }

    override fun onPause() {
        super.onPause()
        stopLiveEmailCheck = true
        stopOnClickEmailCheck = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_email_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultConstraint()
        auth = Firebase.auth
        createRequest()
        b_email_google.setOnClickListener {
            signInGoogle()
        }

        //On Click Listeners
        b_email_phone.setOnClickListener {}

        b_email_next.setOnClickListener {

            stopLiveEmailCheck = true
            val email = et_email_email.text.toString().trim()
            val lowerCaseEmail = email.toLowerCase()
            onClickEmail = email

            val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("email").equalTo(lowerCaseEmail)
            emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(isEmailValid(lowerCaseEmail) && snapshot.childrenCount <= 0) {
                        val bundle = Bundle()
                        bundle.putString("email",lowerCaseEmail)
                        bundle.putString("username", args.username)
                        bundle.putString("password", args.password)
                        findNavController().navigate(R.id.action_emailRegistration_to_passwordRegistration, bundle)
                    }
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
            closeKeyboard()
            val bundle = Bundle()
            val username = args.username
            val password = args.password
            val email = et_email_email.text.toString().trim().toLowerCase()
            bundle.putString("username", username)
            bundle.putString("email", email)
            bundle.putString("password", password)
            findNavController().navigate(R.id.action_emailRegistration_to_usernameRegistration, bundle)
       }
    }

    private fun createRequest() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
            googleSignInClient = GoogleSignIn.getClient(requireActivity().applicationContext, gso)
    }

    private fun signInGoogle() {
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }



    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val loadingDialog = loadingDialog()
        loadingDialog?.show()
        activity?.let {
            auth.signInWithCredential(credential)
                .addOnCompleteListener(it) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val currentUser = auth.currentUser
                        val email = currentUser?.email
                        val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("email").equalTo(email)
                        emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {}
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(snapshot.childrenCount > 0){
                                    FirebaseAuth.getInstance().signOut()
                                     GoogleSignIn.getClient(it, GoogleSignInOptions.Builder(
                                     GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut()
                                    loadingDialog?.dismiss()
                                    displayErrorMessage()
                                } else{
                                    loadingDialog?.dismiss()
                                    val username = args.username
                                    val uid = currentUser?.uid
                                    val user = UserInfo(
                                        username,
                                        email,
                                        "google",
                                        uid
                                    )
                                    uploadUserToDatabase(user)
                                }
                            }
                        })
                    } else {
                        loadingDialog?.dismiss()
                        // If sign in fails, display a message to the user.
                        // ...
                        Toast.makeText(it, "${task.exception}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun loadingDialog(): AlertDialog? {
        val loading = activity?.let { AlertDialog.Builder(it) }
        loading!!.setView(R.layout.registering_account)
        loading.setCancelable(false)
        return loading.create()
    }

    private fun displayErrorMessage(){
        val builder = activity?.let { AlertDialog.Builder(it) }
        builder!!.setTitle("Google registration failed")
        builder.setMessage("There is already an account linked to this email address")
        builder.setCancelable(false)
        builder.setPositiveButton("Ok"){ dialog, _ ->
            dialog.dismiss()
        }
        builder.setNegativeButton("Sign in"){ _, _ ->
            findNavController().navigate(R.id.action_emailRegistration_to_login)
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun uploadUserToDatabase(user: UserInfo){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(user.username!!).setValue(user).addOnCompleteListener {
            if (it.isSuccessful){
                val intent = Intent(requireActivity().applicationContext, Political::class.java)
                startActivity(intent)
                activity?.finish()
            }}.addOnFailureListener {
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.delete()
            GoogleSignIn.getClient(requireActivity().applicationContext, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut()
            errorConstraint()
            closeKeyboard()
            Toast.makeText(requireContext().applicationContext, "${it.message}", Toast.LENGTH_LONG).show()
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
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    //random
    private fun closeKeyboard(){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    fun isEmailValid(email: CharSequence): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    //constraint functions
    private fun defaultConstraint(){
        b_email_next.isClickable = true
        b_email_sign_in.isClickable = false
        b_email_next.visibility = View.VISIBLE
        b_email_sign_in.visibility = View.INVISIBLE
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
        b_email_next.isClickable = true
        b_email_sign_in.isClickable = false
        b_email_next.visibility = View.VISIBLE
        b_email_sign_in.visibility = View.INVISIBLE
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
        CoroutineScope(Dispatchers.Main).launch {
            onClickEmailCheckLogic()}
    }
    private fun runLiveEmailCheck(){
        CoroutineScope(Dispatchers.Main).launch {
            liveEmailCheck()}
    }
    private suspend fun liveEmailCheck() {
        withContext(Dispatchers.IO){
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
        withContext(Dispatchers.Main) {
            defaultConstraint()
            b_email_next.setBackgroundResource(R.drawable.greyed_out_button)
            b_email_next.alpha = 0.5f
        }
    }
    private fun onEmailDoesntExist() {
        defaultConstraint()
        b_email_next.setBackgroundResource(R.drawable.rounded_button)
        b_email_next.alpha = 1f
        iv_email_checkmark.bringToFront()
        iv_email_checkmark.alpha = 1F
        iv_email_x.alpha = 0F
    }
    private fun onIncorrectEmailFormat() {
        errorConstraint()
        b_email_next.setBackgroundResource(R.drawable.greyed_out_button)
        b_email_next.alpha = 0.5f
        iv_email_x.bringToFront()
        iv_email_x.alpha = 1F
        iv_email_checkmark.alpha = 0F
        tv_email_error.text = "Email formatted incorrectly"
        tv_email_error.setTextColor(Color.parseColor("#eb4b4b"))
    }
    private fun onEmailEmpty() {
        errorConstraint()
        b_email_next.setBackgroundResource(R.drawable.greyed_out_button)
        b_email_next.alpha = 0.5f
        iv_email_x.bringToFront()
        iv_email_x.alpha = 1F
        iv_email_checkmark.alpha = 0F
        tv_email_error.text = "Please enter email"
        tv_email_error.setTextColor(Color.parseColor("#eb4b4b"))
    }
    private fun onEmailAlreadyExists() {
        errorConstraint()
        b_email_next.setBackgroundResource(R.drawable.greyed_out_button)
        b_email_next.alpha = 0.5f
        b_email_next.isClickable = false
        b_email_sign_in.isClickable = true
        b_email_next.visibility = View.INVISIBLE
        b_email_sign_in.visibility = View.VISIBLE
        b_email_sign_in.setOnClickListener {
            val email = et_email_email.text.toString()
            val bundle = Bundle()
            bundle.putString("email", email)
            findNavController().navigate(R.id.action_emailRegistration_to_login, bundle)
        }
        iv_email_x.bringToFront()
        iv_email_x.alpha = 1F
        iv_email_checkmark.alpha = 0f
        tv_email_error.text = "Email linked to an existing account"
        tv_email_error.setTextColor(Color.parseColor("#eb4b4b"))
    }


}

