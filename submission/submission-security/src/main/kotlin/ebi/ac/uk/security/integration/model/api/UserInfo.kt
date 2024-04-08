package ebi.ac.uk.security.integration.model.api

class UserInfo(
    private val user: SecurityUser,
    private val token: String,
) {
    operator fun component1() = user

    operator fun component2() = token
}
