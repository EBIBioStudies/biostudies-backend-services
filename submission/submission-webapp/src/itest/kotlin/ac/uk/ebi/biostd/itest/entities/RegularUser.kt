package ac.uk.ebi.biostd.itest.entities

import ebi.ac.uk.api.security.RegisterRequest

/**
 * Represents a bio studies regular user.
 */
object RegularUser {
    const val username = "Regular User"
    const val email = "biostudies-dev@ebi.ac.uk"
    const val password = "678910"

    fun asRegisterRequest() = RegisterRequest(username, email, password, superUser = false)
}
