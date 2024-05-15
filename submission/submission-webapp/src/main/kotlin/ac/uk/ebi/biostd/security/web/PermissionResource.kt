package ac.uk.ebi.biostd.security.web

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.security.domain.service.PermissionService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/permissions")
@PreAuthorize("hasAuthority('ADMIN')")
class PermissionResource(private val permissionsService: PermissionService) {
    @PutMapping("/collections")
    fun grantCollectionPermission(
        @RequestBody request: PermissionRequest,
    ) {
        permissionsService.grantPermission(request.accessType, request.userEmail, request.accNo)
    }

    @PutMapping("/submissions")
    fun grantSubmissionPermission(
        @RequestBody request: PermissionRequest,
    ) {
        permissionsService.createAndGrantPermission(request.accessType, request.userEmail, request.accNo)
    }
}

data class PermissionRequest(
    val userEmail: String,
    val accessType: AccessType,
    val accNo: String,
)
