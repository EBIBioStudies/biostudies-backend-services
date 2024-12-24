package ebi.ac.uk.security.integration.components

import ebi.ac.uk.api.security.ActivateByEmailRequest
import ebi.ac.uk.api.security.ChangePasswordRequest
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.ResetPasswordRequest
import ebi.ac.uk.api.security.RetryActivationRequest
import ebi.ac.uk.model.MigrateHomeOptions
import ebi.ac.uk.model.User
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.integration.model.api.UserInfo

@Suppress("TooManyFunctions")
interface ISecurityService {
    suspend fun registerUser(request: RegisterRequest): SecurityUser

    fun login(request: LoginRequest): UserInfo

    fun logout(authToken: String)

    suspend fun activate(activationKey: String)

    fun activateByEmail(request: ActivateByEmailRequest)

    suspend fun activateAndSetupPassword(request: ChangePasswordRequest): User

    fun retryRegistration(request: RetryActivationRequest)

    suspend fun changePassword(request: ChangePasswordRequest): User

    fun resetPassword(request: ResetPasswordRequest)

    suspend fun refreshUser(email: String): SecurityUser

    suspend fun updateMagicFolder(
        email: String,
        migrateOptions: MigrateHomeOptions,
    ): String
}
