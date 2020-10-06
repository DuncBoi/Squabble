package com.duncboi.realsquabble.messenger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.fragment.findNavController
import com.duncboi.realsquabble.Group
import com.duncboi.realsquabble.PartyGroup
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.Trending
import kotlinx.android.synthetic.main.fragment_default_messenger.*

class DefaultMessenger : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_default_messenger, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iv_default_messanger_dm.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("type", "dm")
            findNavController().navigate(R.id.action_defaultMessenger_to_directMessanger, bundle)
        }

        iv_default_messanger_make_group.setOnClickListener {
            findNavController().navigate(R.id.action_defaultMessenger_to_createGroup)
        }
    }


}