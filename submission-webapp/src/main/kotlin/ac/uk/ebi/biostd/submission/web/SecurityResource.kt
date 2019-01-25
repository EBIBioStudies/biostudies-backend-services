package ac.uk.ebi.biostd.submission.web

import ebi.ac.uk.security.service.SecurityService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/security")
class SecurityResource(private val securityService: SecurityService) {
    @PostMapping("/token")
    fun getAuthToken(@RequestBody tokenRequest: TokenRequest) =
        securityService.login(tokenRequest.user, tokenRequest.password)
}

data class TokenRequest(val user: String, val password: String)
