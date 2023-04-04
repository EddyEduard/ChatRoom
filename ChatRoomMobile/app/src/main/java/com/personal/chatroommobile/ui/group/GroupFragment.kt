package com.personal.chatroommobile.ui.group

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.personal.chatroommobile.MainActivity
import com.personal.chatroommobile.R
import com.personal.chatroommobile.data.CacheData
import com.personal.chatroommobile.data.repositories.RelationshipRepository
import com.personal.chatroommobile.data.source.RelationshipDataSource
import com.personal.chatroommobile.databinding.FragmentGroupBinding
import com.personal.chatroommobile.ui.account.AccountFragment
import com.personal.chatroommobile.ui.group.placeholder.MemberContent
import kotlinx.coroutines.launch

class GroupFragment : Fragment() {

    private var columnCount = 1
    private var newImagePath: String = ""
    private lateinit var binding: FragmentGroupBinding
    private lateinit var currentActivity: FragmentActivity
    private lateinit var memberRecyclerView: MemberRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            currentActivity = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentGroupBinding.inflate(inflater, container, false)

        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
            newImagePath = data?.data?.path.toString()

            Glide.with(currentActivity)
                .load(data?.data)
                .into(binding.groupImage)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val addMember = currentActivity.findViewById<Button>(R.id.add_member_button)
        val cancelAddMember = currentActivity.findViewById<Button>(R.id.cancel_add_member_button)
        val membersLoading = currentActivity.findViewById<ProgressBar>(R.id.loading_members)
        val membersSearch = currentActivity.findViewById<SearchView>(R.id.members_search)
        val recyclerView = currentActivity.findViewById<RecyclerView>(R.id.member_list)

        val groupViewModel = GroupViewModel(
            RelationshipRepository(
                RelationshipDataSource(CacheData.token)
            )
        )

        val updateGroupImage = binding.updateGroupImage
        val groupImage = binding.groupImage
        val groupName = binding.groupName
        val editGroup = binding.editGroupButton
        val deleteGroup = binding.deleteGroupButton
        val editLoading = binding.editLoading
        val deleteLoading = binding.deleteLoading

        // Load profile group.

        Glide.with(currentActivity)
            .load(CacheData.group.image)
            .into(groupImage)

        groupName.setText(CacheData.group.name)

        // Load members from group.

        groupViewModel.membersResult.observe(viewLifecycleOwner, Observer {
            val members = it ?: return@Observer

            if (recyclerView is RecyclerView) {
                recyclerView.layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }

                MemberContent.foundMembers(members)

                memberRecyclerView =
                    MemberRecyclerViewAdapter(
                        currentActivity as MainActivity,
                        this@GroupFragment,
                        MemberContent.ITEMS,
                        groupViewModel
                    )

                recyclerView.adapter = memberRecyclerView
                membersLoading.visibility = View.GONE
                membersSearch.visibility = View.VISIBLE

                membersSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(text: String?): Boolean {
                        memberRecyclerView.filter.filter(text)
                        return false
                    }
                })
            }
        })

        lifecycleScope.launch {
            groupViewModel.members(CacheData.group.id)
        }

        // Manage group data.

        groupViewModel.groupFormState.observe(currentActivity, Observer {
            val formState = it ?: return@Observer

            if (formState.nameError != null) {
                groupName.error = getString(formState.nameError)
            } else {
                groupName.error = null
            }

            editGroup.isEnabled = formState.isDataValid
        })

        groupViewModel.groupChangeResult.observe(currentActivity, Observer {
            val groupChangeResult = it ?: return@Observer

            if (groupChangeResult.isForChangeDataGroup) {
                editLoading.visibility = View.GONE
                editGroup.visibility = View.VISIBLE
            }

            if (groupChangeResult.error != null) {
                Toast.makeText(
                    currentActivity.applicationContext,
                    groupChangeResult.error,
                    Toast.LENGTH_SHORT
                ).show()
            }

            if (groupChangeResult.warning != null) {
                Toast.makeText(
                    currentActivity.applicationContext,
                    groupChangeResult.warning,
                    Toast.LENGTH_SHORT
                ).show()
            }

            if (groupChangeResult.success != null) {
                if (groupChangeResult.isForChangeDataGroup) {
                    Toast.makeText(
                        currentActivity.applicationContext,
                        groupChangeResult.success,
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (groupChangeResult.isForDeleteGroup) {
                    currentActivity.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container,
                            AccountFragment()
                        ).commit()
                }
            }

            editGroup.isEnabled = groupChangeResult.isForChangeDataGroup
        })

        groupName.afterTextChanged {
            groupViewModel.groupDataChanged(
                groupName.text.toString()
            )
        }

        updateGroupImage.setOnClickListener {
            val intent = Intent()
                .setType("image/png")
                .setType("image/jpg")
                .setType("image/jpeg")
                .setAction(Intent.ACTION_GET_CONTENT)

            startActivityForResult(Intent.createChooser(intent, "Select an image"), 111)
        }

        editGroup.setOnClickListener {
            editGroup.visibility = View.GONE
            editLoading.visibility = View.VISIBLE

            lifecycleScope.launch {
                groupViewModel.updateProfileGroup(
                    CacheData.group.id,
                    groupName.text.toString(),
                    newImagePath
                )
            }
        }

        addMember.setOnClickListener {
            addMember.visibility = View.GONE
            cancelAddMember.visibility = View.VISIBLE
            membersLoading.visibility = View.VISIBLE

            lifecycleScope.launch {
                groupViewModel.users(MemberContent.ITEMS as ArrayList<MemberItemView>,
                    CacheData.userId)
            }
        }

        cancelAddMember.setOnClickListener {
            cancelAddMember.visibility = View.GONE
            addMember.visibility = View.VISIBLE
            membersLoading.visibility = View.VISIBLE

            lifecycleScope.launch {
                groupViewModel.members(CacheData.group.id)
            }
        }

        deleteGroup.setOnClickListener {
            AlertDialog.Builder(activity)
                .setMessage("Are you sure you want to delete this group?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, _ ->
                    deleteGroup.visibility = View.GONE
                    deleteLoading.visibility = View.VISIBLE

                    lifecycleScope.launch {
                        groupViewModel.deleteGroup(CacheData.group.id)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}