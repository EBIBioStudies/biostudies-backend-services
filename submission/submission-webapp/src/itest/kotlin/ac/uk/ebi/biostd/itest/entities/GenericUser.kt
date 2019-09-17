package ac.uk.ebi.biostd.itest.entities

import ebi.ac.uk.api.security.RegisterRequest

/**
 * Represent a Generic bio studies user.
 */
object GenericUser {
    const val name = "BioStudies Developer"
    const val email = "biostudies-dev@ebi.ac.uk"
    const val password = "12345"

    fun asRegisterRequest() = RegisterRequest(name, email, password, superUser = true)
}
