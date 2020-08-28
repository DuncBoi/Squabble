package com.duncboi.realsquabble

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
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
 * Use the [default_profile.newInstance] factory method to
 * create an instance of this fragment.
 */
class default_profile : Fragment() {
    private val args: default_profileArgs by navArgs()
    lateinit var usernamePassed:String
    lateinit var namePassed:String
    lateinit var bioPassed:String
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
        val view = inflater.inflate(R.layout.fragment_default_profile, container, false)


        view.b_edit_profile.setOnClickListener{
                val bio = tv_default_profile_bio.text.toString().trim()
                val bundle = Bundle()
                bundle.putString("bio", bioPassed)
                bundle.putString("username", usernamePassed)
                bundle.putString("name", namePassed)
                findNavController().navigate(R.id.default_to_edit_profile, bundle)
        }

        val currentUserUid = FirebaseAuth.getInstance().currentUser!!.uid
        if(currentUserUid != null){
        val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("uid").equalTo(currentUserUid)
        emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val username = childSnapshot.child("username").getValue<String>().toString()
                    val name = childSnapshot.child("name").getValue<String>().toString()
                    val bio = childSnapshot.child("bio").getValue<String>().toString()
                    usernamePassed = username
                    namePassed = name
                    bioPassed = bio
                    val firstLetter = username[0]
                    tv_profile_picture_letter.text = "$firstLetter".split(' ').joinToString(" ") { it.capitalize() }
                    tv_default_profile_username.text = "@$username"
                    if (bio != "null") tv_default_profile_bio.text = "$bio"
                    else tv_default_profile_bio.text = ""
                    if (name != ""){
                        val firstLetter = name[0]
                        view.tv_profile_picture_letter.text = "$firstLetter".split(' ').joinToString(" ") { it.capitalize() }
                        view.tv_default_profile_name.text = "$name"}
                    else tv_default_profile_bio.text = ""
                }
            }
            override fun onCancelled(error: DatabaseError) {} })}

        //action bar


        view.b_profile_logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            activity?.let { it1 ->
                GoogleSignIn.getClient(
                    it1,
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                ).signOut()
            }
            val intent2 = Intent(activity, FirstActivity::class.java)
            intent2.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent2)

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
         * @return A new instance of fragment default_profile.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            default_profile().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}