package com.duncboi.realsquabble.Adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.duncboi.realsquabble.ModelClasses.Groups
import com.duncboi.realsquabble.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_profile.*

class GroupAdapter (context: Context, groupList: List<Groups>): RecyclerView.Adapter<GroupAdapter.ViewHolder?>() {

    private val context: Context
    private val groups: List<Groups>
    private var memberCount = 0

    init {
        this.context = context
        this.groups = groupList

    }
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.recycler_group, viewGroup, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return groups.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = groups[position]

        if (group.getUri() != "") Picasso.get().load(group.getUri()).into(holder.profileImageView)
        holder.groupName.text = group.getName()

        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("groupId", group.getChatId())
            findNavController(holder.itemView).navigate(R.id.action_findGroup_to_groupDescription, bundle)
        }

        if (group.getStateLocation() == "Not Specified"){
            holder.location.text = "Not Specified"
        }
        else{
            if (group.getCountyLocation() != "Not Specified"){
                holder.location.text = "${group.getCountyLocation()}, ${group.getStateLocation()}"
            }
            else{
                holder.location.text = "${group.getStateLocation()}"
            }
        }
        val groupRef = FirebaseDatabase.getInstance().reference.child("Group Chat List").child(group.getChatId()!!)
        groupRef.onDisconnect().cancel()
        groupRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                        val alignment =
                            snapshot.child("alignment").child("0").getValue(String::class.java)
                        holder.alignment.text = alignment
                        when (alignment) {
                            "Moderate" -> {
                                holder.singleAlignment.setImageResource(R.drawable.moderate_banner)
                            }
                            "Conservative" -> {
                                holder.singleAlignment.setImageResource(R.drawable.conservative_banner)
                            }
                            "Liberal" -> {
                                holder.singleAlignment.setImageResource(R.drawable.ic_liberal_banner)
                            }
                            "Libertarian" -> {
                                holder.singleAlignment.setImageResource(R.drawable.libertarian_banner)
                            }
                            "Authoritarian" -> {
                                holder.singleAlignment.setImageResource(R.drawable.authoritarian_banner)
                            }
                        }
                    holder.members.text = "${snapshot.child("members").childrenCount} members"
            }
        })



    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var groupName: TextView
        var profileImageView: CircleImageView
        var location: TextView
        var alignment: TextView
        var singleAlignment: ImageView
        var trending: ImageView
        var members: TextView

        init {
            groupName = itemView.findViewById(R.id.tv_recycler_group_name)
            profileImageView = itemView.findViewById(R.id.civ_recycler_group_image)
            location = itemView.findViewById(R.id.tv_recycler_group_location)
            alignment = itemView.findViewById(R.id.tv_recycler_group_alignment)
            singleAlignment = itemView.findViewById(R.id.iv_recycler_group_single_alignment)
            trending = itemView.findViewById(R.id.iv_recycler_group_trending_up)
            members = itemView.findViewById(R.id.tv_recycler_group_members)
        }
    }

}