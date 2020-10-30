package com.duncboi.realsquabble.PagerDescriptionClasses


import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.duncboi.realsquabble.ModelClasses.Topics
import com.duncboi.realsquabble.ModelClasses.Users
import com.duncboi.realsquabble.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_topic_description.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class TopicDescription(topic: String) : Fragment() {

    private val topic: String

    init {
        this.topic = topic
    }

    private val topicsRef = FirebaseDatabase.getInstance().reference.child("Topics")
    private val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_topic_description, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iv_topic_description_back.setOnClickListener {
            findNavController().navigate(R.id.action_topicHolder_to_videoChat)
        }

        CoroutineScope(Main).launch {
            delay(400)
            topicsRef.child(topic).onDisconnect().cancel()
            topicsRef.child(topic).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    val topic = snapshot.getValue(Topics::class.java)
                    if (topic != null && b_topic_description_edit != null) {
                        doStuff(topic)
                        if (topic.getCreator() == currentUser) {
                            b_topic_description_edit.visibility = View.VISIBLE
                            b_topic_description_edit.setOnClickListener {
                                val bundle = Bundle()
                                bundle.putString("topic", topic.getId())
                                findNavController().navigate(
                                    R.id.action_topicDescription_to_createTopic,
                                    bundle
                                )
                            }
                        }
                    }
                }
            })
        }
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
            var host: Boolean = false
            when (rg_topic_description_answers.checkedRadioButtonId){
                rb_topic_description_answer1.id -> {
                    answer = rb_topic_description_answer1.text.toString()
                    host = true
                }
                rb_topic_description_answer2.id -> {
                    answer = rb_topic_description_answer2.text.toString()
                    host = false
                }
            }
            if (answer != "") {
                val bundle = Bundle()
                bundle.putBoolean("host", host)
                bundle.putString("topicId", "${topic.getId()}")
                findNavController().navigate(R.id.action_topicHolder_to_actualVideo, bundle)
            }

        }

        pb_topic_description_progress.visibility = View.GONE
        tv_topic_description_headline.text = "${topic.getHeadline()}"
        tv_topic_description_description.text = "${topic.getDescription()}"
        if (topic.getUri() != "") {
            Picasso.get().load(topic.getUri()).into(iv_topic_description_image)
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
            usersRef.child("${topic.getCreator()}").onDisconnect().cancel()
            usersRef.child("${topic.getCreator()}").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {

                    val user = snapshot.getValue(Users::class.java)
                    if (user != null && tv_topic_description_username != null) {
                        if (topic.getCreator() == FirebaseAuth.getInstance().currentUser!!.uid) {
                            tv_topic_description_username.setTextColor(Color.parseColor("#4885ed"))
                            if (user.getUri() != "") Picasso.get().load(user.getUri())
                                .into(civ_topic_description_profile)
                            if (topic.getIncludeUserInfo() == false) tv_topic_description_username.text =
                                "You (Anonymous)"
                            else tv_topic_description_username.text = "You"

                        } else {
                            if (topic.getIncludeUserInfo() == false || user.getAnonymous() == "ON") {
                                tv_topic_description_username.text = "Anonymous"
                            } else {
                                if (user.getUri() != "") Picasso.get().load(user.getUri())
                                    .into(civ_topic_description_profile)

                                if (user.getName() != "") tv_topic_description_username.text =
                                    "${user.getName()}"
                                else tv_topic_description_username.text = "${user.getUsername()}"

                                iv_topic_description_profile_holder.setOnClickListener {
                                    val bundle = Bundle()
                                    if (topic.getCreator()
                                            .equals(FirebaseAuth.getInstance().currentUser!!.uid)
                                    ) {
                                    } else {
                                        bundle.putString("uid", "${user.getUid()}")
                                        findNavController().navigate(
                                            R.id.action_topicHolder_to_otherProfile,
                                            bundle
                                        )
                                    }
                                }
                            }
                        }

                    }
                }
            })
        }
    }
}