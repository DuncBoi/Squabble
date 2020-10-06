package com.duncboi.realsquabble

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.webkit.URLUtil
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.raqun.beaverlib.Beaver
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_create_post.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.MalformedURLException
import java.net.URL


class CreatePost : Fragment() {

//    lateinit var mService: NewsService
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    private val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser)
    private var userListener: ValueEventListener?  = null
    private val groupRef = FirebaseDatabase.getInstance().reference.child("Group Chat List")
    private var groupListener: ValueEventListener? = null
    private var url: String = ""
    private var groupId: String? = ""

    override fun onPause() {
        super.onPause()
        if (userListener != null) userRef.removeEventListener(userListener!!)
        if (groupListener != null) groupRef.removeEventListener(groupListener!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userListener = userRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                groupId = snapshot.child("groupId").getValue(String::class.java)
                if (groupId != "") {
                    groupListener = groupRef.addValueEventListener(object : ValueEventListener{
                        override fun onCancelled(error: DatabaseError) {}
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val name = snapshot.child(groupId!!).child("name").getValue(String::class.java)
                            val uri = snapshot.child(groupId!!).child("uri").getValue(String::class.java)
                            Picasso.get().load(uri).into(civ_create_post_group_profile)
                            tv_create_post_group_name.text = name
                        }

                    })
                }
                else{
                    //cannot post
                }
            }

        })

        if (!Beaver.isInitialized()) {
            activity?.let { Beaver.build(it) }
        }

        //mService = Constants.newsService
        rb_create_post_news.setOnClickListener{
            create_post_media_layout.visibility = View.GONE
            create_post_news_layout.visibility = View.VISIBLE
        }

        rb_create_post_media.setOnClickListener{
            create_post_media_layout.visibility = View.VISIBLE
            create_post_news_layout.visibility = View.GONE
        }

        iv_create_post_media_image.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Pick Image"), 438)
        }

        et_create_post_link.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val text = p0.toString().trim()
                if (isValidUrl(text)) {
                    retrieveMetaData(text)
                }
                else{
                    create_post_preview_news_constraint.visibility = View.GONE
                }
            }
        })

        et_create_post_decription.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                val text = p0.toString().trim()

                tv_create_post_error.text = ""
                et_create_post_decription.setBackgroundResource(R.drawable.rounded_edittext_register_login)

                et_create_post_decription.setBackgroundResource(R.drawable.rounded_edittext_register_login)
                val textLength = text.length
                val counter = 150 - textLength
                tv_create_post_word_counter.text = "$counter"
                if (counter < 0){
                    tv_create_post_error.text = "Post body must be less than 150 words"
                    tv_create_post_create.text = ""
                    tv_create_post_word_counter.setTextColor(Color.parseColor("#eb4b4b"))
                    et_create_post_decription.setBackgroundResource(R.drawable.error_edittext_register_login)
                }
                else if (counter == 150){
                    tv_create_post_error.text = "Please include a post body"
                    tv_create_post_create.text = ""
                    tv_create_post_word_counter.setTextColor(Color.parseColor("#eb4b4b"))
                    et_create_post_decription.setBackgroundResource(R.drawable.error_edittext_register_login)
                }
                else{
                    tv_create_post_create.text = "Post"
                    tv_create_post_word_counter.setTextColor(Color.parseColor("#38c96d"))
                }

            }

        })


        tv_create_post_create.setOnClickListener {

            CoroutineScope(Main).launch {
                pb_create_post_posting.bringToFront()
                pb_create_post_posting.visibility = View.VISIBLE
                delay(500)
                if (et_create_post_decription.text.toString().trim().isNotEmpty()) {
                    if (et_create_post_decription.text.toString().trim().length <= 150) {
                        if(groupId != "") {
                            var type = ""
                            val description = et_create_post_decription.text.toString().trim()
                            var newsLink = ""
                            var mediaLink = ""
                            val timeOfPost = System.currentTimeMillis()
                            if (rb_create_post_media.isChecked) {
                                type = "media"
                                mediaLink = url
                            } else if (rb_create_post_news.isChecked) {
                                type = "news"
                                newsLink = et_create_post_link.text.toString().trim()
                            }

                            val key =
                                FirebaseDatabase.getInstance().reference.child("Posts").push().key

                            val postHash = HashMap<String, Any?>()
                            postHash["type"] = type
                            postHash["description"] = description
                            postHash["votes"] = 0
                            postHash["creator"] = currentUser
                            postHash["groupId"] = groupId
                            postHash["mediaPic"] = mediaLink
                            postHash["newsLink"] = newsLink
                            postHash["timeOfPost"] = "$timeOfPost"
                            postHash["id"] = key

                            val postsRef =
                                FirebaseDatabase.getInstance().reference.child("Posts")
                                    .child(groupId!!).child(key!!)
                            postsRef.setValue(postHash).addOnCompleteListener {
                                //next fragment
                                Toast.makeText(activity, "Posted", Toast.LENGTH_SHORT).show()
                                pb_create_post_posting.visibility = View.GONE
                                findNavController().navigate(R.id.action_createPost_to_groupHolder)
                            }
                        }
                        else{
                            //cannot post
                        }
                    } else {
                        pb_create_post_posting.visibility = View.GONE
                        tv_create_post_error.text = "Post body too long"
                        et_create_post_decription.setBackgroundResource(R.drawable.error_edittext_register_login)
                    }
                } else {
                    pb_create_post_posting.visibility = View.GONE
                    tv_create_post_error.text = "Please include a post body"
                    et_create_post_decription.setBackgroundResource(R.drawable.error_edittext_register_login)
                }
            }
        }

    }

    private fun retrieveMetaData(url: String){
        CoroutineScope(Main).launch{
            val metaData = Beaver.load(url).await()
            if (metaData!= null){

                create_post_preview_news_constraint.visibility = View.VISIBLE
                pb_create_post_news_progress.visibility = View.VISIBLE
                news_content_layout.visibility = View.GONE
                tv_create_post_preview_not_available.visibility = View.GONE
                Picasso.get().load(metaData.imageUrl).resize(75,75).into(iv_create_post_news_image)
                Picasso.get().load(metaData.favIcon).resize(15, 15).into(iv_create_post_news_icon)
                tv_create_post_headline.text = metaData.title
                tv_create_post_news_source.text = "- ${metaData.name}"
                delay(1000)
                news_content_layout.visibility = View.VISIBLE
                pb_create_post_news_progress.visibility = View.GONE


            }
            else{
                news_content_layout.visibility = View.GONE
                tv_create_post_preview_not_available.visibility = View.VISIBLE
            }
        }
    }

