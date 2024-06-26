package ac.uk.ebi.biostd.security.web

import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.model.api.RefreshUserRequest
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserResource(val securityService: ISecurityService) {
    @PostMapping("/auth/refresh-user")
    @ResponseBody
    suspend fun refreshUser(
        @RequestBody request: RefreshUserRequest,
    ): SecurityUser {
        return securityService.refreshUser(request.email)
    }
}
