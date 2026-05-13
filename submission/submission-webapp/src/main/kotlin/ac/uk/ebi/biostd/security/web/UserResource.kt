package ac.uk.ebi.biostd.security.web

import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.model.api.RefreshUserRequest
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Security", description = "User registration, authentication and account management.")
class UserResource(val securityService: ISecurityService) {
    @PostMapping("/auth/refresh-user")
    @ResponseBody
    @Operation(
        summary = "Refresh User",
        description = "Refresh cached security details for a registered user and return the updated security profile.",
    )
    suspend fun refreshUser(
        @RequestBody request: RefreshUserRequest,
    ): SecurityUser {
        return securityService.refreshUser(request.email)
    }
}
