package uk.ac.ebi.biostd.client.cli.services

import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig

internal class SecurityService {
    suspend fun grantPermission(
        securityConfig: SecurityConfig,
        accessType: String,
        targetUser: String,
        accNo: String,
    ) =
        performRequest { grant(securityConfig, accessType, targetUser, accNo) }

    suspend fun revokePermission(
        securityConfig: SecurityConfig,
        accessType: String,
        targetUser: String,
        accNo: String,
    ) =
        performRequest { revoke(securityConfig, accessType, targetUser, accNo) }

    private suspend fun grant(
        config: SecurityConfig,
        accessType: String,
        targetUser: String,
        accNo: String,
    ) {
        bioWebClient(config.server, config.user, config.password).grantPermission(
            targetUser,
            accNo,
            accessType,
        )
    }

    private suspend fun revoke(
        config: SecurityConfig,
        accessType: String,
        targetUser: String,
        accNo: String,
    ) {
        bioWebClient(config.server, config.user, config.password).revokePermission(
            targetUser,
            accNo,
            accessType,
        )
    }
}
