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
import com.duncboi.realsquabble.R
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
import kotlinx.coroutines.Dispatchers.Main


class EmailRegistration : Fragment() {

    private val args: EmailRegistrationArgs by navArgs()

    private var job: Job? = null
    private var googleSignInClient: GoogleSignInClient? = null
    private val RC_SIGN_IN = 1000
    private lateinit var auth: FirebaseAuth

    override fun onPause() {
        super.onPause()
        job?.cancel()
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
                    pb_email_progress.visibility = View.VISIBLE
                    job = CoroutineScope(Main).launch {
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
                                        pb_email_progress.visibility = View.INVISIBLE
                                    } else {
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
            findNavController().popBackStack()
            closeKeyboard()
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
                                    userHash["password"] = "google"
                                    userHash["status"] = "OFFLINE"
                                    userHash["userTime"] = "0"
                                    userHash["groupId"] = ""
                                    userHash["alignmentTime"] = ""
                                    uploadUserToDatabase(userHash)
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

    private fun uploadUserToDatabase(user: HashMap<String, Any?>){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(user["uid"].toString()).setValue(user).addOnCompleteListener {
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
        tv_email_error.visibility = View.GONE
        iv_email_checkmark.visibility = View.INVISIBLE
        iv_email_x.visibility = View.INVISIBLE
    }
    private fun errorConstraint(){
        b_email_next.isClickable = true
        b_email_sign_in.isClickable = false
        b_email_next.visibility = View.VISIBLE
        b_email_sign_in.visibility = View.INVISIBLE
        tv_email_error.visibility = View.VISIBLE

    }

    //error functions
    private fun onEmailDoesntExist() {
        defaultConstraint()
        b_email_next.setBackgroundResource(R.drawable.rounded_button)
        b_email_next.alpha = 1f
        iv_email_checkmark.bringToFront()
        iv_email_checkmark.alpha = 1F
        iv_email_x.alpha = 0F
        iv_email_checkmark.visibility = View.VISIBLE
        iv_email_x.visibility = View.INVISIBLE
    }
    private fun onIncorrectEmailFormat() {
        errorConstraint()
        b_email_next.isClickable = false
        b_email_next.setBackgroundResource(R.drawable.greyed_out_button)
        b_email_next.alpha = 0.5f
        tv_email_error.text = "Email formatted incorrectly"
        iv_email_checkmark.visibility = View.INVISIBLE
        iv_email_x.visibility = View.VISIBLE
    }
    private fun onEmailEmpty() {
        errorConstraint()
        b_email_next.isClickable = false
        b_email_next.setBackgroundResource(R.drawable.greyed_out_button)
        b_email_next.alpha = 0.5f
        tv_email_error.text = "Please enter email"
        iv_email_checkmark.visibility = View.INVISIBLE
        iv_email_x.visibility = View.VISIBLE
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
        tv_email_error.text = "Email linked to an existing account"
        iv_email_checkmark.visibility = View.INVISIBLE
        iv_email_x.visibility = View.VISIBLE
    }


}

