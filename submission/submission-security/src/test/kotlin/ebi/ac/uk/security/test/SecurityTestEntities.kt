package ebi.ac.uk.security.test

import ac.uk.ebi.biostd.persistence.model.User
import ebi.ac.uk.api.security.RegisterRequest

internal class SecurityTestEntities {

    companion object {
        const val userId = 55L
        const val username = "Jhon Doe"
        const val email = "Jhon.Doe@test.com"
        const val password = "abc123"
        const val instanceKey = "12345"
        const val path = "/activate_url_path"

        const val adminId = 70L

        val simpleRegistrationRequest: RegisterRequest
            get() = RegisterRequest(username, email, password)

        val preRegisterRequest: RegisterRequest
            get() = RegisterRequest(
                username = username,
                email = email,
                password = password,
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
