package com.duncboi.realsquabble.political

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.duncboi.realsquabble.Constants
import com.duncboi.realsquabble.Constants.economicFreedom
import com.duncboi.realsquabble.Constants.economicScore
import com.duncboi.realsquabble.Constants.fragmentList
import com.duncboi.realsquabble.Constants.gunControl
import com.duncboi.realsquabble.Constants.questionNumber
import com.duncboi.realsquabble.Constants.socialScore
import com.duncboi.realsquabble.R
import kotlinx.android.synthetic.main.fragment_political_intro.*

class PoliticalIntro : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_political_intro, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        questionNumber = 0
        economicScore = 0
        socialScore = 0
        b_political_into_start_quiz.setOnClickListener {
            val nextFragment = fragmentList[questionNumber]
            nextFragment(nextFragment)
        }
    }

    private fun nextFragment(nextFragment: Fragment) {
        Log.d("Moose", "$questionNumber")
        activity?.supportFragmentManager!!.beginTransaction()
            .replace(R.id.root_container, nextFragment)
            .commitAllowingStateLoss()
    }
}