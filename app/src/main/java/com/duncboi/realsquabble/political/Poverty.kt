package com.duncboi.realsquabble.political

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.duncboi.realsquabble.political.Constants.rand
import com.duncboi.realsquabble.R
import kotlinx.android.synthetic.main.fragment_poverty.*

class Poverty : Fragment() {

    private val answer0: String = "The government should subsidize the poor with tax dollars"
    private val answer5: String = "The government should provide only the bare necessities for survival to every American"
    private val answer10: String = "The government should not be expected to help the poor at all with tax dollars"
    private val randomNumber = rand(1,3)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_poverty, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_poverty_question_number.text = "${Constants.questionNumber +1}/${Constants.fragmentList.size}"
        val randomNumber = Constants.randomAnswers(
            randomNumber,
            rb_poverty_answer1,
            rb_poverty_answer2,
            rb_poverty_answer3,
            answer0,
            answer5,
            answer10
        )

        b_poverty_next.setOnClickListener {
            if (rb_poverty_answer1.isChecked || rb_poverty_answer2.isChecked || rb_poverty_answer3.isChecked) {
                val answer = Constants.checkAnswers(
                    randomNumber,
                    rb_poverty_answer1,
                    rb_poverty_answer2,
                    rb_poverty_answer3
                )
                activity?.let { it1 ->
                    Constants.nextFragment(
                        it1,
                        "Poverty",
                        answer,
                        "economic",
                        Constants.questionNumber + 1
                    )
                }
            }
            else{
                Constants.stopRadioRunner = false
                Constants.runRadioChecker(
                    rb_poverty_answer1,
                    rb_poverty_answer2,
                    rb_poverty_answer3,
                    tv_poverty_error,
                    iv_poverty_error
                )
            }
        }

        b_poverty_previous.setOnClickListener {
            activity?.let { it1 -> Constants.previousFragment(it1) }
        }

    }

}