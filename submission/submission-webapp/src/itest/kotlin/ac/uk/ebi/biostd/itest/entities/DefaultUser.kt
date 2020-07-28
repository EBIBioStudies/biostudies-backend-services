package ac.uk.ebi.biostd.itest.entities

import ebi.ac.uk.api.security.RegisterRequest

object DefaultUser : TestUser {
    override val username = "Default User"
    override val email = "default_user@ebi.ac.uk"
    override val password = "1234"
    override val superUser = false

    override fun asRegisterRequest() =
        RegisterRequest(username, email, password, notificationsEnabled = false)
}
