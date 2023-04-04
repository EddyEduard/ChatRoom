package com.personal.chatroommobile.ui.account

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.personal.chatroommobile.R
import com.personal.chatroommobile.data.CacheData
import com.personal.chatroommobile.data.repositories.AccountRepository
import com.personal.chatroommobile.data.repositories.RelationshipRepository
import com.personal.chatroommobile.data.source.AccountDataSource
import com.personal.chatroommobile.data.source.RelationshipDataSource
import com.personal.chatroommobile.databinding.FragmentAccountBinding
import com.personal.chatroommobile.ui.account.placeholder.GroupContent
import kotlinx.coroutines.launch


class AccountFragment : Fragment() {

    private var columnCount = 1
    private var newImagePath: String = ""
    private lateinit var binding: FragmentAccountBinding
    private lateinit var currentActivity: FragmentActivity
    private lateinit var groupRecyclerView: GroupRecyclerViewAdapter

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
        binding = FragmentAccountBinding.inflate(inflater, container, false)

        return binding.root
    }

    private fun getRealPathFromURI(contentURI: Uri): String {
        val result: String
        val cursor: Cursor? = currentActivity.contentResolver.query(contentURI, null, null, null, null)

        if (cursor == null)
            result = contentURI.path.toString()
        else {
            cursor.moveToFirst()
            val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }

        return result
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 111 && resultCode == RESULT_OK) {
            newImagePath = data?.data?.let { getRealPathFromURI(it) }.toString()

            Glide.with(currentActivity)
                .load(data?.data)
                .into(binding.accountImage)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val addGroup = currentActivity.findViewById<Button>(R.id.add_group_button)
        val groupsLoading = currentActivity.findViewById<ProgressBar>(R.id.loading_groups)
        val groupsSearch = currentActivity.findViewById<SearchView>(R.id.groups_search)
        val recyclerView = currentActivity.findViewById<RecyclerView>(R.id.group_list)

        val accountViewModel = AccountViewModel(
            AccountRepository(
                AccountDataSource(CacheData.token)
            ),
            RelationshipRepository(
                RelationshipDataSource(CacheData.token)
            )
        )

        val updateAccountImage = binding.updateAccountImage
        val accountImage = binding.accountImage
        val accountUsername = binding.accountUsername
        val accountEmail = binding.accountEmail
        val editAccount = binding.editAccountButton
        val deleteAccount = binding.deleteAccountButton
        val editLoading = binding.editLoading
        val deleteLoading = binding.deleteLoading

        // Load user profile.

        accountViewModel.profile.observe(viewLifecycleOwner, Observer {
            val profile = it ?: return@Observer

            Glide.with(currentActivity)
                .load(profile.image)
                .into(accountImage)

            accountUsername.setText(profile.name)
            accountEmail.setText(profile.email)

            if (recyclerView is RecyclerView) {
                recyclerView.layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }

                GroupContent.foundGroups(profile.groups)

                groupRecyclerView =
                    GroupRecyclerViewAdapter(currentActivity,
                        GroupContent.ITEMS)

                recyclerView.adapter = groupRecyclerView
                groupsLoading.visibility = View.GONE
                groupsSearch.visibility = View.VISIBLE

                groupsSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(text: String?): Boolean {
                        groupRecyclerView.filter.filter(text)
                        return false
                    }
                })
            }
        })

        lifecycleScope.launch {
            accountViewModel.profile()
        }

        // Manage account data.

        accountViewModel.accountFormState.observe(currentActivity, Observer {
            val formState = it ?: return@Observer

            if (formState.usernameError != null) {
                accountUsername.error = getString(formState.usernameError)
            } else {
                accountUsername.error = null
            }

            if (formState.emailError != null) {
                accountEmail.error = getString(formState.emailError)
            } else {
                accountEmail.error = null
            }

            editAccount.isEnabled = formState.isDataValid
        })

        accountViewModel.accountChangeResult.observe(currentActivity, Observer {
            val accountChangeResult = it ?: return@Observer

            if (accountChangeResult.isForChangeDataAccount) {
                editLoading.visibility = View.GONE
                editAccount.visibility = View.VISIBLE
            }

            if (accountChangeResult.error != null) {
                Toast.makeText(
                    currentActivity.applicationContext,
                    accountChangeResult.error,
                    Toast.LENGTH_SHORT
                ).show()
            }

            if (accountChangeResult.warning != null) {
                Toast.makeText(
                    currentActivity.applicationContext,
                    accountChangeResult.warning,
                    Toast.LENGTH_SHORT
                ).show()
            }

            if (accountChangeResult.success != null) {
                if (accountChangeResult.isForChangeDataAccount) {
                    Toast.makeText(
                        currentActivity.applicationContext,
                        accountChangeResult.success,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            editAccount.isEnabled = accountChangeResult.isForChangeDataAccount
        })

        accountViewModel.createGroupResult.observe(currentActivity, Observer {
            val group = it ?: return@Observer

            groupRecyclerView.addGroup(group.id, group.name, group.image)
        })

        accountUsername.afterTextChanged {
            accountViewModel.accountDataChanged(
                accountUsername.text.toString(),
                accountEmail.text.toString()
            )
        }

        accountEmail.afterTextChanged {
            accountViewModel.accountDataChanged(
                accountUsername.text.toString(),
                accountEmail.text.toString()
            )
        }

        updateAccountImage.setOnClickListener {
            val intent = Intent()
                .setType("image/png")
                .setType("image/jpg")
                .setType("image/jpeg")
                .setAction(Intent.ACTION_GET_CONTENT)

            startActivityForResult(Intent.createChooser(intent, "Select an image"), 111)
        }

        editAccount.setOnClickListener {
            editAccount.visibility = View.GONE
            editLoading.visibility = View.VISIBLE

            lifecycleScope.launch {
                accountViewModel.updateProfile(
                    accountUsername.text.toString(),
                    accountEmail.text.toString(),
                    newImagePath
                )
            }
        }

        addGroup.setOnClickListener {
            val createGroupDialog = AlertDialog.Builder(currentActivity)
            val groupNameInput = EditText(currentActivity)
            val layout = LinearLayout(currentActivity)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

            createGroupDialog.setTitle("Create a new group")
            createGroupDialog.setView(groupNameInput)

            layoutParams.setMargins(40, 0, 40, 0)
            layout.orientation = LinearLayout.VERTICAL
            layout.addView(groupNameInput, layoutParams)

            groupNameInput.hint = "Group name ...";

            createGroupDialog.setView(layout)

            createGroupDialog
                .setMessage("To create a new group, you need to give it a name.")
                .setPositiveButton("Create") { dialog, _ ->
                    val name = groupNameInput.text.toString()

                    if (name.isNotEmpty()) {
                        lifecycleScope.launch {
                            accountViewModel.createGroup(name)
                        }
                    }

                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        deleteAccount.setOnClickListener {
            AlertDialog.Builder(activity)
                .setMessage("Are you sure you want to delete your account?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, _ ->
                    deleteAccount.visibility = View.GONE
                    deleteLoading.visibility = View.VISIBLE

                    lifecycleScope.launch {
                        accountViewModel.deleteAccount()
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