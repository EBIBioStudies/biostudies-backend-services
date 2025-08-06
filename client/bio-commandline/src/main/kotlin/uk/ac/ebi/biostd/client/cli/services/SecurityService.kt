package uk.ac.ebi.biostd.client.cli.services

import uk.ac.ebi.biostd.client.cli.dto.PermissionRequest

internal class SecurityService {
    suspend fun grantPermission(request: PermissionRequest) = performRequest { grant(request) }

    suspend fun revokePermission(request: PermissionRequest) = performRequest { revoke(request) }

    private suspend fun grant(rqt: PermissionRequest) {
        val config = rqt.securityConfig
        bioWebClient(config.server, config.user, config.password).grantPermission(
            rqt.targetUser,
            rqt.accNo,
            rqt.accessType,
        )
    }

    private suspend fun revoke(rqt: PermissionRequest) {
        val config = rqt.securityConfig
        bioWebClient(config.server, config.user, config.password).revokePermission(
            rqt.targetUser,
            rqt.accNo,
            rqt.accessType,
        )
    }
}
