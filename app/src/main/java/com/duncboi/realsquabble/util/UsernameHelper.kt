package com.duncboi.realsquabble.util

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_username.*
import kotlin.concurrent.thread

class UsernameHelper{

    companion object{
        //returns true if userInput exists in query, returns false if it doesn't
        fun checkQuery(pathString: String, childPath: String, userInput: String){
            //query initializer
            val query = FirebaseDatabase.getInstance().reference.child(pathString).orderByChild(childPath).equalTo(userInput)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
                //checks to see if data exists in the query
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.childrenCount <= 0 && userInput.isNotEmpty() && userInput.length <= 20) {

                    }
                } })
        }

    }
    }

