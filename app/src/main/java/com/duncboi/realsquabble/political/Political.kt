package com.duncboi.realsquabble.political

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.duncboi.realsquabble.Constants.gunControl
import com.duncboi.realsquabble.Constants.politicalIntro
import com.duncboi.realsquabble.R

class Political : AppCompatActivity() {

//    private val questionList = getQuestions().shuffled()
//    private val answerChosen = ArrayList<String>()
//
//    private var socialPercent = 0
//    private var economicPercent = 0
//
//    lateinit var question: Question
//    var answerCounter = 0
//    private val size = questionList.size

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_political)

        supportFragmentManager.beginTransaction()
            .replace(R.id.root_container, politicalIntro)
            .commitAllowingStateLoss()

//        answerChosen.add("Burh")     }






//        val rand = rand(1,2)
//
//        bruh()
//        tv_political_question_number.setText("0/$size")
//
//        b_political_next.setOnClickListener {
//            checkAnswers()
//        }
//
//        b_political_previous.setOnClickListener {
//            if(questionCounter!=0 && answerCounter!=0){
//                questionCounter--
//                tv_political_question_number.setText("$questionCounter/$size")
//                val currentQuestion = questionList[questionCounter]
//                tv_political_header.text = currentQuestion.header
//                iv_political_icon.setImageResource(currentQuestion.image)
//                rb_political_answer1.setText(currentQuestion.answer0)
//                rb_political_answer2.setText(currentQuestion.answer5)
//                rb_political_answer3.setText(currentQuestion.answer10)
//                val answer = answerChosen[questionCounter]
//                Log.d("Moose", "$questionCounter")
//                Log.d("Moose", "$answer")
//                Log.d("Moose", "$answerChosen")
//                when (answer){
//                    rb_political_answer1.text.toString() -> rg_political.check(rb_political_answer1.id)
//                    rb_political_answer2.text.toString() -> rg_political.check(rb_political_answer2.id)
//                    rb_political_answer3.text.toString() -> rg_political.check(rb_political_answer3.id)
//                }
//            }
//        }
//        }
//
//    private fun checkAnswers() {
//
//        if (rb_political_answer1.isChecked) {
//            if (question.type == "Social") {
//                when (rb_political_answer1.text) {
//                    question.answer0 -> socialPercent += 0
//                    question.answer5 -> socialPercent += 5
//                    question.answer10 -> socialPercent += 10
//                }
//            } else {
//                when (rb_political_answer1.text) {
//                    question.answer0 -> economicPercent += 0
//                    question.answer5 -> economicPercent += 5
//                    question.answer10 -> economicPercent += 10
//                }
//            }
//            questionCounter++
//            answerCounter++
//            answerChosen.add("burh")
//            if (!answerChosen.isEmpty()){
//                answerChosen.removeAt(questionCounter - 1)
//                answerChosen.add(questionCounter-1, rb_political_answer1.text.toString())}
//            tv_political_question_number.setText("$questionCounter/$size")
//            bruh()
//            rg_political.clearCheck()
//        } else if (rb_political_answer2.isChecked) {
//            if (question.type == "Social") {
//                when (rb_political_answer2.text) {
//                    question.answer0 -> socialPercent += 0
//                    question.answer5 -> socialPercent += 5
//                    question.answer10 -> socialPercent += 10
//                }
//            } else {
//                when (rb_political_answer2.text) {
//                    question.answer0 -> economicPercent += 0
//                    question.answer5 -> economicPercent += 5
//                    question.answer10 -> economicPercent += 10
//                }
//            }
//            questionCounter++
//            answerCounter++
//            answerChosen.add("burh")
//            if (!answerChosen.isEmpty()){
//                answerChosen.removeAt(questionCounter - 1)
//                answerChosen.add(questionCounter-1, rb_political_answer2.text.toString())}
//            tv_political_question_number.setText("$questionCounter/$size")
//            bruh()
//            rg_political.clearCheck()
//        } else if (rb_political_answer3.isChecked) {
//            if (question.type == "Social") {
//                when (rb_political_answer3.text) {
//                    question.answer0 -> socialPercent += 0
//                    question.answer5 -> socialPercent += 5
//                    question.answer10 -> socialPercent += 10
//                }
//            } else {
//                when (rb_political_answer3.text) {
//                    question.answer0 -> economicPercent += 0
//                    question.answer5 -> economicPercent += 5
//                    question.answer10 -> economicPercent += 10
//                }
//            }
//            questionCounter++
//            answerCounter++
//            answerChosen.add("burh")
//            if (!answerChosen.isEmpty()){
//                answerChosen.removeAt(questionCounter - 1)
//                answerChosen.add(questionCounter-1, rb_political_answer3.text.toString())}
//            tv_political_question_number.setText("$questionCounter/$size")
//            bruh()
//            rg_political.clearCheck()
//        } else {
//            runRadioChecker()
//        }
//        Log.d("Moose", "q $questionCounter")
//    }
//
//    fun rand(start: Int, end: Int): Int {
//        require(start <= end) { "Illegal Argument" }
//        return (start..end).random()
//    }
//
//    fun bruh() {
//        if (questionCounter <= size-1) {
//            val randomAnswer = rand(1, 3)
//            question = questionList[questionCounter]
//
//            tv_political_header.text = question.header
//            iv_political_icon.setImageResource(question.image)
//            if (randomAnswer == 1) {
//                rb_political_answer1.setText(question.answer0)
//                rb_political_answer2.setText(question.answer5)
//                rb_political_answer3.setText(question.answer10)
//            } else if (randomAnswer == 2) {
//                rb_political_answer1.setText(question.answer10)
//                rb_political_answer2.setText(question.answer0)
//                rb_political_answer3.setText(question.answer5)
//            } else {
//                rb_political_answer1.setText(question.answer5)
//                rb_political_answer2.setText(question.answer10)
//                rb_political_answer3.setText(question.answer0)
//            }
//
//        }
//    }
//
//    private var stopRadioRunner = false
//
//    private fun runRadioChecker(){
//        CoroutineScope(Main).launch{
//            radioCheckerLogic()
//        }
//    }
//    suspend fun radioCheckerLogic(){
//        while (!stopRadioRunner){
//            delay(200)
//            if (!rb_political_answer1.isChecked && !rb_political_answer2.isChecked && !rb_political_answer3.isChecked){
//                tv_political_error.setText("Please choose an answer")
//                iv_political_error.visibility = View.VISIBLE
//            }
//            else{
//                tv_political_error.setText("")
//                iv_political_error.visibility = View.INVISIBLE
//                stopRadioRunner = true
//            }
//        }
//    }
}}