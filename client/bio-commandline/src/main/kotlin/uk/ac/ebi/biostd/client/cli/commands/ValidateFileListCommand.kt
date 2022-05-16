package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import uk.ac.ebi.biostd.client.cli.common.CommonParameters
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters
import uk.ac.ebi.biostd.client.cli.dto.ValidateFileListRequest
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

internal class ValidateFileListCommand(
    private val submissionService: SubmissionService
) : CliktCommand(name = "validateFileList") {
    private val server by option("-s", "--server", help = CommonParameters.SERVER_HELP).required()
    private val user by option("-u", "--user", help = CommonParameters.USER_HELP).required()
    private val password by option("-p", "--password", help = CommonParameters.PASSWORD_HELP).required()
    private val onBehalf by option("-b", "--onBehalf", help = CommonParameters.ON_BEHALF_HELP)
    private val fileListPath by option("-f", "--fileListPath", help = SubmissionParameters.FILE_LIST_PATH).required()

    override fun run() {
        val request = ValidateFileListRequest(
            server = server,
            user = user,
            password = password,
            onBehalf = onBehalf,
            fileListPath = fileListPath
        )

        submissionService.validateFileList(request)
        echo("SUCCESS: ${request.fileListPath} is a valid file list")
    }
}
