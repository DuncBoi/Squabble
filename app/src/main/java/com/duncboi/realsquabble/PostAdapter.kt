package com.duncboi.realsquabble

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.raqun.beaverlib.Beaver
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_create_post.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostAdapter (context: Context, postList: List<Posts>, type: String): RecyclerView.Adapter<PostAdapter.ViewHolder?>() {

    private val context: Context?
    private val postList: List<Posts>
    private val type: String?
    private var likeChecker = false

    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    init {
        this.postList = postList
        this.context = context
        this.type = type
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
            val view: View = LayoutInflater.from(context).inflate(R.layout.post, viewGroup, false)
            return PostAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: PostAdapter.ViewHolder, position: Int) {

//        MobileAds.initialize(context) {}
//
//        val adLoader = AdLoader.Builder(context, "ca-app-pub-9539691170569038/1983325078")
//            .forUnifiedNativeAd { ad : UnifiedNativeAd ->
//                // Show the ad.
//            }
//            .withAdListener(object : AdListener() {
//                override fun onAdFailedToLoad(adError: LoadAdError) {
//                    // Handle the failure by logging, altering the UI, and so on.
//                }
//            })
//            .withNativeAdOptions(
//                NativeAdOptions.Builder()
//                // Methods in the NativeAdOptions.Builder class can be
//                // used here to specify individual options settings.
//                .build())
//            .build()

        val post: Posts = postList[position]
        var votes: Int = 0

        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("groupId", "${post.getGroupId()}")
            bundle.putString("postId", "${post.getId()}")
            findNavController(holder.itemView).navigate(R.id.action_groupHolder_to_commentView, bundle)
        }

        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(type!!).child(post.getId()!!)
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
        val groupRef = FirebaseDatabase.getInstance().reference.child("Group Chat List")

        val timePosted = post.getTimeOfPost()?.toLong()
        val currentTime = System.currentTimeMillis()
        if (timePosted != null){
            val millisElapsed = currentTime - timePosted
            val minutesElapsed = millisElapsed/60000
            val hoursElapsed = minutesElapsed/60
            val daysElapsed = hoursElapsed/24
            val weeksElapsed = daysElapsed/7
            val monthsElapsed = weeksElapsed/4
            val yearsElapsed = monthsElapsed/12
            if (yearsElapsed > 0) holder.timePosted!!.text = "• ${yearsElapsed}years ago •"
            else if (monthsElapsed > 0) holder.timePosted!!.text = "• ${monthsElapsed}months ago •"
            else if (weeksElapsed > 0) holder.timePosted!!.text = "• ${weeksElapsed}w ago •"
            else if (daysElapsed > 0) holder.timePosted!!.text = "• ${daysElapsed}d ago •"
            else if (hoursElapsed > 0) holder.timePosted!!.text = "• ${hoursElapsed}h ago •"
            else if (minutesElapsed > 0) holder.timePosted!!.text = "• ${minutesElapsed}m ago •"
            else holder.timePosted!!.text = "• Just now •"
        }

        likesRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                var tally = 0
                for (i in snapshot.children){
                    val num = i.getValue(Int::class.java)
                    val user = i.key
                    if (num != null) {
                        tally+=num
                        if (user == currentUser){
                            when (num){
                                1 -> {
                                    holder.upvote!!.visibility = View.INVISIBLE
                                    holder.upvoteSelected!!.visibility = View.VISIBLE
                                }
                                -1 -> {
                                    holder.downvote!!.visibility = View.INVISIBLE
                                    holder.downvoteSelected!!.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                }
                if (tally >= 1000) {
                    val returnTally = Constants.wrap_number(tally.toDouble())
                    holder.voteCount!!.text = "$returnTally"
                }
                else{
                    holder.voteCount!!.text = "$tally"
                }
            }

        })

        if (post.getNewsLink() != ""){
            val link = post.getNewsLink()
            Log.d("postadap", "$link")
            if (!Beaver.isInitialized()) {
                context?.let { Beaver.build(it) }
            }
            holder.contentLayout!!.visibility = View.VISIBLE
            holder.newsLayout!!.visibility = View.VISIBLE
            holder.mediaLayout!!.visibility = View.GONE
            if (link != null) {
                CoroutineScope(Dispatchers.Main).launch{
                    val metaData = Beaver.load(link).await()
                    if (metaData!= null){
                        Picasso.get().load(metaData.imageUrl).resize(75,75).into(holder.newsPicture)
                        holder.newsHeadline!!.text = metaData.title
                        holder.newsSource!!.text = "- ${metaData.name}"
                        Picasso.get().load(metaData.favIcon).resize(15, 15).into(holder.websiteIcon)
                    }
                    else{
                        //no preview
                    }
                }
            }
            holder.newsLayout!!.setOnClickListener{
                val bundle = Bundle()
                bundle.putString("url", post.getNewsLink())
                findNavController(holder.newsLayout!!).navigate(R.id.action_groupHolder_to_web_view, bundle)
            }
        }
        else if (post.getMediaPic() != "") {
            //media
        }
        else {
            val lp: RelativeLayout.LayoutParams? = holder.mediaComment!!.layoutParams as RelativeLayout.LayoutParams?
            lp!!.setMargins(0, 100, 0, 0)
            holder.mediaComment!!.layoutParams = lp
        }

        groupRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val uri = snapshot.child(post.getGroupId()!!).child("uri").getValue(String::class.java)
                val name = snapshot.child(post.getGroupId()!!).child("name").getValue(String::class.java)
                if (uri != "") Picasso.get().load(uri).into(holder.profileImage)
                holder.name!!.text  = name
            }

        })
        userRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child(post.getCreator()!!).child("name").getValue(String::class.java)
                val username = snapshot.child(post.getCreator()!!).child("username").getValue(String::class.java)
                if (name != "") holder.username!!.text = name
                else holder.username!!.text = username
            }
        })

        holder.description!!.text = post.getDescription()

        holder.upvote!!.setOnClickListener {
            likesRef.child(currentUser).setValue(1)
            holder.upvote!!.visibility = View.INVISIBLE
            holder.upvoteSelected!!.visibility = View.VISIBLE
            holder.downvoteSelected!!.visibility = View.INVISIBLE
            holder.downvote!!.visibility = View.VISIBLE
        }
        holder.upvoteSelected!!.setOnClickListener {
            likesRef.child(currentUser).removeValue()
            holder.upvote!!.visibility = View.VISIBLE
            holder.upvoteSelected!!.visibility = View.INVISIBLE
        }
        holder.downvote!!.setOnClickListener {
            likesRef.child(currentUser).setValue(-1)
            holder.downvote!!.visibility = View.INVISIBLE
            holder.downvoteSelected!!.visibility = View.VISIBLE
            holder.upvoteSelected!!.visibility = View.INVISIBLE
            holder.upvote!!.visibility = View.VISIBLE
        }
        holder.downvoteSelected!!.setOnClickListener {
            likesRef.child(currentUser).removeValue()
            holder.downvote!!.visibility = View.VISIBLE
            holder.downvoteSelected!!.visibility = View.INVISIBLE
        }

    }


    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var profileImage: CircleImageView? = null
        var description: TextView? = null
        var upvote: ImageView? = null
        var downvote: ImageView? = null
        var upvoteSelected: ImageView? = null
        var downvoteSelected: ImageView? = null
        var mediaComment: ImageView? = null
        var username: TextView? = null
        var name: TextView? = null
        var voteCount: TextView? = null

        var newsHolder: ImageView? = null
        var newsHeadline: TextView? = null
        var newsSource: TextView? = null
        var newsPicture: ImageView? = null
        var mediaContent: ImageView? = null

        var newsLayout: RelativeLayout? = null
        var mediaLayout: RelativeLayout? = null
        var contentLayout: RelativeLayout? = null

        var websiteIcon: ImageView? = null
        var timePosted: TextView? = null

        init {
            profileImage = itemView.findViewById(R.id.civ_post_profile_pic)
            description = itemView.findViewById(R.id.tv_post_description)
            upvote = itemView.findViewById(R.id.iv_post_upvote)
            downvote = itemView.findViewById(R.id.iv_post_downvote)
            upvoteSelected = itemView.findViewById(R.id.iv_post_upvote_selected)
            downvoteSelected = itemView.findViewById(R.id.iv_post_downvote_selected)
            mediaComment = itemView.findViewById(R.id.iv_post_comment_media)
            name = itemView.findViewById(R.id.tv_post_group_name)
            username = itemView.findViewById(R.id.tv_post_username)
            voteCount = itemView.findViewById(R.id.tv_post_vote_count)

            newsHolder = itemView.findViewById(R.id.iv_post_news_holder)
            newsHeadline = itemView.findViewById(R.id.tv_post_headline)
            newsSource = itemView.findViewById(R.id.tv_post_news_source)
            newsPicture = itemView.findViewById(R.id.iv_post_news_image)
            mediaContent = itemView.findViewById(R.id.iv_media_content_picture)

            newsLayout = itemView.findViewById(R.id.news_content_layout)
            mediaLayout = itemView.findViewById(R.id.media_content_layout)
            contentLayout = itemView.findViewById(R.id.content_post_layout)
            websiteIcon = itemView.findViewById(R.id.iv_post_news_icon)
            timePosted= itemView.findViewById(R.id.tv_post_time_passed)
        }
    }

//    private fun setLikeButtonStatus(){
//        likesRef.addValueEventListener(object : ValueEventListener{
//            override fun onCancelled(error: DatabaseError) {}
//            override fun onDataChange(snapshot: DataSnapshot) {
//                for (i in snapshot.children){
//                    val num = i.getValue(Int::class.java)
//                    if (num != null) {
//                        votes += num
//                    }
//                }
//                if (snapshot.child(currentUser).hasChild(currentUser)){
//                    if (snapshot.child(currentUser).getValue(Int::class.java) == 1) {
//                        holder.voteCount!!.text = "$votes"
//                        holder.upvoteSelected!!.visibility = View.VISIBLE
//                        holder.upvote!!.visibility = View.GONE
//                    }
//                    else if (snapshot.child(currentUser).getValue(Int::class.java) == -1){
//                        holder.voteCount!!.text = "$votes"
//                        holder.downvoteSelected!!.visibility = View.VISIBLE
//                        holder.downvote!!.visibility = View.GONE
//                    }
//                }
//            }
//
//        })
//    }

}