package ac.uk.ebi.biostd.groups.web

import ac.uk.ebi.biostd.persistence.model.UserGroup
import ac.uk.ebi.biostd.submission.converters.BioUser
import ebi.ac.uk.model.Group
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.SUBMISSION_TYPE
import ebi.ac.uk.model.constants.TEXT_XML
import ebi.ac.uk.security.integration.components.IGroupService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@PreAuthorize("isAuthenticated()")
class GroupsResource(private val groupService: IGroupService) {
    @GetMapping("/groups")
    @ResponseBody
    fun getGroups(
        @BioUser user: SecurityUser
    ): List<Group> = user.groupsFolders.map { Group(it.groupName, it.description) }

    @PostMapping("/group/{groupName}")
    @ResponseBody
    fun createGroup(
        @BioUser user: SecurityUser,
        @PathVariable groupName: String,
        @RequestParam description: String
    ): UserGroup = groupService.createGroup(groupName, description)

    @PostMapping("/group/{groupName}")
    fun addUserInGroup(
        @BioUser user: SecurityUser,
        @PathVariable groupName: String,
        @RequestParam userName: String
    ) = groupService.addUserInGroup(groupName, userName)
}
