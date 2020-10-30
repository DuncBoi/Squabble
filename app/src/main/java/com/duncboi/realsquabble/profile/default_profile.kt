package com.duncboi.realsquabble.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.duncboi.realsquabble.*
import com.duncboi.realsquabble.HolderClass.likedPosts
import com.duncboi.realsquabble.ModelClasses.Users
import com.duncboi.realsquabble.PagerDescriptionClasses.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_default_profile.*
import kotlinx.android.synthetic.main.fragment_default_profile.view.*
import java.io.File


class default_profile : Fragment() {

    private var usernamePassed: String? = null
    private var namePassed: String? = null
    private var bioPassed: String? = null

    private val currentUserUid = FirebaseAuth.getInstance().currentUser!!.uid

    private val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").child(currentUserUid)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_default_profile, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MyViewPagerAdapter(childFragmentManager)
        adapter.addFragment(Profile(currentUserUid, "profile"), "Description")
        adapter.addFragment(likedPosts(), "Upvoted Posts")
        val mViewPager = (profileViewPager) as ViewPager
        mViewPager.adapter = adapter
        profileTabLayout.setupWithViewPager(mViewPager)

        b_default_profile_edit_profile.setOnClickListener{
            val bundle = Bundle()
            bundle.putString("bio", bioPassed)
            bundle.putString("username", usernamePassed)
            bundle.putString("name", namePassed)
            findNavController().navigate(R.id.default_to_edit_profile, bundle)
        }

        view.iv_default_profile_settings.setOnClickListener {
            findNavController().navigate(R.id.action_default_profile_to_profile_settings)
        }

        emailQuery.onDisconnect().cancel()
        emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)

                if (user != null && civ_default_profile_picture != null) {

                    if (user.getUri() != "")
                        Glide.with(context!!)
                        .load(user.getUri())
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(civ_default_profile_picture)

                    usernamePassed = user.getUsername()
                    namePassed = user.getName()
                    bioPassed = user.getBio()
                    tv_default_profile_username.text = "@${user.getUsername()}"
                    tv_default_profile_name.text = "${user.getName()}"
                }

            }
            override fun onCancelled(error: DatabaseError) {} })

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