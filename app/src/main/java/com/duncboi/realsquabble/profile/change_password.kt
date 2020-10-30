package com.duncboi.realsquabble.profile

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.duncboi.realsquabble.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_change_password.*


class change_password : Fragment() {

    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iv_change_password_back_button.setOnClickListener {
            findNavController().popBackStack()
        }

        val passwordRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser).child("password")
        passwordRef.onDisconnect().cancel()
            passwordRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val password = snapshot.getValue(String::class.java)
                if (password != null && password != "google" && password != "phone" && et_change_password_edit_text != null){
                    b_change_password_change.setOnClickListener {
                        val newPassword = et_change_password_edit_text.text.trim()
                        val builder = AlertDialog.Builder(activity)
                        builder.setCancelable(false)
                        builder.setTitle("Change Password?")
                        builder.setMessage("Are you sure you want to change your password?")
                        builder.setPositiveButton("Change") { dialogInterface, i ->
                            snapshot.ref.setValue(newPassword)
                            findNavController().popBackStack()
                            Toast.makeText(activity, "Password Changed", Toast.LENGTH_SHORT).show()
                        }
                        builder.setNegativeButton("No") { dialogInterface, i ->
                            dialogInterface.cancel()
                        }
                        builder.create().show()
                    }
                }
                else{
                    if (password == "google"){
                        val builder = AlertDialog.Builder(activity)
                        builder.setCancelable(false)
                        builder.setTitle("Cannot change password")
                        builder.setMessage("You signed in with a google account, and are not authorized to change your password")
                        builder.setPositiveButton("Ok",DialogInterface.OnClickListener { dialogInterface, i ->
                            dialogInterface.cancel()
                            findNavController().popBackStack()
                        })
                        builder.create().show()
                    }
                    else if (password == "phone"){
                        val builder = AlertDialog.Builder(activity)
                        builder.setCancelable(false)
                        builder.setTitle("Cannot change password")
                        builder.setMessage("You signed in with a phone number account, and are not authorized to change your password")
                        builder.setPositiveButton("Ok",DialogInterface.OnClickListener { dialogInterface, i ->
                            dialogInterface.cancel()
                            findNavController().popBackStack()
                        })
                        builder.create().show()
                    }
                }
            }
        })
    }

}