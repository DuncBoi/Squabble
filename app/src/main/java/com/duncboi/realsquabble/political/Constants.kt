package com.duncboi.realsquabble.political

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.util.Patterns
import android.view.View
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.duncboi.realsquabble.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.math.RoundingMode
import java.net.MalformedURLException
import java.net.URL
import java.text.DecimalFormat

object Constants {

    private val gunControl = GunControl()
    private val economicFreedom = EconomicFreedom()
    private val freedomOfSpeech = FreedomOfSpeech()
    private val drugs = Drugs()
    private val environment = Environment()
    private val immigration = Immigration()
    private val religion = Religion()
    private val discrimination = discrimination()
    private val nation = Nation()
    private val businessRegulation = BusinessRegulation()
    private val governmentPerformance = GovernmentPerformance()
    private val taxes = Taxes()
    private val poverty = Poverty()
    private val healthcare = Healthcare()
    private val globalization = Globalization()
    private val foreignPolicy = ForeignPolicy()
    var groupUsers = ArrayList<String>()

    var questionNumber = 0

    //make sure this will be different every time
    val fragmentList = arrayListOf(
        gunControl,
        discrimination,
        economicFreedom,
        freedomOfSpeech,
        drugs,
        environment,
        immigration,
        religion,
        nation,
        businessRegulation,
        governmentPerformance,
        taxes,
        poverty,
        healthcare,
        globalization,
        foreignPolicy
    ).shuffled()

        fun rand(start: Int, end: Int): Int {
        require(start <= end) { "Illegal Argument" }
        return (start..end).random()
    }

    fun previousFragment(activity: FragmentActivity) {
        if (questionNumber -1 >= 0) {
            val fragment = fragmentList[questionNumber - 1]
            questionNumber--
            activity.supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
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
                FirebaseDatabase.getInstance().reference.child("Users").child(uid!!).child("Question Answers").ref.removeValue().addOnCompleteListener {
                    if (it.isSuccessful){
                        val intent = Intent(activity, Political::class.java)
                        activity.finish()
                        activity.startActivity(intent)}
                    else{
                        //display error
                    }
                }
            }
            builder.create().show()
        }
    }

    fun nextFragment(
        activity: FragmentActivity,
        questionHeader: String,
        answer: Double,
        type: String,
        questionNum: Int
    ) {
        if (questionNumber +1 < fragmentList.size) {
            val nextFragment = fragmentList[questionNumber + 1]
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
                uploadToDatabase(questionHeader, answer, type, questionNum)
                CoroutineScope(Main).launch {
                    delay(100)
                    val scoreCalculator = ScoreCalculator()
                    activity.supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                        .replace(R.id.root_container, scoreCalculator)
                        .commitAllowingStateLoss()
            }}
            builder.create().show()
        }
    }

    fun randomAnswers(
        randomNumber: Int,
        rb1: RadioButton,
        rb2: RadioButton,
        rb3: RadioButton,
        answer0: String,
        answer5: String,
        answer10: String
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

    fun checkAnswers(randomNumber: Int, rb1: RadioButton, rb2: RadioButton, rb3: RadioButton): Double {
        when (randomNumber) {
            1 -> {
                when {
                    rb1.isChecked -> {
                        return 0.0
                    }
                    rb2.isChecked -> {
                        return 6.25
                    }
                    rb3.isChecked -> {
                        return 12.5
                    }
                }
            }
            2 -> {
                when {
                    rb1.isChecked -> {
                        return 12.5
                    }
                    rb2.isChecked -> {
                        return 0.0
                    }
                    rb3.isChecked -> {
                        return 6.25
                    }
                }
            }
            3 -> {
                when {
                    rb1.isChecked -> {
                        return 6.25
                    }
                    rb2.isChecked -> {
                        return 12.5
                    }
                    rb3.isChecked -> {
                        return 0.0
                    }
                }
            }
        }
        return 0.0
    }

    var stopRadioRunner = false

    fun runRadioChecker(
        rb1: RadioButton,
        rb2: RadioButton,
        rb3: RadioButton,
        errorMessage: TextView,
        errorIcon: ImageView
    ){
        CoroutineScope(Dispatchers.Main).launch{
            radioCheckerLogic(rb1, rb2, rb3, errorMessage, errorIcon)
        }
    }

    private suspend fun radioCheckerLogic(
        rb1: RadioButton,
        rb2: RadioButton,
        rb3: RadioButton,
        errorMessage: TextView,
        errorIcon: ImageView
    ) {
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
    private fun uploadToDatabase(
        questionHeader: String,
        answer: Double,
        type: String,
        questionNumber: Int
    ){
        CoroutineScope(IO).launch {
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            FirebaseDatabase.getInstance().reference.child("Users").child(uid).child("Question Answers").child(
                "$questionHeader"
            ).child("number").ref.setValue("$questionNumber")
            FirebaseDatabase.getInstance().reference.child("Users").child(uid).child("Question Answers").child(
                "$questionHeader"
            ).child("type").ref.setValue("$type")
            FirebaseDatabase.getInstance().reference.child("Users").child(uid).child("Question Answers").child(
                "$questionHeader"
            ).child("answer points").ref.setValue("$answer")
}}

     fun wrap_number(num: Double): String{
        if (num < 1000){
            return "$num"
        }
        else if (num < 1000000) {
            val poo = num / 1000
            val coo = roundOffDecimal(poo)
            return "${coo}k"
        }
         else{
            val poo = num / 1000000
            val coo = roundOffDecimal(poo)
            return "${coo}M"
        }
    }

    fun roundOffDecimal(number: Double): Double? {
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.CEILING
        return df.format(number).toDouble()
    }

    fun isValidUrl(urlString: String): Boolean {
        try {
            val url = URL(urlString)
            return URLUtil.isValidUrl(url.toString()) && Patterns.WEB_URL.matcher(url.toString()).matches()
        } catch (e: MalformedURLException) {
        }
        return false
    }

}