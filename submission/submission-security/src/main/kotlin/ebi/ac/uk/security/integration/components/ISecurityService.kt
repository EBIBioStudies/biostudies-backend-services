package ebi.ac.uk.security.integration.components

import ebi.ac.uk.api.security.ChangePasswordRequest
import ebi.ac.uk.api.security.GetOrRegisterUserRequest
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.ResetPasswordRequest
import ebi.ac.uk.api.security.RetryActivationRequest
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.integration.model.api.UserInfo

@Suppress("TooManyFunctions")
interface ISecurityService {
    fun registerUser(request: RegisterRequest): SecurityUser
    fun getUser(email: String): SecurityUser
    fun login(request: LoginRequest): UserInfo
    fun logout(authToken: String)
    fun activate(activationKey: String)
    fun retryRegistration(request: RetryActivationRequest)
    fun changePassword(request: ChangePasswordRequest)
    fun resetPassword(request: ResetPasswordRequest)
    fun getUserProfile(authToken: String): UserInfo
    fun getOrCreateInactive(email: String, username: String): SecurityUser
    fun getOrRegisterUser(request: GetOrRegisterUserRequest): SecurityUser
    fun refreshUser(email: String): SecurityUser
}
