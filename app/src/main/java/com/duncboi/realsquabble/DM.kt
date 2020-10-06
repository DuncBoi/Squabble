package com.duncboi.realsquabble

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duncboi.realsquabble.messenger.Chat
import com.duncboi.realsquabble.messenger.ChatsAdapter
import com.duncboi.realsquabble.messenger.Users
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_d_m.*

class DM : Fragment() {

    val args: DMArgs by navArgs()
    var uid: String = ""
    var currentUser: FirebaseUser? = null
    var chatsAdapter: ChatsAdapter? = null
    var chatList: List<Chat>? = null
    lateinit var dmRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_d_m, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iv_dm_info.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("uid", "${args.uid}")
            bundle.putString("type", "dm")
            findNavController().navigate(R.id.action_DM_to_otherProfile, bundle)
        }

        uid = args.uid
        Log.d("Moose", "$uid")
        currentUser = FirebaseAuth.getInstance().currentUser

        dmRecyclerView = view.findViewById(R.id.rv_dm_recycler)
        dmRecyclerView.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.stackFromEnd = true
        dmRecyclerView.layoutManager = linearLayoutManager

        val reference = FirebaseDatabase.getInstance().reference.child("Users").child(uid)
        reference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user: Users? = snapshot.getValue(Users::class.java)
                if (user!!.getName() == ""){
                    Log.d("Moose", "name null")
                    tv_dm_name.text = "@${user!!.getUsername()}"
                    tv_dm_name.setTextColor(Color.parseColor("#bf55ec"))
                }
                else{
                    tv_dm_name.text = "${user.getName()}"
                    tv_dm_name.setTextColor(Color.parseColor("#8a000000"))
                }
                if (user.getStatus() != ""){
                    if (user.getStatus() == "ONLINE"){
                        tv_dm_status.text = "Online"
                        tv_dm_status.setTextColor(Color.parseColor("#38c96d"))
                    }
                    else{
                        tv_dm_status.text = "Offline"
                        tv_dm_status.setTextColor(Color.parseColor("#8a000000"))
                    }
                }
                if (user.getUri() != "") {
                    Picasso.get().load(user.getUri()).into(civ_dm_profile_pic)
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

        })

        tv_dm_send_message.setOnClickListener {
            val message = et_dm_message.text.toString().trim()

            if (message != ""){
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
            findNavController().navigate(R.id.action_DM_to_directMessanger)
        }
    }

    private fun retrieveMessages(senderId: String, receiverId: String, receiverUser: Users?) {
        chatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Direct Messages")
        reference.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                (chatList as ArrayList<Chat>).clear()
                for (snapshot1 in snapshot.children){
                    val chat = snapshot1.getValue(Chat::class.java)
                    Log.d("whale", "${chat!!.getMessage()}")
                    if(chat!!.getReceiver().equals(senderId) && chat.getSender().equals(receiverId) || chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId)){
                        (chatList as ArrayList<Chat>).add(chat)
                    }
                    chatsAdapter = activity?.let { ChatsAdapter(it, (chatList as ArrayList<Chat>), receiverUser!!) }
                    dmRecyclerView.adapter = chatsAdapter
                }
            }
        })
    }

    private fun sendMessage(senderUid: String, receiverUid: String, message: String){
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderUid
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverUid
        messageHashMap["isseen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey
        reference.child("Direct Messages").child(messageKey!!).setValue(messageHashMap).addOnCompleteListener { task ->
            if (task.isSuccessful){
                val chatsListReference = FirebaseDatabase.getInstance().reference.child("ChatList").child(currentUser!!.uid).child(uid)
                chatsListReference.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()){
                            chatsListReference.child("id").setValue(uid)
                        }
                        val chatsListReceiverReference = FirebaseDatabase.getInstance().reference.child("Direct Messages").child(currentUser!!.uid).child(uid)

                        chatsListReference.child("id").setValue(currentUser!!.uid)
                    }

                })

                val reference = FirebaseDatabase.getInstance().reference.child("Users").child(uid).child(currentUser!!.uid)


                //implement notifications


            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data!!.data != null){
            //progress dialog

            val fileUri = data.data
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
                    val downloadUri = task.result
                    val url = downloadUri.toString()
                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = currentUser!!.uid
                    messageHashMap["message"] = "sent you an image."
                    messageHashMap["receiver"] = args.uid
                    messageHashMap["isseen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageId"] = messageId

                    ref.child("Direct Messages").child(messageId!!).setValue(messageHashMap)
                }
            }
        }
    }



}