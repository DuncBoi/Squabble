package com.duncboi.realsquabble.profile

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.duncboi.realsquabble.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*
import kotlinx.android.synthetic.main.profile_picture_dialog.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class EditProfile : Fragment() {

    private val REQUEST_IMAGE_CAPTURE = 100
    private lateinit var imageUri: Uri
    private val args: EditProfileArgs by navArgs()

    override fun onPause() {
        super.onPause()
        stopNameRunner = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("uid").equalTo(FirebaseAuth.getInstance().currentUser!!.uid)
        emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    if (childSnapshot.child("uri").getValue<String>().toString() != "null"){
                        val uriString = childSnapshot.child("uri").getValue<String>().toString()
                        val uri = Uri.parse(uriString)
                        Picasso.get().load(uri).into(civ_edit_profile_picture)
                        iv_edit_profile_photo.alpha = 0f
                        tv_edit_profile_letter.alpha = 0f
                }}
            }
            override fun onCancelled(error: DatabaseError) {} })

        val username = args.username
        val time = args.usernameTime
        view.b_edit_profile_username.text = username

        val firstLetter = username?.get(0)
        view.tv_edit_profile_letter.text = "$firstLetter".split(' ').joinToString(" ") { it.capitalize() }

        val bio = args.bio
        if (bio != "null") view.b_edit_profile_bio.text = "$bio"
        else view.b_edit_profile_bio.text = ""

        val name = args.name
        if (name != "null" && name != "") {
                val firstLetter = name.get(0)
                view.tv_edit_profile_letter.text = "$firstLetter".split(' ').joinToString(" ") { it.capitalize() }
                view.et_edit_profile_name.setText("$name")
        }
        else view.et_edit_profile_name.setText("")

        view.iv_edit_profile_photo.setOnClickListener {
            val photoDialog = waitingForVerificationDialog()
            photoDialog.show()
        }

        view.iv_edit_profile_back_button.setOnClickListener {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
            stopNameRunner = true
            val bundle = Bundle()
            bundle.putString("bio", bio)
            bundle.putLong("usernameTime", time)
            bundle.putString("username", username)
            bundle.putString("name", name)
            findNavController().navigate(R.id.edit_profile_to_default, bundle)
        }

        view.tv_edit_profile_done.setOnClickListener {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
            stopNameRunner = true
            val name = et_edit_profile_name.text.toString().trim()
            val currentUser = FirebaseAuth.getInstance().currentUser
            val uid = currentUser!!.uid
            val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("uid").equalTo(uid)
            emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        if (time != 0L) childSnapshot.child("usernameTime").ref.setValue("$time")
                        childSnapshot.child("username").ref.setValue("$username")
                        childSnapshot.child("name").ref.setValue("$name")
                        childSnapshot.child("bio").ref.setValue("$bio").addOnCompleteListener {
                            val bio = b_edit_profile_bio.text.toString().trim()
                            val bundle = Bundle()
                            bundle.putString("bio", bio)
                            bundle.putString("username", username)
                            bundle.putString("name", name)
                            findNavController().navigate(R.id.edit_profile_to_default, bundle)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        }
        
        view.b_edit_profile_username.setOnClickListener {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
            stopNameRunner = true
            val name = et_edit_profile_name.text.toString().trim()
            val username = b_edit_profile_username.text.toString().trim()
            val bundle = Bundle()
            bundle.putString("bio", bio)
            bundle.putLong("usernameTime", time)
            bundle.putString("username", username)
            bundle.putString("name", name)
            findNavController().navigate(R.id.editProfile_to_edit_username, bundle)
        }

        view.b_edit_profile_bio.setOnClickListener {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
            stopNameRunner = true
            val name = et_edit_profile_name.text.toString().trim()
            val bio = b_edit_profile_bio.text.toString().trim()
            val bundle = Bundle()
            bundle.putString("bio", bio)
            bundle.putString("username", username)
            bundle.putString("name", name)
            findNavController().navigate(R.id.editProfile_to_edit_bio, bundle)
        }
        runNameChecker()
        return view
    }

    private fun waitingForVerificationDialog(): AlertDialog {
        val waitingForVerificationBuilder = activity?.let { AlertDialog.Builder(it) }
        val view = LayoutInflater.from(activity).inflate(R.layout.profile_picture_dialog, null)
        waitingForVerificationBuilder!!.setView(view)
        waitingForVerificationBuilder.setCancelable(false)
        val waitingForVerificationDialog = waitingForVerificationBuilder.create()
        view.b_profile_picture_dialog_cancel.setOnClickListener {
            waitingForVerificationDialog.dismiss()
        }
        view.b_profile_picture_dialog_camera.setOnClickListener {
            waitingForVerificationDialog.dismiss()
            takePictureIntent()
        }
        view.b_profile_picture_dialog_library.setOnClickListener {
            waitingForVerificationDialog.dismiss()
            choosePhotoIntent()
        }
        return waitingForVerificationDialog
    }

    private var stopNameRunner = false

    private fun runNameChecker(){
        CoroutineScope(Dispatchers.Main).launch {
            nameRunnerLogic()
        }
    }

    private suspend fun nameRunnerLogic(){
        var argsName = args.name
        while (!stopNameRunner){
            delay(200)
            val name = et_edit_profile_name.text.toString()
            val length = et_edit_profile_name.text.toString().trim().length
            val nameNumber = 25 - length
            tv_edit_profile_letter_counter.text = "$nameNumber"
            if (argsName == name){
                tv_edit_profile_done.isClickable = true
                tv_edit_profile_done.text = "Done"
                et_edit_profile_name.setPadding(24,24,24,24)
                et_edit_profile_name.bringToFront()
                et_edit_profile_name.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    R.drawable.pen, 0)
            }
            else if (nameNumber < 25){
                argsName += "9"
                tv_edit_profile_letter_counter.bringToFront()
                et_edit_profile_name.setPadding(24,31,100,31)
                et_edit_profile_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                if(nameNumber >= 0){
                    tv_edit_profile_done.isClickable = true
                    tv_edit_profile_done.text = "Done"
                    tv_edit_profile_letter_counter.setTextColor(ResourcesCompat.getColor(
                        resources,
                        R.color.green, null))
                }
                else{
                    tv_edit_profile_done.isClickable = false
                    tv_edit_profile_done.text = ""
                    tv_edit_profile_letter_counter.setTextColor(ResourcesCompat.getColor(
                        resources,
                        R.color.red, null))
                }
            }
            else{
                tv_edit_profile_done.isClickable = true
                tv_edit_profile_done.text = "Done"
                et_edit_profile_name.setPadding(24,24,24,24)
                et_edit_profile_name.bringToFront()
                et_edit_profile_name.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    R.drawable.pen, 0)
            }
        }
    }
    private fun takePictureIntent(){
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { pictureIntent ->
            pictureIntent.resolveActivity(activity?.packageManager!!)?.also {
                startActivityForResult(pictureIntent, 0)
            }
        }
    }

    private fun choosePhotoIntent(){
        stopNameRunner = true
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == RESULT_OK) {
            val uri = data?.data
            CropImage.activity(uri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1)
                .start(
                    this.requireContext(), this
                )
        }
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val uri = data?.data
            CropImage.activity(uri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1)
                .start(
                    this.requireContext(), this
                )
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            pb_edit_profile_upload_picture.visibility = View.VISIBLE
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val fileUri = result.uri
                val storageReference =
                    FirebaseStorage.getInstance().reference.child("Profile Pictures")
                val filePath =
                    storageReference.child("${FirebaseAuth.getInstance().currentUser!!.uid}.jpg")

                var uploadTask: StorageTask<*>
                uploadTask = filePath.putFile(fileUri!!)

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation filePath.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        val url = downloadUri.toString()
                        FirebaseDatabase.getInstance().reference.child("Users")
                            .child("${FirebaseAuth.getInstance().currentUser!!.uid}")
                            .child("uri").ref.setValue("$url").addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Picasso.get().load(url).placeholder(R.drawable.profile_icon)
                                        .into(civ_edit_profile_picture)
                                    pb_edit_profile_upload_picture.visibility = View.INVISIBLE
                                }
                            }
                    }
                }
            }
        }

    }

}