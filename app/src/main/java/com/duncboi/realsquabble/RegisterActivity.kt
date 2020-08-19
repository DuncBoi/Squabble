package com.duncboi.realsquabble

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.util.Log
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_email.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_username.*
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {
    private var bruhPassword: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        passwordError2.run()
        b_password_next.setOnClickListener {
            handler2.removeCallbacks(passwordError2)
            val password = et_password.text.toString().trim()
            bruhPassword = password
            if (password.isEmpty() || password.length < 6 || !isValidPassword(password)){
                passwordError.run()}
            else {
                handler2.removeCallbacks(passwordError2)
                handler.removeCallbacks(passwordError)
                val bruh = Intent(this@RegisterActivity, EmailVerification::class.java)
                val username = intent.getStringExtra("username")
                val email = intent.getStringExtra("email")
                bruh.putExtra("username", username)
                bruh.putExtra("email", email)
                bruh.putExtra("password", password)
                startActivity(bruh)
            }
        }
        tv_password_previous.setOnClickListener {
           finish()
        }

        //action bar
//        var actionBar = supportActionBar
//        actionBar?.setTitle("Create Account")
//        actionBar?.setDisplayHomeAsUpEnabled(true)
//        actionBar?.setDisplayShowHomeEnabled(true)

        //Register button clicked
//        b_password_next.setOnClickListener {
//            //performRegister()
//        }
       }
    private fun closeKeyboard(){
        val view = this.currentFocus
        if (view != null){
            val hideMe = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            hideMe.hideSoftInputFromWindow(view.windowToken, 0)
        }
        //else
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }
    private fun defaultConstraint(){
        val set = ConstraintSet()
        val passwordConstraint = password_constraint
        iv_password_x.alpha = 0F
        iv_password_checkmark.alpha = 0F
        set.clone(passwordConstraint)
        set.clear(tv_password_error.id, ConstraintSet.TOP)
        set.connect(tv_password_error.id, ConstraintSet.TOP,passwordTIL.id, ConstraintSet.TOP)
        set.connect(b_password_next.id, ConstraintSet.TOP,passwordTIL.id, ConstraintSet.BOTTOM, 24)
        set.connect(tv_password_previous.id, ConstraintSet.TOP,b_password_next.id, ConstraintSet.BOTTOM, 200)
        set.applyTo(passwordConstraint)
    }
    private fun errorConstraint(){
        val defaultSet = ConstraintSet()
        val passwordLayout = password_constraint
        defaultSet.clone(passwordLayout)
        defaultSet.clear(b_password_next.id, ConstraintSet.TOP)
        defaultSet.clear(tv_password_error.id, ConstraintSet.TOP)
        defaultSet.connect(tv_password_error.id, ConstraintSet.TOP, passwordTIL.id, ConstraintSet.BOTTOM, 6)
        defaultSet.connect(b_password_next.id, ConstraintSet.TOP, tv_password_error.id, ConstraintSet.BOTTOM, 12)
        defaultSet.applyTo(passwordLayout)
    }
    val handler = Handler()
    var stopped = false
    private val passwordError: Runnable = object : Runnable {
        override fun run() {
            try{
                val passwordUpdated = et_password.text.toString().trim()
                val isPasswordMix = isValidPassword(passwordUpdated)
                if(passwordUpdated.isEmpty()){
                    errorConstraint()
                    iv_password_x.bringToFront()
                    iv_password_x.alpha = 1F
                    iv_password_checkmark.alpha = 0F
                    tv_password_error.setTextColor(Color.parseColor("#eb4b4b"))
                    tv_password_error.setText("Please enter a password")
                }
                else if (passwordUpdated.length < 6){
                    if(bruhPassword != passwordUpdated) {
                        bruhPassword = "*"
                        defaultConstraint()
                        iv_password_x.alpha = 0F
                    }
                    else {
                        errorConstraint()
                        iv_password_x.bringToFront()
                        iv_password_checkmark.alpha = 0F
                        iv_password_x.alpha = 1F
                        tv_password_error.setTextColor(Color.parseColor("#eb4b4b"))
                        tv_password_error.setText("Password must be at least 6 characters")
                    }
                }
                else if(!isPasswordMix){
                    errorConstraint()
                    iv_password_x.bringToFront()
                    iv_password_x.alpha = 1F
                    iv_password_checkmark.alpha = 0F
                    tv_password_error.setTextColor(Color.parseColor("#eb4b4b"))
                    tv_password_error.setText("Password must contain a mix of letters and numbers")
                }
                else{
                    errorConstraint()
                    tv_password_error.setText("Stong password")
                    tv_password_error.setTextColor(Color.parseColor("#38c96d"))
                    iv_password_checkmark.bringToFront()
                    iv_password_checkmark.alpha = 1F
                    iv_password_x.alpha = 0F
                }
            }
            finally {
                if(!stopped){
                    handler.postDelayed(this, 100)} }
        }}
    val handler2 = Handler()
    var stopped2 = false
    private val passwordError2: Runnable = object : Runnable {
        override fun run() {
            try {
                val passwordUpdated = et_password.text.toString().trim()
                if (passwordUpdated.length >= 6 && isValidPassword(passwordUpdated)){
                    errorConstraint()
                    tv_password_error.setText("Stong password")
                    tv_password_error.setTextColor(Color.parseColor("#38c96d"))
                    iv_password_checkmark.bringToFront()
                    iv_password_checkmark.alpha = 1F
                    iv_password_x.alpha = 0F
                }
                else{
                    defaultConstraint()
                }
            }
                finally {
                    if(!stopped2){
                        handler2.postDelayed(this, 100)} }
            }
            }
    fun isValidPassword(password: String?) : Boolean {
        password?.let {
            val passwordPattern = "^(?=.*[0-9])(?=.*[A-Za-zÀ-ȕ]).{6,20}$"
            val passwordMatcher = Regex(passwordPattern)

            return passwordMatcher.find(password) != null
        } ?: return false
    }


}

