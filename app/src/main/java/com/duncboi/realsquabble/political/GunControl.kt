package com.duncboi.realsquabble.political

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.duncboi.realsquabble.Constants.checkAnswers
import com.duncboi.realsquabble.Constants.fragmentList
import com.duncboi.realsquabble.Constants.nextFragment
import com.duncboi.realsquabble.Constants.previousFragment
import com.duncboi.realsquabble.Constants.questionNumber
import com.duncboi.realsquabble.Constants.rand
import com.duncboi.realsquabble.Constants.randomAnswers
import com.duncboi.realsquabble.Constants.runRadioChecker
import com.duncboi.realsquabble.Constants.stopRadioRunner
import com.duncboi.realsquabble.R
import kotlinx.android.synthetic.main.fragment_gun_control.*


class GunControl : Fragment() {

    private val answer0: String = "All gun ownership should be banned"
    private val answer5: String = "People should be able to own non automatic guns"
    private val answer10: String = "Every mentally stable person should be able to own a gun"
    private val randomNumber = rand(1,3)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gun_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_gun_control_question_number.text = "${questionNumber+1}/${fragmentList.size}"

        val randomNumber = randomAnswers(randomNumber, rb_gun_control_answer1, rb_gun_control_answer2, rb_gun_control_answer3, answer0, answer5, answer10)

        b_gun_control_next.setOnClickListener {

            if (rb_gun_control_answer1.isChecked || rb_gun_control_answer2.isChecked || rb_gun_control_answer3.isChecked){
                val answer = checkAnswers(randomNumber, rb_gun_control_answer1, rb_gun_control_answer2, rb_gun_control_answer3)
                activity?.let { it1 -> nextFragment(it1, "Gun Control", answer, "social", questionNumber+1) }
            }
            else{
                stopRadioRunner = false
                runRadioChecker(rb_gun_control_answer1, rb_gun_control_answer2, rb_gun_control_answer3, tv_gun_control_error, iv_gun_control_error)
        }
        }

        b_gun_control_previous.setOnClickListener {
            activity?.let { it1 -> previousFragment(it1) }
        }

    }
}