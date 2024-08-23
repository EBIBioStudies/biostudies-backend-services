package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.runBlocking
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.PASSWORD_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.SERVER_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.USER_HELP
import uk.ac.ebi.biostd.client.cli.common.SubmissionRequestParameters.ACC_NO
import uk.ac.ebi.biostd.client.cli.common.SubmissionRequestParameters.VERSION
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.dto.SubmissionStatusRequest
import uk.ac.ebi.biostd.client.cli.services.SubmissionRequestService

internal class SubmissionRequestStatusCommand(
    private val submissionRequestService: SubmissionRequestService,
) : CliktCommand(name = "requestStatus") {
    private val server by option("-s", "--server", help = SERVER_HELP).required()
    private val user by option("-u", "--user", help = USER_HELP).required()
    private val password by option("-p", "--password", help = PASSWORD_HELP).required()
    private val accNo by option("-ac", "--accNo", help = ACC_NO).required()
    private val version by option("-v", "--version", help = VERSION).int().required()

    override fun run(): Unit =
        runBlocking {
            val securityConfig = SecurityConfig(server, user, password)
            val request = SubmissionStatusRequest(accNo, version, securityConfig)
            val status = submissionRequestService.getRequestStatus(request)

            echo("The submission request '$accNo', version: $version is in status: '$status'")
        }
}
