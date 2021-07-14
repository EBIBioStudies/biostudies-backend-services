package uk.ac.ebi.biostd.client.cli.services

import uk.ac.ebi.biostd.client.cli.dto.PermissionRequest

internal class SecurityService {
    fun grantPermission(request: PermissionRequest) = performRequest { permission(request) }

    private fun permission(request: PermissionRequest) =
        bioWebClient(request.server, request.user, request.password).givePermissionToUser(
            request.targetUser,
            request.accessTagName,
            request.accessType
        )
}
