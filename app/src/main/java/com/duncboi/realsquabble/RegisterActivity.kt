package com.duncboi.realsquabble

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onStart() {
        super.onStart()
        stopLivePasswordCheck = false
        runLivePasswordCheck()
    }

    override fun onStop() {
        super.onStop()
        stopLivePasswordCheck = true
        stopOnClickPasswordCheck = true
    }
    private var onClickPassword: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        defaultConstraint()
        runLivePasswordCheck()

        b_password_next.setOnClickListener {
            stopOnClickPasswordCheck = false
            stopLivePasswordCheck = true
            val password = et_password.text.toString().trim()
            onClickPassword = password

            if (password.isEmpty() || password.length < 6 || !isValidPassword(password)) runOnClickPasswordCheck()
            else startNextActivity(password)

        }
        tv_password_previous.setOnClickListener {
           finish()
        }

       }

    private fun startNextActivity(password: String) {
        val emailVerificationIntent = Intent(this@RegisterActivity, EmailVerification::class.java)
        val username = intent.getStringExtra("username")
        val email = intent.getStringExtra("email")
        emailVerificationIntent.putExtra("username", username)
        emailVerificationIntent.putExtra("email", email)
        emailVerificationIntent.putExtra("password", password)
        startActivity(emailVerificationIntent)
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

    //stop functions
    private var stopOnClickPasswordCheck = false
    private var stopLivePasswordCheck = false

    //runner functions
    private fun runOnClickPasswordCheck(){
        CoroutineScope(Dispatchers.Main).launch {
            onClickPasswordCheckLogic()}
    }
    private fun runLivePasswordCheck(){
        CoroutineScope(Dispatchers.Main).launch {
            livePasswordCheckLogic()}
    }

    private suspend fun onClickPasswordCheckLogic(){
        while (!stopOnClickPasswordCheck){
            delay (100)

            val password = et_password.text.toString().trim()
            val isPasswordMix = isValidPassword(password)

            if(password.isEmpty()) onPasswordEmpty()

            else if (password.length < 6){

                if(onClickPassword != password) {
                    onClickPassword = "*"
                    defaultConstraint()
                }
                else onPasswordShort()

            }
            else if(!isPasswordMix) {
                if(onClickPassword != password) {
                    onClickPassword = "*"
                    defaultConstraint()
                }
                else{
                onPasswordNotStrong()}
            }

            else onStrongPassword()


        }
    }

    private fun onPasswordNotStrong() {
        errorConstraint()
        iv_password_x.bringToFront()
        iv_password_x.alpha = 1F
        iv_password_checkmark.alpha = 0F
        tv_password_error.setTextColor(Color.parseColor("#eb4b4b"))
        tv_password_error.setText("Password must contain a mix of letters and numbers")
    }

    private fun onPasswordShort() {
        errorConstraint()
        iv_password_x.bringToFront()
        iv_password_checkmark.alpha = 0F
        iv_password_x.alpha = 1F
        tv_password_error.setTextColor(Color.parseColor("#eb4b4b"))
        tv_password_error.setText("Password must be at least 6 characters")
    }

    private suspend fun livePasswordCheckLogic(){

        while (!stopLivePasswordCheck) {
            delay (100)
            val password = et_password.text.toString().trim()

            if (password.length >= 6 && isValidPassword(password)) onStrongPassword()
            else defaultConstraint()

        }
    }

    private fun onStrongPassword() {
        errorConstraint()
        tv_password_error.setText("Stong password")
        tv_password_error.setTextColor(Color.parseColor("#38c96d"))
        iv_password_checkmark.bringToFront()
        iv_password_checkmark.alpha = 1F
        iv_password_x.alpha = 0F
    }

    fun isValidPassword(password: String?) : Boolean {
        password?.let {
            val passwordPattern = "^(?=.*[0-9])(?=.*[A-Za-zÀ-ȕ]).{6,20}$"
            val passwordMatcher = Regex(passwordPattern)

            return passwordMatcher.find(password) != null
        } ?: return false
    }

private fun RegisterActivity.onPasswordEmpty() {
    errorConstraint()
    iv_password_x.bringToFront()
    iv_password_x.alpha = 1F
    iv_password_checkmark.alpha = 0F
    tv_password_error.setTextColor(Color.parseColor("#eb4b4b"))
    tv_password_error.setText("Please enter a password")
}
}
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
