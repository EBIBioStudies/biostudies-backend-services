package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.PASSWORD_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.SERVER_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.USER_HELP
import uk.ac.ebi.biostd.client.cli.common.DoiParameters.ACC_NO
import uk.ac.ebi.biostd.client.cli.dto.GenerateDoiRequest
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

internal class GenerateDoiCommand(
    private val submissionService: SubmissionService,
) : CliktCommand(name = "generateDoi") {
    private val server by option("-s", "--server", help = SERVER_HELP).required()
    private val user by option("-u", "--user", help = USER_HELP).required()
    private val password by option("-p", "--password", help = PASSWORD_HELP).required()
    private val accNo by option("-ac", "--accNo", help = ACC_NO).required()

    override fun run() =
        runBlocking {
            val securityConfig = SecurityConfig(server, user, password)
            val request = GenerateDoiRequest(accNo, securityConfig)

            submissionService.generateDoi(request)
            echo("SUCCESS: DOI for accession $accNo has been requested")
        }
}
