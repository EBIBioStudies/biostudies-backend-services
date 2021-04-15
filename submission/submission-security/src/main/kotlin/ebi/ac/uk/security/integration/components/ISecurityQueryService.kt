package ebi.ac.uk.security.integration.components

import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.integration.model.api.UserInfo

interface ISecurityQueryService {
    fun existsByEmail(email: String): Boolean

    fun getUser(email: String): SecurityUser

    fun getUserProfile(authToken: String): UserInfo

    fun getOrCreateInactive(email: String, username: String): SecurityUser
}
