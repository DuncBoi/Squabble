package com.duncboi.realsquabble.political

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.duncboi.realsquabble.political.Constants.rand
import com.duncboi.realsquabble.R
import kotlinx.android.synthetic.main.fragment_freedom_of_speech.*

class FreedomOfSpeech : Fragment() {

    private val answer0: String = "The government should have the ability to censor any message considered \"hateful\""
    private val answer5: String = "The government should censor only misleading information"
    private val answer10: String = "The government should not censor any opinions no matter the case"
    private val randomNumber = rand(1,3)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_freedom_of_speech, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_freedom_of_speech_question_number.text = "${Constants.questionNumber +1}/${Constants.fragmentList.size}"
        val randomNumber = Constants.randomAnswers(
            randomNumber,
            rb_freedom_of_speech_answer1,
            rb_freedom_of_speech_answer2,
            rb_freedom_of_speech_answer3,
            answer0,
            answer5,
            answer10
        )

        b_freedom_of_speech_next.setOnClickListener {
            if (rb_freedom_of_speech_answer1.isChecked || rb_freedom_of_speech_answer2.isChecked || rb_freedom_of_speech_answer3.isChecked) {
                val answer = Constants.checkAnswers(
                    randomNumber,
                    rb_freedom_of_speech_answer1,
                    rb_freedom_of_speech_answer2,
                    rb_freedom_of_speech_answer3
                )
                activity?.let { it1 ->
                    Constants.nextFragment(
                        it1,
                        "Freedom of Speech",
                        answer,
                        "social",
                        Constants.questionNumber + 1
                    )
                }
            }
            else{
                Constants.stopRadioRunner = false
                Constants.runRadioChecker(
                    rb_freedom_of_speech_answer1,
                    rb_freedom_of_speech_answer2,
                    rb_freedom_of_speech_answer3,
                    tv_freedom_of_speech_error,
                    iv_freedom_of_speech_error
                )
            }
        }

        b_freedom_of_speech_previous.setOnClickListener {
            activity?.let { it1 -> Constants.previousFragment(it1) }
        }

    }

}