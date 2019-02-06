package ebi.ac.uk.security.model

import ac.uk.ebi.biostd.persistence.model.User

class UserInfo(private val token: String, private val user: User) {

    operator fun component1() = user

    operator fun component2() = token
}
