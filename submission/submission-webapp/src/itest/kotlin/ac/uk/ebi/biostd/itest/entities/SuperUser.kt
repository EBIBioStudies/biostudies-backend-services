package ac.uk.ebi.biostd.itest.entities

import ebi.ac.uk.api.security.RegisterRequest

/**
 * Represents a bio studies super user.
 */
object SuperUser {
    const val username = "Super User"
    const val email = "biostudies-mgmt@ebi.ac.uk"
    const val password = "12345"

    fun asRegisterRequest() = RegisterRequest(username, email, password, superUser = true)
}
