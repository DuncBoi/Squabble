package com.duncboi.realsquabble.HolderClass

import android.app.AlertDialog
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.duncboi.realsquabble.ModelClasses.Queue
import com.duncboi.realsquabble.ModelClasses.Topics
import com.duncboi.realsquabble.ModelClasses.Users
import com.duncboi.realsquabble.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.opentok.android.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_actual_video.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONException
import pub.devrel.easypermissions.EasyPermissions

class ActualVideo : Fragment(), Session.SessionListener, PublisherKit.PublisherListener{

    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    private var usersOnline = -1
    private var usersAgree = -1
    private var usersDisagree = -1
    private val RC_VIDEO_APP_PERMISSION = 124
    private var job: Job? = null
    private var debateRef: DatabaseReference? = null
    private var otherUserUid = ""

    private val anonymousRef = FirebaseDatabase.getInstance().reference.child("Users").child(
        currentUser
    ).child("anonymous")

    private val topicRef = FirebaseDatabase.getInstance().reference.child("Queue")
    private var topicListener: ValueEventListener? = null

    private val args: ActualVideoArgs by navArgs()
    private var API_KEY = 46930154
    lateinit var SESSION_ID: String
    lateinit var connecting: androidx.appcompat.app.AlertDialog
    private var subscriber: Subscriber? = null
    private var session: Session? = null
    private var publisher: Publisher? = null
    private var removeCode = ""

