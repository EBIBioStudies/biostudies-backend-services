package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.PASSWORD_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.SERVER_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.USER_HELP
import uk.ac.ebi.biostd.client.cli.common.GrantPermissionParameters.ACCESS_TYPE
import uk.ac.ebi.biostd.client.cli.common.GrantPermissionParameters.ACC_NO
import uk.ac.ebi.biostd.client.cli.common.GrantPermissionParameters.TARGET_USER
import uk.ac.ebi.biostd.client.cli.dto.PermissionRequest
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.services.SecurityService

internal class GrantPermissionCommand(
    private val securityService: SecurityService,
) : CliktCommand(name = "grantPermission") {
    private val server by option("-s", "--server", help = SERVER_HELP).required()
    private val user by option("-u", "--user", help = USER_HELP).required()
    private val password by option("-p", "--password", help = PASSWORD_HELP).required()
    private val accessType by option("-at", "--accessType", help = ACCESS_TYPE).required()
    private val targetUser by option("-tu", "--targetUser", help = TARGET_USER).required()
    private val accNo by option("-ac", "--accNo", help = ACC_NO).required()

    override fun run() {
        val securityConfig = SecurityConfig(server, user, password)
        val request = PermissionRequest(securityConfig, accessType, targetUser, accNo)

        securityService.grantPermission(request)
        echo("The user $targetUser has been granted $accessType permission for accession $accNo")
    }
}
