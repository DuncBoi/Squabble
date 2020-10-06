package com.duncboi.realsquabble

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.fragment_queue.*
import kotlinx.android.synthetic.main.fragment_queue.view.*
import kotlinx.android.synthetic.main.fragment_video_chat.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import java.lang.Math.abs


class Queue : Fragment() {

    private val args: QueueArgs by navArgs()
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    private var usersOnline = -1
    private var usersAgree = -1
    private var usersDisagree = -1
    private val RC_VIDEO_APP_PERMISSION = 124
    private var job: Job? = null

    private val anonymousRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser).child("anonymous")
    private var anonymousListener: ValueEventListener? = null

    private val topicRef = FirebaseDatabase.getInstance().reference.child("Queue")
    private var topicListener: ValueEventListener? = null

    override fun onPause() {
        super.onPause()
        if (anonymousListener != null) anonymousRef.removeEventListener(anonymousListener!!)

        if(topicListener != null) topicRef.child(args.topic).removeEventListener(topicListener!!)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_queue, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
        connecting().dismiss()
        FirebaseDatabase.getInstance().reference.child("Queue").child(args.topic).child(currentUser)
            .removeValue()
    }

    override fun onResume() {
        super.onResume()
        FirebaseDatabase.getInstance().reference.child("Queue").child(args.topic).child(currentUser)
            .child("status").setValue("searching")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_queue_headline.text = "${args.topic}"

        val perms = arrayOf(
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )
        requestPermissions(perms, RC_VIDEO_APP_PERMISSION)

        anonymousListener = anonymousRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val poo = snapshot.getValue<String>()
                s_queue_anonymous.isChecked = poo == "ON"
            }
        })

        s_queue_anonymous.setOnClickListener {
            if (s_queue_anonymous.isChecked) {
                s_queue_anonymous.isChecked = false
                anonymousRef.setValue("OFF")
            } else {
                s_queue_anonymous.isChecked = true
                anonymousRef.setValue("ON")
            }
        }

        iv_queue_back.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("topic", args.topic)
            findNavController().navigate(R.id.action_queue_to_topicDescription, bundle)
        }

    }

    private fun stuff(view: View, connectingDialog: AlertDialog) {
        val random = "${rand(0, Int.MAX_VALUE)}"
        val random2 = "${rand(0, Int.MAX_VALUE)}"
        val random3 = "${rand(0, Int.MAX_VALUE)}"

        val currentUserRef = FirebaseDatabase.getInstance().reference.child("Queue").child(args.topic).child(currentUser)
        currentUserRef.child("status").setValue("searching")
        currentUserRef.child("uid").setValue("$currentUser")
        currentUserRef.child("randomNumber").setValue(random)
        currentUserRef.child("randomNumber2").setValue(random2)
        currentUserRef.child("randomNumber3").setValue(random3)
        currentUserRef.child("key").setValue("${currentUserRef.push().key}")
        val currentUserPushKey = "${currentUserRef.push().key}"

        currentUserRef.child("answer").setValue(args.answer)
        currentUserRef.onDisconnect().removeValue()

            topicListener = topicRef.child(args.topic).addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    usersOnline = -1
                    usersAgree = -1
                    usersDisagree = 0
                    for (i in snapshot.children) {
                        if (i.child("answer").getValue(String::class.java).equals(args.answer)){
                            usersAgree ++
                        }
                        else{
                            usersDisagree ++
                        }
                        usersOnline++
                        view.tv_queue_online.text = "$usersOnline"
                        view.tv_queue_agree.text = "$usersAgree"
                        view.tv_queue_disagree.text = "$usersDisagree"

                        val user = i.child("uid").getValue<String>()
                        if (user != currentUser && user != null) {
                            val status = i.child("status").getValue(String::class.java)
                            val answer = i.child("answer").getValue(String::class.java)
                            if (status.equals("searching") && !answer.equals(args.answer) && !answer.equals(
                                    null
                                )
                            ) {
                                //person found
                                FirebaseDatabase.getInstance().reference.child("Queue")
                                    .child(args.topic).removeEventListener(this)

                                currentUserRef.removeValue().addOnCompleteListener {

                                    val otherRandom1 = i.child("randomNumber").getValue<String>()
                                    val otherRandom2 = i.child("randomNumber2").getValue<String>()
                                    val otherRandom3 = i.child("randomNumber3").getValue<String>()
                                    val otherUserPushKey = i.child("key").getValue<String>()
                                    val code1 = abs(user.compareTo(currentUser))
                                    val code2 = otherUserPushKey?.compareTo(currentUserPushKey)
                                        ?.let { kotlin.math.abs(it) }
                                    val code3 =
                                        random.compareTo(otherRandom1!!).let { kotlin.math.abs(it) }
                                    val code4 = random2.compareTo(otherRandom2!!)
                                        .let { kotlin.math.abs(it) }
                                    val code5 = random3.compareTo(otherRandom3!!)
                                        .let { kotlin.math.abs(it) }

                                    val code = "$code1$code2$code3$code4$code5"

                                    FirebaseDatabase.getInstance().reference.child("Debating")
                                        .child(code).child(currentUser).setValue(args.answer)
                                        .addOnCompleteListener {
                                            job = CoroutineScope(Main).launch {
                                                connectingDialog.show()
                                                delay(2000)
                                                connectingDialog.dismiss()
                                                val bundle = Bundle()
                                                bundle.putString("code", code)
                                                bundle.putString("otherUid", user)
                                                bundle.putString("headline", args.topic)
                                                findNavController().navigate(R.id.action_queue_to_actualVideo, bundle)
                                            }
                                            Toast.makeText(
                                                activity,
                                                "Match Found",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                }
                            }
                        }
                    }
                }

            })
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
            view?.let { stuff(it, connecting()) }
        } else {
            val bundle = Bundle()
            bundle.putString("topic", args.topic)
            findNavController().navigate(R.id.action_queue_to_topicDescription, bundle)
            Toast.makeText(activity,"Video and audio permissions are required to join a live call", Toast.LENGTH_LONG).show()
            activity?.let { EasyPermissions.requestPermissions(it, "Camera and Microphone are required in order to join a live video chat", RC_VIDEO_APP_PERMISSION)
            }

        }
    }

    private fun connecting(): AlertDialog {
        val connecting = activity?.let { AlertDialog.Builder(it) }
        connecting!!.setView(R.layout.connecting)
        connecting.setCancelable(false)
        return connecting.create()
    }
}