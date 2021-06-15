package ac.uk.ebi.biostd.security.web

import ac.uk.ebi.biostd.persistence.service.UserSqlPermissionsService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PermissionResource(private val permissionsService: UserSqlPermissionsService) {

    @PutMapping("/permissions/{userName}")
    fun givePermissionToUser(
        @PathVariable userName: String,
        @RequestBody request: PermissionRequest
    ) {
        permissionsService.givePermissionToUser(request.accessType, userName, request.accessTagName)
    }
}

data class PermissionRequest(
    val accessType: String,
    val accessTagName: String
)
