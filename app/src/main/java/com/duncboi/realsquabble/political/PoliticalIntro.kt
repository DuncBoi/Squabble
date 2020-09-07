package com.duncboi.realsquabble.political

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.duncboi.realsquabble.Constants.fragmentList
import com.duncboi.realsquabble.Constants.questionNumber
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

        b_political_into_start_quiz.setOnClickListener {
            val nextFragment = fragmentList[questionNumber]
            nextFragment(nextFragment)
        }
    }

    private fun nextFragment(nextFragment: Fragment) {
        activity?.supportFragmentManager!!.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .replace(R.id.root_container, nextFragment)
            .commitAllowingStateLoss()
    }
}