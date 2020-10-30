package com.duncboi.realsquabble.Miscellaneous

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.duncboi.realsquabble.ModelClasses.Users
import com.duncboi.realsquabble.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.PointsGraphSeries
import kotlinx.android.synthetic.main.fragment_alignment_view.*

class AlignmentView : Fragment() {

    private val args: AlignmentViewArgs by navArgs()
    private lateinit var xySeries: PointsGraphSeries<DataPoint>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_alignment_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(args.uid)

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

        userRef.onDisconnect().cancel()
        userRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)
                if (tv_alignment_view_personal_freedom_score != null) {
                    if (user != null) {
                        if (user.getEconomicScore() != "" && user.getSocialScore() != "") {
                            tv_alignment_view_personal_freedom_score.text =
                                "${user.getSocialScore()}%"
                            tv_alignment_view_economic_freedom_score.text =
                                "${user.getEconomicScore()}%"
                            plotPoint(
                                user.getEconomicScore()!!.toDouble(),
                                user.getSocialScore()!!.toDouble()
                            )
                        }
                        else{
                            cl_alignment_view_not_available_constraint.visibility = View.VISIBLE
                        }
                        when (user.getCategory()) {
                            "Moderate" -> {
                                iv_alignment_view_party_icon.setImageResource(R.drawable.moderate_banner)
                                tv_alignment_view_category.text = "Moderate"
                            }
                            "Conservative" -> {
                                iv_alignment_view_party_icon.setImageResource(R.drawable.conservative_banner)
                                tv_alignment_view_category.text = "Conservative"
                            }
                            "Liberal" -> {
                                iv_alignment_view_party_icon.setImageResource(R.drawable.ic_liberal_banner)
                                tv_alignment_view_category.text = "Liberal"
                            }
                            "Libertarian" -> {
                                iv_alignment_view_party_icon.setImageResource(R.drawable.libertarian_banner)
                                tv_alignment_view_category.text = "Libertarian"
                            }
                            "Authoritarian" -> {
                                iv_alignment_view_party_icon.setImageResource(R.drawable.authoritarian_banner)
                                tv_alignment_view_category.text = "Populist"
                            }
                        }
                    }
                }
            }
        })

        b_alignment_view_continue.setOnClickListener {
            findNavController().popBackStack()
            }

        }
        private fun plotPoint(economicFreedom: Double, personalFreedom: Double) {
            xySeries.shape = PointsGraphSeries.Shape.POINT
            xySeries.size = 25f
            xySeries.color = Color.parseColor("#bf55ec")
            xySeries.appendData(DataPoint(economicFreedom, personalFreedom), false, 1)
            graph.addSeries(xySeries)
        }
    }
