package com.duncboi.realsquabble.ViewPagerFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager.widget.ViewPager
import com.duncboi.realsquabble.ModelClasses.Users
import com.duncboi.realsquabble.PagerDescriptionClasses.Profile
import com.duncboi.realsquabble.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_other_profile.*

class OtherProfile : Fragment() {

    val args: OtherProfileArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_other_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iv_other_profile_back.setOnClickListener {
            findNavController().popBackStack()
        }

        val uid = args.uid
        val reference = FirebaseDatabase.getInstance().reference.child("Users").child(uid)
        reference.onDisconnect().cancel()
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: Users? = snapshot.getValue(Users::class.java)
                if (user != null && tv_default_other_profile_name != null) {

                    val adapter = MyViewPagerAdapter(childFragmentManager)
                    adapter.addFragment(Profile(user.getUid()!!, "other"), "Description")
                    val mViewPager = (other_profileViewPager) as ViewPager
                    mViewPager.adapter = adapter
                    other_profileTabLayout.setupWithViewPager(mViewPager)

                    b_other_profile_send_message.setOnClickListener {
                        val bundle = Bundle()
                        bundle.putString("uid", uid)
                        findNavController().navigate(R.id.action_otherProfile_to_DM, bundle)
                    }

                    if (user.getName() != "") {
                        tv_default_other_profile_name.text = user.getName()
                    }

                    tv_default_other_profile_username.text = "@${user.getUsername()}"

                    if (user.getUri() != "") {
                        Picasso.get().load(user.getUri()).placeholder(R.drawable.profile_icon)
                            .into(civ_default_other_profile_picture)
                    }
                }
            }
        })
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