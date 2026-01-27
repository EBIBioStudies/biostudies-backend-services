package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import ebi.ac.uk.model.SubmissionTransferOptions
import kotlinx.coroutines.runBlocking
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.PASSWORD_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.SERVER_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.USER_HELP
import uk.ac.ebi.biostd.client.cli.common.UpdateUserEmailParameters.CURRENT_EMAIL
import uk.ac.ebi.biostd.client.cli.common.UpdateUserEmailParameters.NEW_EMAIL
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

internal class UpdateUserEmailCommand(
    private val subService: SubmissionService,
) : CliktCommand(name = "updateUserEmail") {
    private val server by option("-s", "--server", help = SERVER_HELP).required()
    private val user by option("-u", "--user", help = USER_HELP).required()
    private val password by option("-p", "--password", help = PASSWORD_HELP).required()
    private val currentEmail by option("-ce", "--currentEmail", help = CURRENT_EMAIL).required()
    private val newEmail by option("-ne", "--newEmail", help = NEW_EMAIL).required()

    override fun run(): Unit =
        runBlocking {
            val securityConfig = SecurityConfig(server, user, password)
            val options = SubmissionTransferOptions(currentEmail, newEmail)

            subService.transferEmailUpdate(securityConfig, options)
            echo("SUCCESS: Email updated and submissions transferred from '$currentEmail' to '$newEmail'")
        }
}
