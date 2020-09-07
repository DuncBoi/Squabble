package com.duncboi.realsquabble.political

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.duncboi.realsquabble.R
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
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.lang.Math.abs
import kotlin.system.measureTimeMillis


class ScoreCalculator : Fragment() {
    //make xyValueArray global
    private lateinit var xySeries: PointsGraphSeries<DataPoint>
    private var economicScore = 0.0
    private var socialScore = 0.0
    private var questionCounter = 0

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

        getData()

    }

    private fun getData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val emailQuery =
            FirebaseDatabase.getInstance().reference.child("Users").orderByChild("uid")
                .equalTo(uid)
        emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val snapshot2 = childSnapshot.child("Question Answers")
                    for (snap in snapshot2.children) {
                        questionCounter++
                        Log.d("Moose", "$snap")
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
        while (!stopLoading){
            delay (3000)
            if(questionCounter == 16) {
                score_calculator_constraint.visibility = View.VISIBLE
                stopLoading
                calculating.dismiss()
                plotPoint(economicScore, socialScore)
                showScore()
            }
        }

    }

    private fun showScore(){
        val x = economicScore-50
        val y = socialScore-50
        val x2 = abs(x)/50 + abs(x)/50
        val y2 = abs(y)/50 + abs(y)/50
        if (x2+y2 < 1){
            textView16.text = "Moderate"
        }
        else if (x2+y2 == 1.0) {
            if (socialScore != 50.0 && economicScore != 50.0) {
                if (socialScore > 50 && economicScore > 50) {
                    textView16.text = "Moderate - Slightly Libertarian"
                } else if (socialScore > 50 && economicScore < 50) {
                    textView16.text = "Moderate - Slightly Liberal"
                } else if (socialScore < 50 && economicScore > 50) {
                    textView16.text = "Moderate - Slightly Conservative"
                } else {
                    textView16.text = "Moderate -Slightly Authoritarian"
                }
            }
            else{
                if (socialScore == 50.0 && economicScore > 50.0){
                    textView16.text = "Moderate - Libertarian/Right"
                }
                else if (socialScore == 50.0 && economicScore < 50.0){
                    textView16.text = "Moderate - Authoritarian/Left"
                }
                else if (economicScore == 50.0 && socialScore > 50.0){
                    textView16.text = "Moderate - Libertarian/Left"
                }
                else{
                    textView16.text = "Moderate - Authoritarian/Right"
                }
            }
        }
        else{
            if (socialScore > 50 && economicScore > 50){
                textView16.text = "Libertarian"
                if (socialScore <= 62.5 && economicScore > 62.5){
                    tv_score_calculator_subtext.text = "(Right Leaning)"
                }
                else if (socialScore > 62.5 && economicScore <= 62.5){
                    tv_score_calculator_subtext.text = "(Left Leaning)"
                }
                else{
                    tv_score_calculator_subtext.text = ""
                }
            }
            else if (socialScore > 50 && economicScore < 50){
                textView16.text = "Liberal"
                if (socialScore <= 62.5 && economicScore < 37.5){
                    tv_score_calculator_subtext.text = "(Authoritarian Leaning)"
                }
                else if (economicScore >= 37.5 && socialScore > 62.5){
                    tv_score_calculator_subtext.text = "(Libertarian Leaning)"
                }
                else{
                    tv_score_calculator_subtext.text = ""
                }
            }
            else if (socialScore < 50 && economicScore > 50){
                textView16.text = "Conservative"
                    if (socialScore >= 37.5 && economicScore > 62.5){
                        tv_score_calculator_subtext.text = "(Libertarian Leaning)"
                }
                else if (economicScore <= 62.5 && socialScore < 37.5){
                        tv_score_calculator_subtext.text = "(Authoritarian Leaning)"
                    }
                else{
                        tv_score_calculator_subtext.text = ""
                    }
            }
            else if (socialScore < 50 && economicScore < 50){
                textView16.text = "Authoritarian"
                if (socialScore >= 37.5 && economicScore < 37.5){
                    tv_score_calculator_subtext.text = "(Left Leaning)"
                }
                else if (economicScore >= 37.5 && socialScore < 37.5){
                    tv_score_calculator_subtext.text = "(Right Leaning)"
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
                    }
                    else{
                        textView16.text = "Authoritarian"
                        tv_score_calculator_subtext.text = "(Right Leaning)"
                    }
                }
                if (socialScore == 50.0){
                    if (economicScore > 50){
                        textView16.text = "Libertarian"
                        tv_score_calculator_subtext.text = "(Right Leaning)"
                    }
                    else{
                        textView16.text = "Authoritarian"
                        tv_score_calculator_subtext.text = "(Left Leaning"
                    }
                }
            }
        }
        tv_score_calculator_personal_freedom_score.text = "$socialScore%"
        tv_score_calculator_economic_freedom_score.text = "$economicScore%"
    }

    private fun plotPoint(economicFreedom: Double, personalFreedom: Double) {
        xySeries = PointsGraphSeries()
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
}