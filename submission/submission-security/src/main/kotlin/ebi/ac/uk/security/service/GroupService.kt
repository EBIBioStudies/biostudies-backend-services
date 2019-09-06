package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.UserGroup
import ac.uk.ebi.biostd.persistence.model.ext.addGroup
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserGroupDataRepository
import ebi.ac.uk.security.integration.components.IGroupService
import java.util.UUID

internal class GroupService(
    private val groupRepository: UserGroupDataRepository,
    private val userRepository: UserDataRepository
) : IGroupService {

    // TODO: add logic to add create secret folder
    override fun creatGroup(groupName: String, description: String): UserGroup {
        return groupRepository.save(UserGroup(groupName, description, UUID.randomUUID().toString()))
    }

    // TODO: add not existing group not existing user handling
    override fun addUserInGroup(groupName: String, userEmail: String) {
        val group = groupRepository.getByName(groupName)
        val user = userRepository.getByEmail(userEmail)
        user.addGroup(group)
        userRepository.save(user)
        group.users.add(user)
        groupRepository.save(group)
    }
}
