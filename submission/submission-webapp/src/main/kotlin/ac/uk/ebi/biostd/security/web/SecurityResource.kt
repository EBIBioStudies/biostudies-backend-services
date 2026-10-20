package ac.uk.ebi.biostd.security.web

import ebi.ac.uk.api.security.ActivateByEmailRequest
import ebi.ac.uk.api.security.ChangePasswordRequest
import ebi.ac.uk.api.security.CheckUserRequest
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.LogoutRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.ResetPasswordRequest
import ebi.ac.uk.api.security.RetryActivationRequest
import ebi.ac.uk.api.security.UserProfile
import ebi.ac.uk.model.User
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.components.SecurityQueryService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth", produces = [APPLICATION_JSON])
@Suppress("TooManyFunctions")
class SecurityResource(
    private val securityMapper: SecurityMapper,
    private val securityService: ISecurityService,
    private val securityQueryService: SecurityQueryService,
) {
    @PostMapping("/register")
    @ResponseStatus(value = HttpStatus.CREATED)
    suspend fun register(
        @Valid @RequestBody register: RegisterRequest,
    ) {
        securityService.registerUser(register)
    }

    @PostMapping("/check-registration")
    @ResponseBody
    fun checkUser(
        @Valid @RequestBody register: CheckUserRequest,
    ): SecurityUser = securityQueryService.getOrCreateInactive(register.userEmail, register.userName)

    @PostMapping("/login")
    @ResponseBody
    fun login(
        @RequestBody loginRequest: LoginRequest,
    ): UserProfile = securityMapper.toUserProfile(securityService.login(loginRequest))

    @PostMapping("/logout")
    @ResponseBody
    fun logout(
        @RequestBody logoutRequest: LogoutRequest,
    ) = securityService.logout(logoutRequest.sessid)

    @PostMapping("/activate")
    @ResponseBody
    fun activate(
        @RequestBody request: ActivateByEmailRequest,
    ): Unit = securityService.generateActivationKey(request)

    @PostMapping("/activate/{activationKey}")
    @ResponseBody
    suspend fun activateByActivationKey(
        @PathVariable activationKey: String,
    ) {
        securityService.activate(activationKey)
    }

    @PostMapping("/retryact")
    @ResponseBody
    fun retryActivation(
        @RequestBody request: RetryActivationRequest,
    ) = securityService.retryRegistration(request)

    @PostMapping("/password/reset")
    @ResponseBody
    fun resetPassword(
        @RequestBody request: ResetPasswordRequest,
    ) = securityService.resetPassword(request)

    @PostMapping("/password/change")
    @ResponseBody
    suspend fun changePassword(
        @RequestBody request: ChangePasswordRequest,
    ): User = securityService.changePassword(request)

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    fun userProfile(authentication: Authentication): UserProfile =
        securityMapper.toUserProfile(securityQueryService.getUserProfile(authentication.credentials as String))
}