    private var agreeList: List<Queue>? = null
    private var disagreeList: List<Queue>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_actual_video, container, false)
    }

    override fun onStop() {
        super.onStop()
        FirebaseDatabase.getInstance().reference.child("Queue").child(args.topicId).child(
            currentUser
        ).removeValue()
        FirebaseDatabase.getInstance().reference.child("Debating").child(removeCode).removeValue()
        if (topicListener != null) topicRef.child(args.topicId).removeEventListener(topicListener!!)
        if (subscriber != null) {
            session?.unpublish(publisher)
            session?.unsubscribe(subscriber)
            subscriber = null
            publisher = null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        connecting = connecting()
        agreeList = ArrayList()
        disagreeList = ArrayList()

        s_queue_anonymous.setOnClickListener{
            anonymousLogic2()
        }

        val perms = arrayOf(
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )
        requestPermissions(perms, RC_VIDEO_APP_PERMISSION)

        val topicIdRef = FirebaseDatabase.getInstance().reference.child("Topics").child(args.topicId)
        topicIdRef.onDisconnect().cancel()
            topicIdRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    val topic = snapshot.getValue(Topics::class.java)
                    if (topic != null && tv_actual_video_headline != null) {
                        tv_actual_video_headline.text = topic.getHeadline()
                        tv_actual_video_question.text = "Question: ${topic.getQuestion()}"
                        if (args.host) tv_actual_video_answer.text =
                            "Your Answer: ${topic.getAnswer1()}"
                        else tv_actual_video_answer.text = "Your Answer: ${topic.getAnswer2()}"
                    }
                }
            })

        anonymousRef.onDisconnect().cancel()
        anonymousRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (s_queue_anonymous != null) {
                    val anonymous = snapshot.getValue<String>().toString()
                    if (anonymous == "ON") s_queue_anonymous.isChecked = true
                    else if (anonymous == "OFF") s_queue_anonymous.isChecked = false
                    else s_queue_anonymous.isChecked = false
                }
            }
        })

        b_actual_video_leave.setOnClickListener {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("Leave Call")
            builder.setMessage("Are you sure you want to leave the call?")
            builder.setPositiveButton("Yes") { _, _ ->
                val bundle = Bundle()
                bundle.putString("topic", args.topicId)
                findNavController().navigate(R.id.action_actualVideo_to_topicHolder, bundle)
            }
            builder.setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            builder.setCancelable(false)
            builder.create().show()
        }
    }

    private fun stuff(connecting: androidx.appcompat.app.AlertDialog) {
        val random = "${rand(0, 1000)}"
        val random2 = "${rand(0, Int.MAX_VALUE)}"
        val random3 = "${rand(0, 6969420)}"

        val currentUserRef = FirebaseDatabase.getInstance().reference.child("Queue").child(args.topicId).child(
            currentUser
        )
        val hash = HashMap<String, Any?>()
        hash["uid"] = currentUser
        hash["randomNumber"] = random
        hash["randomNumber2"] = random2
        hash["randomNumber3"] = random3
        hash["timeOfQueue"] = System.currentTimeMillis()
        hash["host"] = args.host

        currentUserRef.setValue(hash)
        currentUserRef.onDisconnect().removeValue()


        topicRef.child(args.topicId).onDisconnect().cancel()
         topicRef.child(args.topicId).addValueEventListener(object : ValueEventListener {
             override fun onCancelled(error: DatabaseError) {}
             override fun onDataChange(snapshot: DataSnapshot) {
                 if (tv_queue_online != null) {
                     usersOnline = 0
                     usersAgree = 0
                     usersDisagree = 0

                     for (i in snapshot.children) {
                         usersOnline++
                         if (i.child("host").getValue(Boolean::class.java) == true) {
                             usersAgree++
                         } else {
                             usersDisagree++
                         }
                     }

                     tv_queue_online.text = "$usersOnline"
                     tv_queue_agree.text = "$usersAgree"
                     tv_queue_disagree.text = "$usersDisagree"

                     (disagreeList as ArrayList<Queue>).clear()
                     (agreeList as ArrayList<Queue>).clear()

                     Log.d("Moose", "snap $snapshot")

                     for (i in snapshot.children) {
                         val queue = i.getValue(Queue::class.java)
                         if (queue != null) {
                             if (queue.getHost() == true) {
                                 if (!(agreeList as ArrayList<Queue>).contains(queue)) (agreeList as ArrayList<Queue>).add(
                                     queue
                                 )
                             } else {
                                 if (!(disagreeList as ArrayList<Queue>).contains(queue)) (disagreeList as ArrayList<Queue>).add(
                                     queue
                                 )
                             }
                         }
                     }
                     val orderedAgreeList = orderQueue((agreeList as ArrayList<Queue>))
                     val orderedDisagreeList = orderQueue((disagreeList as ArrayList<Queue>))

                     if (args.host == true) {
                         if (orderedAgreeList.isNotEmpty()) {
                             val currentQueue = orderedAgreeList.get(0)
                             if (currentQueue.getUid() == currentUser && orderedDisagreeList.isNotEmpty()) {
                                 //person found
                                 val queue = orderedDisagreeList.get(0)
                                 val queueRef = FirebaseDatabase.getInstance().reference.child(
                                     "Queue"
                                 ).child(args.topicId)
                                 val removeHash = HashMap<String, Any?>()
                                 removeHash[currentUser] = null
                                 removeHash[queue.getUid()!!] = null
                                 queueRef.updateChildren(removeHash)
                                 queueRef.removeEventListener(this)
                                 currentUserRef.removeValue().addOnCompleteListener {
                                     val otherRandom1 = queue.getRandomNumber()
                                     val otherRandom2 = queue.getRandomNumber2()
                                     val otherRandom3 = queue.getRandomNumber3()
                                     val code1 =
                                         Math.abs(
                                             queue.getUid()!!.compareTo(currentUser)
                                         )
                                     val code2 =
                                         (random2.toLong() + otherRandom2!!.toLong()).div(
                                             2
                                         )
                                     val code3 =
                                         (random.toLong() + otherRandom1!!.toLong()).div(
                                             2
                                         )
                                     val code4 =
                                         (random3.toLong() + otherRandom3!!.toLong()).div(
                                             2
                                         )

                                     val code = "$code1$code2$code3$code4"
                                     removeCode = "$code"
                                     otherUserUid = queue.getUid()!!
                                     sendToDebate(code, connecting)
                                 }
                             }
                         }
                     } else {
                         if (orderedDisagreeList.isNotEmpty()) {
                             val currentQueue = orderedDisagreeList.get(0)
                             if (currentQueue.getUid() == currentUser && orderedAgreeList.isNotEmpty()) {
                                 val queue = orderedAgreeList.get(0)
                                 val queueRef =
                                     FirebaseDatabase.getInstance().reference.child("Queue").child(
                                         args.topicId
                                     )
                                 val removeHash = HashMap<String, Any?>()
                                 removeHash[currentUser] = null
                                 removeHash[queue.getUid()!!] = null
                                 queueRef.updateChildren(removeHash)
                                 queueRef.removeEventListener(this)
                                 currentUserRef.removeValue()
                                     .addOnCompleteListener {
                                         val otherRandom1 =
                                             queue.getRandomNumber()
                                         val otherRandom2 =
                                             queue.getRandomNumber2()
                                         val otherRandom3 =
                                             queue.getRandomNumber3()
                                         val code1 = Math.abs(
                                             queue.getUid()!!
                                                 .compareTo(currentUser)
                                         )
                                         val code2 =
                                             (random2.toLong() + otherRandom2!!.toLong()).div(
                                                 2
                                             )
                                         val code3 =
                                             (random.toLong() + otherRandom1!!.toLong()).div(
                                                 2
                                             )
                                         val code4 =
                                             (random3.toLong() + otherRandom3!!.toLong()).div(
                                                 2
                                             )

                                         val code = "$code1$code2$code3$code4"
                                         otherUserUid = queue.getUid()!!
                                         removeCode = "$code"
                                         sendToDebate(code, connecting)
                                     }
                             }

                         }
                     }
                 }
             }

         })
    }

    private fun sendToDebate(code: String, connecting: androidx.appcompat.app.AlertDialog) {
        FirebaseDatabase.getInstance().reference.child("Debating")
            .child(code).child(currentUser).setValue(args.host)
            .addOnCompleteListener {
                connecting.show()
                job = CoroutineScope(Dispatchers.IO).launch {
                    debateRef =
                        FirebaseDatabase.getInstance().reference.child(
                            "Debating"
                        ).child(code)
                    debateRef!!.onDisconnect().removeValue()
                    debateRef!!.child("apiInfo").onDisconnect()
                        .cancel()
                    debateRef!!.child("apiInfo")
                        .addValueEventListener(object :
                            ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {}
                            override fun onDataChange(snapshot: DataSnapshot) {

                                val sessionId =
                                    snapshot.child("sessionId")
                                        .getValue(String::class.java)
                                val apiKey =
                                    snapshot.child("apiKey")
                                        .getValue(String::class.java)

                                if (apiKey != null && sessionId != null) {
                                    val reqQueue: RequestQueue =
                                        Volley.newRequestQueue(activity)
                                    reqQueue.add(JsonObjectRequest(
                                        Request.Method.GET,
                                        "https://squabb.herokuapp.com" + "/room/:${code}",
                                        null,
                                        { response ->
                                            try {
                                                Log.d(
                                                    "Moose",
                                                    "$response"
                                                )

                                                val token = response.getString("token")

                                                session = Session.Builder(
                                                    activity,
                                                    "$apiKey",
                                                    sessionId
                                                ).build()

                                                session!!.setSessionListener(
                                                    this@ActualVideo
                                                )
                                                session!!.connect(token)

                                            } catch (error: JSONException) {
                                                Log.d(
                                                    "Main",
                                                    "Web Service error: " + error.message
                                                )
                                            }
                                        },
                                        { error ->
                                            Toast.makeText(
                                                activity,
                                                "User Disconnected",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                            val bundle = Bundle()
                                            connecting.dismiss()
                                            bundle.putString("topic", args.topicId)
                                            findNavController().navigate(
                                                R.id.action_actualVideo_to_topicHolder,
                                                bundle
                                            )
                                        }
                                    ))
              }
                            }
                        })

                    if (args.host == true) {
                        val reqQueue: RequestQueue =
                            Volley.newRequestQueue(activity)
                        reqQueue.add(JsonObjectRequest(
                            Request.Method.GET,
                            "https://squabb.herokuapp.com" + "/room/:${code}",
                            null,
                            { response ->
                                try {
                                    Log.d(
                                        "Moose",
                                        "$response"
                                    )
                                    API_KEY =
                                        response!!.getInt(
                                            "apiKey"
                                        )
                                    SESSION_ID =
                                        response.getString(
                                            "sessionId"
                                        )
                                    val apiHash =
                                        HashMap<String, Any?>()
                                    apiHash["sessionId"] =
                                        SESSION_ID
                                    apiHash["apiKey"] =
                                        "$API_KEY"
                                    FirebaseDatabase.getInstance().reference.child(
                                        "Debating"
                                    ).child(code)
                                        .child("apiInfo")
                                        .setValue(
                                            apiHash
                                        )
                                } catch (error: JSONException) {
                                    Log.d(
                                        "Main",
                                        "Web Service error: " + error.message
                                    )
                                }
                            },
                            { error ->
                                Toast.makeText(
                                    activity,
                                    "User Disconnected",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                val bundle = Bundle()
                                connecting.dismiss()
                                bundle.putString("topic", args.topicId)
                                findNavController().navigate(
                                    R.id.action_actualVideo_to_topicHolder,
                                    bundle
                                )
                            }
                        ))
                    }


                    Log.d("ActualVideo", "Success")
                }
                Toast.makeText(
                    activity,
                    "Match Found",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun rand(start: Int, end: Int): Int {
        require(start <= end) { "Illegal Argument" }
        return (start..end).random()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, activity)
        requestPermissions()
    }

    private fun requestPermissions() {
        val perms = arrayOf(
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )
        if (EasyPermissions.hasPermissions(activity, *perms)) {
            stuff(connecting)
        } else {
            val bundle = Bundle()
            bundle.putString("topic", args.topicId)
            findNavController().navigate(R.id.action_actualVideo_to_topicHolder, bundle)
            Toast.makeText(
                activity,
                "Video and audio permissions are required to join a live call",
                Toast.LENGTH_LONG
            ).show()
            activity?.let { EasyPermissions.requestPermissions(
                it,
                "Camera and Microphone are required in order to join a live video chat",
                RC_VIDEO_APP_PERMISSION
            )
            }
        }
    }

    private fun showUserStats(uid: String){
        val uidRef = FirebaseDatabase.getInstance().reference.child("Users").child(uid)
        uidRef.onDisconnect().cancel()
            uidRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    val otherUser = snapshot.getValue(Users::class.java)
                    if (iv_actual_video_current_user_screen != null) {
                        if (otherUser != null) {
                            if (otherUser.getAnonymous() != "ON") {
                                if (otherUser.getUri() != "" && otherUser.getUri() != null) {
                                    Picasso.get().load(otherUser.getUri())
                                        .into(civ_actual_video_profile)
                                }
                                if (otherUser.getName() != "") tv_actual_video_username.text =
                                    otherUser.getName()
                                else tv_actual_video_username.text = otherUser.getUsername()
                                when (otherUser.getCategory()) {
                                    "Moderate" -> iv_actual_video_banner.setImageResource(R.drawable.moderate_banner)
                                    "Conservative" -> iv_actual_video_banner.setImageResource(R.drawable.conservative_banner)
                                    "Liberal" -> iv_actual_video_banner.setImageResource(R.drawable.ic_liberal_banner)
                                    "Libertarian" -> iv_actual_video_banner.setImageResource(R.drawable.libertarian_banner)
                                    "Authoritarian" -> iv_actual_video_banner.setImageResource(R.drawable.authoritarian_banner)
                                }
                            }
                            else{
                                tv_actual_video_username.text = "Anonymous"
                            }
                        }
                    }
                }

            })
    }

    private fun connecting(): androidx.appcompat.app.AlertDialog {
        val connecting = activity?.let { androidx.appcompat.app.AlertDialog.Builder(it) }
        connecting!!.setView(R.layout.connecting)
        connecting.setCancelable(false)
        return connecting.create()
    }
        override fun onStreamDropped(p0: Session?, p1: Stream?) {
            Log.d("Status", "Stream Dropped")
            debateRef?.removeValue()
            FirebaseDatabase.getInstance().reference.child("Queue").child(args.topicId).child(
                currentUser
            ).removeValue()
            if (actual_video_other_user_screen != null) {
                actual_video_searching_constraint.visibility = View.VISIBLE
                actual_video_queue_stats_constraint.visibility = View.VISIBLE
                if (subscriber != null) {
                    session!!.unsubscribe(subscriber)
                    session!!.unpublish(publisher)
                    subscriber = null
                    publisher = null
                    actual_video_other_user_screen.removeAllViews()
                    iv_actual_video_current_user_screen.removeAllViews()
                    val bundle = Bundle()
                    bundle.putString("topic", args.topicId)
                    findNavController().navigate(R.id.action_actualVideo_to_topicHolder, bundle)
                }
                Toast.makeText(activity, "Session Disconnected", Toast.LENGTH_LONG).show()
            }
                }

                override fun onStreamReceived(session: Session?, p1: Stream?) {
                    connecting.cancel()
                    if (actual_video_other_user_screen != null) {
                        actual_video_queue_stats_constraint.visibility = View.GONE
                        actual_video_searching_constraint.visibility = View.GONE
                        showUserStats(otherUserUid)
                        Log.d("Status", "Stream Received")
                        if (subscriber == null) {
                            subscriber = Subscriber.Builder(activity, p1).build()
                            session?.subscribe(subscriber)
                            actual_video_other_user_screen.addView(subscriber?.view)
                        }
                    }
                }

                override fun onConnected(session: Session?) {
                    Log.d("Status", "Connected")
                    publisher = Publisher.Builder(activity).build()
                    publisher!!.setPublisherListener(this)
                    if (iv_actual_video_current_user_screen != null) {
                        iv_actual_video_current_user_screen.addView(publisher!!.view)
                        if (publisher!!.view is GLSurfaceView) {
                            (publisher!!.view as GLSurfaceView).setZOrderOnTop(true)
                        }
                        session?.publish(publisher)
                    }
                }

                override fun onDisconnected(p0: Session?) {
                    Log.d("Status", "Disconnected")
                }

                override fun onError(p0: Session?, p1: OpentokError?) {
                    Log.d("Status", "Error")
                }

                override fun onStreamCreated(p0: PublisherKit?, p1: Stream?) {
                    Log.d("Status", "Stream Created")

                }

                override fun onStreamDestroyed(p0: PublisherKit?, p1: Stream?) {
                }

                override fun onError(p0: PublisherKit?, p1: OpentokError?) {
                    Log.d("Status", "Error")
                }

    private fun anonymousLogic2(){
        if(!s_queue_anonymous.isChecked){
            anonymousRef.setValue("OFF")
        }
        else{
            anonymousRef.setValue("ON")
        }
    }

    private fun orderQueue(queueList: List<Queue>): List<Queue>{
        val sorted = queueList.sortedWith(compareBy<Queue> { it.getTimeOfQueue() })
        return sorted
    }

}


