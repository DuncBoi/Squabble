package com.duncboi.realsquabble.registration

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.duncboi.realsquabble.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_username_registration.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class UsernameRegistration : Fragment() {

    private var job: Job? = null
    private val args: UsernameRegistrationArgs by navArgs()

    override fun onPause() {
        super.onPause()
        job?.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_username_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        et_username_username.addTextChangedListener(object: TextWatcher {
            private var searchFor = ""

            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val searchText = p0.toString().trim().toLowerCase()
                if (searchText == searchFor)
                    return
                searchFor = searchText
                pb_username_progress_bar.visibility = View.VISIBLE
                iv_username_x.visibility = View.INVISIBLE
                iv_username_checkmark.visibility = View.INVISIBLE
                tv_username_username_taken.visibility = View.GONE
                job = CoroutineScope(Main).launch {
                    delay(1000)
                    if (searchText != searchFor)
                        return@launch
                    when {
                        searchText == "" -> {
                            onUsernameEmpty()
                            pb_username_progress_bar.visibility = View.INVISIBLE
                        }
                        searchText.length > 20 -> {
                            onUsernameTooLong()
                            pb_username_progress_bar.visibility = View.INVISIBLE
                        }
                        else -> {
                            val usernameQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("username").equalTo(searchText)
                            usernameQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.childrenCount > 0) {
                                        onUsernameUnavailable(et_username_username.text.toString().toLowerCase())
                                        pb_username_progress_bar.visibility = View.INVISIBLE}
                                    else {
                                        onUsernameAvailable()
                                        pb_username_progress_bar.visibility = View.INVISIBLE
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                        }
                    }
                }
            }
        })

        b_username_next.setOnClickListener {
            val username = et_username_username.text.toString().trim()
            val lowerCaseUsername = username.toLowerCase()
            val usernameQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("username").equalTo(lowerCaseUsername)
            usernameQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.childrenCount <= 0 && lowerCaseUsername.isNotEmpty() && lowerCaseUsername.length <= 20){
                        val email = args.email
                        val bundle = Bundle()
                        val password = args.password
                        bundle.putString("password", password)
                        bundle.putString("email",email)
                        bundle.putString("username", lowerCaseUsername)
                        closeKeyboard()
                        findNavController().navigate(R.id.action_usernameRegistration_to_emailRegistration, bundle)
                    }
                    else{
                        if (username.isEmpty()){
                            onUsernameEmpty()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
        tv_username_previous.setOnClickListener {
            job?.cancel()
            findNavController().popBackStack()
        }
    }

    private fun closeKeyboard(){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    //Error Functions
    private fun onUsernameTooLong(){
        iv_username_x.visibility = View.VISIBLE
        iv_username_checkmark.visibility = View.INVISIBLE
        tv_username_username_taken.visibility = View.VISIBLE
        b_username_next.alpha = 0.5f
        b_username_next.setBackgroundResource(R.drawable.greyed_out_button)
        b_username_next.isClickable = false
        tv_username_username_taken.text = "Username length too long"
    }

    private fun onUsernameAvailable() {
        iv_username_x.visibility = View.INVISIBLE
        iv_username_checkmark.visibility = View.VISIBLE
        b_username_next.alpha = 1.0f
        b_username_next.isClickable = true
        b_username_next.setBackgroundResource(R.drawable.rounded_button)
    }

    private fun onUsernameUnavailable(username: String) {
        iv_username_x.visibility = View.VISIBLE
        iv_username_checkmark.visibility = View.INVISIBLE
        tv_username_username_taken.visibility = View.VISIBLE
        b_username_next.alpha = 0.5f
        b_username_next.setBackgroundResource(R.drawable.greyed_out_button)
        b_username_next.isClickable = false
        tv_username_username_taken.text = "@$username is unavailable"
    }

    private fun onUsernameEmpty() {
        iv_username_x.visibility = View.VISIBLE
        iv_username_checkmark.visibility = View.INVISIBLE
        tv_username_username_taken.visibility = View.VISIBLE
        b_username_next.alpha = 0.5f
        b_username_next.setBackgroundResource(R.drawable.greyed_out_button)
        b_username_next.isClickable = true
        tv_username_username_taken.text = "Please enter username"
    }


}
