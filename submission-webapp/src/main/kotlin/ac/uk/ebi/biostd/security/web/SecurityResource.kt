package ac.uk.ebi.biostd.security.web

import ac.uk.ebi.biostd.persistence.model.User
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.LoginResponse
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.SignUpResponse
import ebi.ac.uk.security.service.SecurityService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/auth")
class SecurityResource(private val securityService: SecurityService) {

    @PostMapping(value = ["/signup", "/register"])
    @ResponseBody
    fun register(@RequestBody signUpRequest: RegisterRequest): SignUpResponse =
            securityService.registerUser(signUpRequest).run { toSignUpResponse(this) }

    @PostMapping(value = ["/signin", "/login"])
    @ResponseBody
    fun login(@RequestBody loginRequest: LoginRequest): LoginResponse =
            securityService.login(loginRequest).let { (user, token) -> toLoginResponse(user, token) }

    companion object {

        private fun toLoginResponse(user: User, token: String) =
                LoginResponse(sessid = token, email = user.email, username = user.login, secret = user.secret)

        private fun toSignUpResponse(user: User) = SignUpResponse(user.login)
    }
}
