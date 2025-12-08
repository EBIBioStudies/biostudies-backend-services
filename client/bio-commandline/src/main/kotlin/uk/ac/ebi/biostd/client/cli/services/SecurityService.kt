package uk.ac.ebi.biostd.client.cli.services

import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig

internal class SecurityService {
    suspend fun grantPermission(
        securityConfig: SecurityConfig,
        accessType: String,
        targetUser: String,
        accNo: String,
    ) = performRequest {
        bioWebClient(securityConfig).grantPermission(targetUser, accNo, accessType)
    }

    suspend fun revokePermission(
        securityConfig: SecurityConfig,
        accessType: String,
        targetUser: String,
        accNo: String,
    ) = performRequest {
        bioWebClient(securityConfig).revokePermission(targetUser, accNo, accessType)
    }
}
