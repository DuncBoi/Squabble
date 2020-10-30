package com.duncboi.realsquabble.HolderClass

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.Target
import com.duncboi.realsquabble.ModelClasses.Chat
import com.duncboi.realsquabble.Adapters.ChatsAdapter
import com.duncboi.realsquabble.ModelClasses.Data
import com.duncboi.realsquabble.ModelClasses.Users
import com.duncboi.realsquabble.R
import com.duncboi.realsquabble.notifications.*
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_d_m.*
import kotlinx.android.synthetic.main.fragment_group.*
import kotlinx.android.synthetic.main.fragment_topic_description.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DM : Fragment() {

    val args: DMArgs by navArgs()
    var uid: String = ""
    var currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    var chatsAdapter: ChatsAdapter? = null
    var chatList: List<Chat>? = null
    lateinit var dmRecyclerView: RecyclerView

    var apiService: APIService? = null
    var notify = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseDatabase.getInstance().reference.child("ChatList").child(currentUser!!.uid).child(args.uid).child("isSeen").setValue(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_d_m, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)

        uid = args.uid

        dmRecyclerView = view.findViewById(R.id.rv_dm_recycler)
        dmRecyclerView.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.stackFromEnd = true
        dmRecyclerView.layoutManager = linearLayoutManager

        CoroutineScope(Main).launch {
            delay(300)
            val reference = FirebaseDatabase.getInstance().reference.child("Users").child(uid)
            reference.onDisconnect().cancel()
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val user: Users? = snapshot.getValue(Users::class.java)
                    if (user != null && tv_dm_name != null) {
                        if (user.getAnonymous() == "ON") {
                            tv_dm_name.text = "Anonymous"
                            imageView22.setOnClickListener {
                                Toast.makeText(activity, "Anonymous", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            if (user.getName() == "") {
                                Log.d("Moose", "name null")
                                tv_dm_name.text = "@${user.getUsername()}"
                            } else {
                                tv_dm_name.text = "${user.getName()}"
                                tv_dm_name.setTextColor(Color.parseColor("#8a000000"))
                            }
                            if (user.getUri() != "") {
                                Picasso.get().load(user.getUri()).into(civ_dm_profile_pic)
                            }
                            imageView22.setOnClickListener {
                                val bundle = Bundle()
                                bundle.putString("uid", "${args.uid}")
                                bundle.putString("type", "dm")
                                findNavController().navigate(R.id.action_DM_to_otherProfile, bundle)
                            }
                        }

                        if (user.getStatus() != "") {
                            if (user.getStatus() == "ONLINE") {
                                tv_dm_status.text = "Online"
                                tv_dm_status.setTextColor(Color.parseColor("#0D7B39"))
                            } else {
                                tv_dm_status.text = "Offline"
                                tv_dm_status.setTextColor(Color.parseColor("#8a000000"))
                            }
                        }

                        when (user.getCategory()) {
                            "Moderate" -> iv_dm_banner.setImageResource(R.drawable.moderate_banner)
                            "Conservative" -> iv_dm_banner.setImageResource(R.drawable.conservative_banner)
                            "Liberal" -> iv_dm_banner.setImageResource(R.drawable.ic_liberal_banner)
                            "Libertarian" -> iv_dm_banner.setImageResource(R.drawable.libertarian_banner)
                            "Authoritarian" -> iv_dm_banner.setImageResource(R.drawable.authoritarian_banner)
                        }

                        retrieveMessages(currentUser!!.uid, uid, user)
                    }
                }

            })

            tv_dm_send_message.setOnClickListener {
                notify = true
                val message = et_dm_message.text.toString().trim()

                if (message != "") {
                    sendMessage(currentUser!!.uid, uid, message)
                }
                et_dm_message.setText("")
            }

            iv_dm_picture.setOnClickListener {
                val intent = Intent()
                intent.action = Intent.ACTION_GET_CONTENT
                intent.type = "image/*"
                startActivityForResult(Intent.createChooser(intent, "Pick Image"), 438)

            }

            iv_dm_back.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    private fun retrieveMessages(senderId: String, receiverId: String, receiverUser: Users?) {
        chatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Direct Messages")
        reference.onDisconnect().cancel()
        reference.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                (chatList as ArrayList<Chat>).clear()
                for (snapshot1 in snapshot.children){
                    val chat = snapshot1.getValue(Chat::class.java)
                    if(chat!!.getReceiver().equals(senderId) && chat.getSender().equals(receiverId) || chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId)){
                        (chatList as ArrayList<Chat>).add(chat)
                    }
                    if (pb_dm_progress != null) pb_dm_progress.visibility = View.GONE
                    chatsAdapter = activity?.let { ChatsAdapter(it, (chatList as ArrayList<Chat>), receiverUser!!) }
                    dmRecyclerView.adapter = chatsAdapter
                }
            }
        })
    }

    private fun sendMessage(senderUid: String, receiverUid: String, message: String){
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key
        val time = System.currentTimeMillis()

        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderUid
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverUid
        messageHashMap["isseen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey
        messageHashMap["timeOfMessage"] = "$time"
        messageHashMap["uri"] = ""

        reference.child("Direct Messages").child(messageKey!!).setValue(messageHashMap).addOnCompleteListener { task ->
            if (task.isSuccessful){
                val ReceiverHash = HashMap<String, Any?>()
                ReceiverHash["timeOfMessage"] = "$time"
                ReceiverHash["last message"] = message
                ReceiverHash["isSeen"] = false
                val SenderHash = HashMap<String, Any?>()
                SenderHash["timeOfMessage"] = "$time"
                SenderHash["last message"] = "Delivered"
                FirebaseDatabase.getInstance().reference.child("ChatList").child(uid).child(currentUser!!.uid).setValue(ReceiverHash)
                FirebaseDatabase.getInstance().reference.child("ChatList").child(currentUser!!.uid).child(uid).setValue(SenderHash)
            }
        }

        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser!!.uid)
        userRef.onDisconnect().cancel()
        userRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)
                if (notify == true){
                    if (user != null) {
                        sendNotification(receiverUid, user.getUsername(), message, "")
                    }
                }
                notify = false
            }
        })

    }

    private fun sendNotification(receiverUid: String, username: String?, message: String, groupId: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")

        val query = ref.orderByKey().equalTo(receiverUid)

        val key = ref.push().key
        if (receiverUid != currentUser!!.uid) {
            query.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot in snapshot.children) {
                        val token: Token? = dataSnapshot.getValue(Token::class.java)

                        FirebaseDatabase.getInstance().reference.child("Users")
                            .child(currentUser!!.uid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(error: DatabaseError) {}
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val anonymous =
                                        snapshot.child("anonymous").getValue(String::class.java)
                                    if (anonymous != null) {
                                        val data: Data
                                        if (anonymous == "ON") {
                                            data = Data(
                                                currentUser!!.uid,
                                                R.mipmap.squabble_icon,
                                                "Anonymous sent you a message",
                                                "New Message",
                                                receiverUid,
                                                key!!,
                                                groupId,
                                                ""
                                            )
                                        } else {
                                            data = Data(
                                                currentUser!!.uid,
                                                R.mipmap.squabble_icon,
                                                "$username sent you a message",
                                                "New Message",
                                                receiverUid,
                                                key!!,
                                                groupId,
                                                ""
                                            )
                                        }
                                        val sender = Sender(data, token!!.getToken().toString())

                                        apiService!!.sendNotificaiton(sender)
                                            .enqueue(object : Callback<MyResponse> {
                                                override fun onResponse(
                                                    call: Call<MyResponse>,
                                                    response: Response<MyResponse>
                                                ) {
                                                    if (response.code() == 200) {
                                                        if (response.body()!!.success != 1) {
                                                            Log.d(
                                                                "Notification",
                                                                "failed notification"
                                                            )
                                                        }
                                                    }
                                                }

                                                override fun onFailure(
                                                    call: Call<MyResponse>,
                                                    t: Throwable
                                                ) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.data != null){
            //progress dialog

            val fileUri = data.data
            d_m_confirm_image_constraint.visibility = View.VISIBLE
            pb_dm_progress.visibility = View.VISIBLE

            Glide.with(requireContext())
                .load(fileUri)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .listener(object : com.bumptech.glide.request.RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        pb_dm_progress.visibility = View.GONE
                        return false
                    }
                })
                .into(d_m_confirm_image_image)

            dm_confirm_image_back.setOnClickListener {
                d_m_confirm_image_constraint.visibility = View.GONE
            }

            dm_confirm_image_send.setOnClickListener {
                d_m_confirm_image_constraint.visibility = View.GONE
                pb_dm_progress.visibility = View.VISIBLE

                val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
                val ref = FirebaseDatabase.getInstance().reference
                val messageId = ref.push().key
                val filePath = storageReference.child("$messageId.jpg")

                var uploadTask: StorageTask<*>
                uploadTask = filePath.putFile(fileUri!!)

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful){
                        task.exception?.let {
                            throw it
                        }}
                    return@Continuation filePath.downloadUrl
                }).addOnCompleteListener {task ->
                    if (task.isSuccessful){
                        val time = System.currentTimeMillis()
                        val downloadUri = task.result
                        val url = downloadUri.toString()
                        val messageHashMap = HashMap<String, Any?>()
                        messageHashMap["sender"] = currentUser!!.uid
                        messageHashMap["message"] = ""
                        messageHashMap["receiver"] = args.uid
                        messageHashMap["isseen"] = false
                        messageHashMap["url"] = url
                        messageHashMap["messageId"] = messageId
                        messageHashMap["timeOfMessage"] = "$time"

                        ref.child("Direct Messages").child(messageId!!).setValue(messageHashMap).addOnCompleteListener {
                            if (task.isSuccessful) {
                                val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser!!.uid)
                                pb_dm_progress.visibility = View.GONE

                                userRef.addListenerForSingleValueEvent(object : ValueEventListener{
                                    override fun onCancelled(error: DatabaseError) {}
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val user = snapshot.getValue(Users::class.java)
                                        if (notify == true){
                                            if (user != null) {
                                                sendNotification(args.uid, user.getUsername(), "sent you an image.", "")
                                            }
                                        }
                                        notify = false
                                    }
                                })
                                val ReceiverHash = HashMap<String, Any?>()
                                ReceiverHash["timeOfMessage"] = "$time"
                                ReceiverHash["last message"] = "Sent you an image"
                                ReceiverHash["isSeen"] = false
                                val SenderHash = HashMap<String, Any?>()
                                SenderHash["timeOfMessage"] = "$time"
                                SenderHash["last message"] = "Delivered"
                                FirebaseDatabase.getInstance().reference.child("ChatList").child(uid)
                                    .child(currentUser!!.uid).setValue(ReceiverHash)
                                FirebaseDatabase.getInstance().reference.child("ChatList")
                                    .child(currentUser!!.uid).child(uid).setValue(SenderHash)
                            }
                        }
                    }
                }
            }
        }
    }
}