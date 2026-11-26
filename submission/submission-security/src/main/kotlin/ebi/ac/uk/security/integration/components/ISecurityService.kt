package ebi.ac.uk.security.integration.components

import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.integration.model.api.UserInfo
import uk.ac.ebi.biostd.api.security.ActivateByEmailRequest
import uk.ac.ebi.biostd.api.security.ChangePasswordRequest
import uk.ac.ebi.biostd.api.security.LoginRequest
import uk.ac.ebi.biostd.api.security.RegisterRequest
import uk.ac.ebi.biostd.api.security.ResetPasswordRequest
import uk.ac.ebi.biostd.api.security.RetryActivationRequest
import uk.ac.ebi.biostd.model.MigrateHomeOptions
import uk.ac.ebi.biostd.model.User

interface ISecurityService {
    suspend fun registerUser(request: RegisterRequest): SecurityUser

    fun login(request: LoginRequest): UserInfo

    fun logout(authToken: String)

    suspend fun activate(activationKey: String)

    fun generateActivationKey(request: ActivateByEmailRequest)

    fun retryRegistration(request: RetryActivationRequest)

    suspend fun changePassword(request: ChangePasswordRequest): User

    fun resetPassword(request: ResetPasswordRequest)

    suspend fun updateMagicFolder(
        email: String,
        migrateOptions: MigrateHomeOptions,
    )
}
