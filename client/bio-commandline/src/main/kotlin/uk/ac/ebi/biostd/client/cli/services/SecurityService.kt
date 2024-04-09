package uk.ac.ebi.biostd.client.cli.services

import uk.ac.ebi.biostd.client.cli.dto.PermissionRequest

internal class SecurityService {
    fun grantPermission(request: PermissionRequest) = performRequest { permission(request) }

    private fun permission(rqt: PermissionRequest) {
        val config = rqt.securityConfig
        bioWebClient(config.server, config.user, config.password).givePermissionToUser(
            rqt.targetUser,
            rqt.accessTagName,
            rqt.accessType,
        )
    }
}
