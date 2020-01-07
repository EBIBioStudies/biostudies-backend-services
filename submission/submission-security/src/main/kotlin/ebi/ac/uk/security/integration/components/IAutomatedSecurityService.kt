package ebi.ac.uk.security.integration.components

import ebi.ac.uk.security.integration.model.api.SecurityUser

interface IAutomatedSecurityService {
    fun getOrCreate(email: String, username: String): SecurityUser
}