//    private fun searchNews(searchUrl: String){
//
//        mService.sources.enqueue(object : Callback<WebSite> {
//            override fun onFailure(call: Call<WebSite>, t: Throwable) {}
//
//            override fun onResponse(call: Call<WebSite>, response: Response<WebSite>) {
//                val sources = response.body()?.sources
//                if (sources != null) {
//                    for (i in sources) {
//                        val newsSource = Constants.getNewsApi(i.id!!)
//
//                        mService.getNewsFromSource(newsSource).enqueue(object : Callback<News> {
//                            override fun onFailure(call: Call<News>, t: Throwable) {}
//                            override fun onResponse(call: Call<News>, response: Response<News>) {
//
//                                val articles = response.body()?.articles
//                                if (articles != null) {
//                                    for (i in articles) {
//                                        val url = i.url
//                                        if (searchUrl == url){
//                                            //show preview
//                                            Log.d("News", "Match")
//                                            Picasso.get().load(i.urlToImage).resize(75,75).into(iv_create_post_news_image)
//                                            tv_create_post_headline.text = i.title
//                                            tv_create_post_news_source.text = i.author
//                                        }
//                                        else{
//                                            Log.d("News", "No Match")
//
//                                        }
//                                    }
//                                }
//
//                            }
//
//                        })
//                    }
//                }sddwd
//            }
//
//        })
//    }

    private fun isValidUrl(urlString: String): Boolean {
        try {
            val url = URL(urlString)
            return URLUtil.isValidUrl(url.toString()) && Patterns.WEB_URL.matcher(url.toString()).matches()
        } catch (e: MalformedURLException) {
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        tv_create_post_create.isClickable = false

        if (requestCode == 438 && resultCode == Activity.RESULT_OK && data != null && data.data != null){
            //progress dialog

            pb_create_post_media_progress.visibility = View.VISIBLE
            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Post Media")
            val filePath = storageReference.child("$currentUser.jpg")

            val uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful){
                    task.exception?.let {
                        throw it
                    }}
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    tv_create_post_select_media.visibility = View.INVISIBLE
                    val downloadUri = task.result
                    url = downloadUri.toString()
                    Picasso.get().load(url).resize(150, 175).into(iv_create_post_media_image)
                    pb_create_post_media_progress.visibility = View.GONE
                    tv_create_post_create.isClickable = true
                }
                else{
                    tv_create_post_create.isClickable = true
                }
            }.addOnFailureListener {
                tv_create_post_create.isClickable = true
            }
        }
    }

    fun setMargins(v: View, l: Int, t: Int, r: Int, b: Int) {
        if (v.layoutParams is MarginLayoutParams) {
            val p = v.layoutParams as MarginLayoutParams
            p.setMargins(l, t, r, b)
            v.requestLayout()
        }
    }


}