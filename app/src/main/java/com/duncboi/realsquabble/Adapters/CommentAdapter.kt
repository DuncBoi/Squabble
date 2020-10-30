package com.duncboi.realsquabble.Adapters

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.duncboi.realsquabble.ModelClasses.Comments
import com.duncboi.realsquabble.ModelClasses.Data
import com.duncboi.realsquabble.political.Constants
import com.duncboi.realsquabble.ModelClasses.Posts
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.notifications.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.raqun.beaverlib.Beaver
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_default_profile.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CommentAdapter(
    context: Context,
    commentList: List<Comments>,
    postId: String,
    groupId: String,
    type: String,
    parentComment:String,
    from: String
): RecyclerView.Adapter<CommentAdapter.ViewHolder?>() {

    private val type: String
    private val context: Context?
    private val commentList: List<Comments>
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    private val postId: String
    private val groupId: String
    private val parentComment: String
    private val from: String
    var apiService: APIService? = null
    var likedCommentId: String = ""

    init {
        this.context = context
        this.commentList = commentList
        this.groupId = groupId
        this.postId = postId
        this.type = type
        this.parentComment = parentComment
        this.from = from
    }


    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        if (position == 0){
            if (type == "comment") {
                val view: View = LayoutInflater.from(context).inflate(R.layout.post, parent, false)
                return ViewHolder(view)
            }
            else{
                val view: View = LayoutInflater.from(context).inflate(R.layout.comment, parent, false)
                return ViewHolder(view)
            }
        }
        else {
            val view: View = LayoutInflater.from(context).inflate(R.layout.comment, parent, false)
            return ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        likedCommentId = FirebaseDatabase.getInstance().reference.child("Notifications").push().key!!

        apiService = Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)

        if (position == 0 && type == "comment") {

            holder.downvote!!.visibility = View.VISIBLE
            holder.downvoteSelected!!.visibility = View.INVISIBLE
            holder.upvote!!.visibility = View.VISIBLE
            holder.upvoteSelected!!.visibility = View.INVISIBLE

            val postRef = FirebaseDatabase.getInstance().reference.child("Posts").child(from).child(groupId).child(postId)
                postRef.onDisconnect().cancel()
                postRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val post = snapshot.getValue(Posts::class.java)
                        val postsRef =
                            FirebaseDatabase.getInstance().reference.child("Posts").child(from).child(groupId)
                                .child(postId).child("comments")
                        val likesRef =
                            FirebaseDatabase.getInstance().reference.child("Likes").child("votes")
                                .child(groupId).child(postId)
                        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
                        val groupRef =
                            FirebaseDatabase.getInstance().reference.child("Group Chat List")

                        postsRef.onDisconnect().cancel()
                        postsRef.addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {}
                            override fun onDataChange(snapshot: DataSnapshot) {
                                var tally = 0
                                for (i in snapshot.children) {
                                    tally++
                                    for (x in i.child("comments").children) {
                                        tally++
                                    }
                                }
                                holder.numberOfComments!!.text = "${tally}"
                            }

                        })

                        holder.postProfileImage!!.setOnClickListener {
                            val bundle = Bundle()
                            bundle.putString("groupId", "${post?.getGroupId()}")
                            findNavController(holder.itemView).navigate(R.id.action_commentView_to_groupDescription, bundle)
                        }

                        holder.name!!.setOnClickListener {
                            val bundle = Bundle()
                            bundle.putString("groupId", "${post?.getGroupId()}")
                            findNavController(holder.itemView).navigate(R.id.action_commentView_to_groupDescription, bundle)
                        }

                        val timePosted = post?.getTimeOfPost()?.toLong()
                        val currentTime = System.currentTimeMillis()
                        if (timePosted != null) {
                            val millisElapsed = currentTime - timePosted
                            val minutesElapsed = millisElapsed / 60000
                            val hoursElapsed = minutesElapsed / 60
                            val daysElapsed = hoursElapsed / 24
                            val weeksElapsed = daysElapsed / 7
                            val monthsElapsed = weeksElapsed / 4
                            val yearsElapsed = monthsElapsed / 12
                            if (yearsElapsed > 0) holder.timePosted!!.text =
                                "• ${yearsElapsed} years ago •"
                            else if (monthsElapsed > 0) holder.timePosted!!.text =
                                "• ${monthsElapsed} months ago •"
                            else if (weeksElapsed > 0) holder.timePosted!!.text =
                                "• ${weeksElapsed}w ago •"
                            else if (daysElapsed > 0) holder.timePosted!!.text =
                                "• ${daysElapsed}d ago •"
                            else if (hoursElapsed > 0) holder.timePosted!!.text =
                                "• ${hoursElapsed}h ago •"
                            else if (minutesElapsed > 0) holder.timePosted!!.text =
                                "• ${minutesElapsed}m ago •"
                            else holder.timePosted!!.text = "• Just now •"
                        }

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
                                    .child(post?.getGroupId()!!).child(post.getId()!!).child("votes").setValue(tally)
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

                        if (post != null) {
                            if (post.getNewsLink() != "") {
                                val link = post.getNewsLink()
                                if (!Beaver.isInitialized()) {
                                    context?.let { Beaver.build(it) }
                                }
                                holder.contentLayout!!.visibility = View.VISIBLE
                                holder.newsLayout!!.visibility = View.VISIBLE
                                holder.mediaLayout!!.visibility = View.GONE
                                holder.newsCard!!.visibility = View.VISIBLE
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
                                holder.newsLayout!!.setOnClickListener {
                                    val bundle = Bundle()
                                    bundle.putString("url", post.getNewsLink())
                                    findNavController(holder.newsLayout!!)
                                        .navigate(R.id.action_commentView_to_web_view, bundle)
                                }
                                holder.newsCard!!.setOnClickListener {
                                    val bundle = Bundle()
                                    bundle.putString("url", post.getNewsLink())
                                    findNavController(holder.newsLayout!!)
                                        .navigate(R.id.action_commentView_to_web_view, bundle)
                                }
                            } else if (post.getMediaPic() != "") {
                                //media
                                holder.contentLayout!!.visibility = View.VISIBLE
                                holder.newsLayout!!.visibility = View.GONE
                                holder.mediaLayout!!.visibility = View.VISIBLE

                                Glide.with(context!!)
                                    .load(post.getMediaPic())
                                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                    .into(holder.mediaContent!!)
                                holder.mediaContent!!.setOnClickListener {
                                    val bundle = Bundle()
                                    bundle.putString("uri", "${post.getMediaPic()}")
                                    findNavController(holder.itemView!!).navigate(R.id.action_commentView_to_viewImage, bundle)
                                }


                            } else {
                                val lp: RelativeLayout.LayoutParams? =
                                    holder.mediaComment!!.layoutParams as RelativeLayout.LayoutParams?
                                lp!!.setMargins(0, 125, 0, 0)
                                holder.mediaComment!!.layoutParams = lp
                            }
                        }

                        groupRef.onDisconnect().cancel()
                        groupRef.addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {}
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val uri = snapshot.child(post?.getGroupId()!!).child("uri")
                                    .getValue(String::class.java)
                                val name = snapshot.child(post.getGroupId()!!).child("name")
                                    .getValue(String::class.java)
                                if (uri != "") Picasso.get().load(uri).into(holder.postProfileImage)
                                holder.name!!.text = name
                            }

                        })
                        userRef.onDisconnect().cancel()
                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {}
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val anonymous =
                                    snapshot.child(post?.getCreator()!!).child("anonymous")
                                        .getValue(String::class.java)
                                if (holder.postUsername != null) {
                                    if (post.getCreator() == currentUser) {
                                        holder.postUsername!!.text = "You"
                                        holder.postUsername!!.setTextColor(Color.parseColor("#4885ed"))
                                    } else {
                                        if (anonymous != "ON") {
                                            val name =
                                                snapshot.child(post.getCreator()!!).child("name")
                                                    .getValue(String::class.java)
                                            val username =
                                                snapshot.child(post.getCreator()!!)
                                                    .child("username")
                                                    .getValue(String::class.java)
                                            if (name != "") holder.postUsername!!.text = name
                                            else holder.postUsername!!.text = username
                                        } else holder.postUsername!!.text = "Anonymous"
                                    }
                                }
                            }
                        })

                        holder.description!!.text = post?.getDescription()
                        val likedPostsRef = FirebaseDatabase.getInstance().reference.child("Liked Posts").child(
                            currentUser
                        ).child(post?.getId()!!)
                        val likedPostsHash = HashMap<String, Any?>()
                        likedPostsHash["groupId"] = post.getGroupId()
                        likedPostsHash["postId"] = post.getId()

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
                    }
                })
        }
        else{
            val comment = commentList[position]

            holder.username!!.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("uid", "${comment.getUser()}")
                if (comment.getUser() != currentUser) {
                    if (type == "commentComment") findNavController(holder.itemView).navigate(
                        R.id.action_replyToComment_to_otherProfile,
                        bundle
                    )
                    else findNavController(holder.itemView).navigate(
                        R.id.action_commentView_to_otherProfile,
                        bundle
                    )
                }
            }

            val userRef =
                FirebaseDatabase.getInstance().reference.child("Users").child(comment.getUser()!!)
            val commentLikeRef =
                FirebaseDatabase.getInstance().reference.child("Likes").child("commentLikes")
                    .child(groupId).child(postId).child("comments").child(comment.getCommentId()!!)
                    .child("likes")
            val commentCommentRef =
                FirebaseDatabase.getInstance().reference.child("Posts").child(from).child(groupId).child(postId)
                    .child("comments").child(comment.getCommentId()!!).child("comments")

            if (type == "comment") {
                holder.itemView.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("groupId", groupId)
                    bundle.putString("postId", postId)
                    bundle.putString("commentId", comment.getCommentId())
                    bundle.putString("from", from)
                    findNavController(holder.itemView).navigate(
                        R.id.action_commentView_to_replyToComment,
                        bundle
                    )
                }
            }

            commentCommentRef.onDisconnect().cancel()
            commentCommentRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && holder.viewReplies != null) {
                        var tally = 0
                        for (i in snapshot.children) {
                            tally++
                        }
                        holder.viewReplies!!.text = "View Replies ($tally)"
                        holder.viewReplies!!.visibility = View.VISIBLE
                        holder.viewReplies!!.setOnClickListener {

                        }
                    } else {
                        holder.viewReplies!!.visibility = View.GONE

                    }
                }

            })

            commentLikeRef.onDisconnect().cancel()
            commentLikeRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    var tally = 0
                    for (i in snapshot.children) {
                        val num = i.getValue(Int::class.java)
                        if (num != null) {
                            tally += num
                        }
                    }
                    if (type == "comment") {
                        FirebaseDatabase.getInstance().reference.child("Posts").child(from).child(groupId)
                            .child(postId).child("comments").child(comment.getCommentId()!!)
                            .child("likeNumber").setValue(tally)
                    }
                    else if (type == "commentComment"){
                        FirebaseDatabase.getInstance().reference.child("Posts").child(from).child(groupId)
                            .child(postId).child("comments").child(parentComment)
                            .child("comments").child(comment.getCommentId()!!).child("likeNumber").setValue(tally)
                    }
                }
            })

            commentLikeRef.onDisconnect().cancel()
            commentLikeRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    var tally = 0
                    if (holder.heart != null) {
                        for (i in snapshot.children) {
                            val num = i.getValue(Int::class.java)
                            val user = i.key
                            if (num != null) {
                                tally += num
                                if (user == currentUser) {
                                    when (num) {
                                        1 -> {
                                            holder.heartFull!!.visibility = View.VISIBLE
                                            holder.heart!!.visibility = View.INVISIBLE
                                        }
                                    }
                                }
                            }
                        }
                        if (tally >= 1000) {
                            val returnTally = Constants.wrap_number(tally.toDouble())
                            holder.heartCount!!.text = "$returnTally"
                        } else {
                            holder.heartCount!!.text = "$tally"
                        }
                    }
                }

            })

            holder.heart!!.setOnClickListener {

                commentLikeRef.child(currentUser).setValue(1).addOnCompleteListener {
                    holder.heart!!.visibility = View.INVISIBLE
                    holder.heartFull!!.visibility = View.VISIBLE
                }
                val userRef2 = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser)
                    userRef2.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val username = snapshot.child("username").getValue(String::class.java)
                        if (comment.getUser() != currentUser) {
                            likedCommentSendNotification(
                                comment.getUser()!!,
                                username,
                                comment.getCommentId()!!,
                                groupId,
                                postId
                            )
                        }
                    }
            })
            }

            holder.heartFull!!.setOnClickListener {
                commentLikeRef.child(currentUser).removeValue().addOnCompleteListener {
                    if (holder.heart != null) {
                        holder.heart!!.visibility = View.VISIBLE
                        holder.heartFull!!.visibility = View.INVISIBLE
                    }
                }
                val numberUnreadRef = FirebaseDatabase.getInstance().reference.child("Unread Notifications").child(comment.getUser()!!).child("numberUnread")
                numberUnreadRef.removeValue()
                FirebaseDatabase.getInstance().reference.child("Notifications").child(comment.getUser()!!).child(likedCommentId).removeValue()
            }

            holder.text_message!!.text = comment.getComment()
            val timePosted = comment.getTimePosted()?.toLong()
            val currentTime = System.currentTimeMillis()
            if (timePosted != null) {
                val millisElapsed = currentTime - timePosted
                val minutesElapsed = millisElapsed / 60000
                val hoursElapsed = minutesElapsed / 60
                val daysElapsed = hoursElapsed / 24
                val weeksElapsed = daysElapsed / 7
                val monthsElapsed = weeksElapsed / 4
                val yearsElapsed = monthsElapsed / 12
                if (yearsElapsed > 0) holder.minutesAgo!!.text = "• ${yearsElapsed}years ago •"
                else if (monthsElapsed > 0) holder.minutesAgo!!.text = "• ${monthsElapsed}months ago •"
                else if (weeksElapsed > 0) holder.minutesAgo!!.text = "• ${weeksElapsed}w ago •"
                else if (daysElapsed > 0) holder.minutesAgo!!.text = "• ${daysElapsed}d ago •"
                else if (hoursElapsed > 0) holder.minutesAgo!!.text = "• ${hoursElapsed}h ago •"
                else if (minutesElapsed > 0) holder.minutesAgo!!.text = "• ${minutesElapsed}m ago •"
                else holder.minutesAgo!!.text = "• Just now •"
            }

            userRef.onDisconnect().cancel()
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    val uri = snapshot.child("uri").getValue(String::class.java)
                    val username = snapshot.child("username").getValue(String::class.java)
                    val name = snapshot.child("name").getValue(String::class.java)
                    val anonymous = snapshot.child("anonymous").getValue(String::class.java)
                    if (holder.username != null) {
                        if (comment.getUser() == currentUser){
                            holder.username!!.text = "You"
                            holder.username!!.setTextColor(Color.parseColor("#4885ed"))
                            if (uri != null && uri != "") Picasso.get().load(uri).into(holder.profileImage)
                        }
                        else {
                            if (anonymous != "ON") {
                                if (name != "") holder.username!!.text = name
                                else holder.username!!.text = username
                                if (uri != null && uri != "") {
                                    Picasso.get().load(uri).into(holder.profileImage)
                                }
                                holder.profileImage!!.setOnClickListener {
                                    val bundle = Bundle()
                                    bundle.putString("uid", "${comment.getUser()}")
                                    if (comment.getUser() != currentUser) {
                                        if (type == "commentComment") findNavController(holder.itemView).navigate(
                                            R.id.action_replyToComment_to_otherProfile,
                                            bundle
                                        )
                                        else findNavController(holder.itemView).navigate(
                                            R.id.action_commentView_to_otherProfile,
                                            bundle
                                        )
                                    }
                                }
                            } else {
                                holder.username!!.text = "Anonymous"
                            }
                        }
                    }
                }
            })
        }
    }
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView? = null
        var text_message: TextView? = null
        var minutesAgo: TextView? = null
        var username: TextView? = null
        var heartFull: ImageView? = null
        var heart: ImageView? = null
        var heartCount: TextView? = null
        var viewReplies: TextView? = null
        var edit: EditText? = null

        //post
        var postProfileImage: CircleImageView? = null
        var description: TextView? = null
        var upvote: ImageView? = null
        var downvote: ImageView? = null
        var upvoteSelected: ImageView? = null
        var downvoteSelected: ImageView? = null
        var mediaComment: ImageView? = null
        var postUsername: TextView? = null
        var name: TextView? = null
        var voteCount: TextView? = null

        var newsCard: CardView? = null
        var newsHeadline: TextView? = null
        var newsSource: TextView? = null
        var newsPicture: ImageView? = null
        var mediaContent: ImageView? = null

        var newsLayout: RelativeLayout? = null
        var mediaLayout: RelativeLayout? = null
        var contentLayout: RelativeLayout? = null

        var websiteIcon: ImageView? = null
        var timePosted: TextView? = null
        var numberOfComments: TextView? = null

        init {
            profileImage = itemView.findViewById(R.id.civ_comment_profile_pic)
            text_message = itemView.findViewById(R.id.tv_comment_text)
            minutesAgo = itemView.findViewById(R.id.tv_comment_time)
            username = itemView.findViewById(R.id.tv_comment_username)
            heart = itemView.findViewById(R.id.iv_comment_heart)
            heartFull = itemView.findViewById(R.id.iv_comment_heart_fill)
            heartCount = itemView.findViewById(R.id.tv_comment_like_count)
            viewReplies = itemView.findViewById(R.id.tv_comment_view_replies)
            edit = itemView.findViewById(R.id.et_comment_view_comment)

            //post
            postProfileImage = itemView.findViewById(R.id.civ_post_profile_pic)
            description = itemView.findViewById(R.id.tv_post_description)
            upvote = itemView.findViewById(R.id.iv_post_upvote)
            downvote = itemView.findViewById(R.id.iv_post_downvote)
            upvoteSelected = itemView.findViewById(R.id.iv_post_upvote_selected)
            downvoteSelected = itemView.findViewById(R.id.iv_post_downvote_selected)
            mediaComment = itemView.findViewById(R.id.iv_post_comment_media)
            name = itemView.findViewById(R.id.tv_post_group_name)
            postUsername = itemView.findViewById(R.id.tv_post_username)
            voteCount = itemView.findViewById(R.id.tv_post_vote_count)

            newsCard = itemView.findViewById(R.id.cv_post_news_card)
            newsHeadline = itemView.findViewById(R.id.tv_post_headline)
            newsSource = itemView.findViewById(R.id.tv_post_news_source)
            newsPicture = itemView.findViewById(R.id.iv_post_news_image)
            mediaContent = itemView.findViewById(R.id.iv_media_content_picture)

            newsLayout = itemView.findViewById(R.id.news_content_layout)
            mediaLayout = itemView.findViewById(R.id.media_content_layout)
            contentLayout = itemView.findViewById(R.id.content_post_layout)
            websiteIcon = itemView.findViewById(R.id.iv_post_news_icon)
            timePosted= itemView.findViewById(R.id.tv_post_time_passed)
            numberOfComments= itemView.findViewById(R.id.tv_post_number_of_comments)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return  if (position == 0){
            0
        }
        else{
            1
        }
    }

    private fun likedCommentSendNotification(
        receiverUid: String,
        username: String?,
        likedCommentId: String,
        groupId: String,
        postId: String
    ){
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")

        val query = ref.orderByKey().equalTo(receiverUid)

        query.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children){
                    val token: Token? = dataSnapshot.getValue(Token::class.java)

                    if (receiverUid != currentUser) {
                        FirebaseDatabase.getInstance().reference.child("Users").child(currentUser)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(error: DatabaseError) {}
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val anonymous =
                                        snapshot.child("anonymous").getValue(String::class.java)
                                    if (anonymous != null) {
                                        val data: Data
                                        if (anonymous == "ON") data = Data(currentUser, R.mipmap.squabble_icon, "Anonymous liked your comment", "New Message", receiverUid, postId, groupId, likedCommentId)
                                        else data = Data(currentUser, R.mipmap.squabble_icon, "$username liked your comment", "New Message", receiverUid, postId, groupId, likedCommentId)

                                        val sender = Sender(data, token!!.getToken().toString())

                                        apiService!!.sendNotificaiton(sender).enqueue(object : Callback<MyResponse> {
                                            override fun onResponse(call: Call<MyResponse>, response: Response<MyResponse>) {
                                                if (response.code() == 200){
                                                    if (response.body()!!.success != 1){
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
            }
        })
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