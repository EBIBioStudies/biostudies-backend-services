package ac.uk.ebi.biostd.groups.web

import ac.uk.ebi.biostd.persistence.model.DbUserGroup
import ac.uk.ebi.biostd.submission.converters.BioUser
import ebi.ac.uk.api.dto.UserGroupDto
import ebi.ac.uk.model.Group
import ebi.ac.uk.security.exception.GroupsGroupDescriptionMustNotBeNullException
import ebi.ac.uk.security.exception.GroupsGroupNameMustNotBeNullException
import ebi.ac.uk.security.exception.GroupsUserNameMustNotBeNullException
import ebi.ac.uk.security.integration.components.IGroupService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@PreAuthorize("isAuthenticated()")
class GroupsResource(private val groupService: IGroupService) {
    @GetMapping("/groups")
    @ResponseBody
    fun getGroups(
        @BioUser user: SecurityUser,
    ): List<Group> = user.groupsFolders.map { Group(it.groupName, it.description) }

    @PostMapping("/groups")
    @ResponseBody
    @PreAuthorize("hasAuthority('ADMIN')")
    fun createGroup(
        @RequestBody request: GroupRequest,
    ): UserGroupDto {
        requireNotNull(request.groupName) { throw GroupsGroupNameMustNotBeNullException() }
        requireNotNull(request.description) { throw GroupsGroupDescriptionMustNotBeNullException() }
        return groupService.createGroup(request.groupName, request.description).toUserGroupDto()
    }

    @PutMapping("/groups")
    @ResponseBody
    @PreAuthorize("hasAuthority('ADMIN')")
    fun addUserInGroup(
        @RequestBody request: GroupRequest,
    ) {
        requireNotNull(request.groupName) { throw GroupsGroupNameMustNotBeNullException() }
        requireNotNull(request.userName) { throw GroupsUserNameMustNotBeNullException() }
        return groupService.addUserInGroup(request.groupName, request.userName)
    }

    private fun DbUserGroup.toUserGroupDto() = UserGroupDto(name, description)
}

data class GroupRequest(
    val groupName: String?,
    val description: String?,
    val userName: String?,
)
