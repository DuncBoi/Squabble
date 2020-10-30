package com.duncboi.realsquabble.CreatorClasses

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.*
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.duncboi.realsquabble.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
import kotlinx.android.synthetic.main.fragment_password_registration.*
import java.util.*
import kotlin.collections.HashMap


class CreateGroup : Fragment() {

    val reference = FirebaseDatabase.getInstance().reference
    private val groupChatKey = reference.push().key
    private val creator = FirebaseAuth.getInstance().currentUser!!.uid
    private var type: String = ""
    private var url: String = ""
    var alignment = ArrayList<String>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var stateLocation = "Not Specified"
    private var countyLocation = "Not Specified"
    private var checkedButtons = 0
    private var longitude = 0.0
    private var latitude = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //rb_create_group_public.isChecked = true
        rb_create_group_not_specified.isChecked = true

        FirebaseDatabase.getInstance().reference.child("Users").child(creator).child("category").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val category = snapshot.getValue(String::class.java)
                if (category != null && cb_create_group_alignment_libertarian != null) {
                    if (category == "Moderate") {
                        cb_create_group_alignment_moderate.isClickable = false
                        cb_create_group_alignment_moderate.isChecked = true
                    } else if (category == "Authoritarian") {
                        cb_create_group_alignment_authoritarian.isClickable = false
                        cb_create_group_alignment_authoritarian.isChecked = true
                    } else if (category == "Libertarian") {
                        cb_create_group_alignment_libertarian.isClickable = false
                        cb_create_group_alignment_libertarian.isChecked = true
                    } else if (category == "Liberal") {
                        cb_create_group_alignment_liberal.isClickable = false
                        cb_create_group_alignment_liberal.isChecked = true
                    } else if (category == "Conservative") {
                        cb_create_group_alignment_conservative.isClickable = false
                        cb_create_group_alignment_conservative.isChecked = true
                    }
                }
            }
        })

        iv_create_group_back.setOnClickListener {
            findNavController().navigate(R.id.action_createGroup_to_groupHolder)
        }

        cb_create_group_alignment_moderate.setOnClickListener{
            if (!cb_create_group_alignment_moderate.isChecked) checkedButtons --
            else checkedButtons++
        }
        cb_create_group_alignment_liberal.setOnClickListener{
            if (!cb_create_group_alignment_liberal.isChecked) checkedButtons --
            else checkedButtons++
        }
        cb_create_group_alignment_conservative.setOnClickListener{
            if (!cb_create_group_alignment_conservative.isChecked) checkedButtons --
            else checkedButtons++
        }
        cb_create_group_alignment_authoritarian.setOnClickListener{
            if (!cb_create_group_alignment_authoritarian.isChecked) checkedButtons --
            else checkedButtons++
        }
        cb_create_group_alignment_libertarian.setOnClickListener {
            if (!cb_create_group_alignment_libertarian.isChecked) checkedButtons --
            else checkedButtons++
        }

        civ_create_group_profile.setOnClickListener{
            notClickable()
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Pick Image"), 438)
        }

        rb_create_group_state.setOnClickListener{
            getLocation()
        }

        rb_create_group_state_and_county.setOnClickListener {
            getLocation()
        }

        et_create_group_group_name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim() == "") {
                    notClickable()
                    tv_create_group_error.visibility = View.VISIBLE
                    tv_create_group_error.setText("Please enter group name")
                    et_create_group_group_name.setBackgroundResource(R.drawable.error_edittext_register_login)
                } else if (p0.toString().trim().length > 20) {
                    notClickable()
                    tv_create_group_error.visibility = View.VISIBLE
                    tv_create_group_error.setText("Group name too long")
                    et_create_group_group_name.setBackgroundResource(R.drawable.error_edittext_register_login)
                } else {
                    FirebaseDatabase.getInstance().reference.child("Group Chat List")
                        .orderByChild("name").equalTo(p0.toString().trim())
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {}
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.childrenCount > 0) {
                                    notClickable()
                                    tv_create_group_error.visibility = View.VISIBLE
                                    tv_create_group_error.setText("Group name already exists")
                                    et_create_group_group_name.setBackgroundResource(R.drawable.error_edittext_register_login)
                                } else {
                                    clickable()
                                    tv_create_group_error.visibility = View.INVISIBLE
                                    et_create_group_group_name.setBackgroundResource(R.drawable.rounded_edittext_register_login)
                                }
                            }

                        })
                }
            }

        })

        et_create_group_description.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                et_create_group_description.setBackgroundResource(R.drawable.rounded_edittext_register_login)
                val text = p0.toString().trim()
                val textLength = text.length
                val counter = 150 - textLength
                tv_create_group_description_counter.text = "$counter"
                if (counter < 0) {
                    tv_create_group_description_counter.setTextColor(Color.parseColor("#eb4b4b"))
                } else {
                    tv_create_group_description_counter.setTextColor(Color.parseColor("#38c96d"))
                }
                if (textLength > 150) {
                    et_create_group_description.setBackgroundResource(R.drawable.error_edittext_register_login)
                }
            }

        })


        tv_create_group_create.setOnClickListener {
            closeKeyboard()
            scrollView2.scrollTo(0, 0)
            alignment.clear()
            if (et_create_group_group_name.text.toString().trim() == "") {
                tv_create_group_error.visibility = View.VISIBLE
                tv_create_group_error.setText("Please enter group name")
                et_create_group_group_name.setBackgroundResource(R.drawable.error_edittext_register_login)
            } else {

                if (cb_create_group_alignment_conservative.isChecked) alignment.add("Conservative")
                if (cb_create_group_alignment_liberal.isChecked) alignment.add("Liberal")
                if (cb_create_group_alignment_libertarian.isChecked) alignment.add("Libertarian")
                if (cb_create_group_alignment_moderate.isChecked) alignment.add("Moderate")
                if (cb_create_group_alignment_authoritarian.isChecked) alignment.add("Authoritarian")

                if (rb_create_group_not_specified.isChecked) {
                    stateLocation = "Not Specified"
                    countyLocation = "Not Specified"
                } else if (rb_create_group_state.isChecked) {
                    countyLocation = "Not Specified"
                }

                if (alignment.size > 2) {
                    scrollView2.scrollTo(0, 0)
                    tv_create_group_error.visibility = View.VISIBLE
                    tv_create_group_error.setText("You may only select up to 2 subgroups")
                }
                else if (alignment.size == 0){
                    scrollView2.scrollTo(0, 0)
                    tv_create_group_error.visibility = View.VISIBLE
                    tv_create_group_error.setText("Please select at least one sub group")
                }
                else {
                    val groupChatHashMap = HashMap<String, Any?>()
                    groupChatHashMap["name"] = et_create_group_group_name.text.toString()
                    groupChatHashMap["creator"] = FirebaseAuth.getInstance().currentUser!!.uid
                    groupChatHashMap["type"] = type
                    groupChatHashMap["description"] = et_create_group_description.text.toString()
                    groupChatHashMap["uri"] = url
                    groupChatHashMap["chatid"] = groupChatKey
                    groupChatHashMap["stateLocation"] = stateLocation
                    groupChatHashMap["countyLocation"] = countyLocation
                    if (longitude != 0.0 && latitude != 0.0) {
                        groupChatHashMap["longitude"] = "$longitude"
                        groupChatHashMap["latitude"] = "$latitude"
                    }
                    else{
                        groupChatHashMap["longitude"] = "unknown"
                        groupChatHashMap["latitude"] = "unknown"
                    }

                    reference.child("Group Chat List").child("$groupChatKey")
                        .setValue(groupChatHashMap)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(activity, "Group Created", Toast.LENGTH_SHORT).show()
                                val bundle = Bundle()
                                bundle.putString("groupId", groupChatKey)
                                findNavController().navigate(
                                    R.id.action_createGroup_to_groupHolder,
                                    bundle
                                )
                                reference.child("Group Chat List").child("$groupChatKey")
                                    .child("alignment").ref.setValue(alignment)
                                reference.child("Group Chat List").child("$groupChatKey")
                                    .child("members").child(creator).setValue("owner")
                                FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("groupId").setValue(groupChatKey)
                            }
                        }
                }
            }
        }
    }

    private fun getLocation() {
        fusedLocationClient =
            activity?.let { it1 -> LocationServices.getFusedLocationProviderClient(it1) }!!

        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager: LocationManager =
                context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers: List<String> = locationManager.getProviders(true)
            var location: Location? = null
            for (i in providers.size - 1 downTo 0) {
                location = locationManager.getLastKnownLocation(providers[i])
                if (location != null)
                    break
            }
            val gps = DoubleArray(2)
            if (location != null) {
                gps[0] = location.getLatitude()
                gps[1] = location.getLongitude()

                val gcd = Geocoder(activity, Locale.getDefault())
                val adresses: List<Address>
                adresses = gcd.getFromLocation(gps[0], gps[1], 1)

                stateLocation = "${adresses[0].adminArea}"
                countyLocation = "${adresses[0].subAdminArea}"

                longitude = gps[1]
                latitude = gps[0]

                val locationHashMap = HashMap<String, Any?>()
                locationHashMap["county"] = adresses[0].subAdminArea
                locationHashMap["state"] = adresses[0].adminArea
                locationHashMap["country"] = adresses[0].countryName
                locationHashMap["latitude"] = adresses[0].latitude
                locationHashMap["longitude"] = adresses[0].longitude
                FirebaseDatabase.getInstance().reference.child("Users")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child("Location").ref.setValue(locationHashMap).addOnCompleteListener {
                        if (it.isSuccessful) {
                            rb_create_group_state.setText("State - (${adresses[0].adminArea})")
                            rb_create_group_state_and_county.setText("State and County - (${adresses[0].subAdminArea}, ${adresses[0].adminArea})")
                        }
                    }
            } else {
                rb_create_group_state.setText("State - (Error Finding Location)")
                rb_create_group_state_and_county.setText("State and County - (Error Finding Location)")
            }
        }
        else{
            rb_create_group_not_specified.isChecked = true
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 438 && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            CropImage.activity(uri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1)
                .start(
                    this.requireContext(), this
                )
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            //progress dialog
            pb_create_group_image_load.visibility = View.VISIBLE
            val fileUri = CropImage.getActivityResult(data).uri

            val key = FirebaseDatabase.getInstance().reference.push().key
            val storageReference = FirebaseStorage.getInstance().reference.child("Group Chat Images")
            val filePath = storageReference.child("$key.jpg")

            val uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    pb_create_group_image_load.visibility = View.GONE
                    tv_create_group_profile_pic_text.visibility = View.INVISIBLE
                    val downloadUri = task.result
                    url = downloadUri.toString()
                    Picasso.get().load(url).into(civ_create_group_profile)
                    clickable()
                }
            }.addOnFailureListener {
                clickable()
            }
        }
    }

    private fun notClickable(){
        tv_create_group_create.text = ""
    }

    private fun clickable(){
        tv_create_group_create.text = "Create"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            100 -> {
                if (!grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation()
                } else {
                    if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        rg_create_group_location.clearCheck()
                        rb_create_group_not_specified.isChecked = true
                        Toast.makeText(activity, "Permission Denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    private fun closeKeyboard(){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}