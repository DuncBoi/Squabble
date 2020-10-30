package com.duncboi.realsquabble.HolderClass

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.duncboi.realsquabble.Adapters.TopicsAdapter
import com.duncboi.realsquabble.ModelClasses.Topics
import com.duncboi.realsquabble.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_video_chat.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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
        recyclerView.hasFixedSize()
        recyclerView.layoutManager = LinearLayoutManager(activity)

        topic_swype_refresh.setOnRefreshListener{
            CoroutineScope(Main).launch {
                delay(200)
                retrieveAllTopics()
            }
        }
        topic_swype_refresh.setColorSchemeColors(Color.parseColor("#bf55ec"))
        topic_swype_refresh.setDistanceToTriggerSync(20)
        topic_swype_refresh.setSize(SwipeRefreshLayout.DEFAULT)

        topics = ArrayList()

        CoroutineScope(Main).launch {
            delay(300)
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
    }

    private fun searchForUsers (str: String){
        val topicsRef = FirebaseDatabase.getInstance().reference.child("Topics")
        topicsRef.onDisconnect().cancel()
            topicsRef.addValueEventListener(object : ValueEventListener{
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
                recyclerView.adapter = topicsAdapter
            }
        })
    }

    private fun retrieveAllTopics(){
        val topicsRef = FirebaseDatabase.getInstance().reference.child("Topics")
        topicsRef.onDisconnect().cancel()
            topicsRef.addListenerForSingleValueEvent(object : ValueEventListener{
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
                if (pb_video_chat_progress != null) {
                    pb_video_chat_progress.visibility = View.GONE
                    topic_swype_refresh.isRefreshing = false
                    val sorted = orderTopics((topics as ArrayList<Topics>))
                    topicsAdapter = activity?.let { TopicsAdapter(it, sorted) }
                    recyclerView.adapter = topicsAdapter
                }
            }


        })
    }

    private fun orderTopics(topicsList: List<Topics>): List<Topics>{
        val sorted = topicsList.sortedWith(compareByDescending<Topics> {it.getIsTrending()?.equals(true)})
        return sorted
    }

}