package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.submission.domain.service.ExtUserService
import ebi.ac.uk.extended.model.ExtUser
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/security/users/extended")
@Api(tags = ["Extended User"])
class ExtUserResource(private val extUserService: ExtUserService) {
    @GetMapping("/{id}")
    @ApiOperation("Get the extended information for a user")
    fun getExtUser(
        @ApiParam(name = "id", value = "The user id")
        @PathVariable id: Long
    ): ExtUser = extUserService.getExtUser(id)
}
