package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.ON_BEHALF_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.PASSWORD_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.SERVER_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.USER_HELP
import uk.ac.ebi.biostd.client.cli.common.FileListValidationParameters.ACC_NO
import uk.ac.ebi.biostd.client.cli.common.FileListValidationParameters.ROOT_PATH
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters.FILE_LIST_PATH
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.dto.ValidateFileListRequest
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

internal class ValidateFileListCommand(
    private val submissionService: SubmissionService,
) : CliktCommand(name = "validateFileList") {
    private val server by option("-s", "--server", help = SERVER_HELP).required()
    private val user by option("-u", "--user", help = USER_HELP).required()
    private val password by option("-p", "--password", help = PASSWORD_HELP).required()
    private val onBehalf by option("-b", "--onBehalf", help = ON_BEHALF_HELP)
    private val fileListPath by option("-f", "--fileListPath", help = FILE_LIST_PATH).required()
    private val accNo by option("-ac", "--accNo", help = ACC_NO)
    private val rootPath by option("-rp", "--rootPath", help = ROOT_PATH)

    override fun run() {
        val securityConfig = SecurityConfig(server, user, password, onBehalf)
        val request = ValidateFileListRequest(fileListPath, accNo, rootPath, securityConfig)

        submissionService.validateFileList(request)
        echo("SUCCESS: ${request.fileListPath} is a valid file list")
    }
}
