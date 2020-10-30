package com.duncboi.realsquabble.political

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_manually_choose.*

class ManuallyChoose : Fragment() {

    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manually_choose, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var alignment = ""

        val categoryRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser).child("category")
        categoryRef.onDisconnect().cancel()
            categoryRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentCategory = snapshot.getValue(String::class.java)
                if (currentCategory != null && rb_manually_choose_libertarian != null){
                    when (currentCategory){
                        "Authoritarian" -> rb_manually_choose_authoritarian.isChecked = true
                        "Conservative" -> rb_manually_choose_conservative.isChecked = true
                        "Liberal" -> rb_manually_choose_liberal.isChecked = true
                        "Moderate" -> rb_manually_choose_moderate.isChecked = true
                        "Libertarian" -> rb_manually_choose_libertarian.isChecked = true
                    }
                }
            }
        })

        rg_manually_choose.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: RadioGroup?, p1: Int) {
                b_manually_choose_next.setBackgroundResource(R.drawable.rounded_button)
                b_manually_choose_next.isClickable = true
            }
        })

        iv_manually_choose_back.setOnClickListener {
            activity?.supportFragmentManager!!.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.root_container, PoliticalIntro())
                .commitAllowingStateLoss()
        }

        b_manually_choose_next.setOnClickListener {
            if (rb_manually_choose_authoritarian.isChecked == true || rb_manually_choose_conservative.isChecked == true || rb_manually_choose_liberal.isChecked == true || rb_manually_choose_moderate.isChecked == true || rb_manually_choose_libertarian.isChecked == true){
                when {
                    rb_manually_choose_authoritarian.isChecked == true -> alignment = "Authoritarian"
                    rb_manually_choose_conservative.isChecked == true-> alignment = "Conservative"
                    rb_manually_choose_liberal.isChecked == true-> alignment = "Liberal"
                    rb_manually_choose_moderate.isChecked == true-> alignment = "Moderate"
                    rb_manually_choose_libertarian.isChecked == true-> alignment = "Libertarian"
                }

                val builder = AlertDialog.Builder(activity)
                builder.setTitle("Continue?")
                builder.setMessage("Are you sure uyou wish to continue? You will not be able to change your alignment for 14 days")
                builder.setCancelable(false)
                builder.setPositiveButton("Yes", DialogInterface.OnClickListener { dialogInterface, i ->
                    FirebaseDatabase.getInstance().reference.child("Users").child(currentUser).child("socialScore").setValue("")
                    FirebaseDatabase.getInstance().reference.child("Users").child(currentUser).child("economicScore").setValue("")
                    FirebaseDatabase.getInstance().reference.child("Users").child(currentUser).child("category").setValue(alignment).addOnCompleteListener {
                        FirebaseDatabase.getInstance().reference.child("Users").child(currentUser).child("alignmentTime").setValue("${System.currentTimeMillis()}")
                        val intent = Intent(activity, ProfileActivity::class.java)
                        startActivity(intent)
                        activity?.finish()
                    }
                })
                builder.setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.cancel()
                })
                builder.create().show()
            }
        }



    }


}