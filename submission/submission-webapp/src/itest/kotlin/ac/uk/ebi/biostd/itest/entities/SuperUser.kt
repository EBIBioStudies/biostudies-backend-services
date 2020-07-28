package ac.uk.ebi.biostd.itest.entities

import ebi.ac.uk.api.security.RegisterRequest

/**
 * Represents a bio studies super user.
 */
object SuperUser : TestUser {
    override val username = "Super User"
    override val email = "biostudies-mgmt@ebi.ac.uk"
    override val password = "12345"
    override val superUser = true

    override fun asRegisterRequest() = RegisterRequest(username, email, password)
}
