package ac.uk.ebi.biostd.security.web

import ac.uk.ebi.biostd.security.domain.service.ExtUserService
import ebi.ac.uk.extended.model.ExtUser
import ebi.ac.uk.model.FolderStats
import ebi.ac.uk.model.MigrateHomeOptions
import ebi.ac.uk.security.integration.components.SecurityQueryService
import ebi.ac.uk.security.service.SecurityService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/security/users")
@Tag(name = "User Administration", description = "Internal user inspection and home-folder maintenance operations.")
class UserAdminResource(
    private val extUserService: ExtUserService,
    private val securityService: SecurityService,
    private val securityQueryService: SecurityQueryService,
) {
    @Operation(summary = "Get extended user details")
    @GetMapping("/extended/{email:.*}")
    fun getExtUser(
        @PathVariable email: String,
    ): ExtUser = extUserService.getExtUser(email)

    @Operation(summary = "Get user home-folder statistics")
    @GetMapping("/{email:.*}/home-stats")
    fun getExtUserHomeStats(
        @PathVariable email: String,
    ): FolderStats = securityQueryService.getUserFolderStats(email)

    @Operation(summary = "Migrate a user's home folder")
    @PostMapping("/{email:.*}/migrate")
    suspend fun migrateUser(
        @PathVariable email: String,
        @RequestBody migrateOptions: MigrateHomeOptions,
    ) {
        securityService.updateMagicFolder(email, migrateOptions)
    }
}
