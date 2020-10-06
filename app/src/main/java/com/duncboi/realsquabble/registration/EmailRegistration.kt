package com.duncboi.realsquabble.registration

import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.duncboi.realsquabble.profile.ProfileActivity
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.UserInfo
import com.duncboi.realsquabble.messenger.Users
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
import kotlinx.android.synthetic.main.fragment_username_registration.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main


class EmailRegistration : Fragment() {

    private val args: EmailRegistrationArgs by navArgs()

    private var job: Job? = null
    private var job2: Job? = null
    private var job3: Job? = null
    private var googleSignInClient: GoogleSignInClient? = null
    private val RC_SIGN_IN = 1000
    private lateinit var auth: FirebaseAuth

    override fun onResume() {
        super.onResume()
        val emailPassed = args.email
        if(emailPassed != "null" && emailPassed != "email"){
            et_email_email.setText(emailPassed)
        }
    }

    override fun onPause() {
        super.onPause()
        job?.cancel()
        job2?.cancel()
        job3?.cancel()
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

        et_email_email.addTextChangedListener(object: TextWatcher {
            private var searchFor = ""

            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val searchText = p0.toString().trim().toLowerCase()
                if (searchText == searchFor)
                    return
                searchFor = searchText
                defaultConstraint()
                if (searchText == "") {
                     job2 = CoroutineScope(Main).launch {
                        defaultConstraint()
                        pb_email_progress.visibility = View.INVISIBLE
                        delay(3000)
                         if(searchText != searchFor)
                             return@launch
                        onEmailEmpty()
                    }
                }
                else if (!isEmailValid(searchText)){
                     job3 = CoroutineScope(Main).launch {
                        defaultConstraint()
                        pb_email_progress.visibility = View.INVISIBLE
                        delay(3000)
                         if(searchText != searchFor)
                             return@launch
                        onIncorrectEmailFormat()
                    }
                }
                else {
                    pb_email_progress.visibility = View.VISIBLE
                    job = CoroutineScope(Main).launch {
                    delay(1000)
                    if (searchText != searchFor)
                        return@launch
                        val usernameQuery = FirebaseDatabase.getInstance().reference.child("Users")
                            .orderByChild("email").equalTo(searchText)
                        usernameQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.childrenCount > 0) {
                                    onEmailAlreadyExists()
                                    pb_email_progress.visibility = View.INVISIBLE}
                                else {
                                    onEmailDoesntExist()
                                    pb_email_progress.visibility = View.INVISIBLE
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }
            }
        })

        b_email_google.setOnClickListener {
            createRequest()
            signInGoogle()
        }

        //On Click Listeners
        b_email_phone.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("username", "${args.username}")
            findNavController().navigate(R.id.action_emailRegistration_to_phoneAuthentication, bundle)
        }

        b_email_next.setOnClickListener {

            val email = et_email_email.text.toString().trim()
            val lowerCaseEmail = email.toLowerCase()

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
                        if (lowerCaseEmail.isEmpty()){
                            onEmailEmpty()
                        }
                        else if (!isEmailValid(lowerCaseEmail)){
                            onIncorrectEmailFormat()
                        }
                        }
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
                                    val user = Users("", "", "", "OFF", "$email", "", "$uid", "$username", "", "", "google", "OFFLINE", "0", "")
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

    private fun uploadUserToDatabase(user: Users){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(user.getUid()!!).setValue(user).addOnCompleteListener {
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
        b_email_next.setBackgroundResource(R.drawable.greyed_out_button)
        b_email_next.alpha = 0.5f
        val set = ConstraintSet()
        val emailLayout = email_constraint
        iv_email_x.alpha = 0F
        iv_email_checkmark.alpha = 0F
        set.clone(emailLayout)
        set.clear(tv_email_error.id, ConstraintSet.TOP)
        set.connect(tv_email_error.id, ConstraintSet.TOP,et_email_email.id, ConstraintSet.TOP)
        set.connect(b_email_next.id, ConstraintSet.TOP,et_email_email.id, ConstraintSet.BOTTOM, 24)
        set.connect(tv_email_previous.id, ConstraintSet.TOP,b_email_next.id, ConstraintSet.BOTTOM, 150)
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

    //error functions
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
        b_email_next.isClickable = false
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
        b_email_next.isClickable = false
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

