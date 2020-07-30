package ac.uk.ebi.biostd.security.web

import ac.uk.ebi.biostd.common.config.PublicResource
import ebi.ac.uk.api.security.ChangePasswordRequest
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.LogoutRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.ResetPasswordRequest
import ebi.ac.uk.api.security.RetryActivationRequest
import ebi.ac.uk.api.security.UserProfile
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.security.integration.components.ISecurityService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
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
@Api(tags = ["Security"])
@PublicResource
class SecurityResource(
    private val securityService: ISecurityService,
    private val securityMapper: SecurityMapper
) {
    @PostMapping(value = ["/signup", "/register"])
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiOperation("Register a user")
    fun register(
        @ApiParam(name = "User Info", value = "Information for the new user")
        @Valid @RequestBody register: RegisterRequest
    ) {
        securityService.registerUser(register)
    }

    @PostMapping(value = ["/signin", "/login"])
    @ResponseBody
    @ApiOperation("Authenticate")
    fun login(
        @ApiParam(name = "Login Request", value = "Authentication credentials")
        @RequestBody loginRequest: LoginRequest
    ): UserProfile = securityMapper.toUserProfile(securityService.login(loginRequest))

    @PostMapping(value = ["/signout", "/logout"])
    @ResponseBody
    @ApiOperation("Sign Out")
    fun logout(
        @ApiParam(name = "Logout Request", value = "Session id to expire")
        @RequestBody logoutRequest: LogoutRequest
    ): Unit = securityService.logout(logoutRequest.sessid)

    @PostMapping(value = ["/activate/{activationKey}"])
    @ResponseBody
    @ApiOperation("Activate a user previously registered")
    fun activate(
        @ApiParam(name = "Activation Key", value = "Activation key of the user to be activated")
        @PathVariable activationKey: String
    ): Unit = securityService.activate(activationKey)

    @PostMapping(value = ["/retryact"])
    @ResponseBody
    @ApiOperation("Send the activation E-Mail to a user either to activate the account or reset the password")
    fun retryActivation(
        @ApiParam(name = "Retry Activation Request", value = "User information in order to send the email")
        @RequestBody request: RetryActivationRequest
    ): Unit = securityService.retryRegistration(request)

    @PostMapping(value = ["/password/reset"])
    @ResponseBody
    @ApiOperation("Reset the password of a user")
    fun resetPassword(
        @ApiParam(name = "Reset Request", value = "Information of the user to reset the password")
        @RequestBody request: ResetPasswordRequest
    ): Unit = securityService.resetPassword(request)

    @PostMapping(value = ["/password/change"])
    @ResponseBody
    @ApiOperation("Change the password of a user")
    fun changePassword(
        @ApiParam(name = "Change Password Request", value = "Information to reset the password")
        @RequestBody request: ChangePasswordRequest
    ): Unit = securityService.changePassword(request)

    @GetMapping(value = ["/check", "/profile"])
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    @ApiOperation("Get the information of a user")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun userProfile(authentication: Authentication): UserProfile =
        securityMapper.toUserProfile(securityService.getUserProfile(authentication.credentials as String))
}
