package ac.uk.ebi.biostd.groups.web

import ac.uk.ebi.biostd.submission.converters.BioUser
import ebi.ac.uk.model.Group
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiOperation
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import springfox.documentation.annotations.ApiIgnore

@Controller
@PreAuthorize("isAuthenticated()")
@Api(tags = ["Groups"])
@ApiIgnore
class GroupsResource {
    @GetMapping("/groups")
    @ResponseBody
    @ApiOperation("Get the groups associated to the user")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun getGroups(@BioUser user: SecurityUser): List<Group> =
        user.groupsFolders.map { Group(it.groupName, it.description) }
}
