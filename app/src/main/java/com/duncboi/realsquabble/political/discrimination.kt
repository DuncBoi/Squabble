package com.duncboi.realsquabble.political

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.duncboi.realsquabble.political.Constants.checkAnswers
import com.duncboi.realsquabble.political.Constants.fragmentList
import com.duncboi.realsquabble.political.Constants.nextFragment
import com.duncboi.realsquabble.political.Constants.previousFragment
import com.duncboi.realsquabble.political.Constants.questionNumber
import com.duncboi.realsquabble.political.Constants.rand
import com.duncboi.realsquabble.political.Constants.randomAnswers
import com.duncboi.realsquabble.political.Constants.runRadioChecker
import com.duncboi.realsquabble.R
import kotlinx.android.synthetic.main.fragment_discrimination.*

class discrimination : Fragment() {

    private val answer0: String = "The government should award tax money to underrepresented groups"
    private val answer5: String = "Institutions offering benefits to underrepresented groups should receive rewards"
    private val answer10: String = "Benefits imposed by the government based on race, gender, etc. is wrong"
    private val randomNumber = rand(1,3)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_discrimination, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_discrimination_question_number.text = "${questionNumber+1}/${fragmentList.size}"
        val randomNumber = randomAnswers(randomNumber, rb_discrimination_answer1, rb_discrimination_answer2, rb_discrimination_answer3, answer0, answer5, answer10)

        b_discrimination_next.setOnClickListener {
            if (rb_discrimination_answer1.isChecked || rb_discrimination_answer2.isChecked || rb_discrimination_answer3.isChecked) {
                val answer = checkAnswers(randomNumber, rb_discrimination_answer1, rb_discrimination_answer2, rb_discrimination_answer3)
                activity?.let { it1 -> nextFragment(it1, "Discrimination", answer, "social", questionNumber+1) }
            }
            else{
                Constants.stopRadioRunner = false
                runRadioChecker(rb_discrimination_answer1, rb_discrimination_answer2, rb_discrimination_answer3, tv_discrimination_error, iv_discrimination_error)
            }
        }

        b_discrimination_previous.setOnClickListener {
            activity?.let { it1 -> previousFragment(it1) }
        }

    }
}