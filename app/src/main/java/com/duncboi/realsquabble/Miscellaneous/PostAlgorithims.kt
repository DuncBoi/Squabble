package com.duncboi.realsquabble.Miscellaneous

import android.util.Log
import kotlin.math.abs
import kotlin.math.log10

object PostAlgorithims {

    fun groupLevelAlgorithim(voteCount: Double, timeOfPost: Long): Double{

        val seconds = timeOfPost/1000
        val seconds45: Double = seconds.toDouble()/45000
        var yOffset: Double = 0.0
        var maximalVal: Double = 0.0

        if (voteCount > 0) yOffset = 1.0
        else if (voteCount == 0.0) yOffset = 0.0
        else if (voteCount < 0) yOffset = -1.0

        if (abs(voteCount) >= 1) maximalVal = abs(voteCount)
        else maximalVal = 1.0

        Log.d("zscore", "yOffset $yOffset")
        Log.d("zscore", "maximalVal $maximalVal")
        Log.d("zscore", "voteCount $voteCount")
        Log.d("zscore", "seconds $seconds")

        return yOffset * (log10(maximalVal)) + seconds45

    }

}