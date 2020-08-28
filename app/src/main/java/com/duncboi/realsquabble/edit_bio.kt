package com.duncboi.realsquabble

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_edit_bio.*
import kotlinx.android.synthetic.main.fragment_edit_bio.view.*
import kotlinx.android.synthetic.main.fragment_edit_username.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [edit_bio.newInstance] factory method to
 * create an instance of this fragment.
 */
class edit_bio : Fragment() {
    val args: edit_bioArgs by navArgs()

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_bio, container, false)

        val bio = args.bio
        view.et_edit_bio_bio.setText("$bio")

        val username = args.username
        val name = args.name

        runBioChecker()
        view.tv_edit_bio_done.setOnClickListener {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0)
            stopBioRunner = true
            val bio = et_edit_bio_bio.text.toString().trim()
            val bundle = Bundle()
            bundle.putString("bio", bio)
            bundle.putString("username", username)
            bundle.putString("name", name)
            findNavController().navigate(R.id.edit_bio_to_editProfile, bundle)
        }
        view.iv_edit_bio_back_button.setOnClickListener {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0)
            stopBioRunner = true
            val bundle = Bundle()
            bundle.putString("bio", bio)
            bundle.putString("username", username)
            bundle.putString("name", name)
            findNavController().navigate(R.id.edit_bio_to_editProfile, bundle)
        }
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment edit_bio.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            edit_bio().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private var stopBioRunner = false

    private fun runBioChecker(){
        CoroutineScope(Main).launch {
            bioRunnerLogic()
        }
    }

    private suspend fun bioRunnerLogic(){
        var argsBio = args.bio
        while (!stopBioRunner){
            delay(200)
            val length = et_edit_bio_bio.text.toString().trim().length
            val bio = et_edit_bio_bio.text.toString()
            val bioNumber = 150 - length
            tv_edit_bio_letter_counter.setText("$bioNumber")
            if(bio == argsBio){
                tv_edit_bio_done.isClickable = true
                tv_edit_bio_done.setText("Done")
                et_edit_bio_bio.setPadding(24,24,24,24)
                et_edit_bio_bio.bringToFront()
                et_edit_bio_bio.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.pen, 0)
            }
            else{
                argsBio += "||||||||||||"
                if (bioNumber < 150){
                tv_edit_bio_letter_counter.bringToFront()
                et_edit_bio_bio.setPadding(24,31,100,31)
                et_edit_bio_bio.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                if(bioNumber >= 0){
                    tv_edit_bio_done.isClickable = true
                    tv_edit_bio_done.setText("Done")
                    tv_edit_bio_letter_counter.setTextColor(ResourcesCompat.getColor(getResources(), R.color.green, null))
                    }
                else{
                    tv_edit_bio_done.isClickable = false
                    tv_edit_bio_done.setText("")
                    tv_edit_bio_letter_counter.setTextColor(ResourcesCompat.getColor(getResources(), R.color.red, null))
                }
            }
            else{
                tv_edit_bio_done.isClickable = true
                tv_edit_bio_done.setText("Done")
                et_edit_bio_bio.setPadding(24,24,24,24)
                et_edit_bio_bio.bringToFront()
                et_edit_bio_bio.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.pen, 0)
            }
        }}
    }
}
