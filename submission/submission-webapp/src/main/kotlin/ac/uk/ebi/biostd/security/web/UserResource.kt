package ac.uk.ebi.biostd.security.web

import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class UserResource(val securityService: ISecurityService) {
    @PostMapping("/auth/refresh-user")
    @ResponseBody
    fun refreshUser(@RequestParam("email") email: String): SecurityUser = securityService.refreshUser(email)
}
