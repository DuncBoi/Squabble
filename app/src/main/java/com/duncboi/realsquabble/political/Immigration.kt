package com.duncboi.realsquabble.political

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.duncboi.realsquabble.political.Constants.rand
import com.duncboi.realsquabble.R
import kotlinx.android.synthetic.main.fragment_immigration.*

class Immigration : Fragment() {

    private val answer0: String = "America should strongly limit immigration, deporting those who do not integrate"
    private val answer5: String = "Government should regulate immigration, allowing necessary immigrants to come in legally"
    private val answer10: String = "There should be no regulation on who enters the country"
    private val randomNumber = rand(1,3)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_immigration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_immigration_question_number.text = "${Constants.questionNumber +1}/${Constants.fragmentList.size}"
        val randomNumber = Constants.randomAnswers(
            randomNumber,
            rb_immigration_answer1,
            rb_immigration_answer2,
            rb_immigration_answer3,
            answer0,
            answer5,
            answer10
        )

        b_immigration_next.setOnClickListener {
            if (rb_immigration_answer1.isChecked || rb_immigration_answer2.isChecked || rb_immigration_answer3.isChecked) {
                val answer = Constants.checkAnswers(
                    randomNumber,
                    rb_immigration_answer1,
                    rb_immigration_answer2,
                    rb_immigration_answer3
                )
                activity?.let { it1 ->
                    Constants.nextFragment(
                        it1,
                        "Immigration",
                        answer,
                        "social",
                        Constants.questionNumber + 1
                    )
                }
            }
            else{
                Constants.stopRadioRunner = false
                Constants.runRadioChecker(
                    rb_immigration_answer1,
                    rb_immigration_answer2,
                    rb_immigration_answer3,
                    tv_immigration_error,
                    iv_immigration_error
                )
            }
        }

        b_immigration_previous.setOnClickListener {
            activity?.let { it1 -> Constants.previousFragment(it1) }
        }

    }

}