package ac.uk.ebi.biostd.itest.entities

import ebi.ac.uk.api.security.RegisterRequest

object InvalidUser : TestUser {
    override val username = "Invalid User"
    override val email = "not-a-mail@fake"
    override val password = "1234"

    override fun asRegisterRequest(): RegisterRequest = RegisterRequest(username, email, password)
}
