package com.personal.chatroommobile.ui.account

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.personal.chatroommobile.MainActivity
import com.personal.chatroommobile.R
import com.personal.chatroommobile.data.CacheData
import com.personal.chatroommobile.databinding.FragmentGroupItemBinding
import com.personal.chatroommobile.ui.account.placeholder.GroupContent
import com.personal.chatroommobile.ui.contacts.ContactFragment
import com.personal.chatroommobile.ui.group.GroupFragment
import java.util.*
import kotlin.collections.ArrayList

class GroupRecyclerViewAdapter(
    private val context: Context,
    private val groups: List<GroupItemView>,
) : RecyclerView.Adapter<GroupRecyclerViewAdapter.ViewHolder>(), Filterable {
    var groupsFilter = ArrayList<GroupItemView>()

    init {
        groupsFilter = groups as ArrayList<GroupItemView>
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentGroupItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = groupsFilter[position]
        holder.groupName.text = group.name
        holder.itemView.setOnClickListener {
            CacheData.group = group

            (context as MainActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                GroupFragment()
            ).commit()
        }

        Glide.with(context)
            .load(group.image)
            .into(holder.groupImage)
    }

    override fun getItemCount(): Int = groupsFilter.size

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    fun addGroup(groupId: Int, name: String, image: String) {
        GroupContent.addItem(GroupItemView(groupId, name, image, GroupContent.ITEMS.size))
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()

                groupsFilter = if (charSearch.isEmpty()) ({
                    groups
                }) as ArrayList<GroupItemView> else {
                    val resultList = ArrayList<GroupItemView>()

                    for (row in groups) {
                        if (row.name.lowercase(Locale.ROOT)
                                .contains(charSearch.lowercase(Locale.ROOT))
                        ) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }

                val filterResults = FilterResults()
                filterResults.values = groupsFilter
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                groupsFilter = if (results?.values != null)
                    results.values as ArrayList<GroupItemView>
                else
                    groups as ArrayList<GroupItemView>
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(binding: FragmentGroupItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var groupImage: ShapeableImageView = binding.groupImage
        var groupName: TextView = binding.groupName

        init {
            groupImage = binding.root.findViewById(R.id.group_image)
            groupName = binding.root.findViewById(R.id.group_name)
        }
    }
}