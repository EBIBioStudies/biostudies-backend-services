package ac.uk.ebi.biostd.groups.web

import ac.uk.ebi.biostd.persistence.model.UserGroup
import ac.uk.ebi.biostd.submission.converters.BioUser
import ebi.ac.uk.api.dto.UserGroupDto
import ebi.ac.uk.model.Group
import ebi.ac.uk.security.integration.components.IGroupService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
@PreAuthorize("isAuthenticated()")
class GroupsResource(private val groupService: IGroupService) {
    @GetMapping("/groups")
    @ResponseBody
    fun getGroups(
        @BioUser user: SecurityUser
    ): List<Group> = user.groupsFolders.map { Group(it.groupName, it.description) }

    @PostMapping("/groups")
    @ResponseBody
    fun createGroup(
        @RequestBody request: GroupRequest
    ): UserGroupDto {
        requireNotNull(request.groupName) { "group name must not be null" }
        requireNotNull(request.description) { "description must not be null" }
        return groupService.createGroup(request.groupName, request.description).toUserGroupDto()
    }

    @PutMapping("/groups/{groupName}")
    @ResponseBody
    fun addUserInGroup(
        @PathVariable groupName: String,
        @RequestBody request: GroupRequest
    ) {
        requireNotNull(request.userName) { "user name must not be null" }
        return groupService.addUserInGroup(groupName, request.userName)
    }
    private fun UserGroup.toUserGroupDto() = UserGroupDto(id, name, description)
}

data class GroupRequest(
    val groupName: String?,
    val description: String?,
    val userName: String?
)
