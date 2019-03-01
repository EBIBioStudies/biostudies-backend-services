package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.UserGroup
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserGroupDataRepository
import java.util.UUID

class GroupService(
    private val groupRepository: UserGroupDataRepository,
    private val userRepository: UserDataRepository
) {

    // TODO: add logic to add create secret folder
    fun creatGroup(groupName: String): UserGroup {
        val group = UserGroup()
        group.name = groupName
        group.secret = UUID.randomUUID().toString()
        return groupRepository.save(group)
    }

    // TODO: add not existing group not existing user handling
    fun addUserInGroup(groupName: String, userEmail: String) {
        val group = groupRepository.getByName(groupName)
        val user = userRepository.getByEmail(userEmail)
        user.addGroup(group)
        userRepository.save(user)
        group.users.add(user)
        groupRepository.save(group)
    }
}
