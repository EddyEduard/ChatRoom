package com.personal.chatroommobile.ui.group

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.personal.chatroommobile.MainActivity
import com.personal.chatroommobile.R
import com.personal.chatroommobile.data.CacheData
import com.personal.chatroommobile.databinding.FragmentMemberItemBinding
import com.personal.chatroommobile.ui.group.placeholder.MemberContent
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import androidx.lifecycle.Observer
import com.personal.chatroommobile.ui.account.placeholder.GroupContent

class MemberRecyclerViewAdapter(
    private val activity: MainActivity,
    private val fragment: GroupFragment,
    private val members: List<MemberItemView>,
    private val groupViewModel: GroupViewModel,
) : RecyclerView.Adapter<MemberRecyclerViewAdapter.ViewHolder>(), Filterable {
    var membersFilter = ArrayList<MemberItemView>()

    init {
        membersFilter = members as ArrayList<MemberItemView>
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentMemberItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val member = membersFilter[position]
        holder.memberName.text = member.name
        holder.memberEmail.text = member.email
        holder.addMember.setOnClickListener {
            groupViewModel.groupChangeResult.observe(fragment.viewLifecycleOwner,
                Observer {
                    val result = it ?: return@Observer

                    if (result.isForAddMember) {
                        if (result.success != null) {
                            holder.addMember.visibility = View.GONE
                            holder.removeMember.visibility = View.VISIBLE

                            activity.lifecycleScope.launch {
                                groupViewModel.members(CacheData.group.id)
                            }
                        }
                    }
                })

            activity.lifecycleScope.launch {
                groupViewModel.addMember(CacheData.group.id, member.id)
            }
        }
        holder.removeMember.setOnClickListener {
            AlertDialog.Builder(activity)
                .setMessage("Are you sure you want to delete this member from group?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, _ ->
                    groupViewModel.groupChangeResult.observe(fragment.viewLifecycleOwner,
                        Observer {
                            val result = it ?: return@Observer

                            if (result.isForRemoveMember) {
                                if (result.success != null) {
                                    MemberContent.removeItem(member)
                                    notifyDataSetChanged()
                                }
                            }
                        })

                    activity.lifecycleScope.launch {
                        groupViewModel.removeMember(CacheData.group.id, member.id)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        if (member.exist) {
            holder.addMember.visibility = View.GONE
            holder.removeMember.visibility = View.VISIBLE
        } else {
            holder.removeMember.visibility = View.GONE
            holder.addMember.visibility = View.VISIBLE
        }

        Glide.with(activity)
            .load(member.image)
            .into(holder.memberImage)
    }

    override fun getItemCount(): Int = membersFilter.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()

                membersFilter = if (charSearch.isEmpty()) ({
                    members
                }) as ArrayList<MemberItemView> else {
                    val resultList = ArrayList<MemberItemView>()

                    for (row in members) {
                        if (row.name.lowercase(Locale.ROOT)
                                .contains(charSearch.lowercase(Locale.ROOT))
                        ) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }

                val filterResults = FilterResults()
                filterResults.values = membersFilter
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                membersFilter = if (results?.values != null)
                    results.values as ArrayList<MemberItemView>
                else
                    members as ArrayList<MemberItemView>
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(binding: FragmentMemberItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var memberImage: ShapeableImageView = binding.memberImage
        var memberName: TextView = binding.memberName
        var memberEmail: TextView = binding.memberEmail
        var addMember: ImageButton = binding.addMember
        var removeMember: ImageButton = binding.removeMember

        init {
            memberImage = binding.root.findViewById(R.id.member_image)
            memberName = binding.root.findViewById(R.id.member_name)
            memberEmail = binding.root.findViewById(R.id.member_email)
            addMember = binding.root.findViewById(R.id.add_member)
            removeMember = binding.root.findViewById(R.id.remove_member)
        }
    }
}