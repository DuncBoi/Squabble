package com.duncboi.realsquabble


import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.duncboi.realsquabble.messenger.Users
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_topic_description.*


class TopicDescription : Fragment() {

    private val args: TopicDescriptionArgs by navArgs()

    private val topicsRef = FirebaseDatabase.getInstance().reference.child("Topics").orderByChild("headline")
    private var topicsListener: ValueEventListener? = null
    private val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
    private var usersListener: ValueEventListener? = null

    override fun onPause() {
        super.onPause()
        if (topicsListener != null) topicsRef.equalTo(args.topic).removeEventListener(topicsListener!!)
        if (usersListener != null) usersRef.removeEventListener(usersListener!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_topic_description, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Moosepoo", "${args.topic}")
        if (args.type == "owned"){
            b_topic_description_edit.visibility = View.VISIBLE
            b_topic_description_edit.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("topic", args.topic)
                bundle.putString("type", "edit")
                findNavController().navigate(R.id.action_topicDescription_to_createTopic, bundle)
            }
        }

        iv_topic_description_back.setOnClickListener {
            findNavController().navigate(R.id.action_topicDescription_to_videoChat)
        }

        topicsListener = topicsRef.equalTo(args.topic).addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){
                    val topic = i.getValue(Topics::class.java)
                    if (topic != null) {
                        doStuff(topic)
                    }
                }
            }


        })
    }

    private fun doStuff(topic: Topics){
        rg_topic_description_answers.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: RadioGroup?, p1: Int) {
                b_topic_description_debate.isClickable = true
                b_topic_description_debate.setBackgroundResource(R.drawable.rounded_button)
            }
        })
        b_topic_description_debate.setOnClickListener {
            var answer: String = ""
            when (rg_topic_description_answers.checkedRadioButtonId){
                rb_topic_description_answer1.id -> answer = rb_topic_description_answer1.text.toString()
                rb_topic_description_answer2.id -> answer = rb_topic_description_answer2.text.toString()
            }
            if (answer != "") {
                val bundle = Bundle()
                bundle.putString("answer", "$answer")
                bundle.putString("topic", "${topic.getHeadline()}")
                findNavController().navigate(R.id.action_topicDescription_to_queue, bundle)
            }

        }

        tv_topic_description_headline.text = "${topic.getHeadline()}"
        tv_topic_description_description.text = "${topic.getDescription()}"
        if (topic.getUri() != "") {
            Picasso.get().load(topic.getUri()).into(iv_topic_description_image)
            //iv_topic_description_image.loadSvg(topic.getUri())
        }
        tv_topic_description_question.text = "${topic.getQuestion()}"
        rb_topic_description_answer1.text = "${topic.getAnswer1()}"
        rb_topic_description_answer2.text = "${topic.getAnswer2()}"
        if (topic.getIsTrending() == true){
            iv_topic_description_istrending.visibility = View.VISIBLE
        }
        if (topic.getCreator() == "Squabble"){
            tv_topic_description_username.text = "Squabble"
            tv_topic_description_username.setTextColor(Color.parseColor("#e6be8a"))
        }
        else {
            usersListener = usersRef.child("${topic.getCreator()}").addValueEventListener(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {

                    val user = snapshot.getValue(Users::class.java)
                    usersRef.child("${topic.getCreator()}").removeEventListener(usersListener!!)

                    if (topic.getCreator() == FirebaseAuth.getInstance().currentUser!!.uid){
                        tv_topic_description_username.setTextColor(Color.parseColor("#4885ed"))
                        if (user!!.getUri() != "") Picasso.get().load(user.getUri()).into(civ_topic_description_profile)
                        if (topic.getIncludeUserInfo() == false) tv_topic_description_username.text = "You (Anonymous)"
                        else tv_topic_description_username.text = "You"

                    }
                    else {
                        if (topic.getIncludeUserInfo() == false){
                            tv_topic_description_username.text = "Anonymous"
                        }
                        else {
                            if (user!!.getUri() != "") Picasso.get().load(user.getUri()).into(civ_topic_description_profile)

                            if (user.getName() != "") tv_topic_description_username.text = "${user.getName()}"
                            else tv_topic_description_username.text = "${user.getUsername()}"

                            iv_topic_description_profile_holder.setOnClickListener {
                                val bundle = Bundle()
                                if (topic.getCreator()
                                        .equals(FirebaseAuth.getInstance().currentUser!!.uid)
                                ) {
                                } else {
                                    bundle.putString("uid", "${user.getUid()}")
                                    findNavController().navigate(
                                        R.id.action_topicDescription_to_otherProfile,
                                        bundle
                                    )
                                }
                            }
                        }
                    }

                }

            })
        }
    }

    fun ImageView.loadSvg(url: String?) {
        GlideToVectorYou
            .init()
            .with(this.context)
            .setPlaceHolder(R.drawable.long_arrow, R.drawable.ic_baseline_error_24)
            .load(Uri.parse(url), this)
    }

}