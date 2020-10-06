package com.duncboi.realsquabble

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_web_view.*

class web_view : Fragment() {

    private val args: web_viewArgs by navArgs()

    override fun onPause() {
        super.onPause()
        findNavController().navigate(R.id.action_web_view_to_defaultMessenger)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_web_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        wv_web_view.loadUrl(args.url)
    }


}