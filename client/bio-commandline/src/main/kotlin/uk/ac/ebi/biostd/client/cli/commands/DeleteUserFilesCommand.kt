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
import uk.ac.ebi.biostd.client.cli.dto.DeleteUserFilesRequest
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.services.UserFilesService

internal class DeleteUserFilesCommand(
    private val userFilesService: UserFilesService,
) : CliktCommand(name = "deleteUserFiles") {
    private val server by option("-s", "--server", help = SERVER_HELP).required()
    private val user by option("-u", "--user", help = USER_HELP).required()
    private val password by option("-p", "--password", help = PASSWORD_HELP).required()
    private val file by option("-f", "--fileName", help = FILES_HELP).required()
    private val relPath by option("-rp", "--relPath", help = REL_PATH_HELP).default("")

    override fun run() =
        runBlocking {
            val request =
                DeleteUserFilesRequest(
                    fileName = file,
                    relPath = relPath,
                    securityConfig = SecurityConfig(server, user, password),
                )
            userFilesService.deleteUserFiles(request)
            echo("SUCCESS: User files deleted")
        }
}
