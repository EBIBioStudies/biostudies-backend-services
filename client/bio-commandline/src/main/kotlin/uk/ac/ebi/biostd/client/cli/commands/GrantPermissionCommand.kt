package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.ON_BEHALF_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.PASSWORD_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.SERVER_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.USER_HELP
import uk.ac.ebi.biostd.client.cli.common.GrantPermissionParameters.ACCESS_TAG_NAME
import uk.ac.ebi.biostd.client.cli.common.GrantPermissionParameters.ACCESS_TYPE
import uk.ac.ebi.biostd.client.cli.common.GrantPermissionParameters.TARGET_USER
import uk.ac.ebi.biostd.client.cli.dto.PermissionRequest
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

internal class GrantPermissionCommand(private val submissionService: SubmissionService) :
    CliktCommand(name = "permission") {

    private val server by option("-s", "--server", help = SERVER_HELP).required()
    private val user by option("-u", "--user", help = USER_HELP).required()
    private val password by option("-p", "--password", help = PASSWORD_HELP).required()
    private val onBehalf by option("-b", "--onBehalf", help = ON_BEHALF_HELP)
    private val accessType by option("-at", "--accessType", help = ACCESS_TYPE).required()
    private val targetUser by option("-tu", "--targetUser", help = TARGET_USER).required()
    private val accessTagName by option("-atn", "--accessTagName", help = ACCESS_TAG_NAME).required()

    override fun run() {
        val request = permissionRequest()

        submissionService.grantPermission(request)
        echo("SUCCESS: The user $targetUser has permission to $accessType in the collection $accessTagName")
    }

    private fun permissionRequest() = PermissionRequest(
        server = server,
        user = user,
        password = password,
        onBehalf = onBehalf,
        accessType = accessType,
        targetUser = targetUser,
        accessTagName = accessTagName
    )
}
