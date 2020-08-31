package com.duncboi.realsquabble.registration

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.duncboi.realsquabble.R
import kotlinx.android.synthetic.main.fragment_first.*


class First : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //login button
        b_first_login.setOnClickListener {
            findNavController().navigate(R.id.action_first_to_login)
        }

        //register button
        b_first_register.setOnClickListener {
            findNavController().navigate(R.id.action_first_to_usernameRegistration)

        }
    }
}