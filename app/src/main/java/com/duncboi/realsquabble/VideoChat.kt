package com.duncboi.realsquabble

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duncboi.realsquabble.messenger.Chat
import com.duncboi.realsquabble.messenger.UserAdapter
import com.duncboi.realsquabble.messenger.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_video_chat.*


class VideoChat : Fragment() {

    private var topics: List<Topics>? = null
    lateinit var recyclerView: RecyclerView
    private var topicsAdapter: TopicsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_chat, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rv_video_topics_recycler_view)
        recyclerView!!.hasFixedSize()
        recyclerView!!.layoutManager = LinearLayoutManager(activity)

        topics = ArrayList()

        retrieveAllTopics()

        iv_video_chat_new_topic.setOnClickListener {
            findNavController().navigate(R.id.action_videoChat_to_createTopic)
        }

        et_video_chat_search!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(cs: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchForUsers(cs.toString().toLowerCase())
            }

        })


    }

    private fun searchForUsers (str: String){
        FirebaseDatabase.getInstance().reference.child("Topics").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                (topics as ArrayList<Topics>).clear()
                for (snap in snapshot.children){
                    val topic = snap.getValue(Topics::class.java)
                    if (topic != null) {
                        if (topic.getHeadline().toString().toLowerCase().contains(str) || topic.getDescription().toString().toLowerCase().contains(str)) {
                            if (topic.getIsTrending() == true) {
                                (topics as ArrayList<Topics>).add(0, topic)
                            }
                            else{
                                (topics as ArrayList<Topics>).add(topic)
                            }
                        }
                    }
                }
                topicsAdapter = activity?.let { TopicsAdapter(it, topics!!) }
                recyclerView?.adapter = topicsAdapter
            }
        })
    }

    private fun retrieveAllTopics(){
        FirebaseDatabase.getInstance().reference.child("Topics").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                (topics as ArrayList<Topics>).clear()
                for (snap in snapshot.children){
                    val topic = snap.getValue(Topics::class.java)
                    if (topic != null) {
                        if (topic.getIsTrending() == true) {
                            (topics as ArrayList<Topics>).add(0, topic)
                        }
                        else{
                            (topics as ArrayList<Topics>).add(topic)
                        }
                    }
                }
                topicsAdapter = activity?.let { TopicsAdapter(it, topics!!) }
                recyclerView?.adapter = topicsAdapter
            }


        })
    }

}