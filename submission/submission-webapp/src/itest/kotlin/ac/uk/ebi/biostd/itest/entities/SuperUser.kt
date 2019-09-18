package ac.uk.ebi.biostd.itest.entities

import ebi.ac.uk.api.security.RegisterRequest

/**
 * Represents a bio studies super user.
 */
object SuperUser {
    const val username = "jhon doe"
    const val email = "test@biostudies.com"
    const val password = "12345"

    fun asRegisterRequest() = RegisterRequest(username, email, password, superUser = true)
}
