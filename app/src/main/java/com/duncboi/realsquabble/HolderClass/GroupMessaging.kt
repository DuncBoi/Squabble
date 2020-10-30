package com.duncboi.realsquabble.HolderClass

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duncboi.realsquabble.ModelClasses.Chat
import com.duncboi.realsquabble.Adapters.GroupChatAdapter
import com.duncboi.realsquabble.ModelClasses.Users
import com.duncboi.realsquabble.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_group_messaging.*

class GroupMessaging : Fragment() {

    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    var chatsAdapter: GroupChatAdapter? = null
    var chatList: List<Chat>? = null
    lateinit var topicRecycler: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_messaging, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        topicRecycler = view.findViewById(R.id.group_messaging_recycler)
        topicRecycler.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.stackFromEnd = true
        topicRecycler.layoutManager = linearLayoutManager

        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser)
        userRef.onDisconnect().cancel()
            userRef.addListenerForSingleValueEvent(object : ValueEventListener{
           override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)
                val groupId = user?.getGroupId()
                if (groupId != null && et_group_messaging_message != null) {
                    if (groupId != "") {
                        retrieveMessages(groupId)
                        b_group_messaging_send.setOnClickListener {
                            val message = et_group_messaging_message.text.toString().trim()

                            if (message != "") {
                                sendMessage(message, groupId)
                            }
                            et_group_messaging_message.setText("")
                        }
                    }
                    else{
                        tv_group_messaging_do_not_belong_to_group.visibility = View.VISIBLE
                        b_group_messaging_create_group.visibility = View.VISIBLE
                        b_group_messaging_find_group.visibility = View.VISIBLE

                        b_group_messaging_find_group.setOnClickListener {
                            findNavController().navigate(R.id.action_groupHolder_to_findGroup)
                        }

                        b_group_messaging_create_group.setOnClickListener {
                            findNavController().navigate(R.id.action_groupHolder_to_createGroup)
                        }
                    }
                }
            }
        })
    }

    private fun sendMessage(message: String, groupId: String){
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key
        val time = System.currentTimeMillis()
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser)
        userRef.onDisconnect().cancel()
            userRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)

                val messageHashMap = HashMap<String, Any?>()
                messageHashMap["sender"] = currentUser
                messageHashMap["message"] = message
                messageHashMap["receiver"] = groupId
                messageHashMap["isseen"] = false
                messageHashMap["url"] = ""
                messageHashMap["messageId"] = messageKey
                messageHashMap["timeOfMessage"] = "$time"
                if (user != null) {
                    messageHashMap["uri"] = "${user.getUri()}"
                    messageHashMap["senderName"] = user.getName()
                    messageHashMap["senderUsername"] = user.getUsername()
                }

                reference.child("Group Messaging").child(groupId).child(messageKey!!).setValue(messageHashMap)
            }
        })
    }


    private fun retrieveMessages(groupId: String) {
        chatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Group Messaging").child(groupId)
        reference.onDisconnect().cancel()
        reference.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                (chatList as ArrayList<Chat>).clear()
                for (i in snapshot.children){
                    val chat = i.getValue(Chat::class.java)
                    val sender = chat?.getSender()
                    if (sender != null) {
                        (chatList as ArrayList<Chat>).add(chat)
                    }
                }
                chatsAdapter = activity?.let { GroupChatAdapter(it, ((chatList as ArrayList<Chat>))) }
                topicRecycler.adapter = chatsAdapter
            }
        })
    }

}