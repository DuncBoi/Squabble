package com.duncboi.realsquabble.ViewPagerFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.duncboi.realsquabble.HolderClass.DirectMessages
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.HolderClass.Inbox
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_default_messenger.*

class DefaultMessenger : Fragment() {

    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_default_messenger, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val numberUnreadRef = FirebaseDatabase.getInstance().reference.child("Unread Notifications").child(
            currentUser
        ).child("numberUnread")

        numberUnreadRef.onDisconnect().cancel()
        numberUnreadRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val number = snapshot.getValue(Long::class.java)
                if (tv_default_messenger_number_of_notifications_not_seen != null && number != null && number != 0.toLong()) {
                    tv_default_messenger_number_of_notifications_not_seen.visibility = View.VISIBLE
                    tv_default_messenger_number_of_notifications_not_seen.text = "$number"
                }
                else{
                    if (tv_default_messenger_number_of_notifications_not_seen != null){
                    tv_default_messenger_number_of_notifications_not_seen.visibility = View.INVISIBLE
                        }
                }
            }
        })

        val adapter = MyViewPagerAdapter(childFragmentManager)
        adapter.addFragment(DirectMessages(), "Direct Messages")
        adapter.addFragment(Inbox(), "Inbox")
        val mViewPager = (messagingViewPager) as ViewPager
        mViewPager.adapter = adapter
        messagingTabLayout.setupWithViewPager(mViewPager)

        mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageSelected(position: Int) {
                if (position == 1){
                    numberUnreadRef.removeValue()
                }
            }
        })

        iv_messaging_new_message.setOnClickListener {
            findNavController().navigate(R.id.action_defaultMessenger_to_directMessanger)
        }
    }


    class MyViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(
        manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
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