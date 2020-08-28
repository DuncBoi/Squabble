package com.duncboi.realsquabble

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*
import kotlinx.android.synthetic.main.profile_picture_dialog.view.*
import kotlinx.android.synthetic.main.waiting_for_verification.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditProfile.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditProfile : Fragment() {

    private val REQUEST_IMAGE_CAPTURE = 100
    private lateinit var imageUri: Uri
    private val args: EditProfileArgs by navArgs()

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        if (args.bitmap != "null"){
            val bitmap = args.bitmap
            Log.d("Moose", "$bitmap")
            val imageBytes = Base64.decode(bitmap, 0)
            Log.d("Moose", "$imageBytes")
            val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            Log.d("Moose", "$image")
            uploadImageAndSaveUri(image)
        }

        val username = args.username
        view.b_edit_profile_username.text = username

        val firstLetter = username?.get(0)
        view.tv_edit_profile_letter.text = "$firstLetter".split(' ').joinToString(" ") { it.capitalize() }

        val bio = args.bio
        if (bio != "null") view.b_edit_profile_bio.text = "$bio"
        else view.b_edit_profile_bio.text = ""

        val name = args.name
        if (name != "") {
                val firstLetter = name?.get(0)
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
            imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0)
            stopNameRunner = true
            val bundle = Bundle()
            bundle.putString("bio", bio)
            bundle.putString("username", username)
            bundle.putString("name", name)
            findNavController().navigate(R.id.edit_profile_to_default, bundle)
        }

        view.tv_edit_profile_done.setOnClickListener {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0)
            stopNameRunner = true
            val name = et_edit_profile_name.text.toString().trim()
            val currentUser = FirebaseAuth.getInstance().currentUser
            val uid = currentUser!!.uid
            val emailQuery = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("uid").equalTo(uid)
            emailQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
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
            imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0)
            stopNameRunner = true
            val name = et_edit_profile_name.text.toString().trim()
            val username = b_edit_profile_username.text.toString().trim()
            val bundle = Bundle()
            bundle.putString("bio", bio)
            bundle.putString("username", username)
            bundle.putString("name", name)
            findNavController().navigate(R.id.editProfile_to_edit_username, bundle)
        }

        view.b_edit_profile_bio.setOnClickListener {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0)
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


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EditProfile.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditProfile().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
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
            tv_edit_profile_letter_counter.setText("$nameNumber")
            if (argsName == name){
                tv_edit_profile_done.isClickable = true
                tv_edit_profile_done.setText("Done")
                et_edit_profile_name.setPadding(24,24,24,24)
                et_edit_profile_name.bringToFront()
                et_edit_profile_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.pen, 0)
            }
            else if (nameNumber < 25){
                argsName += "9"
                tv_edit_profile_letter_counter.bringToFront()
                et_edit_profile_name.setPadding(24,31,100,31)
                et_edit_profile_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                if(nameNumber >= 0){
                    tv_edit_profile_done.isClickable = true
                    tv_edit_profile_done.setText("Done")
                    tv_edit_profile_letter_counter.setTextColor(ResourcesCompat.getColor(getResources(), R.color.green, null))
                }
                else{
                    tv_edit_profile_done.isClickable = false
                    tv_edit_profile_done.setText("")
                    tv_edit_profile_letter_counter.setTextColor(ResourcesCompat.getColor(getResources(), R.color.red, null))
                }
            }
            else{
                tv_edit_profile_done.isClickable = true
                tv_edit_profile_done.setText("Done")
                et_edit_profile_name.setPadding(24,24,24,24)
                et_edit_profile_name.bringToFront()
                et_edit_profile_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.pen, 0)
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

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == RESULT_OK){
            val uri = data?.data
            CropImage.activity(uri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(
                this!!.requireContext(), this)
        }
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val uri = data?.data
            CropImage.activity(uri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(
                this!!.requireContext(), this)
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK){
                val uri = result.uri
                val source = activity?.contentResolver?.let { ImageDecoder.createSource(it, uri!!) }
                val bitmap = ImageDecoder.decodeBitmap(source!!)
                uploadImageAndSaveUri(bitmap)
            }
        }
    }
    private fun uploadImageAndSaveUri(bitmap: Bitmap){
        val baos = ByteArrayOutputStream()
        val storageRef = FirebaseStorage.getInstance().reference.child("pics/${FirebaseAuth.getInstance().currentUser?.uid}")
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val image = baos.toByteArray()
        val upload = storageRef.putBytes(image)

        pb_edit_profile_upload_picture.visibility = View.VISIBLE

        upload.addOnCompleteListener { uploadTask ->
            pb_edit_profile_upload_picture.visibility = View.INVISIBLE
            if(uploadTask.isSuccessful){
                storageRef.downloadUrl.addOnCompleteListener { urlTask ->
                    urlTask.result?.let {
                        imageUri = it
                        civ_edit_profile_picture.setImageBitmap(bitmap)
                        iv_edit_profile_photo.alpha = 0f
                        tv_edit_profile_letter.alpha = 0f
                    }
                }
            }
            else{
                uploadTask.exception?.let {
                    Toast.makeText(activity, "${it.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}