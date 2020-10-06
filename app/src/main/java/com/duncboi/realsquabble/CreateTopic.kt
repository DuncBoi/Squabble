package com.duncboi.realsquabble

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_create_group.*
import kotlinx.android.synthetic.main.fragment_create_topic.*
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_other_profile.*

class CreateTopic : Fragment() {

    private val args: CreateTopicArgs by navArgs()
    private var includeUserInfo = true
    private val creator = FirebaseAuth.getInstance().currentUser!!.uid
    private var url: String = ""

    private val topicsRef = FirebaseDatabase.getInstance().reference.child("Topics").orderByChild("headline")
    private var topicsListener: ValueEventListener? = null

    override fun onPause() {
        super.onPause()
        if (topicsListener != null) topicsRef.equalTo(args.topic).removeEventListener(topicsListener!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_topic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rb_create_topic_yes.isChecked = true

        if (args.type == "edit") {
            b_create_topic_create.text = "Save"
            b_create_topic_delete.visibility = View.VISIBLE
            b_create_topic_delete.setOnClickListener {
                val builder = AlertDialog.Builder(activity)
                builder.setCancelable(false)
                builder.setTitle("Delete topic")
                builder.setMessage("Are you sure you want to delete this topic?")
                builder.setPositiveButton("Delete") { dialog, i ->
                    FirebaseDatabase.getInstance().reference.child("Topics").child(args.topic)
                        .removeValue().addOnCompleteListener {
                        findNavController().navigate(R.id.action_createTopic_to_videoChat)
                            Toast.makeText(activity, "Topic Deleted", Toast.LENGTH_SHORT).show()
                    }
                }
                builder.setNegativeButton("No") { dialogInterface, i ->
                    dialogInterface.dismiss()
                }
                builder.create().show()
            }
        }
                topicsListener = topicsRef.equalTo(args.topic).addValueEventListener(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children) {
                        val topic = i.getValue(Topics::class.java)
                        if (topic != null) {
                            et_create_topic_headline.setText(topic.getHeadline())
                            et_create_topic_description.setText(topic.getDescription())
                            et_create_topic_question.setText(topic.getQuestion())
                            et_create_topic_answer1.setText(topic.getAnswer1())
                            et_create_topic_answer2.setText(topic.getAnswer2())
                            if (topic.getUri() != "") {
                                url = topic.getUri().toString()
                                Picasso.get().load(topic.getUri()).into(iv_create_topic_picture)
                                tv_create_topic_add_picture_text.visibility = View.GONE
                            }
                            if (topic.getIsTrending() == true) {
                                iv_create_group_trending.visibility = View.VISIBLE
                                tv_create_group_trending.visibility = View.VISIBLE
                            }
                            if (topic.getIncludeUserInfo() == false) rb_create_topic_no.isChecked =
                                true
                        }
                    }
                }


            })

            iv_create_topic_picture.setOnClickListener {
                val intent = Intent()
                intent.action = Intent.ACTION_GET_CONTENT
                intent.type = "image/*"
                startActivityForResult(Intent.createChooser(intent, "Pick Image"), 439)
            }

            iv_create_topic_back.setOnClickListener {
                val builder = AlertDialog.Builder(activity)
                builder.setCancelable(false)
                builder.setTitle("Exit?")
                builder.setMessage("Are you sure you want to exit the topic creator?  Your changes will not be saved")
                builder.setNegativeButton("No") { dialogInterface, i ->
                    dialogInterface.dismiss()
                }
                if (args.type == "edit"){
                    builder.setPositiveButton("Exit") { dialogInterface, i ->
                        val bundle = Bundle()
                        bundle.putString("topic", args.topic)
                        bundle.putString("type", "owned")
                        findNavController().navigate(R.id.action_createTopic_to_topicDescription, bundle)
                    }
                }
                else{
                    builder.setPositiveButton("Exit", DialogInterface.OnClickListener { dialogInterface, i ->
                        findNavController().navigate(R.id.action_createTopic_to_videoChat)
                    })
                }
                builder.create().show()
            }

            b_create_topic_create.setOnClickListener {
                if (et_create_topic_headline.text.toString()
                        .isNotEmpty() && et_create_topic_question.text.toString()
                        .isNotEmpty() && et_create_topic_answer1.text.toString()
                        .isNotEmpty() && et_create_topic_answer2.text.toString()
                        .isNotEmpty() && et_create_topic_description.text.toString().length <= 150
                ) {
                    val description = et_create_topic_description.text.toString()
                    val headline = et_create_topic_headline.text.toString()
                    val question = et_create_topic_question.text.toString()
                    val answer1 = et_create_topic_answer1.text.toString()
                    val answer2 = et_create_topic_answer2.text.toString()
                    val istrending = false
                    if (rb_create_topic_no.isChecked) includeUserInfo = false
                    val topic = Topics(
                        headline,
                        description,
                        url,
                        istrending,
                        creator,
                        question,
                        answer1,
                        answer2,
                        includeUserInfo
                    )
                    FirebaseDatabase.getInstance().reference.child("Topics").child(headline)
                        .setValue(topic).addOnCompleteListener {
                            val bundle = Bundle()
                            bundle.putString("type", "owned")
                            bundle.putString("topic", "${topic.getHeadline()}")
                                findNavController().navigate(
                                R.id.action_createTopic_to_topicDescription,
                                bundle
                            )
                            Toast.makeText(activity, "Topic published", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    if (et_create_topic_headline.text.toString().isEmpty()) {
                        closeKeyboard()
                        create_topic_scroll.fullScroll(View.FOCUS_UP)
                        create_topic_scroll.scrollTo(0, 0)
                        tv_create_topic_error.text = "Please include headline"
                        et_create_topic_headline.setBackgroundResource(R.drawable.error_edittext_register_login)
                    } else if (et_create_topic_question.text.toString().isEmpty()) {
                        closeKeyboard()
                        create_topic_scroll.fullScroll(View.FOCUS_DOWN)
                        tv_create_topic_error.text = "Please include question"
                        et_create_topic_question.setBackgroundResource(R.drawable.error_edittext_register_login)
                    } else if (et_create_topic_answer1.text.toString().isEmpty()) {
                        closeKeyboard()
                        create_topic_scroll.fullScroll(View.FOCUS_DOWN)
                        tv_create_topic_error.text = "Please include answer 1"
                        et_create_topic_answer1.setBackgroundResource(R.drawable.error_edittext_register_login)
                    } else if (et_create_topic_answer2.text.toString().isEmpty()) {
                        closeKeyboard()
                        create_topic_scroll.fullScroll(View.FOCUS_DOWN)
                        tv_create_topic_error.text = "Please include answer 2"
                        et_create_topic_answer2.setBackgroundResource(R.drawable.error_edittext_register_login)
                    } else if (et_create_topic_description.text.toString().length > 150) {
                        et_create_topic_description.setBackgroundResource(R.drawable.error_edittext_register_login)
                        tv_create_topic_error.text = "Description too long"
                    }
                }
            }

            et_create_topic_headline.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val text = p0.toString().trim()
                    if (text.length > 25) {
                        tv_create_topic_error.text = "Headline too long"
                        et_create_topic_headline.setBackgroundResource(R.drawable.error_edittext_register_login)
                    } else if (text.isEmpty()) {
                        tv_create_topic_error.text = "Please include headline"
                        et_create_topic_headline.setBackgroundResource(R.drawable.error_edittext_register_login)
                    } else {
                        tv_create_topic_error.text = ""
                        et_create_topic_headline.setBackgroundResource(R.drawable.rounded_edittext_register_login)
                    }
                }
            })
            et_create_topic_question.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val text = p0.toString().trim()
                    if (text.length > 25) {
                        tv_create_topic_error.text = "Question too long"
                        et_create_topic_question.setBackgroundResource(R.drawable.error_edittext_register_login)
                    } else if (text.isEmpty()) {
                        tv_create_topic_error.text = "Please include question"
                        et_create_topic_question.setBackgroundResource(R.drawable.error_edittext_register_login)
                    } else {
                        tv_create_topic_error.text = ""
                        et_create_topic_question.setBackgroundResource(R.drawable.rounded_edittext_register_login)
                    }
                }
            })
            et_create_topic_answer1.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val text = p0.toString().trim()
                    if (text.length > 25) {
                        tv_create_topic_error.text = "Answer 1 too long"
                        et_create_topic_answer1.setBackgroundResource(R.drawable.error_edittext_register_login)
                    } else if (text.isEmpty()) {
                        tv_create_topic_error.text = "Please include answer 1"
                        et_create_topic_answer1.setBackgroundResource(R.drawable.error_edittext_register_login)
                    } else {
                        tv_create_topic_error.text = ""
                        et_create_topic_answer1.setBackgroundResource(R.drawable.rounded_edittext_register_login)
                    }
                }
            })
            et_create_topic_answer2.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val text = p0.toString().trim()
                    if (text.length > 25) {
                        tv_create_topic_error.text = "Answer 2 too long"
                        et_create_topic_answer2.setBackgroundResource(R.drawable.error_edittext_register_login)
                    } else if (text.isEmpty()) {
                        tv_create_topic_error.text = "Please include answer 2"
                        et_create_topic_answer2.setBackgroundResource(R.drawable.error_edittext_register_login)
                    } else {
                        tv_create_topic_error.text = ""
                        et_create_topic_answer2.setBackgroundResource(R.drawable.rounded_edittext_register_login)
                    }
                }
            })

            et_create_topic_description.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    et_create_topic_description.setBackgroundResource(R.drawable.rounded_edittext_register_login)
                    val text = p0.toString().trim()
                    val textLength = text.length
                    val counter = 150 - textLength
                    tv_create_topic_counter.text = "$counter"
                    if (counter < 0) {
                        tv_create_topic_counter.setTextColor(Color.parseColor("#eb4b4b"))
                    } else {
                        tv_create_topic_counter.setTextColor(Color.parseColor("#38c96d"))
                    }
                    if (textLength > 150) {
                        et_create_topic_description.setBackgroundResource(R.drawable.error_edittext_register_login)
                        tv_create_topic_error.text = "Description too long"
                    }
                }

            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            pb_create_topic_picture.visibility = View.VISIBLE
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val fileUri = result.uri

                val storageReference = FirebaseStorage.getInstance().reference.child("Topics Images")
                val key = topicsRef.ref.push().key
                val filePath = storageReference.child("$creator").child("$key")

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
                        pb_create_topic_picture.visibility = View.INVISIBLE
                        tv_create_topic_add_picture_text.visibility = View.INVISIBLE
                        val downloadUri = task.result
                        url = downloadUri.toString()
                        if (url != null) {
                            Picasso.get().load(url).into(iv_create_topic_picture)
                        }
                        else{
                            tv_create_topic_add_picture_text.visibility = View.VISIBLE
                        }
                    }
                }.addOnFailureListener {
                }
            }}

        if (requestCode == 439 && resultCode == Activity.RESULT_OK && data != null && data.data != null){

            val fileUri = data.data
            CropImage.activity(fileUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1).start(this.requireContext(), this)


        }
    }

    private fun closeKeyboard(){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
    }



