package com.duncboi.realsquabble.profile

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.navigation.fragment.findNavController
import com.duncboi.realsquabble.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_link.*

class link : Fragment() {

    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_link, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iv_link_back.setOnClickListener {
            findNavController().popBackStack()
        }

        b_link_send.setOnClickListener {
            val message = et_link_feedback.text.toString().trim()

            val feedbackRef = FirebaseDatabase.getInstance().reference.child("Feedback").child(currentUser)
            val feedbackKey = feedbackRef.push().key

                feedbackRef.child(feedbackKey!!).setValue(message).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val builder = AlertDialog.Builder(activity)
                        builder.setCancelable(false)
                        builder.setTitle("Message Sent")
                        builder.setMessage("Thank you for helping us improve Squabble")
                        builder.setCancelable(false)
                        builder.setPositiveButton("Ok") { dialogInterface, i ->
                            dialogInterface.cancel()
                            findNavController().popBackStack()
                            closeKeyboard()
                        }
                        builder.create().show()
                    }
                }
        }
    }

    private fun closeKeyboard(){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

}