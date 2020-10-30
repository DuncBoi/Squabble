package com.duncboi.realsquabble.political

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.duncboi.realsquabble.political.Constants.rand
import com.duncboi.realsquabble.R
import kotlinx.android.synthetic.main.fragment_taxes.*


class Taxes : Fragment() {

    private val answer0: String = "The rich should pay more in taxes"
    private val answer5: String = "Current taxes on the rich should not change"
    private val answer10: String = "The rich are too highly taxed"
    private val randomNumber = rand(1,3)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_taxes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_taxes_question_number.text = "${Constants.questionNumber +1}/${Constants.fragmentList.size}"
        val randomNumber = Constants.randomAnswers(
            randomNumber,
            rb_taxes_answer1,
            rb_taxes_answer2,
            rb_taxes_answer3,
            answer0,
            answer5,
            answer10
        )

        b_taxes_next.setOnClickListener {
            if (rb_taxes_answer1.isChecked || rb_taxes_answer2.isChecked || rb_taxes_answer3.isChecked) {
                val answer = Constants.checkAnswers(
                    randomNumber,
                    rb_taxes_answer1,
                    rb_taxes_answer2,
                    rb_taxes_answer3
                )
                activity?.let { it1 ->
                    Constants.nextFragment(
                        it1,
                        "Taxes",
                        answer,
                        "economic",
                        Constants.questionNumber + 1
                    )
                }
            }
            else{
                Constants.stopRadioRunner = false
                Constants.runRadioChecker(
                    rb_taxes_answer1,
                    rb_taxes_answer2,
                    rb_taxes_answer3,
                    tv_taxes_error,
                    iv_taxes_error
                )
            }
        }

        b_taxes_previous.setOnClickListener {
            activity?.let { it1 -> Constants.previousFragment(it1) }
        }

    }


}