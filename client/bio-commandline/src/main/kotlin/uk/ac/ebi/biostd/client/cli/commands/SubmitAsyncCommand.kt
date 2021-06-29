package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import uk.ac.ebi.biostd.client.cli.common.CommonParameters
import uk.ac.ebi.biostd.client.cli.common.FILES_SEPARATOR
import uk.ac.ebi.biostd.client.cli.common.getFiles
import uk.ac.ebi.biostd.client.cli.dto.SubmissionRequest
import uk.ac.ebi.biostd.client.cli.services.SubmissionService
import java.io.File

internal class SubmitAsyncCommand(private val submissionService: SubmissionService) :
    CliktCommand(name = "submitAsync") {
    private val server by option("-s", "--server", help = CommonParameters.SERVER_HELP).required()
    private val user by option("-u", "--user", help = CommonParameters.USER_HELP).required()
    private val password by option("-p", "--password", help = CommonParameters.PASSWORD_HELP).required()
    private val onBehalf by option("-b", "--onBehalf", help = CommonParameters.ON_BEHALF_HELP)
    private val input by option("-i", "--input", help = CommonParameters.INPUT_HELP).file(exists = true).required()
    private val attached by option("-a", "--attached", help = CommonParameters.ATTACHED_HELP)

    override fun run() {
        val request = SubmissionRequest(
            server = server,
            user = user,
            password = password,
            onBehalf = onBehalf,
            file = input,
            attached = attached?.split(FILES_SEPARATOR)?.flatMap { getFiles(File(it)) }.orEmpty()
        )

        submissionService.submitAsync(request)
        echo("SUCCESS: Submission is in queue to be submitted")
    }
}
