package ac.uk.ebi.biostd.security.web

import ebi.ac.uk.api.security.ChangePasswordRequest
import ebi.ac.uk.api.security.CheckUserRequest
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.LogoutRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.ResetPasswordRequest
import ebi.ac.uk.api.security.RetryActivationRequest
import ebi.ac.uk.api.security.UserProfile
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.security.integration.components.ISecurityService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import javax.validation.Valid

@Controller
@Validated
@RequestMapping("/auth", produces = [APPLICATION_JSON])
class SecurityResource(
    private val securityMapper: SecurityMapper,
    private val securityService: ISecurityService,
    private val securityQueryService: ISecurityQueryService
) {
    @PostMapping(value = ["/signup", "/register"])
    @ResponseStatus(value = HttpStatus.CREATED)
    fun register(@Valid @RequestBody register: RegisterRequest) {
        securityService.registerUser(register)
    }

    @PostMapping(value = ["/check-registration"])
    fun checkUser(@Valid @RequestBody register: CheckUserRequest) {
        securityQueryService.getOrCreateInactive(register.userEmail, register.userName)
    }

    @PostMapping(value = ["/signin", "/login"])
    @ResponseBody
    fun login(@RequestBody loginRequest: LoginRequest): UserProfile =
        securityMapper.toUserProfile(securityService.login(loginRequest))

    @PostMapping(value = ["/signout", "/logout"])
    @ResponseBody
    fun logout(@RequestBody logoutRequest: LogoutRequest) = securityService.logout(logoutRequest.sessid)

    @PostMapping(value = ["/activate/{activationKey}"])
    @ResponseBody
    fun activate(@PathVariable activationKey: String): Unit = securityService.activate(activationKey)

    @PostMapping(value = ["/retryact"])
    @ResponseBody
    fun retryActivation(@RequestBody request: RetryActivationRequest) = securityService.retryRegistration(request)

    @PostMapping(value = ["/password/reset"])
    @ResponseBody
    fun resetPassword(@RequestBody request: ResetPasswordRequest) = securityService.resetPassword(request)

    @PostMapping(value = ["/password/change"])
    @ResponseBody
    fun changePassword(@RequestBody request: ChangePasswordRequest) = securityService.changePassword(request)

    @GetMapping(value = ["/check", "/profile"])
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    fun userProfile(authentication: Authentication): UserProfile =
        securityMapper.toUserProfile(securityQueryService.getUserProfile(authentication.credentials as String))
}
