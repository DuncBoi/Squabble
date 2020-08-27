package com.duncboi.realsquabble

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_default_profile.*
import kotlinx.android.synthetic.main.fragment_default_profile.view.*
import kotlinx.android.synthetic.main.fragment_edit_bio.*
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditProfile.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditProfile : Fragment() {

    private val args: EditProfileArgs by navArgs()

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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        val username = args.username
        view.b_edit_profile_username.text = username

        val bio = args.bio
        view.b_edit_profile_bio.text = "$bio"

        view.tv_edit_profile_done.setOnClickListener {
            val bio = b_edit_profile_bio.text.toString().trim()
            val bundle = Bundle()
            bundle.putString("bio", bio)
            findNavController().navigate(R.id.edit_profile_to_default, bundle)
        }
        
        view.b_edit_profile_username.setOnClickListener {
            val action = EditProfileDirections.editProfileToEditUsername(username!!)
            Navigation.findNavController(view).navigate(action)}

        view.b_edit_profile_bio.setOnClickListener {
            val bio = b_edit_profile_bio.text.toString().trim()
            val bundle = Bundle()
            bundle.putString("bio", bio)
            findNavController().navigate(R.id.editProfile_to_edit_bio, bundle)
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
         * @return A new instance of fragment EditProfile.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditProfile().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}