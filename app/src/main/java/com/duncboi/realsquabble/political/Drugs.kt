package com.duncboi.realsquabble.political

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.duncboi.realsquabble.political.Constants.rand
import com.duncboi.realsquabble.R
import kotlinx.android.synthetic.main.fragment_drugs.*

class Drugs : Fragment() {

    private val answer0: String = "The government should fight to minimize recreational drug use and its distribution"
    private val answer5: String = "Recreational drugs should be legal, with regulated distribution"
    private val answer10: String = "Recreational drugs should be legal, without regulation"

    private val randomNumber = rand(1,3)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_drugs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_drugs_question_number.text = "${Constants.questionNumber +1}/${Constants.fragmentList.size}"
        val randomNumber = Constants.randomAnswers(
            randomNumber,
            rb_drugs_answer1,
            rb_drugs_answer2,
            rb_drugs_answer3,
            answer0,
            answer5,
            answer10
        )

        b_drugs_next.setOnClickListener {
            if (rb_drugs_answer1.isChecked || rb_drugs_answer2.isChecked || rb_drugs_answer3.isChecked) {
                val answer = Constants.checkAnswers(
                    randomNumber,
                    rb_drugs_answer1,
                    rb_drugs_answer2,
                    rb_drugs_answer3
                )
                activity?.let { it1 ->
                    Constants.nextFragment(
                        it1,
                        "Drugs",
                        answer,
                        "social",
                        Constants.questionNumber + 1
                    )
                }
            }
            else{
                Constants.stopRadioRunner = false
                Constants.runRadioChecker(
                    rb_drugs_answer1,
                    rb_drugs_answer2,
                    rb_drugs_answer3,
                    tv_drugs_error,
                    iv_drugs_error
                )
            }
        }

        b_drugs_previous.setOnClickListener {
            activity?.let { it1 -> Constants.previousFragment(it1) }
        }

    }

}