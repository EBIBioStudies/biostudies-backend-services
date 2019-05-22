package ac.uk.ebi.biostd.security.web

import ebi.ac.uk.api.security.ChangePasswordRequest
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.LogoutRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.RegisterResponse
import ebi.ac.uk.api.security.ResetPasswordRequest
import ebi.ac.uk.api.security.RetryActivationRequest
import ebi.ac.uk.api.security.UserProfile
import ebi.ac.uk.security.integration.components.ISecurityService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/auth")
class SecurityResource(
    private val securityService: ISecurityService,
    private val securityMapper: SecurityMapper) {

    @PostMapping(value = ["/signup", "/register"])
    @ResponseBody
    fun register(@RequestBody register: RegisterRequest): RegisterResponse =
        securityMapper.toSignUpResponse(securityService.registerUser(register))

    @PostMapping(value = ["/signin", "/login"])
    @ResponseBody
    fun login(@RequestBody loginRequest: LoginRequest): UserProfile =
        securityMapper.toUserProfile(securityService.login(loginRequest))

    @PostMapping(value = ["/signout", "/logout"])
    @ResponseBody
    fun logout(@RequestBody logoutRequest: LogoutRequest): Unit = securityService.logout(logoutRequest.sessid)

    @PostMapping(value = ["/activate/{activationKey}"])
    @ResponseBody
    fun activate(@PathVariable activationKey: String): Unit = securityService.activate(activationKey)

    @PostMapping(value = ["/retryact"])
    @ResponseBody
    fun retryActivation(@RequestBody request: RetryActivationRequest): Unit = securityService.retryRegistration(request)

    @PostMapping(value = ["/passreset"])
    @ResponseBody
    fun resetPassword(@RequestBody request: ResetPasswordRequest): Unit = securityService.resetPassword(request)

    @PostMapping(value = ["/passrstreq", "/changePassword"])
    @ResponseBody
    fun changePassword(@RequestBody request: ChangePasswordRequest): Unit = securityService.changePassword(request)

    @PostMapping(value = ["/check", "/profile"])
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    fun userProfile(authentication: Authentication): UserProfile =
        securityMapper.toUserProfile(securityService.getUserProfile(authentication.credentials as String))
}
