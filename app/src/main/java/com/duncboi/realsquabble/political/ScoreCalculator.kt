package com.duncboi.realsquabble.political

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.PointsGraphSeries
import kotlinx.android.synthetic.main.fragment_score_calculator.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.lang.Math.abs


class ScoreCalculator() : Fragment() {
    //make xyValueArray global
    private lateinit var xySeries: PointsGraphSeries<DataPoint>
    private var economicScore = 0.0
    private var socialScore = 0.0
    private var questionCounter = 0

    val uid = FirebaseAuth.getInstance().currentUser!!.uid
    private val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").child(uid)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_score_calculator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        score_calculator_constraint.visibility = View.INVISIBLE
        runLoading()
        graph.viewport.isScalable = false
        graph.viewport.isYAxisBoundsManual = true
        graph.viewport.isXAxisBoundsManual = true
        graph.gridLabelRenderer.horizontalLabelsColor = Color.parseColor("#bf55ec")
        graph.gridLabelRenderer.verticalLabelsColor = Color.parseColor("#bf55ec")
        graph.gridLabelRenderer.horizontalAxisTitle = " "
        graph.gridLabelRenderer.labelHorizontalHeight = 0
        graph.gridLabelRenderer.labelsSpace = 25
        graph.viewport.setMaxX(100.0)
        graph.viewport.setScalableY(false)
        graph.viewport.setMinX(0.0)
        graph.viewport.setMaxY(100.0)
        xySeries = PointsGraphSeries()

        getData()

