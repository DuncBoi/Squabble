package com.duncboi.realsquabble.profile

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.duncboi.realsquabble.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_edit_username.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main


class edit_username : Fragment() {

    val args: edit_usernameArgs by navArgs()
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    private var job: Job? = null

    private val timeRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser).child("usernameTime")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_username, container, false)

        return view
    }

    override fun onPause() {
        super.onPause()
        job?.cancel()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        et_change_username_username.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.pen, 0)

        pb_edit_username_progress.visibility = View.INVISIBLE

        et_change_username_username.setText(args.username)

        val bio = args.bio
        val name = args.name

        timeRef.onDisconnect().cancel()
        timeRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentTime = System.currentTimeMillis()
                val lastTime = snapshot.getValue(String::class.java)?.toLong()
                if (lastTime != null) {
                    val timePassed = currentTime - lastTime
                    val monthMilis = 2592000000
                    val timeLeft = monthMilis - timePassed
                    val daysLeft = timeLeft / 86400000

                    if (timeLeft <= 0L || lastTime == 0L) {
                        tv_edit_username_time_left.text = "Now"
                        tv_edit_username_time_left.setTextColor(Color.parseColor("#38c96d"))
                        doStuff(bio, name)
                    } else {
                        tv_edit_username_time_left.text = "$daysLeft days"
                        tv_edit_username_time_left.setTextColor(Color.parseColor("#eb4b4b"))
                        tv_edit_username_done.setOnClickListener {
                            tv_edit_username_time_left_text.setBackgroundResource(R.drawable.error_edittext_register_login)
                        }
                    }
                }
                else{
                    tv_edit_username_time_left.text = "Now"
                    tv_edit_username_time_left.setTextColor(Color.parseColor("#38c96d"))
                    doStuff(bio, name)
                }
            }

        })

        iv_change_username_back_button.setOnClickListener{
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
            val bundle = Bundle()
            bundle.putString("bio", bio)
            bundle.putString("username", args.username)
            bundle.putString("name", name)
            bundle.putLong("usernameTime", args.usernameTime)
            findNavController().navigate(R.id.edit_username_to_editProfile, bundle)}
    }

    private fun doStuff(bio: String, name: String) {
        et_change_username_username.addTextChangedListener(object : TextWatcher {
            private var searchFor = ""
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val searchText = p0.toString().trim().toLowerCase()
                if (searchText == searchFor)
                    return
                searchFor = searchText
                pb_edit_username_progress.visibility = View.VISIBLE
                defaultConstraint()
                job = CoroutineScope(Main).launch {
                    delay(1000)
                    if (searchText != searchFor)
                        return@launch
                    when {
                        searchText == "" -> {
                            onUsernameEmpty()
                            pb_edit_username_progress.visibility = View.INVISIBLE
                        }
                        searchText.length > 20 -> {
                            onUsernameTooLong()
                            pb_edit_username_progress.visibility = View.INVISIBLE
                        }
                        searchText == args.username ->{
                            onUsernameAvailable(searchText)
                            pb_edit_username_progress.visibility = View.INVISIBLE
                        }
                        else -> {
                            val usernameQuery =
                                FirebaseDatabase.getInstance().reference.child("Users")
                                    .orderByChild("username").equalTo(searchText)
                            usernameQuery.addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.childrenCount > 0) {
                                        onUsernameUnavailable(searchText)
                                        pb_edit_username_progress.visibility = View.INVISIBLE
                                    } else {
                                        onUsernameAvailable(searchText)
                                        pb_edit_username_progress.visibility = View.INVISIBLE
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                        }
                    }
                }
            }

        })

        tv_edit_username_done.setOnClickListener {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
            val username = et_change_username_username.text.toString().trim()
            val usernameTime = System.currentTimeMillis()
            val usernameQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("username").equalTo(username)
            usernameQuery.addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.childrenCount <= 0 && username != "" && username.length <= 20) {
                        val bundle = Bundle()
                        bundle.putString("bio", bio)
                        bundle.putLong("usernameTime", usernameTime)
                        bundle.putString("username", username)
                        bundle.putString("name", name)
                        findNavController().navigate(R.id.edit_username_to_editProfile, bundle)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        }
    }

    private fun defaultConstraint(){
        tv_edit_username_done.text = ""
        tv_edit_username_done.isEnabled = false
        et_change_username_username.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        tv_change_username_error_message.visibility = View.GONE
    }
    private fun errorConstraint(){
        tv_edit_username_done.text = ""
        tv_edit_username_done.isEnabled = false
        tv_change_username_error_message.visibility = View.VISIBLE
    }


    //Error Functions

    private fun onUsernameTooLong(){
        errorConstraint()
        et_change_username_username.setCompoundDrawablesWithIntrinsicBounds(0, 0,
            R.drawable.x, 0)
        tv_change_username_error_message.setTextColor(Color.parseColor("#eb4b4b"))
        tv_change_username_error_message.text = "Username length too long"
    }
    private fun onUsernameAvailable(username: String) {
        errorConstraint()
        tv_edit_username_done.text = "Done"
        tv_edit_username_done.isEnabled = true
        et_change_username_username.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0)
        tv_change_username_error_message.setTextColor(Color.parseColor("#38c96d"))
        tv_change_username_error_message.text = "@$username is available"
    }
    private fun onUsernameUnavailable(username: String) {
        errorConstraint()
        et_change_username_username.setCompoundDrawablesWithIntrinsicBounds(0, 0,
            R.drawable.x, 0)
        tv_change_username_error_message.setTextColor(Color.parseColor("#eb4b4b"))
        tv_change_username_error_message.text = "@$username is unavailable"
    }
    private fun onUsernameEmpty() {
        errorConstraint()
        et_change_username_username.setCompoundDrawablesWithIntrinsicBounds(0, 0,
            R.drawable.x, 0)
        tv_change_username_error_message.text = "Please enter username"
        tv_change_username_error_message.setTextColor(Color.parseColor("#eb4b4b"))
    }
    }