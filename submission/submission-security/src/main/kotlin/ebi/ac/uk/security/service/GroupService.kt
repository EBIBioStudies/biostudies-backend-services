package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.DbUserGroup
import ac.uk.ebi.biostd.persistence.model.addGroup
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserGroupDataRepository
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.security.exception.GroupsGroupDoesNotExistsException
import ebi.ac.uk.security.exception.GroupsUserDoesNotExistsException
import ebi.ac.uk.security.integration.components.IGroupService
import java.nio.file.Paths
import java.util.UUID

class GroupService(
    private val groupRepository: UserGroupDataRepository,
    private val userRepository: UserDataRepository,
    private val filesDirPath: String,
) : IGroupService {
    override fun createGroup(
        groupName: String,
        description: String,
    ): DbUserGroup {
        val savedGroup = groupRepository.save(DbUserGroup(groupName, description, UUID.randomUUID().toString()))
        FileUtils.createEmptyFolder(groupMagicFolder(savedGroup), RWXR_XR_X)
        return savedGroup
    }

    override fun addUserInGroup(
        groupName: String,
        userEmail: String,
    ) {
        val group = groupRepository.findByName(groupName) ?: throw GroupsGroupDoesNotExistsException(groupName)
        val user = userRepository.findByEmail(userEmail) ?: throw GroupsUserDoesNotExistsException(userEmail)
        userRepository.save(user.addGroup(group))
    }

    private fun groupMagicFolder(it: DbUserGroup) = Paths.get("$filesDirPath/${magicPath(it.secret, it.id, "b")}")

    private fun magicPath(
        secret: String,
        id: Long,
        suffix: String,
    ) = "${secret.take(2)}/${secret.drop(2)}-$suffix$id"
}
