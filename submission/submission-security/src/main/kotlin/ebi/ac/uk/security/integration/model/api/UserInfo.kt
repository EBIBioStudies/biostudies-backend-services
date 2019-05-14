package ebi.ac.uk.security.integration.model.api

import ac.uk.ebi.biostd.persistence.model.User

class UserInfo(private val user: User, private val token: String) {

    operator fun component1() = user

    operator fun component2() = token
}
