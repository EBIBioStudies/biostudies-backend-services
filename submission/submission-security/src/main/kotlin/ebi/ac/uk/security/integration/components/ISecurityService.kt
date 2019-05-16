package ebi.ac.uk.security.integration.components

import ac.uk.ebi.biostd.persistence.model.User
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.ResetPasswordRequest
import ebi.ac.uk.api.security.RetryActivationRequest
import ebi.ac.uk.security.integration.model.api.UserInfo

interface ISecurityService {

    fun registerUser(request: RegisterRequest): User

    fun login(loginRequest: LoginRequest): UserInfo

    fun logout(authToken: String)

    fun activate(activationKey: String)

    fun retryPreRegistration(retryActivation: RetryActivationRequest)

    fun changePassword(activationKey: String, password: String)

    fun resetPassword(resetPasswordRequest: ResetPasswordRequest)
}
