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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "Security", description = "User registration, authentication and account management.")
@Suppress("TooManyFunctions")
class SecurityResource(
    private val securityMapper: SecurityMapper,
    private val securityService: ISecurityService,
    private val securityQueryService: SecurityQueryService,
) {
    @PostMapping(value = ["/register", "/signup"])
    @ResponseStatus(value = HttpStatus.CREATED)
    @Operation(summary = "Register User", description = "Create a new BioStudies user.")
    suspend fun register(
        @Valid @RequestBody register: RegisterRequest,
    ) {
        securityService.registerUser(register)
    }

    @PostMapping(value = ["/check-registration"])
    @ResponseBody
    @Operation(
        summary = "Check Registration",
        description =
            "Checks if a user with the given email is registered. If not, a new inactive user is created with the " +
                "given email and user name. The new user must be activated via an activation link.",
    )
    fun checkUser(
        @Valid @RequestBody register: CheckUserRequest,
    ): SecurityUser = securityQueryService.getOrCreateInactive(register.userEmail, register.userName)

    @PostMapping(value = ["/login", "/signin"])
    @ResponseBody
    @Operation(summary = "Login", description = "Retrieves the user authentication token.")
    fun login(
        @RequestBody loginRequest: LoginRequest,
    ): UserProfile = securityMapper.toUserProfile(securityService.login(loginRequest))

    @PostMapping(value = ["/logout", "/signout"])
    @ResponseBody
    @Operation(summary = "Logout", description = "Expires the given authorization token.")
    fun logout(
        @RequestBody logoutRequest: LogoutRequest,
    ) = securityService.logout(logoutRequest.sessid)

    @PostMapping(value = ["/activate"])
    @ResponseBody
    @Operation(
        summary = "Activate User By Email",
        description = "Triggers the process to set up a new password and activate a user.",
    )
    fun activate(
        @RequestBody request: ActivateByEmailRequest,
    ): Unit = securityService.activateByEmail(request)

    @PostMapping(value = ["/activate/{activationKey}"])
    @ResponseBody
    @Operation(summary = "Activate User", description = "Activates a newly created user.")
    suspend fun activateByActivationKey(
        @Parameter(description = "User activation key", required = true)
        @PathVariable activationKey: String,
    ) {
        securityService.activate(activationKey)
    }

    @PostMapping(value = ["/retryact"])
    @ResponseBody
    @Operation(
        summary = "Retry User Activation",
        description = "Send the activation email to a user to either activate the account or reset the password.",
    )
    fun retryActivation(
        @RequestBody request: RetryActivationRequest,
    ) = securityService.retryRegistration(request)

    @PostMapping(value = ["/password/reset"])
    @ResponseBody
    @Operation(
        summary = "Reset Password",
        description = "Reset the user password. An email is sent to the user to create a new password.",
    )
    fun resetPassword(
        @RequestBody request: ResetPasswordRequest,
    ) = securityService.resetPassword(request)

    @PostMapping(value = ["/password/change"])
    @ResponseBody
    @Operation(summary = "Change Password", description = "Change the user password.")
    suspend fun changePassword(
        @RequestBody request: ChangePasswordRequest,
    ): User = securityService.changePassword(request)

    @PostMapping(value = ["/password/setup"])
    @ResponseBody
    @Operation(
        summary = "Set Up Password",
        description = "Activates and sets up the password for the user with the given activation key.",
    )
    suspend fun setUpPassword(
        @RequestBody request: ChangePasswordRequest,
    ): User = securityService.activateAndSetupPassword(request)

    @GetMapping(value = ["/profile", "/check"])
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    @Operation(summary = "User Info", description = "Get the profile of the authenticated user.")
    fun userProfile(
        @Parameter(hidden = true) authentication: Authentication,
    ): UserProfile = securityMapper.toUserProfile(securityQueryService.getUserProfile(authentication.credentials as String))
}
