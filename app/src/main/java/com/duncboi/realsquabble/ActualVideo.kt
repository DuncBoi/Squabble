package com.duncboi.realsquabble

import android.app.AlertDialog
import android.content.DialogInterface
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavDestination
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.duncboi.realsquabble.messenger.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.opentok.android.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_actual_video.*
import org.json.JSONException
import org.json.JSONObject

class ActualVideo : Fragment(){

    private val args: ActualVideoArgs by navArgs()
    private var API_KEY = 46930154
    lateinit var SESSION_ID: String
    lateinit var TOKEN: String
    private var subscriber: Subscriber? = null
    private var session: Session? = null
    private var publisher: Publisher? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_actual_video, container, false)
    }

    override fun onStop() {
        super.onStop()
        if (subscriber != null) {
            session?.unpublish(publisher)
            session?.unsubscribe(subscriber)
        }
        FirebaseDatabase.getInstance().reference.child("Debating").child(args.code)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pb_actual_video_progress.visibility = View.VISIBLE
        val otherUid = args.otherUid

        tv_actual_video_headline.text = args.headline

        FirebaseDatabase.getInstance().reference.child("Users").child(otherUid).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val otherUser = snapshot.getValue(Users::class.java)
                if (otherUser != null) {
                    if (otherUser.getUri() != "" && otherUser.getUri() != null) {
                        Picasso.get().load(otherUser.getUri()).into(civ_actual_video_profile)
                    }
                    if (otherUser.getName() != "") tv_actual_video_username.text = otherUser.getName()
                    else tv_actual_video_username.text = otherUser.getUsername()
                    when (otherUser.getCategory()) {
                        "Moderate" -> iv_actual_video_banner.setImageResource(R.drawable.moderate_banner)
                        "Conservative" -> iv_actual_video_banner.setImageResource(R.drawable.conservative_banner)
                        "Liberal" -> iv_actual_video_banner.setImageResource(R.drawable.ic_liberal_banner)
                        "Libertarian" -> iv_actual_video_banner.setImageResource(R.drawable.libertarian_banner)
                        "Authoritarian" -> iv_actual_video_banner.setImageResource(R.drawable.authoritarian_banner)
                    }
                }
            }

        })

        b_actual_video_leave.setOnClickListener {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("Leave Call")
            builder.setMessage("Are you sure you want to leave the call?")
            builder.setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->
                val bundle = Bundle()
                bundle.putString("topic", args.headline)
                findNavController().navigate(R.id.action_actualVideo_to_topicDescription, bundle)
            })
            builder.setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, _ ->
                dialogInterface.cancel()
            })
            builder.setCancelable(false)
            builder.create().show()
        }

        val reqQueue: RequestQueue = Volley.newRequestQueue(activity)
        reqQueue.add(JsonObjectRequest(
            Request.Method.GET,
            "https://squabb.herokuapp.com" + "/room/:${args.code}",
            null,
            object : Response.Listener<JSONObject>,
                Session.SessionListener, PublisherKit.PublisherListener {
                override fun onResponse(response: JSONObject?) {
                    try {
                        API_KEY = response!!.getInt("apiKey")
                        SESSION_ID = response.getString("sessionId")
                        TOKEN = response.getString("token")
                        session = Session.Builder(activity, "$API_KEY", SESSION_ID).build()
                        session!!.setSessionListener(this)
                        session!!.connect(TOKEN)
                    } catch (error: JSONException) {
                        Log.d("Main", "Web Service error: " + error.message)
                    }
                }

                override fun onStreamDropped(p0: Session?, p1: com.opentok.android.Stream?) {
                    Log.d("Status", "Stream Dropped")
                    val bundle = Bundle()
                    bundle.putString("topic", args.headline)
                        findNavController().navigate(R.id.action_actualVideo_to_topicDescription, bundle)
                    Toast.makeText(activity, "Session Disconnected", Toast.LENGTH_LONG).show()
                    if (subscriber != null) {
                        actual_video_other_user_screen.removeAllViews()
                    }
                }

                override fun onStreamReceived(session: Session?, p1: com.opentok.android.Stream?) {
                    pb_actual_video_progress.visibility = View.GONE
                    Log.d("Status", "Stream Received")
                    if (subscriber == null) {
                        subscriber = Subscriber.Builder(activity, p1).build()
                        session?.subscribe(subscriber)
                        actual_video_other_user_screen.addView(subscriber?.view)
                    }
                }

                override fun onConnected(session: Session?) {
                    Log.d("Status", "Connected")
                    publisher = Publisher.Builder(activity).build()
                    publisher!!.setPublisherListener(this)
                    iv_actual_video_current_user_screen.addView(publisher!!.view)
                    if (publisher!!.view is GLSurfaceView) {
                        (publisher!!.view as GLSurfaceView).setZOrderOnTop(true)
                    }
                    session?.publish(publisher)
                }

                override fun onDisconnected(p0: Session?) {
                    Log.d("Status", "Disconnected")
                }

                override fun onError(p0: Session?, p1: OpentokError?) {
                    Log.d("Status", "Error")
                }

                override fun onStreamCreated(p0: PublisherKit?, p1: com.opentok.android.Stream?) {
                    Log.d("Status", "Stream Created")

                }

                override fun onStreamDestroyed(p0: PublisherKit?, p1: com.opentok.android.Stream?) {
                }

                override fun onError(p0: PublisherKit?, p1: OpentokError?) {
                    Log.d("Status", "Error")
                }


            },
            Response.ErrorListener { error -> Log.d("Moose", "Web Service error: " + error?.message); }
        ))
    }

}