        b_score_calculator_continue.setOnClickListener {
            val intent = Intent(activity, ProfileActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

    }

    private fun getData() {
         emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                    val snapshot2 = snapshot.child("Question Answers")
                questionCounter = snapshot2.childrenCount.toInt()
                    for (snap in snapshot2.children) {
                        if (snap.child("type").getValue<String>()
                                .toString() == "social"
                        ) {
                            socialScore += snap.child("answer points")
                                .getValue<String>()
                                .toString()
                                .toDouble()
                        } else {
                            economicScore += snap.child("answer points")
                                .getValue<String>()
                                .toString().toDouble()
                        }
                    }
                }
        })
    }

    private val stopLoading = false

    private fun runLoading(){
        CoroutineScope(Main).launch {
            loadingLogic()
        }
    }

    private suspend fun loadingLogic(){
        val calculating = calculatingResults()
        calculating.show()
        delay (3000)
            if(questionCounter == 16) {
                score_calculator_constraint.visibility = View.VISIBLE
                stopLoading
                calculating.dismiss()
                plotPoint(economicScore, socialScore)
                showScore()
            }
        else{
            }

    }

    private fun showScore(){
        val x = economicScore-50
        val y = socialScore-50
        val x2 = abs(x)/50 + abs(x)/50
        val y2 = abs(y)/50 + abs(y)/50
        if (x2+y2 < 1){
            textView16.text = "Moderate"
            iv_score_calculator_party_icon.setImageResource(R.drawable.auth_symbol)
            xySeries.color = activity?.let { ContextCompat.getColor(it, R.color.Moderate) }!!
            activity?.let { ContextCompat.getColor(it, R.color.Moderate) }?.let { textView16.setTextColor(it) }
        }
        else if (x2+y2 == 1.0) {
            if (socialScore != 50.0 && economicScore != 50.0) {
                if (socialScore > 50 && economicScore > 50) {
                    iv_score_calculator_party_icon.setImageResource(R.drawable.auth_symbol)
                    xySeries.color = activity?.let { ContextCompat.getColor(it, R.color.Moderate) }!!
                    activity?.let { ContextCompat.getColor(it, R.color.Moderate) }?.let { textView16.setTextColor(it) }
                    textView16.text = "Moderate"
                    tv_score_calculator_subtext.text = "(Libertarian Leaning)"
                    activity?.let { ContextCompat.getColor(it, R.color.Libertarian) }?.let { tv_score_calculator_subtext.setTextColor(it) }
                    subtextConstraint()
                } else if (socialScore > 50 && economicScore < 50) {
                    iv_score_calculator_party_icon.setImageResource(R.drawable.auth_symbol)
                    xySeries.color = activity?.let { ContextCompat.getColor(it, R.color.Moderate) }!!
                    activity?.let { ContextCompat.getColor(it, R.color.Moderate) }?.let { textView16.setTextColor(it) }
                    textView16.text = "Moderate"
                    tv_score_calculator_subtext.text = "(Liberal Leaning)"
                    activity?.let { ContextCompat.getColor(it, R.color.Liberal) }?.let { tv_score_calculator_subtext.setTextColor(it) }
                    subtextConstraint()
                } else if (socialScore < 50 && economicScore > 50) {
                    iv_score_calculator_party_icon.setImageResource(R.drawable.auth_symbol)
                    xySeries.color = activity?.let { ContextCompat.getColor(it, R.color.Moderate) }!!
                    activity?.let { ContextCompat.getColor(it, R.color.Moderate) }?.let { textView16.setTextColor(it) }
                    textView16.text = "Moderate"
                    tv_score_calculator_subtext.text = "(Conservative Leaning)"
                    activity?.let { ContextCompat.getColor(it, R.color.Conservative) }?.let { tv_score_calculator_subtext.setTextColor(it) }
                    subtextConstraint()
                } else {
                    iv_score_calculator_party_icon.setImageResource(R.drawable.auth_symbol)
                    xySeries.color = activity?.let { ContextCompat.getColor(it, R.color.Moderate) }!!
                    activity?.let { ContextCompat.getColor(it, R.color.Moderate) }?.let { textView16.setTextColor(it) }
                    textView16.text = "Moderate"
                    tv_score_calculator_subtext.text = "(Authoritarian Leaning)"
                    activity?.let { ContextCompat.getColor(it, R.color.Authoritarian) }?.let { tv_score_calculator_subtext.setTextColor(it) }
                    subtextConstraint()
                }
            }
            else{
                if (socialScore == 50.0 && economicScore > 50.0){
                    iv_score_calculator_party_icon.setImageResource(R.drawable.torch)
                    xySeries.color = activity?.let { ContextCompat.getColor(it, R.color.Libertarian) }!!
                    activity?.let { ContextCompat.getColor(it, R.color.Libertarian) }?.let { textView16.setTextColor(it) }
                    textView16.text = "Libertarian"
                    tv_score_calculator_subtext.text = "(Right Leaning)"
                    activity?.let { ContextCompat.getColor(it, R.color.Conservative) }?.let { tv_score_calculator_subtext.setTextColor(it) }
                    subtextConstraint()
                }
                else if (socialScore == 50.0 && economicScore < 50.0){
                    iv_score_calculator_party_icon.setImageResource(R.drawable.capitol_hill)
                    xySeries.color = activity?.let { ContextCompat.getColor(it, R.color.Authoritarian) }!!
                    activity?.let { ContextCompat.getColor(it, R.color.Authoritarian) }?.let { textView16.setTextColor(it) }
                    textView16.text = "Authoritarian"
                    tv_score_calculator_subtext.text = "(Left Leaning)"
                    activity?.let { ContextCompat.getColor(it, R.color.Liberal) }?.let { tv_score_calculator_subtext.setTextColor(it) }
                    subtextConstraint()
                }
                else if (economicScore == 50.0 && socialScore > 50.0){
                    iv_score_calculator_party_icon.setImageResource(R.drawable.torch)
                    xySeries.color = activity?.let { ContextCompat.getColor(it, R.color.Libertarian) }!!
                    activity?.let { ContextCompat.getColor(it, R.color.Libertarian) }?.let { textView16.setTextColor(it) }
                    textView16.text = "Libertarian"
                    tv_score_calculator_subtext.text = "(Left Leaning)"
                    activity?.let { ContextCompat.getColor(it, R.color.Liberal) }?.let { tv_score_calculator_subtext.setTextColor(it) }
                    subtextConstraint()
                }
                else{
                    iv_score_calculator_party_icon.setImageResource(R.drawable.capitol_hill)
                    xySeries.color = activity?.let { ContextCompat.getColor(it, R.color.Authoritarian) }!!
                    activity?.let { ContextCompat.getColor(it, R.color.Authoritarian) }?.let { textView16.setTextColor(it) }
                    textView16.text = "Authoritarian"
                    tv_score_calculator_subtext.text = "(Right Leaning)"
                    activity?.let { ContextCompat.getColor(it, R.color.Conservative) }?.let { tv_score_calculator_subtext.setTextColor(it) }
                    subtextConstraint()
                }
            }
        }
        else{
            if (socialScore > 50 && economicScore > 50){
                textView16.text = "Libertarian"
                iv_score_calculator_party_icon.setImageResource(R.drawable.torch)
                xySeries.color = activity?.let { ContextCompat.getColor(it, R.color.Libertarian) }!!
                activity?.let { ContextCompat.getColor(it, R.color.Libertarian) }?.let { textView16.setTextColor(it) }
                if (socialScore <= 62.5 && economicScore > 62.5){
                    tv_score_calculator_subtext.text = "(Right Leaning)"
                    activity?.let { ContextCompat.getColor(it, R.color.Conservative) }?.let { tv_score_calculator_subtext.setTextColor(it) }
                    subtextConstraint()
                }
                else if (socialScore > 62.5 && economicScore <= 62.5){
                    tv_score_calculator_subtext.text = "(Left Leaning)"
                    activity?.let { ContextCompat.getColor(it, R.color.Liberal) }?.let { tv_score_calculator_subtext.setTextColor(it) }
                    subtextConstraint()
                }
                else{
                    tv_score_calculator_subtext.text = ""
                }
            }
            else if (socialScore > 50 && economicScore < 50){
                textView16.text = "Liberal"
                iv_score_calculator_party_icon.setImageResource(R.drawable.ic_donkey)
                xySeries.color = activity?.let { ContextCompat.getColor(it, R.color.Liberal) }!!
                activity?.let { ContextCompat.getColor(it, R.color.Liberal) }?.let { textView16.setTextColor(it) }
                if (socialScore <= 62.5 && economicScore < 37.5){
                    tv_score_calculator_subtext.text = "(Authoritarian Leaning)"
                    activity?.let { ContextCompat.getColor(it, R.color.Authoritarian) }?.let { tv_score_calculator_subtext.setTextColor(it) }
                    subtextConstraint()
                }
                else if (economicScore >= 37.5 && socialScore > 62.5){
                    tv_score_calculator_subtext.text = "(Libertarian Leaning)"
                    activity?.let { ContextCompat.getColor(it, R.color.Libertarian) }?.let { tv_score_calculator_subtext.setTextColor(it) }
                    subtextConstraint()
                }
                else{
                    tv_score_calculator_subtext.text = ""
                }
            }
            else if (socialScore < 50 && economicScore > 50){
                textView16.text = "Conservative"
                iv_score_calculator_party_icon.setImageResource(R.drawable.elephant)
                xySeries.color = activity?.let { ContextCompat.getColor(it, R.color.Conservative) }!!
                activity?.let { ContextCompat.getColor(it, R.color.Conservative) }?.let { textView16.setTextColor(it) }
                if (socialScore >= 37.5 && economicScore > 62.5){
                        tv_score_calculator_subtext.text = "(Libertarian Leaning)"
                        activity?.let { ContextCompat.getColor(it, R.color.Libertarian) }?.let { tv_score_calculator_subtext.setTextColor(it) }
                    subtextConstraint()
                }
                else if (economicScore <= 62.5 && socialScore < 37.5){
                        tv_score_calculator_subtext.text = "(Authoritarian Leaning)"
                        activity?.let { ContextCompat.getColor(it, R.color.Authoritarian) }?.let { tv_score_calculator_subtext.setTextColor(it) }
                    subtextConstraint()
                }
                else{
                        tv_score_calculator_subtext.text = ""
                    }
            }
            else if (socialScore < 50 && economicScore < 50){
                textView16.text = "Authoritarian"
                xySeries.color = activity?.let { ContextCompat.getColor(it, R.color.Authoritarian) }!!
                iv_score_calculator_party_icon.setImageResource(R.drawable.capitol_hill)
                activity?.let { ContextCompat.getColor(it, R.color.Authoritarian) }?.let { textView16.setTextColor(it) }
                if (socialScore >= 37.5 && economicScore < 37.5){
                    tv_score_calculator_subtext.text = "(Left Leaning)"
                    activity?.let { ContextCompat.getColor(it, R.color.Liberal) }?.let { tv_score_calculator_subtext.setTextColor(it) }
                    subtextConstraint()
                }
                else if (economicScore >= 37.5 && socialScore < 37.5){
                    tv_score_calculator_subtext.text = "(Right Leaning)"
                    activity?.let { ContextCompat.getColor(it, R.color.Conservative) }?.let { tv_score_calculator_subtext.setTextColor(it) }
                    subtextConstraint()
                }
                else{
                    tv_score_calculator_subtext.text = ""
                }
            }
            else{
                if (economicScore == 50.0){
                    if (socialScore > 50.0){
                        textView16.text = "Libertarian"
                        tv_score_calculator_subtext.text = "(Left Leaning)"
                        activity?.let { ContextCompat.getColor(it, R.color.Libertarian) }?.let { tv_score_calculator_subtext.setTextColor(it) }
                        subtextConstraint()
                    }
                    else{
                        textView16.text = "Authoritarian"
                        tv_score_calculator_subtext.text = "(Right Leaning)"
                        activity?.let { ContextCompat.getColor(it, R.color.Authoritarian) }?.let { tv_score_calculator_subtext.setTextColor(it) }
                        subtextConstraint()
                    }
                }
                if (socialScore == 50.0){
                    if (economicScore > 50){
                        textView16.text = "Libertarian"
                        tv_score_calculator_subtext.text = "(Right Leaning)"
                        activity?.let { ContextCompat.getColor(it, R.color.Conservative) }?.let { tv_score_calculator_subtext.setTextColor(it) }
                        subtextConstraint()
                    }
                    else{
                        textView16.text = "Authoritarian"
                        tv_score_calculator_subtext.text = "(Left Leaning)"
                        activity?.let { ContextCompat.getColor(it, R.color.Liberal) }?.let { tv_score_calculator_subtext.setTextColor(it) }
                        subtextConstraint()
                    }
                }
            }
        }
         emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.child("category").ref.setValue(textView16.text.toString().trim())
                snapshot.child("economicScore").ref.setValue("$economicScore")
                snapshot.child("socialScore").ref.setValue("$socialScore")
                snapshot.child("alignmentTime").ref.setValue("${System.currentTimeMillis()}")
            }
        })
        tv_score_calculator_personal_freedom_score.text = "$socialScore%"
        tv_score_calculator_economic_freedom_score.text = "$economicScore%"
    }

    private fun plotPoint(economicFreedom: Double, personalFreedom: Double) {
        xySeries.shape = PointsGraphSeries.Shape.POINT
        xySeries.size = 25f
        xySeries.color = Color.parseColor("#bf55ec")
        xySeries.appendData(DataPoint(economicFreedom, personalFreedom), false, 1)
        graph.addSeries(xySeries)
    }

    private fun calculatingResults(): AlertDialog {
        val sendingEmail = activity?.let { AlertDialog.Builder(it) }
        sendingEmail!!.setView(R.layout.calculating_results)
        sendingEmail.setCancelable(false)
        return sendingEmail.create()
    }

    private fun subtextConstraint(){
        imageView3.visibility = View.INVISIBLE
        iv_score_calc_subtext.visibility = View.VISIBLE
        val set = ConstraintSet()
        val scoreCalculator = alignment_view_constraint
        set.clone(scoreCalculator)
        set.clear(tv_score_calculator_subtext.id, ConstraintSet.TOP)
        set.connect(tv_score_calculator_subtext.id, ConstraintSet.BOTTOM, iv_score_calc_subtext.id, ConstraintSet.BOTTOM, 10)
        set.clear(textView13.id, ConstraintSet.TOP)
        set.connect(textView13.id, ConstraintSet.TOP, iv_score_calc_subtext.id, ConstraintSet.BOTTOM, 24)
        set.applyTo(scoreCalculator)
    }
}