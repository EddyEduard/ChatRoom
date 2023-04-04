package com.personal.chatroommobile.ui.group

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.chatroommobile.R
import com.personal.chatroommobile.data.Result
import com.personal.chatroommobile.data.repositories.RelationshipRepository
import kotlinx.coroutines.*

class GroupViewModel(private val relationshipRepository: RelationshipRepository) : ViewModel() {

    private val _groupFormState = MutableLiveData<GroupFormState>()
    val groupFormState: LiveData<GroupFormState> = _groupFormState

    private val _groupChangeResult = MutableLiveData<GroupChangeResult>()
    val groupChangeResult: LiveData<GroupChangeResult> = _groupChangeResult

    private val _membersResult = MutableLiveData<ArrayList<MemberItemView>>()
    val membersResult: LiveData<ArrayList<MemberItemView>> = _membersResult

    /**
     * Get members from a group.
     *
     * @param groupId
     * @return members
     * */
    @OptIn(DelicateCoroutinesApi::class)
    fun members(groupId: Int) {
        viewModelScope.launch {
            GlobalScope.launch {
                suspend {
                    val result = relationshipRepository.members(groupId)

                    withContext(Dispatchers.Main) {
                        if (result is Result.Success) {
                            val members: ArrayList<MemberItemView> = ArrayList()
                            var position = 0

                            result.data.forEach {
                                members.add(MemberItemView(it.id,
                                    it.name,
                                    it.email,
                                    it.image,
                                    true,
                                    position))
                                position++
                            }

                            _membersResult.value = members
                        }
                    }
                }.invoke()
            }
        }
    }

    /**
     * Get users.
     *
     * @param members
     * @param userId
     * */
    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(DelicateCoroutinesApi::class)
    fun users(members: ArrayList<MemberItemView>, userId: Int) {
        viewModelScope.launch {
            GlobalScope.launch {
                suspend {
                    val result = relationshipRepository.users()

                    withContext(Dispatchers.Main) {
                        if (result is Result.Success) {
                            val memberList: ArrayList<MemberItemView> = ArrayList()
                            var position = 0

                            result.data.forEach {
                                if (it.id != userId) {
                                    if (members.find { it_ -> it_.id == it.id } == null) {
                                        memberList.add(MemberItemView(
                                            it.id,
                                            it.name,
                                            it.email,
                                            it.image,
                                            false,
                                            position
                                        ))

                                        position++
                                    }
                                }
                            }

                            _membersResult.value = memberList
                        }
                    }
                }.invoke()
            }
        }
    }

    /**
     * Update profile group.
     *
     * @param groupId
     * @param name
     * @param imagePath
     * */
    @OptIn(DelicateCoroutinesApi::class)
    fun updateProfileGroup(groupId: Int, name: String, imagePath: String) {
        viewModelScope.launch {
            GlobalScope.launch {
                suspend {
                    val result = relationshipRepository.updateProfileGroup(groupId, name, imagePath)

                    withContext(Dispatchers.Main) {
                        if (result is Result.Success) {
                            _groupChangeResult.value = GroupChangeResult(success = result.data,
                                isForChangeDataGroup = true)
                        } else {
                            if (result is Result.Warning)
                                _groupChangeResult.value =
                                    GroupChangeResult(warning = result.warning,
                                        isForChangeDataGroup = true)
                            else
                                _groupChangeResult.value =
                                    GroupChangeResult(error = R.string.update_profile_failed,
                                        isForChangeDataGroup = true)
                        }
                    }
                }.invoke()
            }
        }
    }

    /**
     * Remove member from group.
     *
     * @param groupId
     * @param userId
     * */
    @OptIn(DelicateCoroutinesApi::class)
    fun addMember(groupId: Int, userId: Int) {
        viewModelScope.launch {
            GlobalScope.launch {
                suspend {
                    val result = relationshipRepository.addMemberToGroup(groupId, userId)

                    withContext(Dispatchers.Main) {
                        if (result is Result.Success) {
                            _groupChangeResult.value =
                                GroupChangeResult(success = result.data, isForAddMember = true)
                        } else {
                            if (result is Result.Warning)
                                _groupChangeResult.value =
                                    GroupChangeResult(warning = result.warning,
                                        isForAddMember = true)
                            else
                                _groupChangeResult.value =
                                    GroupChangeResult(error = R.string.add_member_failed,
                                        isForAddMember = true)
                        }
                    }
                }.invoke()
            }
        }
    }

    /**
     * Remove member from group.
     *
     * @param groupId
     * @param userId
     * */
    @OptIn(DelicateCoroutinesApi::class)
    fun removeMember(groupId: Int, userId: Int) {
        viewModelScope.launch {
            GlobalScope.launch {
                suspend {
                    val result = relationshipRepository.removeMemberFromGroup(groupId, userId)

                    withContext(Dispatchers.Main) {
                        if (result is Result.Success) {
                            _groupChangeResult.value =
                                GroupChangeResult(success = result.data, isForRemoveMember = true)
                        } else {
                            if (result is Result.Warning)
                                _groupChangeResult.value =
                                    GroupChangeResult(warning = result.warning,
                                        isForRemoveMember = true)
                            else
                                _groupChangeResult.value =
                                    GroupChangeResult(error = R.string.remove_member_failed,
                                        isForRemoveMember = true)
                        }
                    }
                }.invoke()
            }
        }
    }

    /**
     * Delete group.
     *
     * @param groupId
     * */
    @OptIn(DelicateCoroutinesApi::class)
    fun deleteGroup(groupId: Int) {
        viewModelScope.launch {
            GlobalScope.launch {
                suspend {
                    val result = relationshipRepository.deleteGroup(groupId)

                    withContext(Dispatchers.Main) {
                        if (result is Result.Success) {
                            _groupChangeResult.value = GroupChangeResult(success = result.data,
                                isForDeleteGroup = true)
                        } else {
                            if (result is Result.Warning)
                                _groupChangeResult.value =
                                    GroupChangeResult(warning = result.warning,
                                        isForDeleteGroup = true)
                            else
                                _groupChangeResult.value =
                                    GroupChangeResult(error = R.string.delete_group_failed,
                                        isForDeleteGroup = true)
                        }
                    }
                }.invoke()
            }
        }
    }

    /**
     * Validate group data when is changed.
     *
     * @param name
     * */
    fun groupDataChanged(name: String) {
        if (!isNameValid(name)) {
            _groupFormState.value = GroupFormState(nameError = R.string.invalid_group_name)
        } else {
            _groupFormState.value = GroupFormState(isDataValid = true)
        }
    }

    /**
     * A placeholder name validation check.
     *
     * @param name
     * @return true if name is valid or false otherwise
     * */
    private fun isNameValid(name: String): Boolean {
        return name.replace(" ", "").length >= 3
    }
}