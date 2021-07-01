package ebi.ac.uk.security.test

import ac.uk.ebi.biostd.persistence.model.DbUser
import ebi.ac.uk.api.security.ActivateByEmailRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.ResetPasswordRequest
import ebi.ac.uk.api.security.RetryActivationRequest
import ebi.ac.uk.security.integration.model.api.MagicFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.nio.file.Path
import java.nio.file.Paths

internal class SecurityTestEntities {

    companion object {
        const val userId = 55L
        const val name = "BioStudies Developer"
        const val email = "biostudies-dev@ebi.ac.uk"
        const val password = "abc123"
        const val instanceKey = "12345"
        const val path = "/activate_url_path"
        const val captcha = "captcha-key"

        val magicFolderPath: Path = Paths.get("/abc/user-folder")
        val magicFolderRelativePath: Path = Paths.get("user-folder")

        const val adminId = 70L

        val registrationRequest: RegisterRequest
            get() = RegisterRequest(name, email, password, captcha = captcha)

        val preRegisterRequest: RegisterRequest
            get() = RegisterRequest(
                name = name,
                email = email,
                password = password,
                instanceKey = instanceKey,
                captcha = captcha,
                path = path
            )

        val resetPasswordRequest: ResetPasswordRequest
            get() = ResetPasswordRequest(
                email = email,
                instanceKey = instanceKey,
                path = path,
                captcha = captcha
            )

        val activateByEmailRequest: ActivateByEmailRequest
            get() = ActivateByEmailRequest(
                email = email,
                instanceKey = instanceKey,
                path = path
            )

        val retryActivation: RetryActivationRequest
            get() = RetryActivationRequest(
                email = email,
                instanceKey = instanceKey,
                path = path
            )

        const val secret = "secret"
        val passwordDiggest = ByteArray(0)

        val simpleUser: DbUser by lazy {
            DbUser(
                id = userId,
                email = email,
                fullName = name,
                secret = secret,
                passwordDigest = passwordDiggest
            )
        }

        fun securityUser(): SecurityUser =
            SecurityUser(
                id = userId,
                email = email,
                login = null,
                fullName = name,
                secret = secret,
                permissions = emptySet(),
                groupsFolders = emptyList(),
                magicFolder = MagicFolder(magicFolderPath, magicFolderRelativePath),
                superuser = false,
                notificationsEnabled = false
            )

        val adminUser: DbUser by lazy {
            DbUser(
                id = adminId,
                email = email,
                fullName = name,
                secret = secret,
                passwordDigest = passwordDiggest,
                superuser = true
            )
        }
    }
}
