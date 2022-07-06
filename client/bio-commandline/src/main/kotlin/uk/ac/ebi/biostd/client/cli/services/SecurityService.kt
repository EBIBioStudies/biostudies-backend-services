package uk.ac.ebi.biostd.client.cli.services

import uk.ac.ebi.biostd.client.cli.dto.PermissionRequest

internal class SecurityService {
    fun grantPermission(request: PermissionRequest) = performRequest { permission(request) }

    private fun permission(request: PermissionRequest) {
        val (server, user, password, _) = request.securityConfig

        bioWebClient(server, user, password).givePermissionToUser(
            request.targetUser,
            request.accessTagName,
            request.accessType
        )
    }
}
