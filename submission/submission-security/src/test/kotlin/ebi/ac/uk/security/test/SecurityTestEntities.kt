package ebi.ac.uk.security.test

import ac.uk.ebi.biostd.persistence.model.User
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.ResetPasswordRequest
import ebi.ac.uk.api.security.RetryActivationRequest
import ebi.ac.uk.security.integration.model.api.MagicFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.nio.file.Paths

internal class SecurityTestEntities {

    companion object {
        const val userId = 55L
        const val username = "Jhon Doe"
        const val email = "Jhon.Doe@test.com"
        const val password = "abc123"
        const val instanceKey = "12345"
        const val path = "/activate_url_path"

        val magicFolderPath = Paths.get("/abc/user-folder")
        val magicFolderRelativePath = Paths.get("user-folder")

        const val adminId = 70L

        val registrationRequest: RegisterRequest
            get() = RegisterRequest(username, email, password)

        val preRegisterRequest: RegisterRequest
            get() = RegisterRequest(
                username = username,
                email = email,
                password = password,
                instanceKey = instanceKey,
                path = path)

        val resetPasswordRequest: ResetPasswordRequest
            get() = ResetPasswordRequest(
                email = email,
                instanceKey = instanceKey,
                path = path)

        val retryActivation: RetryActivationRequest
            get() = RetryActivationRequest(
                email = email,
                instanceKey = instanceKey,
                path = path)

        const val secret = "secret"
        val passwordDiggest = ByteArray(0)

        val simpleUser: User by lazy {
            User(
                id = userId,
                email = email,
                fullName = username,
                secret = secret,
                passwordDigest = passwordDiggest)
        }

        val securityUser: SecurityUser by lazy {
            SecurityUser(
                id = userId,
                email = email,
                login = null,
                fullName = username,
                secret = secret,
                permissions = emptySet(),
                groupsFolders = emptyList(),
                magicFolder = MagicFolder(magicFolderPath, magicFolderRelativePath),
                superuser = false
            )
        }

        val adminUser: User by lazy {
            User(
                id = adminId,
                email = email,
                fullName = username,
                secret = secret,
                passwordDigest = passwordDiggest,
                superuser = true)
        }
    }
}
