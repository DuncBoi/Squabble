package com.duncboi.realsquabble

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation.findNavController
import androidx.navigation.activity
import androidx.recyclerview.widget.RecyclerView
import com.duncboi.realsquabble.messenger.UserAdapter
import com.duncboi.realsquabble.messenger.Users
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage.activity
import de.hdodenhof.circleimageview.CircleImageView

class GroupAdapter (context: Context, groupList: List<Groups>): RecyclerView.Adapter<GroupAdapter.ViewHolder?>() {

    private val context: Context
    private val groups: List<Groups>

    init {
        this.context = context
        this.groups = groupList

    }
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): GroupAdapter.ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.recycler_group, viewGroup, false)
        return GroupAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return groups.size
    }

    override fun onBindViewHolder(holder: GroupAdapter.ViewHolder, position: Int) {
        val group = groups[position]

        if (group.getUri() != "") Picasso.get().load(group.getUri()).into(holder.profileImageView)
        holder.groupName.text = group.getName()

        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("groupId", group.getChatId())
            findNavController(holder.itemView).navigate(R.id.action_findGroup_to_groupDescription, bundle)
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var groupName: TextView
        var profileImageView: CircleImageView
        var location: TextView
        var alignment: TextView
        var singleAlignment: ImageView
        var doubleAlignment1: ImageView
        var doubleAlignment2: ImageView
        var trending: ImageView
        var members: TextView

        init {
            groupName = itemView.findViewById(R.id.tv_recycler_group_name)
            profileImageView = itemView.findViewById(R.id.civ_recycler_group_image)
            location = itemView.findViewById(R.id.tv_recycler_group_location)
            alignment = itemView.findViewById(R.id.tv_recycler_group_alignment)
            singleAlignment = itemView.findViewById(R.id.iv_recycler_group_single_alignment)
            doubleAlignment1 = itemView.findViewById(R.id.iv_recycler_group_double_alignment_1)
            doubleAlignment2 = itemView.findViewById(R.id.iv_recycler_group_double_alignment_2)
            trending = itemView.findViewById(R.id.iv_recycler_group_trending_up)
            members = itemView.findViewById(R.id.tv_recycler_group_members)
        }
    }

}