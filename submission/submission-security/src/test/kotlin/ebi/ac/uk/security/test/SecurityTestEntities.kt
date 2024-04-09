package ebi.ac.uk.security.test

import ac.uk.ebi.biostd.common.properties.StorageMode
import ac.uk.ebi.biostd.persistence.model.DbUser
import ebi.ac.uk.api.security.ActivateByEmailRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.ResetPasswordRequest
import ebi.ac.uk.api.security.RetryActivationRequest

internal class SecurityTestEntities {
    companion object {
        const val USER_ID = 55L
        const val NAME = "BioStudies Developer"
        const val EMAIL = "biostudies-dev@ebi.ac.uk"
        const val ORCID = "0000-0002-1825-0097"
        const val PASSWORD = "abc123"
        const val INSTANCE_KEY = "12345"
        const val PATH = "/activate_url_path"
        const val CAPTCHA = "captcha-key"
        const val ADMIN_ID = 70L
        const val SECRET = "secret"

        val registrationRequest: RegisterRequest
            get() = RegisterRequest(NAME, EMAIL, PASSWORD, orcid = ORCID, captcha = CAPTCHA)

        val preRegisterRequest: RegisterRequest
            get() =
                RegisterRequest(
                    name = NAME,
                    email = EMAIL,
                    password = PASSWORD,
                    instanceKey = INSTANCE_KEY,
                    captcha = CAPTCHA,
                    path = PATH,
                )

        val resetPasswordRequest: ResetPasswordRequest
            get() =
                ResetPasswordRequest(
                    email = EMAIL,
                    instanceKey = INSTANCE_KEY,
                    path = PATH,
                    captcha = CAPTCHA,
                )

        val activateByEmailRequest: ActivateByEmailRequest
            get() =
                ActivateByEmailRequest(
                    email = EMAIL,
                    instanceKey = INSTANCE_KEY,
                    path = PATH,
                )

        val retryActivation: RetryActivationRequest
            get() =
                RetryActivationRequest(
                    email = EMAIL,
                    instanceKey = INSTANCE_KEY,
                    path = PATH,
                )

        val passwordDigest = ByteArray(0)

        val simpleUser: DbUser
            get() =
                DbUser(
                    id = USER_ID,
                    email = EMAIL,
                    fullName = NAME,
                    secret = SECRET,
                    storageMode = StorageMode.NFS,
                    passwordDigest = passwordDigest,
                )

        val adminUser: DbUser by lazy {
            DbUser(
                id = ADMIN_ID,
                email = EMAIL,
                fullName = NAME,
                secret = SECRET,
                storageMode = StorageMode.NFS,
                passwordDigest = passwordDigest,
                superuser = true,
            )
        }
    }
}
