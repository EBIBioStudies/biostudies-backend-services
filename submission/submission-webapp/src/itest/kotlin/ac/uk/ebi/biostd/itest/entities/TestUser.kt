package ac.uk.ebi.biostd.itest.entities

import ebi.ac.uk.api.security.RegisterRequest

interface TestUser {
    val username: String
    val email: String
    val password: String
    val superUser: Boolean

    fun asRegisterRequest(): RegisterRequest
}
