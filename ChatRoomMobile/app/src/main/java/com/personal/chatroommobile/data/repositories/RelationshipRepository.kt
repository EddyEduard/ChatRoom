package com.personal.chatroommobile.data.repositories

import com.personal.chatroommobile.data.Result
import com.personal.chatroommobile.data.model.Group
import com.personal.chatroommobile.data.model.User
import com.personal.chatroommobile.data.source.RelationshipDataSource

class RelationshipRepository(private val relationshipDataSource: RelationshipDataSource) {

    /**
     * Get users.
     *
     * @return users
     * */
    fun users(): Result<ArrayList<User>> {
        return relationshipDataSource.users()
    }

    /**
     * Get contacts.
     *
     * @return contacts
     * */
    fun contacts(): Result<Pair<ArrayList<User>, ArrayList<Group>>> {
        return relationshipDataSource.contacts()
    }

    /**
     * Get members.
     *
     * @param groupId
     * @return members
     * */
    fun members(groupId: Int): Result<ArrayList<User>> {
        return relationshipDataSource.members(groupId)
    }

    /**
     * Create group.
     *
     * @param name
     * */
    fun createGroup(name: String): Result<Group> {
        return relationshipDataSource.createGroup(name)
    }

    /**
     * Update profile group.
     *
     * @param groupId
     * @param name
     * @param imagePath
     * */
    fun updateProfileGroup(groupId: Int, name: String, imagePath: String): Result<String> {
        if (imagePath.isNotEmpty())
            relationshipDataSource.updateProfileGroupImage(groupId, imagePath)

        return relationshipDataSource.updateProfileGroup(groupId, name)
    }

    /**
     * Add a new member into a group.
     *
     * @param groupId
     * @param userId
     * */
    fun addMemberToGroup(groupId: Int, userId: Int): Result<String> {
        return relationshipDataSource.addMemberToGroup(groupId, userId)
    }

    /**
     * Remove a member from group.
     *
     * @param groupId
     * @param userId
     * */
    fun removeMemberFromGroup(groupId: Int, userId: Int): Result<String> {
        return relationshipDataSource.removeMemberFromGroup(groupId, userId)
    }

    /**
     * Delete a group.
     *
     * @param groupId
     * */
    fun deleteGroup(groupId: Int): Result<String> {
        return relationshipDataSource.deleteGroup(groupId)
    }
}