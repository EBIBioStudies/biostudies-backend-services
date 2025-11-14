package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.PASSWORD_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.SERVER_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.USER_HELP
import uk.ac.ebi.biostd.client.cli.common.UploadUserFilesParameters.FILES_HELP
import uk.ac.ebi.biostd.client.cli.common.UploadUserFilesParameters.REL_PATH_HELP
import uk.ac.ebi.biostd.client.cli.common.splitFiles
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.dto.UserFilesRequest
import uk.ac.ebi.biostd.client.cli.services.UserFilesService

internal class UploadUserFilesCommand(
    private val userFilesService: UserFilesService,
) : CliktCommand(name = "uploadFiles") {
    private val server by option("-s", "--server", help = SERVER_HELP).required()
    private val user by option("-u", "--user", help = USER_HELP).required()
    private val password by option("-p", "--password", help = PASSWORD_HELP).required()
    private val files by option("-f", "--files", help = FILES_HELP).required()
    private val relPath by option("-rp", "--relPath", help = REL_PATH_HELP).default("")

    override fun run() =
        runBlocking {
            val request =
                UserFilesRequest(
                    relPath = relPath,
                    files = splitFiles(files),
                    securityConfig = SecurityConfig(server, user, password),
                )
            userFilesService.uploadUserFiles(request)
            echo("SUCCESS: User files uploaded")
        }
}