//        //Already Have an Account? Button Clicked
//        tv_alreadyhaveaccount.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//            finish()
//    }
//        //select photo button clicked
//        b_selectphoto.setOnClickListener {
//            Log.d("Main Activity", "Try to show photo selector")
//
//            val intent = Intent(Intent.ACTION_PICK)
//            intent.type = "image/*"
//            startActivityForResult(intent, 0)
//            onBackPressed()
//        }}
//
//    //action bar back button pressed
//    @Override
//    public override fun onSupportNavigateUp(): Boolean {
//        onBackPressed()
//        return super.onSupportNavigateUp()
//    }
//
//    //image selector code
//    var selectedPhotoUri: Uri? = null
//        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//            super.onActivityResult(requestCode, resultCode, data)
//
//            if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
//                Log.d("RegisterActivity", "photo was selected")
//            }
//            selectedPhotoUri = data?.data
//
//            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
//            iv_selectphoto.setImageBitmap(bitmap)
////            val bitmapDrawable = BitmapDrawable(bitmap)
////            b_selectphoto.setBackgroundDrawable(bitmapDrawable)
//            b_selectphoto.alpha =0f
//        }
//
//    //firebase registration
//    private fun performRegister(){
//        val progressDialog = ProgressDialog(this)
//        progressDialog.setMessage("Registering User")
//
//        progressDialog.show()
//        val username = et_login_email.text.toString().trim()
//        val email= et_email.text.toString().trim()
//        val password = et_password.text.toString()
//
//        if (username.isEmpty()){
//            Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show()
//            progressDialog.dismiss()
//            return
//        }
//        if (email.isEmpty()){
//            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
//            progressDialog.dismiss()
//            return
//        }
//        if(password.isEmpty()){
//            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
//            progressDialog.dismiss()
//            return
//        }
//        Log.d("RegisterActivity", "Email is " + email)
//        Log.d("RegisterActivity", "Password is $password")
//        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
//            .addOnCompleteListener{
//                if(!it.isSuccessful){
//                    return@addOnCompleteListener
//                    progressDialog.dismiss()
//                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT)
//                }
//            else{
//                    Log.d("RegisterActivity", "Successfully created user with uid: ${it.result?.user?.uid}")
//                    progressDialog.dismiss()
//                    val user = auth.currentUser
//                    Toast.makeText(this, "Registered\n ${user?.getEmail()}", Toast.LENGTH_SHORT).show()
//                    val intent = Intent(this, ProfileActivity::class.java)
//                    startActivity(intent)
//                    finish()
//                }
//                uploadImageToFirebaseStorage()
//            }
//            .addOnFailureListener {
//                Log.d("Main", "Failed to create user ${it.message}")
//                Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
//                progressDialog.dismiss()
//            }
//    }
//
//    //firebase database image uploading
//    private fun uploadImageToFirebaseStorage(){
//        if (selectedPhotoUri == null) return
//
//        val filename = UUID.randomUUID().toString()
//        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
//
//        ref.putFile(selectedPhotoUri!!)
//            .addOnSuccessListener {
//                Log.d("RegisterActivity", "Successfully uploaded image: ${it.metadata?.path}")
//
//                ref.downloadUrl.addOnSuccessListener {
//                    it.toString()
//                    Log.d("RegisterActivity", "File LocationL $it")
//
//                    saveUserToFirebaseDatabase(it.toString())
//                }
//            }
//    }
//
//    //firebase database user uploading
//    private fun saveUserToFirebaseDatabase(profileImageUrl: String){
//        val uid = FirebaseAuth.getInstance().uid ?: ""
//        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
//
//        val user = User(uid, et_login_email.text.toString(), profileImageUrl)
//        ref.setValue(user)
//            .addOnSuccessListener {
//                Log.d("RegisterActivity", "saved user to Firebase database")
//
//                val intent = Intent(this, ProfileActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)
//            }
//            .addOnFailureListener{
//                Log.d("RegisterActivity", "failure saving user to firebase Database")
//            }
//    }
//}
//class User(val uid: String, val username: String, val profileImageUrl: String)
