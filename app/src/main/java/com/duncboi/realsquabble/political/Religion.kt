package com.duncboi.realsquabble.political

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.duncboi.realsquabble.political.Constants.rand
import com.duncboi.realsquabble.R
import kotlinx.android.synthetic.main.fragment_religion.*

class Religion : Fragment() {

    private val answer0: String = "Abortion should be unlawful and considered murder"
    private val answer5: String = "Abortion should be legal but not receive funding from the government"
    private val answer10: String = "Abortion should be legal everywhere"
    private val randomNumber = rand(1,3)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_religion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_religion_question_number.text = "${Constants.questionNumber +1}/${Constants.fragmentList.size}"
        val randomNumber = Constants.randomAnswers(
            randomNumber,
            rb_religion_answer1,
            rb_religion_answer2,
            rb_religion_answer3,
            answer0,
            answer5,
            answer10
        )

        b_religion_next.setOnClickListener {
            if (rb_religion_answer1.isChecked || rb_religion_answer2.isChecked || rb_religion_answer3.isChecked) {
                val answer = Constants.checkAnswers(
                    randomNumber,
                    rb_religion_answer1,
                    rb_religion_answer2,
                    rb_religion_answer3
                )
                activity?.let { it1 ->
                    Constants.nextFragment(
                        it1,
                        "Religion",
                        answer,
                        "social",
                        Constants.questionNumber + 1
                    )
                }
            }
            else{
                Constants.stopRadioRunner = false
                Constants.runRadioChecker(
                    rb_religion_answer1,
                    rb_religion_answer2,
                    rb_religion_answer3,
                    tv_religion_error,
                    iv_religion_error
                )
            }
        }

        b_religion_previous.setOnClickListener {
            activity?.let { it1 -> Constants.previousFragment(it1) }
        }

    }


}