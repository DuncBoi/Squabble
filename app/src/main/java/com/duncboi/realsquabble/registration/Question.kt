package com.duncboi.realsquabble.registration

data class Question(val qid: Int,
                    val type: String,
                    val header: String,
                    val image: Int,
                    val answer0: String,
                    val answer5: String,
                    val answer10: String)