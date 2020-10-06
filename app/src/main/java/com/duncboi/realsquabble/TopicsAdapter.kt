package com.duncboi.realsquabble

import android.content.Context
import android.icu.number.NumberFormatter.with
import android.icu.number.NumberRangeFormatter.with
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.duncboi.realsquabble.messenger.UserAdapter
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_topic_description.*

class TopicsAdapter(context: Context, topics: List<Topics>): RecyclerView.Adapter<TopicsAdapter.ViewHolder?>() {

    private val context: Context
    private val topics: List<Topics>
    init {
        this.context = context
        this.topics = topics
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.recycler_topics, parent, false)
        return TopicsAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return topics.size
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val topic: Topics = topics[i]
        Log.d("Moose", "poo: ${topic.getUri()}")

        holder.header.text = topic.getHeadline()
        holder.description.text = topic.getDescription()
        if (topic.getUri() != "") {
           // holder.image.loadSvg(topic.getUri())
            Log.d("Moosepoo", "${topic.getUri()}")
            Picasso.get().load(topic.getUri()).into(holder.image)
        }
        if (topic.getIsTrending() == true){
            holder.trending.visibility = View.VISIBLE
        }
        holder.itemView.setOnClickListener {
            var type = "normal"
            if (topic.getCreator() == FirebaseAuth.getInstance().currentUser!!.uid) type = "owned"
            val bundle = Bundle()
            bundle.putString("type", "$type")
            bundle.putString("topic", "${topic.getHeadline()}")
            findNavController(holder.itemView).navigate(R.id.action_videoChat_to_topicDescription, bundle
            )
        }
        var numberOnline = 0
        FirebaseDatabase.getInstance().reference.child("Queue").child(topic.getHeadline()!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    numberOnline = 0
                    for (i in snapshot.children){
                        numberOnline ++
                        holder.online.text = "$numberOnline users online"
                    }
                }
                })
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var header: TextView
        var description: TextView
        var image: ImageView
        var trending: ImageView
        var online: TextView

        init {
            online = itemView.findViewById(R.id.tv_recycler_topics_online)
            header = itemView.findViewById(R.id.tv_recycler_topics_header)
            description = itemView.findViewById(R.id.tv_recycler_topics_description)
            image = itemView.findViewById(R.id.iv_recycler_topics_image)
            trending = itemView.findViewById(R.id.iv_recycler_topics_trending)

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