package ac.uk.ebi.biostd.groups.web

import ebi.ac.uk.model.Group
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@PreAuthorize("isAuthenticated()")
class GroupsResource {

    @GetMapping("/groups")
    @ResponseBody
    fun getGroups(@AuthenticationPrincipal user: SecurityUser): List<Group> =
        user.groupsFolders.map { Group(it.groupName, it.description) }
}
