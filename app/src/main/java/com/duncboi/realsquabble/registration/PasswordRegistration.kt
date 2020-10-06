package com.duncboi.realsquabble.registration

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.duncboi.realsquabble.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_password_registration.*
import kotlinx.android.synthetic.main.fragment_username_registration.*
import kotlinx.coroutines.*
import java.lang.Error


class PasswordRegistration : Fragment() {
    private val args: PasswordRegistrationArgs by navArgs()
    private var job: Job? = null


    override fun onPause() {
        super.onPause()
        job?.cancel()
    }

    override fun onResume() {
        super.onResume()
        val passwordPassed = args.password
        if (passwordPassed != "password") {
            et_password.setText(passwordPassed)
        }
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

        et_password.addTextChangedListener(object: TextWatcher {
            private var searchFor = ""

            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val searchText = p0.toString().trim().toLowerCase()
                if (searchText == searchFor)
                    return
                searchFor = searchText
                defaultConstraint()
                job = CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    if (searchText != searchFor)
                        return@launch
                        if (searchText == "") {
                            onPasswordEmpty()
                        }
                        else if (searchText.length >= 6 && isValidPassword(searchText)) {
                            onStrongPassword()
                        }

                }
            }
        })

        b_password_next.setOnClickListener {
            val password = et_password.text.toString().trim()
            val isPasswordMix = isValidPassword(password)

            if(password.isEmpty()) {
                errorConstraint()
                onPasswordEmpty()}
            else if (password.length < 6) {
                errorConstraint()
                onPasswordShort()
            }
            else if(!isPasswordMix) {
                errorConstraint()
                onPasswordNotStrong()
                b_password_next.alpha = 0.5f
                b_password_next.setBackgroundResource(R.drawable.greyed_out_button)
                }
            else{
                errorConstraint()
                onStrongPassword()
                startNextActivity(password)
            }


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
        b_password_next.setBackgroundResource(R.drawable.greyed_out_button)
        b_password_next.alpha = 0.5f
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
