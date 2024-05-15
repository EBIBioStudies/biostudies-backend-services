package uk.ac.ebi.biostd.client.cli.services

import uk.ac.ebi.biostd.client.cli.dto.PermissionRequest

internal class SecurityService {
    fun grantCollectionPermission(request: PermissionRequest) =
        performRequest {
            val config = request.securityConfig
            bioWebClient(config.server, config.user, config.password).grantCollectionPermission(
                request.targetUser,
                request.accNo,
                request.accessType,
            )
        }

    fun grantSubmissionPermission(request: PermissionRequest) =
        performRequest {
            val config = request.securityConfig
            bioWebClient(config.server, config.user, config.password).grantSubmissionPermission(
                request.targetUser,
                request.accNo,
                request.accessType,
            )
        }
}
