package com.duncboi.realsquabble

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_username.*
import kotlinx.android.synthetic.main.fragment_default_profile.view.*
import kotlinx.android.synthetic.main.fragment_edit_username.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [edit_username.newInstance] factory method to
 * create an instance of this fragment.
 */
class edit_username : Fragment() {

    val args: edit_usernameArgs by navArgs()

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_username, container, false)

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment edit_username.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            edit_username().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        et_change_username_username.setText(args.username)

        tv_edit_username_done.setOnClickListener {
            stopLiveUsernameCheck = true
            val action = edit_usernameDirections.editUsernameToEditProfile(et_change_username_username.text.toString().trim().toLowerCase())
            Navigation.findNavController(view).navigate(action)}

        iv_change_username_back_button.setOnClickListener{
            stopLiveUsernameCheck = true
            val action = edit_usernameDirections.editUsernameToEditProfile(args.username)
            Navigation.findNavController(view).navigate(action)}

        defaultConstraint()
        runLiveUsernameCheck()
    }
    private fun defaultConstraint(){
        tv_edit_username_done.setText("")
        tv_edit_username_done.isEnabled = false
        et_change_username_username.bringToFront()
        et_change_username_username.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.pen, 0);
        val set = ConstraintSet()
        val usernameLayout = edit_username_constraint
        set.clone(usernameLayout)
        set.clear(tv_change_username_error_message.id, ConstraintSet.TOP)
        set.connect(tv_change_username_error_message.id,ConstraintSet.TOP,et_change_username_username.id,ConstraintSet.TOP)
        set.connect(tv_change_username_subtext.id,ConstraintSet.TOP,et_change_username_username.id,ConstraintSet.BOTTOM, 24)
        set.applyTo(usernameLayout)
    }
    private fun errorConstraint(){
        tv_edit_username_done.setText("")
        tv_edit_username_done.isEnabled = false
        val defaultSet = ConstraintSet()
        val usernameLayout = edit_username_constraint
        defaultSet.clone(usernameLayout)
        defaultSet.clear(tv_change_username_subtext.id, ConstraintSet.TOP)
        defaultSet.clear(tv_change_username_error_message.id, ConstraintSet.TOP)
        defaultSet.connect(tv_change_username_error_message.id, ConstraintSet.TOP, et_change_username_username.id, ConstraintSet.BOTTOM, 6)
        defaultSet.connect(tv_change_username_subtext.id, ConstraintSet.TOP, tv_change_username_error_message.id, ConstraintSet.BOTTOM, 12)
        defaultSet.applyTo(usernameLayout)
    }

    //Start Next Activity

    //Coroutine stop variables
    private var stopLiveUsernameCheck = false

    //Coroutine Runner Functions
    private fun runLiveUsernameCheck(){
        CoroutineScope(Dispatchers.Main).launch {
            liveUsernameCheck()}
    }
    private suspend fun liveUsernameCheck() {
        withContext(Dispatchers.IO){
            usernameCheckLogic()
        }
    }

    private suspend fun usernameCheckLogic() {
        while (!stopLiveUsernameCheck) {

            delay(200)

            val username = et_change_username_username.text.toString().trim()
            val lowerCaseUsername = username.toLowerCase()

            if(lowerCaseUsername == args.username){
                withContext(Main){
                onUsernameAvailable(lowerCaseUsername)}
            }
            else if(lowerCaseUsername.isEmpty()) {
                defaultMainThread()
            }
            else if (username.length > 20){
                withContext(Dispatchers.Main){
                    onUsernameTooLong()}
            }
            else{
                val usernameQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("username").equalTo(lowerCaseUsername)
                usernameQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.childrenCount > 0) {
                            onUsernameUnavailable(lowerCaseUsername)
                        } else {
                            onUsernameAvailable(lowerCaseUsername)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }}}

    //Error Functions
    private suspend fun defaultMainThread() {
        withContext(Dispatchers.Main) {
            defaultConstraint()
        }
    }
    private fun onUsernameTooLong(){
        errorConstraint()
        et_change_username_username.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.x, 0);
        tv_change_username_error_message.setTextColor(Color.parseColor("#eb4b4b"))
        tv_change_username_error_message.text = "Username length too long"
    }
    private fun onUsernameAvailable(username: String) {
        errorConstraint()
        tv_edit_username_done.setText("Done")
        tv_edit_username_done.isEnabled = true
        et_change_username_username.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmark, 0);
        tv_change_username_error_message.setTextColor(Color.parseColor("#38c96d"))
        tv_change_username_error_message.text = "@$username is available"
    }
    private fun onUsernameUnavailable(username: String) {
        errorConstraint()
        et_change_username_username.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.x, 0);
        tv_change_username_error_message.setTextColor(Color.parseColor("#eb4b4b"))
        tv_change_username_error_message.text = "@$username is unavailable"
    }
    private fun onUsernameEmpty() {
        errorConstraint()
        et_change_username_username.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.x, 0);
        tv_change_username_error_message.text = "Please enter username"
        tv_change_username_error_message.setTextColor(Color.parseColor("#eb4b4b"))
    }
}