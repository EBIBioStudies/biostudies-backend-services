package ebi.ac.uk.security.integration.components

import ebi.ac.uk.model.FolderStats
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.integration.model.api.UserInfo

interface SecurityQueryService {
    fun existsByEmail(
        email: String,
        onlyActive: Boolean = true,
    ): Boolean

    fun getUser(email: String): SecurityUser

    fun getUserProfile(authToken: String): UserInfo

    fun getOrCreateInactive(
        email: String,
        username: String,
    ): SecurityUser

    fun getUserFolderStats(email: String): FolderStats
}
