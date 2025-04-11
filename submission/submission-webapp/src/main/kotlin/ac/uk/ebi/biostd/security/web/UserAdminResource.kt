package ac.uk.ebi.biostd.security.web

import ac.uk.ebi.biostd.security.domain.service.ExtUserService
import ebi.ac.uk.extended.model.ExtUser
import ebi.ac.uk.model.FolderStats
import ebi.ac.uk.model.MigrateHomeOptions
import ebi.ac.uk.security.integration.components.SecurityQueryService
import ebi.ac.uk.security.service.SecurityService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/security/users")
class UserAdminResource(
    private val extUserService: ExtUserService,
    private val securityService: SecurityService,
    private val securityQueryService: SecurityQueryService,
) {
    @GetMapping("/extended/{email:.*}")
    fun getExtUser(
        @PathVariable email: String,
    ): ExtUser = extUserService.getExtUser(email)

    @GetMapping("/{email:.*}/home-stats")
    fun getExtUserHomeStats(
        @PathVariable email: String,
    ): FolderStats = securityQueryService.getUserFolderStats(email)

    @PostMapping("/{email:.*}/migrate")
    suspend fun migrateUser(
        @PathVariable email: String,
        @RequestBody migrateOptions: MigrateHomeOptions,
    ) {
        securityService.updateMagicFolder(email, migrateOptions)
    }
}
