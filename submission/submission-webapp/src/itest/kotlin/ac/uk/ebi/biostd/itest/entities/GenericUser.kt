package ac.uk.ebi.biostd.itest.entities

import ebi.ac.uk.api.security.RegisterRequest

/**
 * Represent a Generic bio studies user.
 */
object GenericUser {
    const val username = "jhon doe"
    const val email = "test@biostudies.com"
    const val password = "12345"

    fun asRegisterRequest() = RegisterRequest(email, username, password, superUser = true)
}
