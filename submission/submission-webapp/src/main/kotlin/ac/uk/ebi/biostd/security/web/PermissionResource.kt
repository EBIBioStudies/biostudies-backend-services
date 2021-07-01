package ac.uk.ebi.biostd.security.web

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.security.domain.service.PermissionService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@PreAuthorize("hasAuthority('ADMIN')")
class PermissionResource(private val permissionsService: PermissionService) {

    @PutMapping("/permissions")
    fun givePermissionToUser(@RequestBody request: PermissionRequest) =
        permissionsService.givePermissionToUser(request.accessType, request.userEmail, request.accessTagName)
}

data class PermissionRequest(
    val userEmail: String,
    val accessType: AccessType,
    val accessTagName: String
)
