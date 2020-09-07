package com.duncboi.realsquabble.political

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.duncboi.realsquabble.Constants
import com.duncboi.realsquabble.Constants.rand
import com.duncboi.realsquabble.R
import kotlinx.android.synthetic.main.fragment_government_performance.*

class GovernmentPerformance : Fragment() {

    private val answer0: String = "0"
    private val answer5: String = "5"
    private val answer10: String = "10"
    private val randomNumber = rand(1,3)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_government_performance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_government_performance_question_number.text = "${Constants.questionNumber +1}/${Constants.fragmentList.size}"
        val randomNumber = Constants.randomAnswers(
            randomNumber,
            rb_government_performance_answer1,
            rb_government_performance_answer2,
            rb_government_performance_answer3,
            answer0,
            answer5,
            answer10
        )

        b_government_performance_next.setOnClickListener {
            if (rb_government_performance_answer1.isChecked || rb_government_performance_answer2.isChecked || rb_government_performance_answer3.isChecked) {
                val answer = Constants.checkAnswers(
                    randomNumber,
                    rb_government_performance_answer1,
                    rb_government_performance_answer2,
                    rb_government_performance_answer3
                )
                activity?.let { it1 ->
                    Constants.nextFragment(
                        it1,
                        "Government Performance",
                        answer,
                        "economic",
                        Constants.questionNumber + 1
                    )
                }
            }
            else{
                Constants.stopRadioRunner = false
                Constants.runRadioChecker(
                    rb_government_performance_answer1,
                    rb_government_performance_answer2,
                    rb_government_performance_answer3,
                    tv_government_performance_error,
                    iv_government_performance_error
                )
            }
        }

        b_government_performance_previous.setOnClickListener {
            activity?.let { it1 -> Constants.previousFragment(it1) }
        }

    }

}