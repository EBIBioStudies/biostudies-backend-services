package ac.uk.ebi.biostd.security.web

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.security.domain.service.PermissionService
import ac.uk.ebi.biostd.security.domain.service.RevokePermissionService
import ac.uk.ebi.biostd.submission.converters.BioUser
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/permissions")
@PreAuthorize("isAuthenticated()")
class PermissionResource(
    private val permissionsService: PermissionService,
    private val revokePermissionService: RevokePermissionService,
) {
    @PutMapping
    suspend fun grantPermission(
        @RequestBody request: PermissionRequest,
        @BioUser user: SecurityUser,
    ) {
        permissionsService.grantPermission(user.email, request.accessType, request.userEmail, request.accNo)
    }

    @PostMapping("/revoke")
    suspend fun revokePermission(
        @RequestBody request: PermissionRequest,
        @BioUser user: SecurityUser,
    ) {
        revokePermissionService.revokePermission(user.email, request.accessType, request.userEmail, request.accNo)
    }
}

data class PermissionRequest(
    val userEmail: String,
    val accessType: AccessType,
    val accNo: String,
)
