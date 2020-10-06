package com.duncboi.realsquabble

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_default_messenger.*
import kotlinx.android.synthetic.main.fragment_group_holder.*

class GroupHolder : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_holder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MyViewPagerAdapter(requireActivity().supportFragmentManager)
        adapter.addFragment(PartyGroup(), "Party")
        adapter.addFragment(Group(), "Group")
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)

    }

    class MyViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ){

        private val fragmentList: MutableList<Fragment> = ArrayList()
        private val titleList: MutableList<String> = ArrayList()

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }
        override fun getCount(): Int {
            return  fragmentList.size
        }
        fun addFragment(fragment: Fragment, title: String){
            fragmentList.add(fragment)
            titleList.add(title)
        }
        override fun getPageTitle(position: Int): CharSequence? {
            return titleList[position]
        }
    }

}