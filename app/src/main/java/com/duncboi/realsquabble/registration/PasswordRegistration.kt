package com.duncboi.realsquabble.registration

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.duncboi.realsquabble.R
import kotlinx.android.synthetic.main.fragment_password_registration.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class PasswordRegistration : Fragment() {
    private val args: PasswordRegistrationArgs by navArgs()
    private var onClickPassword: String = ""


    override fun onPause() {
        super.onPause()
        stopLivePasswordCheck = true
        stopOnClickPasswordCheck = true
    }

    override fun onResume() {
        super.onResume()
        val passwordPassed = args.password
        if (passwordPassed != "password") {
            et_password.setText(passwordPassed)
        }
        stopLivePasswordCheck = false
        runLivePasswordCheck()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_password_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            closeKeyboard()
            val bundle = Bundle()
            val username = args.username
            val email = args.email
            val password = et_password.text.toString().trim()
            bundle.putString("username", username)
            bundle.putString("email", email)
            bundle.putString("password", password)
            findNavController().navigate(R.id.action_passwordRegistration_to_emailRegistration, bundle)
        }

    }

    private fun startNextActivity(password: String) {val bundle = Bundle()
        val username = args.username
        val email = args.email
        bundle.putString("username", username)
        bundle.putString("email", email)
        bundle.putString("password", password)
        findNavController().navigate(R.id.action_passwordRegistration_to_emailVerificationRegistration, bundle)
    }

    private fun closeKeyboard(){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
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
                    b_password_next.alpha = 0.5f
                    b_password_next.setBackgroundResource(R.drawable.greyed_out_button)
                }
                else onPasswordShort()

            }
            else if(!isPasswordMix) {
                if(onClickPassword != password) {
                    onClickPassword = "*"
                    defaultConstraint()
                    b_password_next.alpha = 0.5f
                    b_password_next.setBackgroundResource(R.drawable.greyed_out_button)
                }
                else{
                    onPasswordNotStrong()}
            }

            else onStrongPassword()


        }
    }

    private fun onPasswordNotStrong() {
        errorConstraint()
        b_password_next.alpha = 0.5f
        b_password_next.setBackgroundResource(R.drawable.greyed_out_button)
        iv_password_x.bringToFront()
        iv_password_x.alpha = 1F
        iv_password_checkmark.alpha = 0F
        tv_password_error.setTextColor(Color.parseColor("#eb4b4b"))
        tv_password_error.text = "Password must contain a mix of letters and numbers"
    }

    private fun onPasswordShort() {
        errorConstraint()
        b_password_next.alpha = 0.5f
        b_password_next.setBackgroundResource(R.drawable.greyed_out_button)
        iv_password_x.bringToFront()
        iv_password_checkmark.alpha = 0F
        iv_password_x.alpha = 1F
        tv_password_error.setTextColor(Color.parseColor("#eb4b4b"))
        tv_password_error.text = "Password must be at least 6 characters"
    }

    private suspend fun livePasswordCheckLogic(){

        while (!stopLivePasswordCheck) {
            delay (100)
            val password = et_password.text.toString().trim()

            if (password.length >= 6 && isValidPassword(password)) onStrongPassword()
            else{
                defaultConstraint()
                b_password_next.alpha = 0.5f
                b_password_next.setBackgroundResource(R.drawable.greyed_out_button)
            }

        }
    }

    private fun onStrongPassword() {
        errorConstraint()
        b_password_next.alpha = 1f
        b_password_next.setBackgroundResource(R.drawable.rounded_button)
        tv_password_error.text = "Stong password"
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

    private fun onPasswordEmpty() {
        errorConstraint()
        b_password_next.alpha = 0.5f
        b_password_next.setBackgroundResource(R.drawable.greyed_out_button)
        iv_password_x.bringToFront()
        iv_password_x.alpha = 1F
        iv_password_checkmark.alpha = 0F
        tv_password_error.setTextColor(Color.parseColor("#eb4b4b"))
        tv_password_error.text = "Please enter a password"
    }
    }
