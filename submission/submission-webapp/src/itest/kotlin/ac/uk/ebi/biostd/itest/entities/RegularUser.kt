package ac.uk.ebi.biostd.itest.entities

import ebi.ac.uk.api.security.RegisterRequest

/**
 * Represents a bio studies regular user.
 */
object RegularUser : TestUser {
    override val username = "Regular User"
    override val email = "regular@ebi.ac.uk"
    override val password = "678910"
    override val superUser = false

    override fun asRegisterRequest() =
        RegisterRequest(username, email, password, notificationsEnabled = true)
}
