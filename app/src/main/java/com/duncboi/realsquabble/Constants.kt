package com.duncboi.realsquabble

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.duncboi.realsquabble.political.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_email_verification_registration.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Constants {

    val politicalIntro = PoliticalIntro()
    val gunControl = GunControl()
    val economicFreedom = EconomicFreedom()
    val freedomOfSpeech = FreedomOfSpeech()
    val drugs = Drugs()
    val environment = Environment()
    val immigration = Immigration()
    val religion = Religion()
    val discrimination = discrimination()
    val nation = Nation()
    val scoreCalculator = ScoreCalculator()

    var questionNumber = 0
    var economicScore = 0
    var socialScore = 0

    //make sure this will be different every time
    val fragmentList = arrayListOf(gunControl, discrimination, economicFreedom, freedomOfSpeech, drugs, environment, immigration, religion, nation).shuffled()
        fun rand(start: Int, end: Int): Int {
        require(start <= end) { "Illegal Argument" }
        return (start..end).random()
    }

    fun previousFragment(activity: FragmentActivity) {
        if (questionNumber-1 >= 0) {
            val fragment = Constants.fragmentList[questionNumber - 1]
            questionNumber--
            activity.supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .replace(R.id.root_container, fragment)
                .commitAllowingStateLoss()
        }
        else{
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("Exit Quiz?")
            builder.setMessage("Are you sure you want to exit the quiz, your changes will not be saved")
            builder.setCancelable(false)
            builder.setNegativeButton("Cancel"){ _: DialogInterface, _: Int -> }
            builder.setPositiveButton("Exit"){ _: DialogInterface, _: Int ->
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("uid").equalTo(uid)
                emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (childSnapshot in snapshot.children) {
                            childSnapshot.child("Question Answers").ref.removeValue()
                        }
                    }})
                val intent = Intent(activity, Political::class.java)
                activity.finish()
                activity.startActivity(intent)
            }
            builder.create().show()
        }
    }

    fun nextFragment(activity: FragmentActivity, questionHeader: String, answer: Int, type: String, questionNum: Int) {
        if (questionNumber +1 < fragmentList.size) {
            val nextFragment = fragmentList[questionNumber+1]
            questionNumber++
            uploadToDatabase(questionHeader, answer, type, questionNum)
            activity.supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.root_container, nextFragment)
                .commitAllowingStateLoss()
        }
        else{
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("Submit Quiz?")
            builder.setMessage("Are you sure you would like to submit this quiz")
            builder.setCancelable(false)
            builder.setNegativeButton("Cancel"){ _: DialogInterface, _: Int -> }
            builder.setPositiveButton("Submit"){ _: DialogInterface, _: Int ->
                activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.root_container, scoreCalculator)
                    .commitAllowingStateLoss()
            }
            builder.create().show()
        }
    }

    fun randomAnswers(randomNumber: Int, rb1: RadioButton, rb2: RadioButton, rb3: RadioButton, answer0: String, answer5: String, answer10:String
    ): Int {
        when (randomNumber) {
            1 -> {
                rb1.text = answer0
                rb2.text = answer5
                rb3.text = answer10
            }
            2 -> {
                rb1.text = answer10
                rb2.text = answer0
                rb3.text = answer5
            }
            3 -> {
                rb1.text = answer5
                rb2.text = answer10
                rb3.text = answer0
            }
        }
        return randomNumber
    }

    fun checkAnswers(randomNumber: Int, rb1: RadioButton, rb2: RadioButton, rb3:RadioButton): Int {
        when (randomNumber) {
            1 -> {
                when {
                    rb1.isChecked -> {
                        return 0
                        Constants.socialScore += 0
                    }
                    rb2.isChecked -> {
                        return 5
                        Constants.socialScore += 5}
                    rb3.isChecked -> {
                        return 10
                        Constants.socialScore += 10
                }
            }}
            2 -> {
                when {
                    rb1.isChecked -> {
                        return 10
                        Constants.socialScore += 10
                    }
                    rb2.isChecked -> {
                        return 0
                        Constants.socialScore += 0}
                    rb3.isChecked -> {
                        return 5
                        Constants.socialScore += 5
                    }
                }
            }
            3 -> {
                when {
                    rb1.isChecked -> {
                        return 5
                        Constants.socialScore += 5
                    }
                    rb2.isChecked -> {
                        return 10
                        Constants.socialScore += 10}
                    rb3.isChecked -> {
                        return 0
                        Constants.socialScore += 0
                    }
                }
            }
        }
        return -1
    }

    var stopRadioRunner = false

    fun runRadioChecker(rb1: RadioButton, rb2: RadioButton, rb3: RadioButton, errorMessage: TextView, errorIcon: ImageView){
        CoroutineScope(Dispatchers.Main).launch{
            radioCheckerLogic(rb1,rb2,rb3,errorMessage,errorIcon)
        }
    }

    private suspend fun radioCheckerLogic(rb1: RadioButton, rb2: RadioButton, rb3: RadioButton, errorMessage: TextView, errorIcon: ImageView) {
        while (!stopRadioRunner){
            delay(200)
            if (!rb1.isChecked && !rb2.isChecked && !rb3.isChecked){
                errorMessage.text = "Please choose an answer"
                errorIcon.visibility = View.VISIBLE
            }
            else{
                errorMessage.text = ""
                errorIcon.visibility = View.INVISIBLE
                stopRadioRunner = true
            }
        }
    }
    private fun uploadToDatabase(questionHeader: String, answer: Int, type: String, questionNumber: Int){
        CoroutineScope(IO).launch {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("uid").equalTo(uid)
        emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    childSnapshot.child("Question Answers").child("$questionHeader").child("number").ref.setValue("$questionNumber")
                    childSnapshot.child("Question Answers").child("$questionHeader").child("type").ref.setValue("$type")
                    childSnapshot.child("Question Answers").child("$questionHeader").child("answer points").ref.setValue("$answer")
                }
            }
        })
}}
}