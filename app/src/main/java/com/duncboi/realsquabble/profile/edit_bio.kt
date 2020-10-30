package com.duncboi.realsquabble.profile

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.duncboi.realsquabble.R
import kotlinx.android.synthetic.main.fragment_edit_bio.*
import kotlinx.android.synthetic.main.fragment_edit_bio.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class edit_bio : Fragment() {
    val args: edit_bioArgs by navArgs()

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

        //runBioChecker()

        view.et_edit_bio_bio.addTextChangedListener(object : TextWatcher{
            var argsBio = args.bio
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val text = p0.toString().trim()
                val length = text.length
                val bioNumber = 150 - length
                tv_edit_bio_letter_counter.text = "$bioNumber"
                if(text == argsBio){
                    tv_edit_bio_done.isClickable = true
                    tv_edit_bio_done.text = "Done"
                    et_edit_bio_bio.setPadding(24,24,24,24)
                    et_edit_bio_bio.bringToFront()
                    et_edit_bio_bio.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                        R.drawable.pen, 0)
                }
                else{
                    argsBio += "||||||||||||"
                    if (bioNumber < 150){
                        tv_edit_bio_letter_counter.bringToFront()
                        et_edit_bio_bio.setPadding(24,31,100,31)
                        et_edit_bio_bio.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                        if(bioNumber >= 0){
                            tv_edit_bio_done.isClickable = true
                            tv_edit_bio_done.text = "Done"
                            tv_edit_bio_letter_counter.setTextColor(ResourcesCompat.getColor(
                                resources,
                                R.color.green, null))
                        }
                        else{
                            tv_edit_bio_done.isClickable = false
                            tv_edit_bio_done.text = ""
                            tv_edit_bio_letter_counter.setTextColor(ResourcesCompat.getColor(
                                resources,
                                R.color.red, null))
                        }
                    }
                    else{
                        tv_edit_bio_done.isClickable = true
                        tv_edit_bio_done.text = "Done"
                        et_edit_bio_bio.setPadding(24,24,24,24)
                        et_edit_bio_bio.bringToFront()
                        et_edit_bio_bio.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                            R.drawable.pen, 0)
                    }
                }
            }

        })
        view.tv_edit_bio_done.setOnClickListener {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
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
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
            stopBioRunner = true
            val bundle = Bundle()
            bundle.putString("bio", bio)
            bundle.putString("username", username)
            bundle.putString("name", name)
            findNavController().navigate(R.id.edit_bio_to_editProfile, bundle)
        }
        return view
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
            tv_edit_bio_letter_counter.text = "$bioNumber"
            if(bio == argsBio){
                tv_edit_bio_done.isClickable = true
                tv_edit_bio_done.text = "Done"
                et_edit_bio_bio.setPadding(24,24,24,24)
                et_edit_bio_bio.bringToFront()
                et_edit_bio_bio.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    R.drawable.pen, 0)
            }
            else{
                argsBio += "||||||||||||"
                if (bioNumber < 150){
                tv_edit_bio_letter_counter.bringToFront()
                et_edit_bio_bio.setPadding(24,31,100,31)
                et_edit_bio_bio.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                if(bioNumber >= 0){
                    tv_edit_bio_done.isClickable = true
                    tv_edit_bio_done.text = "Done"
                    tv_edit_bio_letter_counter.setTextColor(ResourcesCompat.getColor(
                        resources,
                        R.color.green, null))
                    }
                else{
                    tv_edit_bio_done.isClickable = false
                    tv_edit_bio_done.text = ""
                    tv_edit_bio_letter_counter.setTextColor(ResourcesCompat.getColor(
                        resources,
                        R.color.red, null))
                }
            }
            else{
                tv_edit_bio_done.isClickable = true
                    tv_edit_bio_done.text = "Done"
                et_edit_bio_bio.setPadding(24,24,24,24)
                et_edit_bio_bio.bringToFront()
                et_edit_bio_bio.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    R.drawable.pen, 0)
            }
        }}
    }
}
