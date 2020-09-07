package com.duncboi.realsquabble.political

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.duncboi.realsquabble.Constants
import com.duncboi.realsquabble.Constants.checkAnswers
import com.duncboi.realsquabble.Constants.fragmentList
import com.duncboi.realsquabble.Constants.nextFragment
import com.duncboi.realsquabble.Constants.previousFragment
import com.duncboi.realsquabble.Constants.questionNumber
import com.duncboi.realsquabble.Constants.rand
import com.duncboi.realsquabble.Constants.randomAnswers
import com.duncboi.realsquabble.Constants.runRadioChecker
import com.duncboi.realsquabble.R
import kotlinx.android.synthetic.main.fragment_discrimination.*

class discrimination : Fragment() {

    private val answer0: String = "0"
    private val answer5: String = "5"
    private val answer10: String = "10"
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