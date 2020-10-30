package com.duncboi.realsquabble.political

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.duncboi.realsquabble.political.Constants.rand
import com.duncboi.realsquabble.R
import kotlinx.android.synthetic.main.fragment_environment.*

class Environment : Fragment() {

    private val answer0: String = "Companies should be forced to follow environmental regulations"
    private val answer5: String = "Companies with eco-friendly practices should receive tax benefits and subsidies"
    private val answer10: String = "Strict environmental regulations hurt the economy and should not be enforced"
    private val randomNumber = rand(1,3)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_environment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_environment_question_number.text = "${Constants.questionNumber +1}/${Constants.fragmentList.size}"
        val randomNumber = Constants.randomAnswers(
            randomNumber,
            rb_environment_answer1,
            rb_environment_answer2,
            rb_environment_answer3,
            answer0,
            answer5,
            answer10
        )

        b_environment_next.setOnClickListener {
            if (rb_environment_answer1.isChecked || rb_environment_answer2.isChecked || rb_environment_answer3.isChecked) {
                val answer = Constants.checkAnswers(
                    randomNumber,
                    rb_environment_answer1,
                    rb_environment_answer2,
                    rb_environment_answer3
                )
                activity?.let { it1 ->
                    Constants.nextFragment(
                        it1,
                        "Environment",
                        answer,
                        "economic",
                        Constants.questionNumber + 1
                    )
                }
            }
            else{
                Constants.stopRadioRunner = false
                Constants.runRadioChecker(
                    rb_environment_answer1,
                    rb_environment_answer2,
                    rb_environment_answer3,
                    tv_environment_error,
                    iv_environment_error
                )
            }
        }

        b_environment_previous.setOnClickListener {
            activity?.let { it1 -> Constants.previousFragment(it1) }
        }

    }

}