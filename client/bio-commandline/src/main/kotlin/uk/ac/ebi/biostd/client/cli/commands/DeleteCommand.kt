package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import uk.ac.ebi.biostd.client.cli.ACC_NO_HELP
import uk.ac.ebi.biostd.client.cli.ON_BEHALF_HELP
import uk.ac.ebi.biostd.client.cli.PASSWORD_HELP
import uk.ac.ebi.biostd.client.cli.SERVER_HELP
import uk.ac.ebi.biostd.client.cli.USER_HELP
import uk.ac.ebi.biostd.client.cli.services.SubmissionDeleteRequest
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

class DeleteCommand(private val submissionService: SubmissionService) : CliktCommand(name = "delete") {
    private val server by option("-s", "--server", help = SERVER_HELP).required()
    private val user by option("-u", "--user", help = USER_HELP).required()
    private val password by option("-p", "--password", help = PASSWORD_HELP).required()
    private val onBehalf by option("-b", "--onBehalf", help = ON_BEHALF_HELP)
    private val accNo by option("-ac", "--accNo", help = ACC_NO_HELP).required()

    @Suppress("TooGenericExceptionCaught")
    override fun run() {
        val request = SubmissionDeleteRequest(
            server = server,
            user = user,
            password = password,
            onBehalf = onBehalf,
            accNo = accNo
        )

        submissionService.delete(request)
        echo("SUCCESS: Submission with AccNo ${request.accNo} was deleted")
    }
}
