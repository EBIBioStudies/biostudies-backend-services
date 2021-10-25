package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.extended.model.FileMode.MOVE
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.ON_BEHALF_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.PASSWORD_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.SERVER_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.USER_HELP
import uk.ac.ebi.biostd.client.cli.common.FILES_SEPARATOR
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters.ATTACHED_HELP
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters.INPUT_HELP
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters.MOVE_FILES
import uk.ac.ebi.biostd.client.cli.common.getFiles
import uk.ac.ebi.biostd.client.cli.dto.SubmissionRequest
import uk.ac.ebi.biostd.client.cli.services.SubmissionService
import java.io.File

internal class SubmitAsyncCommand(private val submissionService: SubmissionService) :
    CliktCommand(name = "submitAsync") {
    private val server by option("-s", "--server", help = SERVER_HELP).required()
    private val user by option("-u", "--user", help = USER_HELP).required()
    private val password by option("-p", "--password", help = PASSWORD_HELP).required()
    private val onBehalf by option("-b", "--onBehalf", help = ON_BEHALF_HELP)
    private val input by option("-i", "--input", help = INPUT_HELP).file(exists = true).required()
    private val attached by option("-a", "--attached", help = ATTACHED_HELP)
    private val moveFiles by option("-m", "--moveFiles", help = MOVE_FILES).flag(default = false)

    override fun run() {
        val request = SubmissionRequest(
            server = server,
            user = user,
            password = password,
            onBehalf = onBehalf,
            file = input,
            attached = attached?.split(FILES_SEPARATOR)?.flatMap { getFiles(File(it)) }.orEmpty(),
            fileMode = if (moveFiles) MOVE else COPY
        )

        submissionService.submitAsync(request)
        echo("SUCCESS: Submission is in queue to be submitted")
    }
}
