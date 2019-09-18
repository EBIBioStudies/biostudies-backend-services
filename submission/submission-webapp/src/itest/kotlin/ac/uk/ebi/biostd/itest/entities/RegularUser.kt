package ac.uk.ebi.biostd.itest.entities

import ebi.ac.uk.api.security.RegisterRequest

/**
 * Represents a bio studies regular user.
 */
object RegularUser {
    const val username = "jane jones"
    const val email = "jane@biostudies.com"
    const val password = "678910"

    fun asRegisterRequest() = RegisterRequest(email, username, password, superUser = false)
}
