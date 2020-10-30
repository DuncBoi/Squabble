package com.duncboi.realsquabble.political

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.duncboi.realsquabble.political.Constants.rand
import com.duncboi.realsquabble.R
import kotlinx.android.synthetic.main.fragment_nation.*

class Nation : Fragment() {

    private val answer0: String = "Military service should be required from every able-bodied citizen"
    private val answer5: String = "Military service should only be required during times of war"
    private val answer10: String = "Military service should always be voluntary"
    private val randomNumber = rand(1,3)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_nation_question_number.text = "${Constants.questionNumber +1}/${Constants.fragmentList.size}"
        val randomNumber = Constants.randomAnswers(
            randomNumber,
            rb_nation_answer1,
            rb_nation_answer2,
            rb_nation_answer3,
            answer0,
            answer5,
            answer10
        )

        b_nation_next.setOnClickListener {
            if (rb_nation_answer1.isChecked || rb_nation_answer2.isChecked || rb_nation_answer3.isChecked) {
                val answer = Constants.checkAnswers(
                    randomNumber,
                    rb_nation_answer1,
                    rb_nation_answer2,
                    rb_nation_answer3
                )
                activity?.let { it1 ->
                    Constants.nextFragment(
                        it1,
                        "Nation",
                        answer,
                        "social",
                        Constants.questionNumber + 1
                    )
                }
            }
            else{
                Constants.stopRadioRunner = false
                Constants.runRadioChecker(
                    rb_nation_answer1,
                    rb_nation_answer2,
                    rb_nation_answer3,
                    tv_nation_error,
                    iv_nation_error
                )
            }
        }

        b_nation_previous.setOnClickListener {
            activity?.let { it1 -> Constants.previousFragment(it1) }
        }

    }

}