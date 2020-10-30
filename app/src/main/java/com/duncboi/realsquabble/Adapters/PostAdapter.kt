package com.duncboi.realsquabble.Adapters

import android.content.Context
import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.VerifiedInputEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.duncboi.realsquabble.Miscellaneous.PostAlgorithims
import com.duncboi.realsquabble.ModelClasses.Data
import com.duncboi.realsquabble.ModelClasses.Posts
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.notifications.*
import com.duncboi.realsquabble.political.Constants
import com.duncboi.realsquabble.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.raqun.beaverlib.Beaver
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PostAdapter(context: Context, postList: List<Posts>, type: String, from: String, nav: String): RecyclerView.Adapter<PostAdapter.ViewHolder?>() {


    interface onLoadMoreItemsListener{
        fun onLoadMoreItems(from: String)
    }

    var mOnLoadMoreItemsListener: onLoadMoreItemsListener? = null

    fun setListener(mOnLoadMoreItemsListener: onLoadMoreItemsListener?) {
        this.mOnLoadMoreItemsListener = mOnLoadMoreItemsListener
    }

    var apiService: APIService? = null
    private val context: Context?
    private val postList: List<Posts>
    private val type: String?
    private var from: String?
    private var nav: String?

    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    init {
        this.postList = postList
        this.context = context
        this.type = type
        this.from = from
        this.nav = nav
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
            val view: View = LayoutInflater.from(context).inflate(R.layout.post, viewGroup, false)
            return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        apiService = Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)

        val post: Posts = postList[position]

        holder.mediaContent!!.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("uri", "${post.getMediaPic()}")
            PostAdapterVM.trendingPosition = position
            if (nav == "trending" || nav == "party") {
                if (nav == "trending"){
                    PostAdapterVM.trendingPosition = position
                }
                else{
                    PostAdapterVM.partyPosition = position
                }
                findNavController(holder.itemView).navigate(
                    R.id.action_trending_to_viewImage,
                    bundle
                )
            }
            else if (nav == "group") {
                PostAdapterVM.groupPosition = position
                findNavController(holder.itemView).navigate(
                    R.id.action_groupHolder_to_viewImage,
                    bundle
                )
            }
            else if (nav == "profile") findNavController(holder.itemView).navigate(
                R.id.action_default_profile_to_viewImage,
                bundle
            )
        }

        val zscore = PostAlgorithims.groupLevelAlgorithim(
            post.getVotes()!!.toDouble(),
            post.getTimeOfPost()?.toLong()!!
        )
        FirebaseDatabase.getInstance().reference.child("Posts").child(from!!)
            .child(post.getGroupId()!!).child(post.getId()!!).child("zscore")
            .setValue(zscore)

        val alignmentRef = FirebaseDatabase.getInstance().reference.child("Group Chat List").child(
            post.getGroupId()!!
        ).child("filter").child("alignment")
        alignmentRef.onDisconnect().cancel()
            alignmentRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children) {
                        val party = i.getValue(String::class.java)
                        if (party != null) {
                            FirebaseDatabase.getInstance().reference.child("PostsRef").child(party)
                                .child(
                                    post.getId()!!
                                ).child("zscore").setValue(zscore)
                        }
                    }
                }
            })

        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("groupId", "${post.getGroupId()}")
            bundle.putString("postId", "${post.getId()}")
            bundle.putString("from", from)
            if (nav == "trending" || nav == "party") {
                if (nav == "trending"){
                    PostAdapterVM.trendingPosition = position
                }
                else{
                    PostAdapterVM.partyPosition = position
                }
                findNavController(holder.itemView).navigate(
                    R.id.action_trending_to_commentView,
                    bundle
                )
            }
            else if (nav == "group") {
                PostAdapterVM.groupPosition = position
                findNavController(holder.itemView).navigate(
                    R.id.action_groupHolder_to_commentView,
                    bundle
                )
            }
            else if (nav == "profile") findNavController(holder.itemView).navigate(
                R.id.action_default_profile_to_commentView,
                bundle
            )
        }

        val likedPostsHash = HashMap<String, Any?>()
        likedPostsHash["groupId"] = post.getGroupId()
        likedPostsHash["postId"] = post.getId()

        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(from!!).child(
            post.getGroupId()!!
        ).child(post.getId()!!).child("comments")
        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child("votes").child(
            type!!
        ).child(post.getId()!!)
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
        val groupRef = FirebaseDatabase.getInstance().reference.child("Group Chat List")
        val likedPostsRef = FirebaseDatabase.getInstance().reference.child("Liked Posts").child(
            currentUser
        ).child(post.getId()!!)

        postsRef.onDisconnect().cancel()
        postsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                var tally = 0
                if (holder.numberOfComments != null) {
                    for (i in snapshot.children) {
                        tally++
                        for (x in i.child("comments").children) {
                            tally++
                        }
                    }
                    holder.numberOfComments!!.text = "${tally}"
                }
            }

        })

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
            if (yearsElapsed > 0) holder.timePosted!!.text = "• ${yearsElapsed} years ago •"
            else if (monthsElapsed > 0) holder.timePosted!!.text = "• ${monthsElapsed} months ago •"
            else if (weeksElapsed > 0) holder.timePosted!!.text = "• ${weeksElapsed}w ago •"
            else if (daysElapsed > 0) holder.timePosted!!.text = "• ${daysElapsed}d ago •"
            else if (hoursElapsed > 0) holder.timePosted!!.text = "• ${hoursElapsed}h ago •"
            else if (minutesElapsed > 0) holder.timePosted!!.text = "• ${minutesElapsed}m ago •"
            else holder.timePosted!!.text = "• Just now •"
        }

        holder.downvote!!.visibility = View.VISIBLE
        holder.downvoteSelected!!.visibility = View.INVISIBLE
        holder.upvote!!.visibility = View.VISIBLE
        holder.upvoteSelected!!.visibility = View.INVISIBLE

        likesRef.onDisconnect().cancel()
        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                var tally = 0
                if (holder.downvote != null) {
                    for (i in snapshot.children) {
                        val num = i.getValue(Int::class.java)
                        val user = i.key
                        if (num != null) {
                            tally += num
                            if (user == currentUser) {
                                when (num) {
                                    1 -> {
                                        holder.upvote!!.visibility = View.INVISIBLE
                                        holder.upvoteSelected!!.visibility = View.VISIBLE
                                        holder.downvote!!.visibility = View.VISIBLE
                                        holder.downvoteSelected!!.visibility = View.INVISIBLE
                                    }
                                    -1 -> {
                                        holder.downvote!!.visibility = View.INVISIBLE
                                        holder.downvoteSelected!!.visibility = View.VISIBLE
                                        holder.upvote!!.visibility = View.VISIBLE
                                        holder.upvoteSelected!!.visibility = View.INVISIBLE
                                    }
                                }
                            }
                        }
                    }
                }
                FirebaseDatabase.getInstance().reference.child("Posts").child(from!!)
                    .child(post.getGroupId()!!).child(post.getId()!!).child("votes").setValue(tally)
                    .addOnCompleteListener {
                        if (holder.voteCount != null) {
                            if (tally >= 1000) {
                                val returnTally = Constants.wrap_number(tally.toDouble())
                                holder.voteCount!!.text = returnTally
                            } else {
                                holder.voteCount!!.text = "$tally"
                            }
                        }
                    }
            }

        })
        holder.newsIcon!!.visibility = View.GONE
        holder.newsCard!!.visibility = View.GONE
        holder.websiteIcon!!.visibility = View.GONE

        val lp: RelativeLayout.LayoutParams? = holder.mediaComment!!.layoutParams as RelativeLayout.LayoutParams?
        lp!!.setMargins(0, 30, 0, 0)

        if (post.getNewsLink() != ""){
            holder.websiteIcon!!.visibility = View.VISIBLE
            holder.newsIcon!!.visibility = View.VISIBLE
            holder.newsCard!!.visibility = View.VISIBLE
            val link = post.getNewsLink()
            try {
                if (!Beaver.isInitialized()) {
                    context?.let { Beaver.build(it) }
                }
                holder.contentLayout!!.visibility = View.VISIBLE
                holder.newsLayout!!.visibility = View.VISIBLE
                holder.mediaLayout!!.visibility = View.GONE
                if (link != null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val metaData = Beaver.load(link).await()
                        if (metaData != null) {
                            Picasso.get().load(metaData.imageUrl).into(holder.newsPicture)
                            holder.newsHeadline!!.text = metaData.title
                            holder.newsSource!!.text = "- ${metaData.name}"
                            Picasso.get().load(metaData.favIcon).into(holder.websiteIcon)
                        } else {
                            //no preview
                        }
                    }
                }
            }
            catch (e: SQLiteCantOpenDatabaseException){

            }
            holder.newsLayout!!.setOnClickListener{
                    val bundle = Bundle()
                    bundle.putString("url", post.getNewsLink())
                PostAdapterVM.trendingPosition = position
                if (nav == "group") {
                    PostAdapterVM.groupPosition = position
                    findNavController(holder.newsLayout!!).navigate(
                        R.id.action_groupHolder_to_web_view,
                        bundle
                    )
                }
                else if (nav == "trending" || nav == "party"){
                    if (nav == "trending"){
                        PostAdapterVM.trendingPosition = position
                    }
                    else{
                        PostAdapterVM.partyPosition = position
                    }
                    findNavController(holder.newsLayout!!).navigate(
                        R.id.action_trending_to_web_view,
                        bundle
                    )
                }
                else{
                    findNavController(holder.newsLayout!!).navigate(
                        R.id.action_default_profile_to_web_view,
                        bundle
                    )
                }
            }
            holder.newsCard!!.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("url", post.getNewsLink())
                PostAdapterVM.trendingPosition = position
                if (nav == "group") {
                    PostAdapterVM.groupPosition = position
                    findNavController(holder.newsLayout!!).navigate(
                        R.id.action_groupHolder_to_web_view,
                        bundle
                    )
                }
                else if (nav == "trending" || nav == "party"){
                    if (nav == "trending"){
                        PostAdapterVM.trendingPosition = position
                    }
                    else{
                        PostAdapterVM.partyPosition = position
                    }
                    findNavController(holder.newsLayout!!).navigate(
                        R.id.action_trending_to_web_view,
                        bundle
                    )
                }
                else{
                    findNavController(holder.newsLayout!!).navigate(
                        R.id.action_default_profile_to_web_view,
                        bundle
                    )
                }
            }
        }
        else if (post.getMediaPic() != "") {
            //media

            holder.contentLayout!!.visibility = View.VISIBLE
            holder.newsLayout!!.visibility = View.GONE
            holder.mediaLayout!!.visibility = View.VISIBLE
            holder.mediaContent!!.visibility = View.VISIBLE

            holder.mediaContent!!.layout(0,0,0,0)
            Glide.with(context!!)
                .load(post.getMediaPic())
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(holder.mediaContent!!)
        }
        else {
            holder.contentLayout!!.visibility = View.VISIBLE
            holder.newsLayout!!.visibility = View.GONE
            holder.mediaLayout!!.visibility = View.GONE
            val lp: RelativeLayout.LayoutParams? = holder.mediaComment!!.layoutParams as RelativeLayout.LayoutParams?
            lp!!.setMargins(0, 125, 0, 0)
            holder.mediaComment!!.layoutParams = lp
        }

        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (holder.profileImage != null) {
                    val uri = snapshot.child(post.getGroupId()!!).child("uri")
                        .getValue(String::class.java)
                    val name = snapshot.child(post.getGroupId()!!).child("name")
                        .getValue(String::class.java)
                    if (uri != "") Picasso.get().load(uri).into(holder.profileImage)
                    holder.name!!.text = name
                }
            }

        })
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val anonymous = snapshot.child(post.getCreator()!!).child("anonymous")
                    .getValue(String::class.java)
                if (post.getCreator() == currentUser) {
                    holder.username!!.text = "You"
                    holder.username!!.setTextColor(Color.parseColor("#4885ed"))
                } else {
                    if (anonymous != null) {
                        if (anonymous != "ON") {
                            val name = snapshot.child(post.getCreator()!!).child("name")
                                .getValue(String::class.java)
                            val username = snapshot.child(post.getCreator()!!).child("username")
                                .getValue(String::class.java)
                            if (name != "") holder.username!!.text = name
                            else holder.username!!.text = username
                        } else holder.username!!.text = "Anonymous"
                    }
                }
            }
        })

        holder.description!!.text = post.getDescription()

        holder.upvote!!.setOnClickListener {
            likesRef.child(currentUser).setValue(1).addOnCompleteListener {
                if (holder.upvote != null) {
                    likedPostsRef.setValue(likedPostsHash)
                    holder.upvote!!.visibility = View.INVISIBLE
                    holder.upvoteSelected!!.visibility = View.VISIBLE
                    holder.downvoteSelected!!.visibility = View.INVISIBLE
                    holder.downvote!!.visibility = View.VISIBLE
                }
                FirebaseDatabase.getInstance().reference.child("Users").child(currentUser)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {}
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val username = snapshot.child("username").getValue(String::class.java)
                            if (post.getCreator() != currentUser) {
                                sendNotification(
                                    post.getCreator()!!,
                                    username,
                                    post.getId()!!,
                                    post.getGroupId()!!
                                )
                            }
                        }
                    })
            }
        }

        likesRef.onDisconnect().cancel()
        holder.upvoteSelected!!.setOnClickListener {
            likesRef.child(currentUser).removeValue().addOnCompleteListener {
                if (holder.upvote != null) {
                    likedPostsRef.removeValue()
                    holder.upvote!!.visibility = View.VISIBLE
                    holder.upvoteSelected!!.visibility = View.INVISIBLE
                }
                lowerUnreadNotification(post)
            }
        }
        holder.downvote!!.setOnClickListener {
            likesRef.child(currentUser).setValue(-1).addOnCompleteListener {
                if (holder.downvote != null) {
                    likedPostsRef.removeValue()
                    holder.downvote!!.visibility = View.INVISIBLE
                    holder.downvoteSelected!!.visibility = View.VISIBLE
                    holder.upvoteSelected!!.visibility = View.INVISIBLE
                    holder.upvote!!.visibility = View.VISIBLE
                }
                FirebaseDatabase.getInstance().reference.child("Notifications")
                    .child(post.getCreator()!!).child(post.getId()!!).removeValue()
                lowerUnreadNotification(post)
            }
        }
        holder.downvoteSelected!!.setOnClickListener {
            likesRef.child(currentUser).removeValue().addOnCompleteListener {
                if (holder.downvote != null) {
                    likedPostsRef.removeValue()
                    holder.downvote!!.visibility = View.VISIBLE
                    holder.downvoteSelected!!.visibility = View.INVISIBLE
                }
            }
            lowerUnreadNotification(post)
        }

        if (reachedEndOfList(position)){
            loadMoreData()
        }

    }

    private fun lowerUnreadNotification(post: Posts) {
        FirebaseDatabase.getInstance().reference.child("Notifications").child(post.getCreator()!!)
            .child(post.getId()!!).removeValue()
        val unreadRef = FirebaseDatabase.getInstance().reference.child("Unread Notifications")
            .child(post.getCreator()!!).child("numberUnread")
        unreadRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentNumber = snapshot.getValue(Long::class.java)
                if (currentNumber != null && currentNumber > 0) {
                    unreadRef.setValue(currentNumber - 1)
                }
            }

        })
    }

    private fun reachedEndOfList(position: Int): Boolean{
        return position == itemCount-1
    }

    private fun loadMoreData(){
        setListener(ProfileActivity())
        mOnLoadMoreItemsListener?.onLoadMoreItems(nav!!)
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

        var newsIcon: ImageView? = null
        var newsHeadline: TextView? = null
        var newsSource: TextView? = null
        var newsPicture: ImageView? = null
        var mediaContent: ImageView? = null

        var newsLayout: RelativeLayout? = null
        var mediaLayout: RelativeLayout? = null
        var contentLayout: RelativeLayout? = null
        var mediaLoader: ProgressBar? = null

        var websiteIcon: ImageView? = null
        var timePosted: TextView? = null
        var numberOfComments: TextView? = null
        var newsCard: CardView? = null

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

            newsHeadline = itemView.findViewById(R.id.tv_post_headline)
            newsSource = itemView.findViewById(R.id.tv_post_news_source)
            newsPicture = itemView.findViewById(R.id.iv_post_news_image)
            mediaContent = itemView.findViewById(R.id.iv_media_content_picture)
            mediaLoader = itemView.findViewById(R.id.pb_media_content_progress)

            newsLayout = itemView.findViewById(R.id.news_content_layout)
            mediaLayout = itemView.findViewById(R.id.media_content_layout)
            contentLayout = itemView.findViewById(R.id.content_post_layout)
            websiteIcon = itemView.findViewById(R.id.iv_post_news_icon)
            timePosted = itemView.findViewById(R.id.tv_post_time_passed)
            numberOfComments = itemView.findViewById(R.id.tv_post_number_of_comments)
            newsIcon = itemView.findViewById(R.id.iv_post_link_icon)
            newsCard = itemView.findViewById(R.id.cv_post_news_card)
        }
    }

    private fun sendNotification(
        receiverUid: String,
        username: String?,
        likedCommentId: String,
        groupId: String
    ){
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")

        val query = ref.orderByKey().equalTo(receiverUid)

        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val token: Token? = dataSnapshot.getValue(Token::class.java)

                    FirebaseDatabase.getInstance().reference.child("Users").child(currentUser).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {}
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val anonymous = snapshot.child("anonymous").getValue(String::class.java)
                            if (anonymous != null) {
                                val data: Data
                                if (anonymous == "ON") data = Data(currentUser, R.mipmap.squabble_icon, "Anonymous upvoted your post", "New Message", receiverUid, likedCommentId, groupId, "")
                                else data = Data(currentUser, R.mipmap.squabble_icon, "$username upvoted your post", "New Message", receiverUid, likedCommentId, groupId, "")


                                val sender = Sender(data, token!!.getToken().toString())

                                apiService!!.sendNotificaiton(sender).enqueue(object : Callback<MyResponse> {
                                    override fun onResponse(call: Call<MyResponse>, response: Response<MyResponse>) {
                                        if (response.code() == 200) {
                                            if (response.body()!!.success != 1) {
                                                Log.d("Notification", "failed notification")
                                            }
                                        }
                                    }

                                    override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                                    }
                                })
                            }
                        }
                    })
                }
            }
        })
    }

}